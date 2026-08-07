package com.modwin.ModwinChatApp.dto;

import com.modwin.ModwinChatApp.persistence.model.User;
import lombok.*;

import java.util.List;
import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
public class ChatDto {
    private Integer ID;
    private Set<UserDto> users;
    private List<MessageDto> chatMessages;
    private UserDto owner;
}
