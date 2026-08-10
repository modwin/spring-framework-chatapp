package com.modwin.ModwinChatApp.util;

import com.modwin.ModwinChatApp.dto.UserResponse;
import com.modwin.ModwinChatApp.persistence.model.Role;
import com.modwin.ModwinChatApp.persistence.model.User;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getName(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .sorted()
                        .collect(Collectors.toCollection(LinkedHashSet::new))
        );
    }
}
