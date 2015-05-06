package de.hypoport.example

import groovy.util.logging.Slf4j
import org.gradle.api.Project

@Slf4j
class ConsumerVersionResolver {

  private Project project

  public ConsumerVersionResolver(Project project) {
    assert project != null
    this.project = project
  }

  public String resolveBuildVersion() {
    private File versionFile = project.file('consumer-version.txt')
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
//    def http = new HTTPBuilder('https://www.consumer.com/version')
//    http.contentEncoding = ContentEncoding.Type.DEFLATE
//    def version = http.request(Method.GET, ContentType.TEXT) {
//      response.success = { resp, reader -> reader.text }
//    }
    def version = resolveBuildVersion()
    log.info "Using version from prod: ${version}"
    return version
  }
}
