package org.hypoport.example.backend

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

@Component
class ExampleHealthCheck implements HealthIndicator {

  @Override
  Health health() {
    if (Math.random() > 0.5) {
      return Health.up().build()
    } else {
      return Health.down().build()
    }
  }
}
