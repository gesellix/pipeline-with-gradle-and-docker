package de.hypoport.example

import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture

import static de.hypoport.example.HealthCheckService.DELAY
import static de.hypoport.example.HealthCheckService.INSTANCE
import static java.util.concurrent.TimeUnit.SECONDS

class HealthCheckServiceTest extends Specification {

  HealthCheckService service
  RESTClient restclient = Mock()
  HttpResponseDecorator response = Mock()
  ScheduledExecutorService scheduler = Mock()
  Callable task = Mock()
  ScheduledFuture future = Mock()

  def setup() {
    service = Spy(HealthCheckService)
    service.restclient = restclient
    service.scheduler = scheduler
  }

  def "HealthCheckTask should call url"() {
    response.isSuccess() >> true

    when:
    service.getHealthCheckTask("http://host:port/context/health").call()

    then:
    1 * restclient.get(_) >> { args ->
      assert args[0].uri == "http://host:port/context/health"
      response
    }
  }

  def "HealthCheckTask should return true if response was successful"() {
    restclient.get(_) >> response
    response.isSuccess() >> true

    when:
    def result = service.getHealthCheckTask("http://host:port/context/health").call()

    then:
    result
  }

  def "HealthCheckTask should throw exeption if response was not successful"() {
    restclient.get(_) >> response
    response.isSuccess() >> false

    when:
    service.getHealthCheckTask("http://host:port/context/health").call()

    then:
    thrown(RuntimeException)
  }

  def "scheduleHealthCheck should schedule task"() {
    future.get() >> true

    when:
    service.scheduleHealthCheck("http://host:port/context/health")

    then:
    1 * service.getHealthCheckTask("http://host:port/context/health") >> task
    1 * scheduler.schedule(task, DELAY, SECONDS) >> future
  }

  def "scheduleHealthCheck should retry scheduled task if it not successful"() {
    service.getHealthCheckTask("http://host:port/context/health") >> task
    future.get() >>> [false, false, false, false, true]

    when:
    service.scheduleHealthCheck("http://host:port/context/health")

    then:
    5 * scheduler.schedule(task, DELAY, SECONDS) >> future
  }

  def "scheduleHealthCheck should retry scheduled task if it not successful and throw exception"() {
    service.getHealthCheckTask("http://host:port/context/health") >> task
    scheduler.schedule(_, _, _) >> future
    future.get() >> false

    when:
    service.scheduleHealthCheck("http://host:port/context/health")

    then:
    thrown(RuntimeException)
  }

  def "scheduleHealthCheck should rethrow exception of scheduled task if all retries failed"() {
    service.getHealthCheckTask("http://host:port/context/health") >> task
    scheduler.schedule(_, _, _) >> future
    def exception = new IllegalArgumentException()

    when:
    service.scheduleHealthCheck("http://host:port/context/health")

    then:
    20 * future.get() >> {
      throw exception
    }
    def ex = thrown(RuntimeException)
    ex.cause == exception
  }

  def "should call frontend, backend and adapter health check for each server"() {
    given:
    INSTANCE = service

    when:
    HealthCheckService.awaitServiceHealth("http://host:port/context/health")

    then:
    1 * service.scheduleHealthCheck("http://host:port/context/health") >> true
  }
}
