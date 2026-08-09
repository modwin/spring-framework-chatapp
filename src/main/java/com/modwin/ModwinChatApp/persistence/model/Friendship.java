package com.modwin.ModwinChatApp.persistence.model;

import com.modwin.ModwinChatApp.dto.UserDto;
import com.modwin.ModwinChatApp.util.FriendshipStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Friendship {
    private Integer id;
    private User requester;
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status;



}
