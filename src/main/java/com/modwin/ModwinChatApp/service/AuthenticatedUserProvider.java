package com.modwin.ModwinChatApp.service;

import com.modwin.ModwinChatApp.persistence.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserProvider {

    private final UserService userService;

    public AuthenticatedUserProvider(UserService userService) {
        this.userService = userService;
    }

    public User require(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            return userService.requireByEmail(oidcUser.getEmail());
        }
        return userService.requireByUsername(authentication.getName());
    }
}
