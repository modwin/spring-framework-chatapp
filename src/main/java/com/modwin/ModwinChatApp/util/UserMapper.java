package com.modwin.ModwinChatApp.util;

import com.modwin.ModwinChatApp.dto.UserDto;
import com.modwin.ModwinChatApp.persistence.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public static UserDto toDTO(User user){
        if(user == null){
            return new UserDto();
        }
        return UserDto.builder()
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .password(null)
                .friends(user.getFriends())
                .build();
    }

    public static User toEntity(UserDto userDTO){
        if(userDTO == null){
            return new User();
        }
        return User.builder()
                .name(userDTO.getName())
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .roles(userDTO.getRoles())
                .friends(userDTO.getFriends())
                .password(null)
                .build();
    }
}
