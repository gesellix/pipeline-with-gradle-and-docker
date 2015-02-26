package org.hypoport.example.metrics

import com.codahale.metrics.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.endpoint.MetricsEndpoint
import org.springframework.boot.actuate.endpoint.PublicMetrics

class CompositeMetricsEndpoint extends MetricsEndpoint {

  @Autowired
  MetricRegistry metricRegistry

  public CompositeMetricsEndpoint(PublicMetrics metrics) {
    super(metrics)
  }

  @Override
  Map<String, Object> invoke() {
    def result = super.invoke()

    for (Map.Entry<String, Gauge> metric : metricRegistry.gauges.entrySet()) {
      result.put(metric.key, metric.value)
    }
    for (Map.Entry<String, Counter> metric : metricRegistry.counters.entrySet()) {
      result.put(metric.key, metric.value)
    }
    for (Map.Entry<String, Histogram> metric : metricRegistry.histograms.entrySet()) {
      result.put(metric.key, metric.value)
    }
    for (Map.Entry<String, Meter> metric : metricRegistry.meters.entrySet()) {
      result.put(metric.key, metric.value)
    }
    for (Map.Entry<String, Timer> metric : metricRegistry.timers.entrySet()) {
      result.put(metric.key, metric.value)
    }

    return result
  }
}
