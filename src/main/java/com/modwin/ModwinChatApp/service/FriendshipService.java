package com.modwin.ModwinChatApp.service;

import com.modwin.ModwinChatApp.dto.UserDto;
import com.modwin.ModwinChatApp.exception.UserNotFoundException;
import com.modwin.ModwinChatApp.persistence.model.Friendship;
import com.modwin.ModwinChatApp.persistence.model.User;
import com.modwin.ModwinChatApp.persistence.repository.FriendshipRepository;
import com.modwin.ModwinChatApp.persistence.repository.UserRepository;
import com.modwin.ModwinChatApp.util.FriendshipStatus;
import jakarta.transaction.Transactional;

import java.util.Optional;

public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public FriendshipService(FriendshipRepository friendshipRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void sendFriendRequest(UserDto user, String friendEmail){
        Optional<User> requester = userRepository.findByEmail(user.getEmail());
        Optional<User> recipient = userRepository.findByEmail(friendEmail);

        if(requester.isPresent() && recipient.isPresent()) {
            Friendship friendship = Friendship.builder().recipient(recipient.get()).requester(requester.get()).status(FriendshipStatus.PENDING).build();
            friendshipRepository.save(friendship);
        }
        else throw new UserNotFoundException("No user registered associated with that email address.");
    }




}
