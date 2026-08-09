package com.modwin.ModwinChatApp.service;

import com.modwin.ModwinChatApp.exception.AccessDeniedException;
import com.modwin.ModwinChatApp.exception.FriendshipNotFoundException;
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
    public void sendRequest(Integer requesterId, String recipientEmail){
        Optional<User> requester = userRepository.findById(requesterId);
        Optional<User> recipient = userRepository.findByEmail(recipientEmail);

        if(requester.isPresent() && recipient.isPresent()) {
            Friendship friendship = Friendship.builder().recipient(recipient.get()).requester(requester.get()).status(FriendshipStatus.PENDING).build();
            friendshipRepository.save(friendship);
        }
        else throw new UserNotFoundException("No user registered associated with that email address.");
    }

    public void removeFriendship(Integer friendshipId, Integer userId) {
        Friendship f = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException(friendshipId));

        boolean isParticipant = f.getRequester().getId().equals(userId)
                || f.getRecipient().getId().equals(userId);
        if (!isParticipant) {
            throw new AccessDeniedException("You are not part of this friendship");
        }

        friendshipRepository.delete(f);
    }



}
