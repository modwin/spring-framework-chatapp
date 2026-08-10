package com.modwin.ModwinChatApp.dto;

import java.util.Set;

public record UserResponse(
        Integer id,
        String email,
        String username,
        String name,
        Set<String> roles
) {
}
