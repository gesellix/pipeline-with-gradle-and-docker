This is the final part of the [article series](http://blog-it.hypoport.de/2014/07/25/a-continuous-deployment-pipeline-with-gradle-and-docker/) about our continuous deployment pipeline. The previous articles showed you how we build and publish our Spring Boot based application, perform AngularJS end-to-end tests with Protractor, and how we perform contract tests to external services as consumer and provider as well.

What's missing are the description of how we package our application in Docker images, and how we distribute and deploy them to our production servers.

Though we originally had a dedicated *Docker build and push* step in our pipeline, things have changed since last year. The Docker image build has been integrated into the very first step, so that we not only have the Spring Boot artifacts in our repository, but the corresponding Docker image in our registry, as soon as possible.

The Docker build and push code isn't very large, so this article will also show you how we use the Docker images to deploy and run our application on our production hosts. The [example code](https://github.com/gesellix/pipeline-with-gradle-and-docker/tree/part5) will be available at GitHub.

# 
