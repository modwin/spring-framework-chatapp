package com.modwin.ModwinChatApp.controller;

import com.modwin.ModwinChatApp.dto.FriendshipRequest;
import com.modwin.ModwinChatApp.dto.FriendshipResponse;
import com.modwin.ModwinChatApp.persistence.model.User;
import com.modwin.ModwinChatApp.service.AuthenticatedUserProvider;
import com.modwin.ModwinChatApp.service.FriendshipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/friendships")
public class FriendshipController {

    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final FriendshipService friendshipService;

    public FriendshipController(
            AuthenticatedUserProvider authenticatedUserProvider,
            FriendshipService friendshipService
    ) {
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.friendshipService = friendshipService;
    }

    @GetMapping
    public List<FriendshipResponse> list(Authentication authentication) {
        return friendshipService.listFor(authenticatedUserProvider.require(authentication));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FriendshipResponse sendRequest(
            @Valid @RequestBody FriendshipRequest request,
            Authentication authentication
    ) {
        User currentUser = authenticatedUserProvider.require(authentication);
        return friendshipService.sendRequest(currentUser, request.recipientEmail());
    }

    @PatchMapping("/{friendshipId}/accept")
    public FriendshipResponse accept(
            @PathVariable Integer friendshipId,
            Authentication authentication
    ) {
        return friendshipService.accept(
                friendshipId,
                authenticatedUserProvider.require(authentication)
        );
    }

    @DeleteMapping("/{friendshipId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(
            @PathVariable Integer friendshipId,
            Authentication authentication
    ) {
        friendshipService.remove(
                friendshipId,
                authenticatedUserProvider.require(authentication)
        );
    }
}
