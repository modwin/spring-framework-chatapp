package com.modwin.ModwinChatApp.dto;

import java.util.Map;

public class GoogleUserDto {

    private final Map<String, Object> attributes;

    public GoogleUserDto(Map<String, Object> attributes){
        this.attributes = attributes;
    }

    public String getId(){
        return (String) attributes.get("sub");
    }

    public String getName(){
        return (String) attributes.get("name");
    }

    public String getEmail(){
        return (String) attributes.get("email");
    }
}
