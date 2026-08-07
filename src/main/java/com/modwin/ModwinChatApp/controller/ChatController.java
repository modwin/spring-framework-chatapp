package com.modwin.ModwinChatApp.controller;

import com.modwin.ModwinChatApp.dto.ChatDto;
import com.modwin.ModwinChatApp.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller("/api/chats")
public class ChatController {

    // TODO: Add chat business layer logic for sending/receiving messages.

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("")
    public ResponseEntity<?> saveChat(ChatDto chatDto) throws Exception {
        chatService.saveChat(chatDto);
        return ResponseEntity.ok(null);
    }

    @GetMapping("{chatId}")
    public ResponseEntity<ChatDto> getChatById(@PathVariable Integer chatId) throws Exception {
        return ResponseEntity.ok(chatService.getChatById(chatId));
    }



    @GetMapping("/{chatId}/messages")
    public ResponseEntity<ChatDto> getChatMessages(@PathVariable Integer chatId) {
        return ResponseEntity.ok(null);
    }

    @PostMapping("{chatId}")
    public ResponseEntity<?> saveChatMessages(ChatDto chatDto) {
        chatService.saveChatMessages(chatDto);
        return ResponseEntity.ok(null);
    }

}
