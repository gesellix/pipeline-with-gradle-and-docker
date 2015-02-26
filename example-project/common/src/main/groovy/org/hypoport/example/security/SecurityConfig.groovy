package org.hypoport.example.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.env.Environment
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  Environment env

  public SecurityConfig() {
    super(disableDefaults: true)
  }

  @Override
  void configure(WebSecurity web) throws Exception {
    if (true /* it's an example project */
        || env.acceptsProfiles("no-auth")) {
      web.ignoring().anyRequest()
    }
    web.ignoring().antMatchers("/main.css", "**/favicon.ico", "/lib/production/**")
  }
}
