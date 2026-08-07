package com.modwin.ModwinChatApp.dto;

import com.modwin.ModwinChatApp.persistence.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatDto {
    private Integer ID;
    private Set<UserDto> users;
    private List<MessageDto> chatMessages;
    private UserDto owner;
}
