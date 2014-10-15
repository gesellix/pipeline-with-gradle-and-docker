package org.hypoport.example.metrics

import com.ryantenney.metrics.spring.config.annotation.EnableMetrics
import org.springframework.boot.actuate.autoconfigure.MetricFilterAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.MetricRepositoryAutoConfiguration
import org.springframework.boot.actuate.endpoint.MetricsEndpoint
import org.springframework.boot.actuate.endpoint.VanillaPublicMetrics
import org.springframework.boot.actuate.metrics.repository.InMemoryMetricRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@EnableMetrics
@Configuration
@Import([MetricRepositoryAutoConfiguration, MetricFilterAutoConfiguration])
class MetricsConfig {

  @Bean
  public MetricsEndpoint metricsEndpoint() {
    def metricRepository = new InMemoryMetricRepository()
    def metrics = new VanillaPublicMetrics(metricRepository)
    return new CompositeMetricsEndpoint(metrics)
  }
}
