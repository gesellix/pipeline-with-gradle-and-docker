package de.hypoport.example

import groovy.util.logging.Slf4j
import org.gradle.api.Project

@Slf4j
public class ProducerVersionResolver {

  private Project project

  public ProducerVersionResolver(Project project) {
    assert project != null
    this.project = project
  }

  public String resolveBuildVersion() {
    File versionFile = project.file('producer-version.txt')
    if (versionFile.exists()) {
      String version = versionFile.text.trim()
      log.info "Using version from file '${versionFile.name}': ${version}"
      return version
    }
    else {
      throw new FileNotFoundException("Expected version file '${versionFile.name}' not found")
    }
  }

  public String resolveProductionVersion() {
    // here we would normally ask the productive service to tell us its version,
    // but for demonstration purposes, we'll just return a constant value (the build version).
//    try {
//      return new HTTPBuilder('http://www.producer.com/info', ContentType.JSON).request(Method.GET) {
//        headers.accept = 'application/json'
//        response.success = { request, response -> response.build.version }
//      }
//    }
//    catch (HttpResponseException e) {
//      throw new RuntimeException("Failed to retrieve version from production stage", e)
//    }
    def version = resolveBuildVersion()
    log.info "Using version from prod: ${version}"
    return version
  }
}
