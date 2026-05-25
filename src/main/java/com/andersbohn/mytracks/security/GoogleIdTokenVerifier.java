package com.andersbohn.mytracks.security;

public interface GoogleIdTokenVerifier {
  GoogleTokenClaims verify(String idToken);
}
