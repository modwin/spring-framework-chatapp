package com.modwin.ModwinChatApp.dto;

import com.modwin.ModwinChatApp.util.FriendshipStatus;

public record FriendshipResponse(
        Integer id,
        FriendDto requester,
        FriendDto recipient,
        FriendshipStatus status
) {
}
