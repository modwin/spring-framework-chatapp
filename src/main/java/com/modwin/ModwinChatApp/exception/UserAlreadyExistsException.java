package com.modwin.ModwinChatApp.exception;


public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String msg){
        super(msg);
    }
}
