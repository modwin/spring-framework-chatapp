package com.modwin.ModwinChatApp.util;

import com.modwin.ModwinChatApp.dto.UserDto;
import com.modwin.ModwinChatApp.persistence.model.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public static UserDto toDTO(User user){
        if(user == null){
            return new UserDto();
        }
        return UserDto.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .roles(user.getRoles())
                .password(null)
                .build();
    }

    public static User toEntity(UserDto userDTO){
        if(userDTO == null){
            return new User();
        }
        return User.builder()
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .name(userDTO.getName())
                .roles(userDTO.getRoles())
                .password(null)
                .build();
    }
}
