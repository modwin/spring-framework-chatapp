package com.modwin.ModwinChatApp.service;

import com.modwin.ModwinChatApp.dto.ChatDto;
import com.modwin.ModwinChatApp.persistence.model.Chat;
import com.modwin.ModwinChatApp.persistence.repository.ChatRepository;
import com.modwin.ModwinChatApp.persistence.repository.MessageRepository;

import java.util.Optional;

public class ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    public ChatService(ChatRepository chatRepository, MessageRepository messageRepository) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
    }


    public ChatDto getChatById(Integer id) throws Exception {
        Optional<Chat> chat = chatRepository.findById(id);
        if(chat.isEmpty()) throw new Exception();
        return null;
    }

    // TODO: Add chat @Service layer logic for sending/receiving messages.

}
