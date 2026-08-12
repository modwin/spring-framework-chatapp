package com.modwin.ModwinChatApp.service;

import com.modwin.ModwinChatApp.dto.LoginRequest;
import com.modwin.ModwinChatApp.dto.RegisterUserRequest;
import com.modwin.ModwinChatApp.dto.UserResponse;
import com.modwin.ModwinChatApp.persistence.model.User;
import com.modwin.ModwinChatApp.util.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UserService userService;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            UserService userService
    ) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.userService = userService;
    }

    public UserResponse register(
            RegisterUserRequest registration,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        User user = userService.register(registration);
        authenticate(registration.email(), registration.password(), request, response);
        return UserMapper.toResponse(user);
    }

    public UserResponse login(
            LoginRequest login,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Authentication authentication = authenticate(login.email(), login.password(), request, response);
        return userService.responseByUsername(authentication.getName());
    }

    private Authentication authenticate(
            String email,
            String password,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        email.trim().toLowerCase(Locale.ROOT),
                        password
                )
        );

        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        return authentication;
    }
}
