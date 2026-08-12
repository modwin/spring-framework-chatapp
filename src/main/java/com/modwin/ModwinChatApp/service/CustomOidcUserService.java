package com.modwin.ModwinChatApp.service;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomOidcUserService extends OidcUserService {

    private final UserService userService;

    public CustomOidcUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        try {
            String email = oidcUser.getEmail();
            if (email == null || !Boolean.TRUE.equals(oidcUser.getEmailVerified())) {
                throw new InternalAuthenticationServiceException("Google did not provide a verified email address.");
            }
            userService.provisionOidcUser(email, oidcUser.getFullName());
            return new DefaultOidcUser(
                    oidcUser.getAuthorities(),
                    oidcUser.getIdToken(),
                    oidcUser.getUserInfo(),
                    "email"
            );
        } catch (InternalAuthenticationServiceException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new InternalAuthenticationServiceException("Could not provision the Google user.", exception);
        }
    }
}
