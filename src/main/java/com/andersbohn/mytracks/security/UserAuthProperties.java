package com.andersbohn.mytracks.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
public class UserAuthProperties {
  private String mockEmail;

  public String getMockEmail() {
    return mockEmail;
  }

  public void setMockEmail(String mockEmail) {
    this.mockEmail = mockEmail;
  }

  public boolean hasMockEmail() {
    return mockEmail != null && !mockEmail.isBlank();
  }
}
