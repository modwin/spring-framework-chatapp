package com.modwin.ModwinChatApp.controller;

import com.modwin.ModwinChatApp.dto.FriendshipRequest;
import com.modwin.ModwinChatApp.dto.FriendshipResponse;
import com.modwin.ModwinChatApp.service.FriendshipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/friendships")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @PostMapping
    public ResponseEntity<FriendshipResponse> sendRequest(
            @Valid @RequestBody FriendshipRequest request,
            Principal principal
    ) {
        FriendshipResponse response = friendshipService.sendRequest(
                principal.getName(),
                request.recipientEmail()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{friendshipId}")
    public ResponseEntity<Void> removeFriendship(
            @PathVariable Integer friendshipId,
            Principal principal
    ) {
        friendshipService.removeFriendship(friendshipId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
