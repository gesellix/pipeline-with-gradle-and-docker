This series of posts will show you some aspects of our continuous deployment pipeline for one of our products. It is built, tested and deployed to our servers by using [Gradle](http://www.gradle.org/), while the application itself runs inside [Docker](https://www.docker.com/) containers.

We want to show you how we use Gradle to implement a complete pipeline with minimal dependency on command line tools. We'll also describe how to perform rollouts to production without the need for shell scripts or even remote shell access, by using the [Docker remote API](https://docs.docker.com/reference/api/docker_remote_api/). All details regarding our [AngularJS](https://angularjs.org/) frontend, test concepts for multi-product compatibility and detailed code examples will be explained in upcoming posts, with [example code](https://github.com/gesellix/pipeline-with-gradle-and-docker) provided at GitHub. This post starts with a bird's-eye view of our pipeline.

Overview
=

Our deployment pipeline is divided into six build goals, combined in a [TeamCity](http://www.jetbrains.com/teamcity/) *Build Chain*. We'll add links to each build goal as soon as a corresponding article has been published:

* [build, publish](http://wp.me/p1E7sK-nG)
* [e2e test](http://wp.me/p1E7sK-oI)
* contract test
* build image
* deploy on dev
* deploy on prod

Every git push to a shared Git repository triggers a new build and is automatically deployed to production.

The first step builds a multi module project and produces two Spring Boot jar files for our backend and frontend webapps. Both jars are published to our Nexus artifact repository. Building a [Spring Boot](http://projects.spring.io/spring-boot/) application with Gradle is straight-forward, you'll find examples in the Spring Boot [guides](http://spring.io/guides/gs/spring-boot/). The [gradle-grunt-plugin](http://plugins.gradle.org/plugin/com.moowork.grunt) helps us building and unit testing the AngularJS frontend by delegating build steps to the [Grunt](http://gruntjs.com/) task runner.

Our *e2e-test* build step runs some integration tests on our frontend to ensure that it is compatible to our backend. The next step runs so-called *contract tests*, which runs cross-product tests to ensure our new release still plays well with the other services on our platform.

The fourth step builds a Docker image containing both frontend and backend webapps and pushes it to a private Docker registry. After that, we pull the newly built image to our development and production stages and run container instances. In order to maximize product availability, both stages use [blue-green deployment](http://martinfowler.com/bliki/BlueGreenDeployment.html).

Gradle and Groovy power
=

As already mentioned, the complete pipeline is implemented using Gradle. Running the build and publish tasks is quite trivial, some code snippets will be shown in the following posts. The integration of our frontend build using the gradle-grunt-plugin has been straight forward, too, while we added some configuration to let Gradle know about Grunt's inputs and outputs. That way, we enable Gradle to use its cache and [skip up to date tasks](http://www.gradle.org/docs/current/userguide/more_about_tasks.html#sec:up_to_date_checks) when there aren't any code changes.

Running the e2e-tests and contract-tests wasn't possible with existing plugins, so we had to create some special tasks. Since Gradle lets us write native Groovy code, we didn't need to create dedicated shell scripts, but [execute commands](http://groovy.codehaus.org/Executing+External+Processes+From+Groovy) as simply as `"command".execute()`. That way we can perform the following steps to run our e2e-tests with [Protractor](http://www.protractortest.org):

* start selenium-server
* start e2e-reverse-proxy
* start frontend and backend
* run protractor e2e-tests
* tear down

In contrast to the e2e-tests, where we only check our frontend and backend application, we have some contract-tests to check our interaction with other services. Our backend interacts with some other products of our platform, and we want to be sure that after deploying a new release of our product, it still works together with current versions of the other products. Our contract-tests are implemented as [Spock framework](http://spockframework.org/) and [TestNG](http://testng.org/) tests and are a submodule of our product. A dedicated contract-tester module in an own project performs all necessary steps to find and run the external webapps in their released versions and to perform our contract-tests against their temporary instances. Like with the e2e-tests, all steps are implemented in Gradle, but this time we could use plugins like [Gradle Cargo plugin](https://github.com/bmuschko/gradle-cargo-plugin) and [Gradle Download Task](https://github.com/michel-kraemer/gradle-download-task), furthermore Gradle's built in test runner and dynamic dependency resolution for our contract-tests artifact:

* collect participating product versions
* download each product's webapp from Nexus
* start the participating webapps and infrastructure services
* run contract-tests
* tear down

Gradle and Docker
=

With our artifacts being tested, we package them in Docker images, deploy the images to our private registries and run fresh containers on our servers. Docker allows us to describe the image contents by writing [Dockerfiles](https://docs.docker.com/reference/builder/) as plain text, so that we can include all build instructions in our Git repository. Before using a [Gradle Docker plugin](http://plugins.gradle.org/plugin/de.gesellix.docker), we used Gradle to orchestrate Docker clients, which had to be installed on our TeamCity agents and the application servers. Like described above, we used the Groovy command executor to access the [Docker command line interface](https://docs.docker.com/reference/commandline/cli/). We're now in a transition to only use the Docker remote API, so that we don't need a Docker client on every build server, but only need to point the plugin to any Docker enabled server.

Building and distributing our images, followed by starting the containers is only one part of our deployment. In order to implement continuous delivery without interrupting availability of our product, we implemented blue-green deployment. Therefore, our Gradle deployment script needs to ask our reverse proxy in front of our application servers for a deployable stage (e.g. green), perform the Docker container tasks and toggle a switch from the current to the new stage, e.g. from blue to green:

* get the deployable stage
* pull the new image from the Docker registry
* stop and remove the old container
* run a new container based on the new image
* cleanup (e.g. remove unused images)
* switch to the new stage with the fresh container

Summary
=

With this brief overview you should have an impression of the key elements of our pipeline. In the upcoming posts we'll dive into each of these build steps, provide some code examples and discuss our experience regarding the chosen technologies and frameworks in context of our server setups.

If you'd like to know special details, please leave a comment or contact us via Twitter [@gesellix](https://twitter.com/gesellix), so that we can include your wishes in the following posts. Even if you'd like us to talk about non technical aspects, e.g. like our experience introducing the above technologies to our teams, just ask!
