package org.hypoport.example.web

import org.slf4j.MDC

import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AddSelectedBackendToMdcFilter implements Filter {

  static final def REQ_BACKEND = "request.backend"

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;

    def currentBackend = request.getHeader("X-Backend")
    MDC.put(REQ_BACKEND, !currentBackend?.empty ? currentBackend : "unknown")
    try {
      chain.doFilter(request, response)
    }
    finally {
      MDC.remove(REQ_BACKEND)
    }
  }

  @Override
  public void destroy() {
  }
}
