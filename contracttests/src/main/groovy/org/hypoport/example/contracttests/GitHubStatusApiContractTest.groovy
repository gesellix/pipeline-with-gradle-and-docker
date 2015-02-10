package org.hypoport.example.contracttests

import org.hypoport.example.web.HttpConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.RestOperations
import spock.lang.Specification

import static org.hypoport.example.backend.GitHubStatusService.STATUS_API_BASEURL
import static org.hypoport.example.backend.GitHubStatusService.STATUS_LINKNAME

@ContextConfiguration(classes = HttpConfig)
class GitHubStatusApiContractTest extends Specification {

  final static String STATUS_PROPERTY_NAME = 'status'

  @Autowired
  def RestOperations restOperations

  def setup() {
  }

  def "entrypoint should link to the status_url"() {
    when:
    def gitHubApiMethods = restOperations.getForObject(STATUS_API_BASEURL, Map, [])
    then:
    gitHubApiMethods[STATUS_LINKNAME] instanceof String
    and:
    new URI(gitHubApiMethods[STATUS_LINKNAME] as String).isAbsolute()
  }

  def "status_url should return status as property"() {
    given:
    def gitHubApiMethods = restOperations.getForObject(STATUS_API_BASEURL, Map, [])
    String statusUrl = gitHubApiMethods[STATUS_LINKNAME]
    when:
    def currentSystemStatus = restOperations.getForObject(statusUrl, Map, [])
    then:
    currentSystemStatus[STATUS_PROPERTY_NAME] =~ "\\w+"
  }
}
