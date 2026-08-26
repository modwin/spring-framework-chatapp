package com.modwin.ModwinChatApp.controller;

import com.modwin.ModwinChatApp.dto.AuthProvidersResponse;
import com.modwin.ModwinChatApp.dto.LoginRequest;
import com.modwin.ModwinChatApp.dto.RegisterUserRequest;
import com.modwin.ModwinChatApp.dto.UserResponse;
import com.modwin.ModwinChatApp.security.OAuthProviderAvailability;
import com.modwin.ModwinChatApp.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final OAuthProviderAvailability oauthProviderAvailability;

    public AuthenticationController(
            AuthenticationService authenticationService,
            OAuthProviderAvailability oauthProviderAvailability
    ) {
        this.authenticationService = authenticationService;
        this.oauthProviderAvailability = oauthProviderAvailability;
    }

    @GetMapping("/providers")
    public AuthProvidersResponse providers() {
        Set<String> providers = new LinkedHashSet<>();
        providers.add("LOCAL");
        if (oauthProviderAvailability.isGoogleConfigured()) {
            providers.add("GOOGLE");
        }
        return new AuthProvidersResponse(providers);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(
            @Valid @RequestBody RegisterUserRequest registration,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return authenticationService.register(registration, request, response);
    }

    @PostMapping("/login")
    public UserResponse login(
            @Valid @RequestBody LoginRequest login,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return authenticationService.login(login, request, response);
    }
}
