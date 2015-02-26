package org.hypoport.example.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.filter.CharacterEncodingFilter
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.filter.ShallowEtagHeaderFilter
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@EnableWebMvc
@Configuration
@Import(HttpConfig)
class WebMvcConfig extends WebMvcConfigurerAdapter {

  @Autowired
  StringHttpMessageConverter stringHttpMessageConverter

  @Autowired
  MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter

  @Override
  void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(stringHttpMessageConverter)
    converters.add(mappingJackson2HttpMessageConverter)
  }

  @Bean
  Filter characterEncodingFilter() {
    CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
    characterEncodingFilter.setEncoding("UTF-8");
    characterEncodingFilter.setForceEncoding(true);
    return characterEncodingFilter
  }

  @Bean
  Filter addSelectedBackendToMdcFilter() {
    new AddSelectedBackendToMdcFilter()
  }

  @Bean
  Filter etagFilter() {
    new ShallowEtagHeaderFilter()
  }

  @Bean
  Filter noCacheFilter() {
    new OncePerRequestFilter() {

      @Override
      protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-cache")
        filterChain.doFilter(request, response);
      }
    }
  }

  @Bean
  ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver() {
    ExceptionHandlerExceptionResolver resolver = new ExceptionHandlerExceptionResolver()
    resolver.messageConverters = [mappingJackson2HttpMessageConverter, stringHttpMessageConverter]
    return resolver
  }

  @Override
  void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    configurer.enable()
  }
}
