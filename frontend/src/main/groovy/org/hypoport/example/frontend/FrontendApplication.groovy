package org.hypoport.example.frontend

import org.springframework.boot.SpringApplication
import org.springframework.boot.actuate.autoconfigure.*
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Import([EmbeddedServletContainerAutoConfiguration,
    DispatcherServletAutoConfiguration,
    PropertyPlaceholderAutoConfiguration,
    ServerPropertiesAutoConfiguration,
    ManagementServerPropertiesAutoConfiguration,
    ManagementSecurityAutoConfiguration,
    SecurityAutoConfiguration,
    EndpointAutoConfiguration,
    EndpointWebMvcAutoConfiguration,
    HealthIndicatorAutoConfiguration])
@Configuration
@ComponentScan(basePackages = "org.hypoport.example")
class FrontendApplication {

  public static void main(String[] args) {
    SpringApplication.run(FrontendApplication, args);
  }
}
