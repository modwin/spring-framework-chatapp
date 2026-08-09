package com.modwin.ModwinChatApp.exception;

public class FriendshipAlreadyExistsException extends RuntimeException {
    public FriendshipAlreadyExistsException() {
        super("A friendship or pending friend request already exists between these users.");
    }
}
