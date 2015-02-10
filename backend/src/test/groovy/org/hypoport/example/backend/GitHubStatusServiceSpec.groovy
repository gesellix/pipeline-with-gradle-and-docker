package org.hypoport.example.backend

import org.springframework.web.client.RestOperations
import spock.lang.Specification

import static org.hypoport.example.backend.GitHubStatusService.STATUS_API_BASEURL

class GitHubStatusServiceSpec extends Specification {

  def gitHubStatusService = new GitHubStatusService()
  RestOperations restOperations = Mock(RestOperations)

  def setup() {
    gitHubStatusService.restOperations = restOperations
  }

  def "should traverse from the official api entrypoint to the status api"() {
    given:
    1 * restOperations.getForObject(STATUS_API_BASEURL, Map, []) >> [
        'status_url': "http://our.actual.status.url/as.json"]
    1 * restOperations.getForObject("http://our.actual.status.url/as.json", Map, []) >> [
        'status': "wonderful"]
    when:
    def gitHubStatus = gitHubStatusService.currentGitHubStatus
    then:
    gitHubStatus?.status == "wonderful"
  }
}
