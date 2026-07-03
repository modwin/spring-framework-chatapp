package com.modwin.ModwinChatApp.exception;


public class UserAlreadyExistsException extends IllegalArgumentException{

    public UserAlreadyExistsException(String msg){
        super(msg);
    }
}
