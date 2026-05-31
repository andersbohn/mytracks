package com.andersbohn.mytracks.api;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/otlp")
public class OtlpProxyController {

  private final String jaegerBase;
  private final RestTemplate restTemplate;

  public OtlpProxyController(@Value("${management.otlp.tracing.endpoint:}") String otlpEndpoint) {
    this.jaegerBase = otlpEndpoint.replaceAll("/v1/traces$", "").trim();
    var converter = new ByteArrayHttpMessageConverter();
    converter.setSupportedMediaTypes(List.of(MediaType.ALL));
    this.restTemplate = new RestTemplate(List.of(converter));
  }

  @PostMapping("/v1/traces")
  public ResponseEntity<byte[]> proxyTraces(HttpServletRequest request) throws IOException {
    if (jaegerBase.isBlank()) {
      return ResponseEntity.noContent().build();
    }
    byte[] body = request.getInputStream().readAllBytes();
    var headers = new HttpHeaders();
    if (request.getContentType() != null) {
      headers.set(HttpHeaders.CONTENT_TYPE, request.getContentType());
    }
    try {
      return restTemplate.postForEntity(
          jaegerBase + "/v1/traces", new HttpEntity<>(body, headers), byte[].class);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }
  }
}
