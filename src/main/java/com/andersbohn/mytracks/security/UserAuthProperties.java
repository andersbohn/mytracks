package com.andersbohn.mytracks.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
public class UserAuthProperties {
  private List<String> allowedEmails = List.of("*");
  private String mockEmail;

  public List<String> getAllowedEmails() {
    return allowedEmails;
  }

  public void setAllowedEmails(List<String> allowedEmails) {
    this.allowedEmails = allowedEmails;
  }

  public String getMockEmail() {
    return mockEmail;
  }

  public void setMockEmail(String mockEmail) {
    this.mockEmail = mockEmail;
  }

  public boolean isAllowed(String email) {
    if (allowedEmails.contains("*")) return true;
    return allowedEmails.stream().anyMatch(e -> e.equalsIgnoreCase(email));
  }

  public boolean hasMockEmail() {
    return mockEmail != null && !mockEmail.isBlank();
  }
}
