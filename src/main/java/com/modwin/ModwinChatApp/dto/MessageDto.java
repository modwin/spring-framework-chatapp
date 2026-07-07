package com.modwin.ModwinChatApp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MessageDto {
    private Integer ID;
    private ChatDto chat;
    private String text;
    private LocalDateTime published;
}
