package de.hypoport.example

import groovyx.net.http.RESTClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

import static java.util.concurrent.TimeUnit.SECONDS

class HealthCheckService {

  public static final int MAX_RETRIES = 20
  public static final int DELAY = 2
  public static HealthCheckService INSTANCE = new HealthCheckService()

  ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  Logger logger = LoggerFactory.getLogger(getClass())
  RESTClient restclient = new RESTClient()

  static def awaitServiceHealth(healthCheckUri) {
    INSTANCE.scheduleHealthCheck(healthCheckUri)
  }

  def scheduleHealthCheck(healthCheckUri) {
    Callable task = getHealthCheckTask(healthCheckUri)

    def count = 0
    def success = false
    while (++count <= MAX_RETRIES && !success) {
      def future = scheduler.schedule(task, DELAY, SECONDS)
      try {
        success = future.get()
      }
      catch (Exception e) {
        if (count >= MAX_RETRIES) {
          throw new RuntimeException("Health check was not successful after ${MAX_RETRIES} retries", e)
        }
        else {
          logger.info "$e"
        }
      }
    }
    if (!success) {
      throw new RuntimeException("Health check was not successful after ${MAX_RETRIES} retries")
    }
  }

  def Callable getHealthCheckTask(healthCheckUri) {
    return new Callable() {

      @Override
      Object call() throws Exception {
        def response = restclient.get(uri: healthCheckUri)
        if (!response.isSuccess()) {
          throw new RuntimeException("Health check for ${healthCheckUri} returned status code ${response.status}")
        }
        logger.info "Health check for ${healthCheckUri} is ok: ${response.data}"
        return true
      }
    }
  }
}
