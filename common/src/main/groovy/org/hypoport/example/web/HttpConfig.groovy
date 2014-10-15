package org.hypoport.example.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter

@Configuration
class HttpConfig {

  @Bean
  public MappingJackson2HttpMessageConverter jackson2HttpMessageConverter() {
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter()
    converter.objectMapper = jsonObjectMapper()
    return converter
  }

  @Bean
  public JsonObjectMapper jsonObjectMapper() {
    return new JsonObjectMapper()
  }

  @Bean
  StringHttpMessageConverter stringHttpMessageConverter() {
    StringHttpMessageConverter converter = new StringHttpMessageConverter()
    converter.writeAcceptCharset = false
    converter.supportedMediaTypes = [MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON]
    return converter
  }
}
