package org.hypoport.example.backend

import org.springframework.boot.SpringApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackages = "org.hypoport.example")
class BackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(BackendApplication, args)
  }
}
