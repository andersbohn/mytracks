package com.andersbohn.mytracks.security;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HttpGoogleIdTokenVerifier implements GoogleIdTokenVerifier {

  private final RestClient restClient;

  public HttpGoogleIdTokenVerifier(RestClient.Builder restClientBuilder) {
    this.restClient = restClientBuilder.baseUrl("https://oauth2.googleapis.com").build();
  }

  @Override
  public GoogleTokenClaims verify(String idToken) {
    try {
      var response =
          restClient
              .get()
              .uri("/oauth2/v3/tokeninfo?id_token={token}", idToken)
              .retrieve()
              .body(TokenInfoResponse.class);
      if (response == null || response.sub() == null) {
        throw new InvalidTokenException("invalid token response");
      }
      return new GoogleTokenClaims(response.sub(), response.email(), response.name());
    } catch (RestClientException e) {
      throw new InvalidTokenException("token verification failed", e);
    }
  }

  private record TokenInfoResponse(String sub, String email, String name) {}
}
