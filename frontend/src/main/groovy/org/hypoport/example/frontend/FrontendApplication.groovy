package org.hypoport.example.frontend

import org.springframework.boot.SpringApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackages = "org.hypoport.example")
class FrontendApplication {

  public static void main(String[] args) {
    SpringApplication.run(FrontendApplication, args);
  }
}
