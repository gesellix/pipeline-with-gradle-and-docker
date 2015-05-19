package org.hypoport.example.backend

import com.matttproud.accepts.Accept
import com.matttproud.accepts.Parser
import io.prometheus.client.Prometheus
import io.prometheus.client.utility.hotspot.Hotspot
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.springframework.http.HttpStatus.OK
import static org.springframework.web.bind.annotation.RequestMethod.GET

@RestController
class ExampleMetricsService {

  @Autowired
  SpringBootMetricsExpositionHook springBootMetricsExpositionHook

  @PostConstruct
  def init() {
    Prometheus.defaultInitialize()
    Prometheus.defaultAddPreexpositionHook(new Hotspot())
    Prometheus.defaultAddPreexpositionHook(springBootMetricsExpositionHook)
  }

  @RequestMapping(value = "/prom", method = GET)
  @ResponseStatus(OK)
  def metrics(HttpServletRequest request, HttpServletResponse response) throws IOException {
    allowCrossDomainRequests response
    boolean dispensed = false
    try {
      for (final Accept spec : Parser.parse(request)) {
        if ("application".equals(spec.getType()) && "vnd.google.protobuf".equals(spec.getSubtype())) {
          final Map<String, String> params = spec.getParams()
          if ("io.prometheus.client.MetricFamily".equals(params.get("proto"))
              && "delimited".equals(params.get("encoding"))) {
            dumpProto(response)
            dispensed = true
            break
          }
        }
      }
    }
    catch (final IllegalArgumentException ignore) {
    }
    finally {
      dispensed || dumpJson(response)
    }
  }

  def dumpJson(final HttpServletResponse resp) throws IOException {
    resp.setContentType("application/json; schema=\"prometheus/telemetry\"; version=0.0.2")
    Prometheus.defaultDumpJson(resp.getWriter())
  }

  def dumpProto(final HttpServletResponse resp) throws IOException {
    resp.setContentType("application/vnd.google.protobuf; proto=\"io.prometheus.client.MetricFamily\"; encoding=\"delimited\"")
    final BufferedOutputStream buf = new BufferedOutputStream(resp.getOutputStream())
    Prometheus.defaultDumpProto(buf)
    buf.close()
  }

  def allowCrossDomainRequests(HttpServletResponse response) {
    // see https://developer.mozilla.org/En/HTTP_access_control
    // or  http://chase-seibert.github.com/blog/2012/01/27/using-access-control-allow-origin-to-make-cross-domain-post-requests-from-javsacript.html
    /*
    response = HttpResponse(json.dumps({"key": "value", "key2": "value"}))
    response["Access-Control-Allow-Origin"] = "*"
    response["Access-Control-Allow-Methods"] = "POST, GET, OPTIONS"
    response["Access-Control-Max-Age"] = "1000"
    response["Access-Control-Allow-Headers"] = "*"
     */
    response.addHeader("Access-Control-Allow-Origin", "*")
  }
}
