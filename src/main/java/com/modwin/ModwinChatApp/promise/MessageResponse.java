package com.modwin.ModwinChatApp.promise;

import java.time.LocalDateTime;

public record MessageResponse(
        Integer id,
        String content,
        LocalDateTime sentAt
) {}
