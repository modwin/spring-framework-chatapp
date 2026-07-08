package com.modwin.ModwinChatApp.controller;

import com.modwin.ModwinChatApp.dto.ChatDto;
import com.modwin.ModwinChatApp.dto.UserDto;
import com.modwin.ModwinChatApp.persistence.repository.ChatRepository;
import com.modwin.ModwinChatApp.persistence.repository.MessageRepository;
import com.modwin.ModwinChatApp.service.ChatService;
import org.springframework.http.ResponseEntity;

public class ChatController {

    // TODO: Add chat business layer logic for sending/receiving messages.

    private final ChatService chatService;

    public ChatController(ChatRepository chatRepository, ChatService chatService) {

        this.chatService = chatService;
    }

    public ResponseEntity<ChatDto> getChatById(Integer id) {
        return ResponseEntity.ok(chatService.getChatById(id));
    }
}
