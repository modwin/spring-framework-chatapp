package com.modwin.ModwinChatApp.controller;

import com.modwin.ModwinChatApp.service.FriendshipService;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/friendships")
public class FriendshipController {

    private final FriendshipService friendshipService;
    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }
}
