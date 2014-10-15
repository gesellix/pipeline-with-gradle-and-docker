package org.hypoport.example.frontend

import org.hypoport.example.web.WebMvcConfig
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry

@EnableWebMvc
@Configuration
class FrontendWebMvcConfig extends WebMvcConfig {

  @Override
  void addResourceHandlers(ResourceHandlerRegistry registry) {
    if (!registry.hasMappingForPattern("/**")) {
      registry.addResourceHandler("/**")
          .addResourceLocations("classpath:/public/")
    }
  }

  @Override
  void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/").setViewName("/index.html")
  }
}
