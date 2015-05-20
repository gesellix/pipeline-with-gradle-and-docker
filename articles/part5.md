This is the final part of the [article series](http://blog-it.hypoport.de/2014/07/25/a-continuous-deployment-pipeline-with-gradle-and-docker/) about our continuous deployment pipeline. The previous articles showed you how we build and publish our Spring Boot based application, perform AngularJS end-to-end tests with Protractor, and how we perform contract tests to external services as consumer and provider as well. 

What's missing are the description of how we package our application in Docker images, and how we distribute and deploy them to our production servers.

Though we originally had a dedicated *Docker build and push* step in our pipeline, things have changed since last year. The Docker image build has been integrated into the very first step, so that we not only have the Spring Boot artifacts in our repository, but the corresponding Docker image in our registry, as soon as possible.  

The Docker build and push code isn't very large, so this article will also show you how we use the Docker images to deploy and run our application on our production hosts. The [example code](https://github.com/gesellix/pipeline-with-gradle-and-docker/tree/part5) will be available at GitHub.

# Docker for Application Packaging

You've certainly heard about Docker, so we won't go into any detailed Docker concepts here. If you're new to Docker and would like to learn some basics, please head over to the [10-minute tutorial](https://www.docker.com/tryit/) at the official [docker.com](https://www.docker.com/) web site.

We're using Docker to package, distribute, and run our application. Similar to the executable Spring Boot `.jar` files, Docker helps us wrapping all runtime dependencies in so called images and run image instances as Linux containers. The encapsulation of Docker containers allows developers to define a huge part of the runtime environment (like the Java runtime) instead of being dependent on the tools installed on the host.

With such a more explicitly defined environment we can also expect the application to behave in a consistent way on different hosts. Due to the simplicity of a reduced Docker image we also have a smaller scope to consider when changing or updating the environment.

The Docker daemon on our build infrastructure is usually available via its HTTP remote api, so that we can use any Docker client library instead of the Docker command line binary. Our Gradle scripts leverage the communication to the Docker daemon with the help of a Gradle Docker plugin. It adds several tasks to our build scripts which can be configured quite easily to create new Docker images, and push them to our private Docker registry. We also use other tasks to pull and run Docker images in our contract test build step, like already described in the last article.

The Docker build task depends on some preparation tasks which copy the necessary application jar and the Dockerfile to a temporary directory. That directory is considered as build context, which is sent to the Docker daemon as source for the final image.

Our Docker images are tagged with the same version like the application jar, which allows us to use the version text file through the whole pipeline. The Gradle `publish` task is configured to trigger the Docker image push task. With the Docker images in our Docker registry we can finish our pipeline by deploying the application to our staging and production hosts.

# Ansible for Application Deployment

Our tool of choice to orchestrate the deployments is Ansible. We use Ansible to provision and maintain our infrastructure, and it also allows us to perform ad hoc tasks like application deployments or cleanup tasks. Ansible uses tasks, roles, and playbooks to describe a desired system state.

Relevant in the context of our application deployment are such details like blue-green deployment and load balancing of the same application version on different hosts. We use a HAProxy as load balancer and as switch between our blue and green versions in front of our applications. Our application isn't aware of those aspects, which increases scalablility and flexibility. So, the Ansible playbook has to decide which version (blue or green) needs to be replaced by the newly build release. In summary, the Ansible playbook needs to perform the following tasks:

* determine which version to replace (blue or green)
* pull the new Docker image to our hosts
* stop and remove the old containers
* run new container instances based on the new image
* update the HAProxy config to route new requests to the new containers

Additionally to these essentials, there are some book keeping and cleanup tasks necessary.

## Example Playbook and Tasks

We won't share our complete Ansible repository here, so the examples won't work out of the box, but to help you get an idea how Ansible tasks can look like, please have a look at the `ansible` directory.

In the *hosts* directory you'll only find a production sub directory with the inventory and production specific variables. Beside the production directory we have other stages or environments defined, too. The beauty of Ansible lies in the possibility to keep our internal, staging, and production inventory seperated, while the tasks are usually applicable on any host.

The *library* directory contains scripts or Ansible modules which can be executed in the context of a task. Since Ansible tasks should be declarative and less imperative, we moved some commands to the library scripts.

In *playbooks* you'll find the entrypoint when working with an Ansible project. Playbooks configure tasks, the affected hosts and other environment specific details. Most work is performed in the *tasks* directory, though.

You'll see that the generic *docker-service* task configures an Ansible Docker module to communicate with a Docker daemon. Some tasks are obsolete since Ansible 1.9, where a `reloaded` state has been introduced. Apart from most variables being configured in the playbook or the *group_vars* and *host_vars*, the `example_version` variable is passed via command line in our TeamCity build step.
