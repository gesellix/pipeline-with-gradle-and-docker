package org.hypoport.example.backend

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestOperations

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static org.springframework.web.bind.annotation.RequestMethod.GET

@RestController
class GitHubStatusService {

  final static String STATUS_API_BASEURL = 'https://status.github.com/api.json'
  final static String STATUS_LINKNAME = 'status_url'

  @Autowired
  RestOperations restOperations

  @RequestMapping(value = '/github/status', method = GET, produces = APPLICATION_JSON_VALUE)
  def getCurrentGitHubStatus() {
    def gitHubApiMethods = restOperations.getForObject(STATUS_API_BASEURL, Map, [])
    String statusUrl = gitHubApiMethods[STATUS_LINKNAME]
    def currentSystemStatus = restOperations.getForObject(statusUrl, Map, [])
    return currentSystemStatus
  }
}
