package org.hypoport.example.backend

import io.prometheus.client.Prometheus
import io.prometheus.client.metrics.Gauge
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.endpoint.PublicMetrics
import org.springframework.boot.actuate.endpoint.VanillaPublicMetrics
import org.springframework.boot.actuate.metrics.Metric
import org.springframework.boot.actuate.metrics.reader.MetricReader
import org.springframework.boot.actuate.metrics.repository.InMemoryMetricRepository
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
class SpringBootMetricsExpositionHook implements Prometheus.ExpositionHook {

  @Autowired
  def MetricReader metricRepository = new InMemoryMetricRepository()
  def PublicMetrics metrics

  def static final Gauge publicMetrics = Gauge.newBuilder()
      .namespace("spring")
      .name("metrics")
      .labelNames("name")
      .documentation("All Spring public metrics")
      .build()


  @PostConstruct
  def init() {
    metrics = new VanillaPublicMetrics(metricRepository)
  }

  @Override
  void run() {
    for (Metric<? extends Number> metric : metrics.metrics()) {
      publicMetrics.newPartial()
          .labelPair("name", metric.getName())
          .apply()
          .set(metric.getValue().doubleValue())
    }
  }
}
