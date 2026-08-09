package com.modwin.ModwinChatApp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record FriendshipRequest(
        @NotBlank(message = "Recipient email is required.")
        @Email(message = "Invalid recipient email format.")
        String recipientEmail
) {
}
