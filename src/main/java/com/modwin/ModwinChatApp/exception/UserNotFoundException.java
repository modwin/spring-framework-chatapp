package com.modwin.ModwinChatApp.exception;

public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException(String msg){
        super(msg);
    }

}
