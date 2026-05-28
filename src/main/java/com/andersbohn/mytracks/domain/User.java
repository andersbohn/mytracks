package com.andersbohn.mytracks.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "display_name")
  private String displayName;

  @Column(name = "sso_provider", nullable = false)
  private String ssoProvider;

  @Column(name = "sso_subject", nullable = false)
  private String ssoSubject;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role = UserRole.GUEST;

  protected User() {}

  public User(
      String email, String displayName, String ssoProvider, String ssoSubject, Instant createdAt) {
    this.email = email;
    this.displayName = displayName;
    this.ssoProvider = ssoProvider;
    this.ssoSubject = ssoSubject;
    this.createdAt = createdAt;
    this.role = UserRole.GUEST;
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getSsoProvider() {
    return ssoProvider;
  }

  public String getSsoSubject() {
    return ssoSubject;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }
}
