package com.modwin.ModwinChatApp.controller;

import com.modwin.ModwinChatApp.dto.ChatDto;
import com.modwin.ModwinChatApp.dto.MessageDto;
import com.modwin.ModwinChatApp.promise.MessageResponse;
import com.modwin.ModwinChatApp.promise.SendMessageRequest;
import com.modwin.ModwinChatApp.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.Principal;
import java.util.List;

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
    public ResponseEntity<ChatDto> getChatMessages(@PathVariable Integer chatId) throws Exception {
        ChatDto chatDto = chatService.getChatById(chatId);
        boolean hasMessage  = chatDto.getChatMessages().isEmpty();
        if(hasMessage) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(chatDto);
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(@PathVariable Integer chatId, @Valid @RequestBody SendMessageRequest request, Principal principal) {
        return null;
    }

}
