package com.andersbohn.mytracks.security;

import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
  private final UserAuthService userAuthService;

  public CustomOAuth2UserService(UserAuthService userAuthService) {
    this.userAuthService = userAuthService;
  }

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);

    String email = oAuth2User.getAttribute("email");
    String name = oAuth2User.getAttribute("name");
    String sub = oAuth2User.getAttribute("sub");

    if (!userAuthService.isAllowed(email)) {
      throw new OAuth2AuthenticationException(
          new OAuth2Error("access_denied"), "Not an allowed user: " + email);
    }

    userAuthService.findOrRegister(email, name, "google", sub);

    return new DefaultOAuth2User(
        List.of(new SimpleGrantedAuthority("ROLE_USER")), oAuth2User.getAttributes(), "email");
  }
}
