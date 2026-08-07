package com.modwin.ModwinChatApp.dto;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
public class MessageDto {
    private Integer ID;
    private ChatDto chat;
    private String content;
    private LocalDateTime published;
    private UserDto sender;
}
