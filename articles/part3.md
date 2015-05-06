This is the third part in our series about our deployment of a JVM and AngularJS based application using Gradle as build tool and Docker as deployment vehicle. You'll find the overview on all posts in the [introductury post](http://blog-it.hypoport.de/2014/07/25/a-continuous-deployment-pipeline-with-gradle-and-docker/).

As seen in the overview, our next step in the deployment pipeline performs so called e2e tests. The common Gradle project setup has already been [described in part 2](http://blog-it.hypoport.de/2014/10/15/continuous-deployment-with-gradle-and-docker-part-2/), so we can start with the Gradle script for the e2e test submodule. To follow the descriptions below, you can use the code examples at the GitHub project [in branch "part3"](https://github.com/gesellix/pipeline-with-gradle-and-docker/tree/part3).

# E2E Test Basics

We already compared different levels of code and application testing in a [dedicated post](http://blog-it.hypoport.de/2013/09/28/angularjs-test-pyramid/), where e2e tests had been described as a way to test from a user's perspective. You'll see that the concepts described there haven't changed, but the tooling has been greatly improved. Though the AngularJS [e2e testing guide](https://docs.angularjs.org/guide/e2e-testing) mentions the deprecated way of using the Angular Scenario Runner, it recommend the new test runner [Protractor](https://github.com/angular/protractor) as the way to go.

If you're already familiar with WebDriver or Selenium tests, Protractor will feel very familiar. You can imagine Protractor as an AngularJS specific extension on top of a JavaScript implementation of WebDriver. In fact, you might use the native WebDriverJs tool to write e2e tests, but Protractor allows you to hook into AngularJS specific events and element locators, so that you can focus on test code and less on technical details.

Since e2e tests are executed from a user's perspective, you run your application in a way similar to your production environment. With our example project, we need to run the Spring Boot frontend and backend applications. For a real project, you'll probably need to mock external dependencies or use another database, so that your tests cannot be influenced by external changes or instabilities.

# Example Project Setup

Looking at the example project, you'll find a Gradle submodule for [our e2e tests](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/build.gradle). The Gradle script should only need to run the application and the Protractor e2e tests. As simple as it seems, we also wanted the `e2eTests` task to run on our TeamCity agents with a dedicated build version. That means, we wanted to pass an application version and let Gradle fetch the desired artifacts and run them, instead of using the current build artifacts of the Gradle project. This allows us to parallelize the e2e tests with other build steps, which decreases the overall deployment time.

## Selecting the application version

The mechanism to pass our application version from the first TeamCity build goal to the e2e test build goal works with text files being written before starting the Gradle build. See the TeamCity [Artifact Dependencies](https://confluence.jetbrains.com/display/TCD9/Artifact+Dependencies) docs for details. In essence, our Gradle script expects a file to contain the application version that the e2e tests have to test. The file is available with a default version at [/application-version.txt](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/application-version.txt) and needs to be overwritten with an existing version. The file is read before downloading the application artifacts at [readVersionFile](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/build.gradle#L23).

## Running our services as background tasks

Before running Protractor, we need to start a Selenium server and the application artifacts. Additionally, we run a reverse proxy to make both webapps available on a single port. All four services need to be started and to be kept running until the Gradle e2eTest task has finished.

There are several ways to run background tasks during a Gradle task. As you see in our script, we chose to manually manage our background services by calling the [`execAsync` function](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/build.gradle#L78). It [manages the environments](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/build.gradle#L83) of our commands, allows [optional logging](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/build.gradle#L90) of a command's output, and allows to [wait for an expected output](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/build.gradle#L93) by adding a CountDownLatch.

The [`startSeleniumServer`](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/build.gradle#L144) task uses the nice [selenium-server-standalone-jar](https://www.npmjs.com/package/selenium-server-standalone-jar) Node module to fetch the required jar file, then runs it on the default port `4449` with a simple `java -jar ...` call.

Our [reverse proxy](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/e2e-reverse-proxy.js) to combine both frontend and backend webapp ports on a single port is implemented as a small node module, which delegates every request to the according webapp. Requests with the first path segment "example-backend" are delegated to the backend port, requests with the path segment "example" are delegated to the frontend port. The important part is our [default proxy port](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/e2e-reverse-proxy.js#L34) `3010`, which needs to be used in the base uri of our e2e tests.

Both webapps are run including the mentioned CountDownLatch, which expects a Tomcat specific message to appear in the output [before continuing](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/build.gradle#L163) the Gradle task.

# Running the e2e tests with Protractor

The last step in the e2eTests task triggers Protractor to perform our e2e tests. Protractor allows to provide your [individual configuration](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/build.gradle#L165) in a JavaScript file. Our example configuration is taken from our TeamCity config. It sets some defaults like the [application's base uri](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/protractor-conf-defaults.js#L14), configures the [specs pattern](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/protractor-conf-defaults.js#L18) to tell Protractor where to find our tests, and also allows us to [hook into the Protractor lifecycle](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/protractor-conf-defaults.js#L24) to prepare or enhance the Browser object, which is used in the test code. That's the place where you might override or configure the Angular application, e.g. by [disabling animations](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/protractor-conf-defaults.js#L30).

Some more technical hooks are extracted to our [`protractorUtils.js`](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/protractorUtils.js), where we add some features like [passing browser console logs to our shell](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/protractorUtils.js#L119) or [creating browser screenshots](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/protractorUtils.js#L95) in case of failed tests. Those helper features need to be enabled in every test suite, so we [add a `globalSetup`](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/protractorUtils.js#L127) function to the Browser object.

A TeamCity specific part of our configuration enables better logging and a tests count feature in the TeamCity builds. We only needed to [add a TeamCity reporter](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/protractor-conf-teamcity.js#L18), which is available as Node module.

Protractor can now find our [tests](https://github.com/gesellix/pipeline-with-gradle-and-docker/tree/part3/e2e-tests/tests) at the configured path. The e2e tests look similar to the typical unit tests, but the first action in our e2e tests is [executing the `globalSetup`](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/tests/example-test.js#L4) on the Browser instance.

The actual tests use the convenience methods provided by Protractor to [locate elements](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/tests/example-test.js#L7) by their id or [by their Angular binding](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/tests/example-test.js#L11). We won't go into a detailed description of the supported features here, but recommend to browse through the [Protractor reference](http://angular.github.io/protractor/#/api).

# Task cleanup

When the `e2eTests` task is finished we need to stop the background services. To accomplish this, we used the Gradle feature to [finalize the `e2eTests`](https://github.com/gesellix/pipeline-with-gradle-and-docker/blob/part3/e2e-tests/build.gradle#L175) task with a `stopProcesses` task. It simply calls `destroy()` on our background threads. Only in rare cases where we break the Gradle script with an Exception, so that Gradle doesn't have a chance to call our `stopProcesses` task, we end up with old running processes on our TeamCity agent. To kill those processes, we added a simple shell script to our TeamCity build goals to find and kill all processes on our well known ports (8080, 8090, 3010, 4449).

# Summary

A test run started with `./gradlew e2eTests` in the project root looks like the screenshot below. You get some information how many tests have passed; in case of a failure you get some more details and even stacktraces.

![e2e tests output](https://github.com/gesellix/pipeline-with-gradle-and-docker/raw/part3/articles/e2etests-console.png)

Though e2e tests are easy to write, and the Protractor features help on locating elements and minimizing timing issues, they can slow down your deployment pipeline a lot. In our case, the initial `build/publish` step needs less than two minutes, other steps in our pipeline also need more or less two minutes, but the e2e tests step needs at least seven minutes to execute 54 tests.

In fact, we often discuss how to minimize the number of e2e test cases, but since one mostly needs a more integrative way to ensure everything is working as expected, there will always be some good use cases for e2e tests.

Our e2e tests focus on the user interface with mocked external dependencies. But another aspect to check your application before deploying it to production is the interoperability with other services. This is where the Contract Tests come into play, which is the theme for the next part of this series.

If you have questions so far, please use the comments feature below, or contact us via Twitter [@gesellix](https://twitter.com/gesellix).
