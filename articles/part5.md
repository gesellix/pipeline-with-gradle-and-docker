This is the final part of the [article series](http://blog-it.hypoport.de/2014/07/25/a-continuous-deployment-pipeline-with-gradle-and-docker/) about our continuous deployment pipeline. The previous articles showed you how we build and publish our Spring Boot based application, perform AngularJS end-to-end tests with Protractor, and how we perform contract tests to external services as consumer and provider as well.

What's missing are the description of how we package our application in Docker images, and how we distribute and deploy them to our production servers.

Though we originally had a dedicated *Docker build and push* step in our pipeline, things have changed since last year. The Docker image build has been integrated into the very first step, so that we not only have the Spring Boot artifacts in our repository, but the corresponding Docker image in our registry as early as possible.

The Docker build and push code isn't very large, so this article will also show you how we use the Docker images to deploy and run our application on our production hosts. The [example code](https://github.com/gesellix/pipeline-with-gradle-and-docker/tree/part5) is available at GitHub.

# Docker for Application Packaging

You've certainly heard about Docker, so we won't go into any detailed Docker concepts here. If you're new to Docker and would like to learn some basics, please head over to the [10-minute tutorial](https://www.docker.com/tryit/) at the official [docker.com](https://www.docker.com/) web site.

We're using Docker to package, distribute, and run our application. Similar to the executable Spring Boot `.jar` files, Docker helps us wrapping all runtime dependencies in so called images and run image instances as Linux containers. The encapsulation of Docker containers allows developers to define a huge part of the runtime environment (like the Java runtime) instead of being dependent on the tools installed on the host.

With such a more explicitly defined environment we can also expect the application to behave in a consistent way on different hosts. Due to the simplicity of a reduced Docker image we also have a smaller scope to consider when changing or updating the environment. Even changing the hosts' operating system from Oracle Linux to Ubuntu didn't have any effect on our application.

The Docker daemon on our build infrastructure is usually available via its HTTP remote api, so that we can use any Docker client library instead of the Docker command line binary. Our Gradle scripts leverage the communication to the Docker daemon with the help of a [Gradle Docker plugin](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part5/example-project/backend/build.gradle#L14). It adds several tasks to our build scripts which can be configured quite easily to [create new Docker images](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part5/example-project/backend/build.gradle#L73), and [push them](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part5/example-project/backend/build.gradle#L83) to our private Docker registry. We also use other tasks to pull and run Docker images in our contract test build step, like already described in [part 4](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/master/articles/part4.md).

The Docker build task depends on some preparation tasks which copy the necessary application jar and the Dockerfile to a temporary directory. That directory is considered as build context, which is sent to the Docker daemon as source for the final image.

Our Docker images are [tagged with the same version](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part5/example-project/backend/build.gradle#L75) like the application jar, which allows us to use the same version text file through the whole pipeline. The Gradle `publish` task is configured to [automatically trigger](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part5/example-project/backend/build.gradle#L89) the Docker image push task.

With the Docker images in our Docker registry we can complete our pipeline by deploying the application to our staging and production hosts.

# Ansible for Application Deployment

Our tool of choice to orchestrate the deployments is [Ansible](http://www.ansible.com/home). We use Ansible to provision and maintain our infrastructure, and it also allows us to perform ad hoc tasks like application deployments or cleanup tasks. Ansible uses tasks, roles, and playbooks to describe a desired system state.

Relevant in the context of our application deployment are such details like [blue-green deployment](http://martinfowler.com/bliki/BlueGreenDeployment.html) and load balancing of the same application version on different hosts. We use a [HAProxy](http://www.haproxy.org/) as load balancer and as switch between our blue and green versions in front of our application webapps. Our application isn't aware of those aspects, which increases scalablility and flexibility. So, the Ansible playbook has to decide which version (blue or green) needs to be replaced by the newly build release. In summary, the Ansible playbook needs to perform the following tasks:

* determine which version to replace (blue or green)
* pull the new Docker image to our hosts
* stop and remove the old containers
* run new container instances based on the new image
* update the HAProxy config to route new requests to the new containers

Additionally to these essentials, there are some book keeping and cleanup tasks necessary.

## Example Playbook and Tasks

We won't share our complete Ansible repository here, so the examples won't work out of the box, but to help you get an idea how Ansible tasks can look like, please have a look at the [`ansible` directory](https://github.com/gesellix/pipeline-with-gradle-and-docker/tree/part5/ansible).

In the *hosts* directory you'll only find an [inventory file](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part5/ansible/hosts/inventory) with our hosts and their aliases. Hosts can also be grouped together, here we added both loadbalanced webapp hosts to the *example-backend* group. The loadbalancer host as been aliased as *example-backend-loadbalancer*.

Normally, you would find more hosts and different environments like development or production. The beauty of Ansible lies in the possibility to keep several internal, staging, and production inventories seperated, while tasks are usually applicable on any host.

The *library* directory contains scripts or Ansible modules which can be executed in the context of a task. Since Ansible tasks should be declarative and less imperative, we moved some [shell commands](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part5/ansible/library/docker_facts) to gather container runtime information to the library.

A good entrypoint when working with an Ansible project is the *playbooks* directory. Playbooks configure tasks, the affected hosts and other environment specific details. We added a playbook to [deploy the example-backend](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part5/ansible/playbooks/deploy-example-backend.yml#L25) on our webapp hosts and configure the loadbalancer to [switch to the new target stage](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part5/ansible/playbooks/deploy-example-backend.yml#L42) (blue or green). The other tasks in the playbook collect the active stage by asking the loadbalancer and set the relevant facts for the deployment task.

Most work is performed in the *roles* directory, though. You'll see that the generic *docker-service* task [configures an Ansible Docker module](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part5/ansible/roles/docker-service/tasks/main.yml#L22) to communicate with a Docker daemon. The other steps only prepare the actual deployment: a new image is pulled from our registry, the previous image id of the old container is saved for a cleanup step at the end, and the old container is removed. Some steps are obsolete since Ansible 1.9, where a `reloaded` state has been introduced to automatically replace a container based on a new image.

Ansible doesn't only make sense as task runner for deployments, but we also use it to provision our hosts: there's not much difference between a regular deployment and the very first deployment. In a microservice oriented culture, adding new services with their satellites, loadbalancers, and pipelines needs to be simple and efficient. Ansible helps us to extract a common "dockerized service" role and only configure some service specific values. That way our deployments become more declarative and maintainable.

Since we're completely Docker infected, our Ansible deployment project is available for our CI as Docker image. You can find our simple [Dockerfile](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part5/ansible/Dockerfile) at the root of the ansible directory. Additionally to our deployment tasks and playbooks, it only provides Ansible itself. On TeamCity our builds perform `docker run` commands like shown below. Accessing our hosts is allowed by volume mounting ssh keys into the container:

```
docker run -it --rm -v ~/.ssh/id_rsa:/root/.ssh/id_rsa hypoport/ansible ansible-playbook -i inventory playbooks/deploy-example-backend.yml
```

# Summary

We've now reached the end of the article series about our continuous deployment pipeline.

You learned how we build and package our application in Docker images, perform tests on different levels and with different scopes, and how we deploy new releases on our hosts.

Over the past year we learned a lot about other use cases and concepts in the DevOps universe. Docker helped us to define clear interfaces between ours and other services. Our Gradle scripts now focus on the build and publish tasks, while Ansible is our tool of choice for provisioning, deployment, and maintenance tasks.

Building pipelines hasn't become trivial, but with the right tools we feel quite confident and can handle new requirements very easily.

Though many pipelines end with the deployment of a release in the production environment, the rollout of new features doesn't end. Dynamically updating releases wtithout downtime requires feature toggles, backwards compatibility to other services, flexible database schemata, and good monitoring.

We'll cover some aspects in future posts, so just keep following our blog.

For feedback or questions on this article series, please contact us [@gesellix](https://twitter.com/gesellix) or [@hypoport](https://twitter.com/hypoport), or add a comment below. Thanks!
