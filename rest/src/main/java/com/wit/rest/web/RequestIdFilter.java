// after: rest/src/main/java/com/wit/rest/web/RequestIdFilter.java
package com.wit.rest.web;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that ensures each incoming HTTP request has a request id.
 * - If client supplies X-Request-Id, reuse it.
 * - Otherwise generate a new UUID and set it as X-Request-Id response header.
 * - Put the request id into SLF4J MDC under key "requestId" for logging.
 */
@Component
public class RequestIdFilter implements Filter {
  public static final String HEADER = "X-Request-Id";
  public static final String MDC_KEY = "requestId";

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;

    String id = request.getHeader(HEADER);
    if (id == null || id.isBlank()) {
      id = UUID.randomUUID().toString();
    }

    // Put into MDC for downstream logging in the REST app
    MDC.put(MDC_KEY, id);

    // Ensure it's on the response (used by the bonus requirement)
    response.setHeader(HEADER, id);

    try {
      chain.doFilter(request, response);
    } finally {
      MDC.remove(MDC_KEY);
    }
  }
}
