package com.modwin.ModwinChatApp.exception;

public class FriendshipNotFoundException extends RuntimeException {
    public FriendshipNotFoundException(Integer friendshipId) {
        super("Friendship not found: " + friendshipId);
    }
}
