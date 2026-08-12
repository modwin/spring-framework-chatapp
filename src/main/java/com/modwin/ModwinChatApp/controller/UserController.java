package com.modwin.ModwinChatApp.controller;

import com.modwin.ModwinChatApp.dto.UserResponse;
import com.modwin.ModwinChatApp.service.AuthenticatedUserProvider;
import com.modwin.ModwinChatApp.util.UserMapper;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthenticatedUserProvider authenticatedUserProvider;

    public UserController(AuthenticatedUserProvider authenticatedUserProvider) {
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @GetMapping("/me")
    public UserResponse currentUser(Authentication authentication) {
        return UserMapper.toResponse(authenticatedUserProvider.require(authentication));
    }
}
