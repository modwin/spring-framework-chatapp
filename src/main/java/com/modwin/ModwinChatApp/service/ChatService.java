package com.modwin.ModwinChatApp.service;

import com.modwin.ModwinChatApp.dto.ChatDto;
import com.modwin.ModwinChatApp.persistence.model.Chat;
import com.modwin.ModwinChatApp.persistence.model.Message;
import com.modwin.ModwinChatApp.persistence.repository.ChatRepository;
import com.modwin.ModwinChatApp.persistence.repository.MessageRepository;
import com.modwin.ModwinChatApp.persistence.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ChatService(ChatRepository chatRepository, MessageRepository messageRepository, UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public ChatDto getChatById(Integer id) throws Exception {
        Optional<Chat> chat = chatRepository.findById(id);
        if(chat.isEmpty()) throw new Exception();
        Principal authPrincipal = SecurityContextHolder.getContext().getAuthentication();
        return null;
    }

    // TODO: Add chat @Service layer logic for sending/receiving messages.

    public Set<Message> getChatMessages(Integer chatId){
        Optional<Chat> optionalChat = chatRepository.findById(chatId);
        return new HashSet<>();
    }

    public void saveChat(ChatDto chatDto) throws Exception {

    }

}
