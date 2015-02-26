package org.hypoport.example.web

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate

@Configuration
class HttpConfig {

  Logger logger = LoggerFactory.getLogger(HttpConfig)

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

  def httpsProxyAwareRequestFactory() {
    def factory = new SimpleClientHttpRequestFactory()
    def httpsProxy = System.env.https_proxy
    if (httpsProxy && !"null".equals(httpsProxy)) {
      logger.warn "configuring proxy at ${httpsProxy}..."
      def httpsProxyUri = new URI(httpsProxy as String)
      factory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpsProxyUri.host as String, httpsProxyUri.port)))
    }
    return factory
  }

  @Bean
  public RestOperations restOperations() {
    RestTemplate restTemplate = new RestTemplate(httpsProxyAwareRequestFactory())
    restTemplate.messageConverters.add(0, stringHttpMessageConverter())
    restTemplate.messageConverters.add(1, jackson2HttpMessageConverter())
    return restTemplate
  }
}
