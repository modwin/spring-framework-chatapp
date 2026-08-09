package com.modwin.ModwinChatApp.controller;

import com.modwin.ModwinChatApp.service.MessageService;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }
}
