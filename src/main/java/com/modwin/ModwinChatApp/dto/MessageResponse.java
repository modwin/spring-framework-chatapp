package com.modwin.ModwinChatApp.dto;

import java.time.LocalDateTime;

public record MessageResponse(
        Integer id,
        String content,
        LocalDateTime sentAt
) {}
