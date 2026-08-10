package com.modwin.ModwinChatApp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank(message = "Email is required.")
        @Email(message = "Email must be valid.")
        @Size(max = 254, message = "Email is too long.")
        String email,

        @NotBlank(message = "Username is required.")
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters.")
        @Pattern(regexp = "[A-Za-z0-9._-]+", message = "Username contains unsupported characters.")
        String username,

        @NotBlank(message = "Name is required.")
        @Size(max = 100, message = "Name must be at most 100 characters.")
        String name,

        @NotBlank(message = "Password is required.")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters.")
        String password
) {
}
