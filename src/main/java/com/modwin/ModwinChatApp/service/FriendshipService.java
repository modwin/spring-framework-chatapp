package com.modwin.ModwinChatApp.service;

import com.modwin.ModwinChatApp.dto.FriendDto;
import com.modwin.ModwinChatApp.dto.FriendshipResponse;
import com.modwin.ModwinChatApp.exception.AccessDeniedException;
import com.modwin.ModwinChatApp.exception.FriendshipAlreadyExistsException;
import com.modwin.ModwinChatApp.exception.FriendshipNotFoundException;
import com.modwin.ModwinChatApp.exception.UserNotFoundException;
import com.modwin.ModwinChatApp.persistence.model.Friendship;
import com.modwin.ModwinChatApp.persistence.model.User;
import com.modwin.ModwinChatApp.persistence.repository.FriendshipRepository;
import com.modwin.ModwinChatApp.persistence.repository.UserRepository;
import com.modwin.ModwinChatApp.util.FriendshipStatus;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public FriendshipService(FriendshipRepository friendshipRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FriendshipResponse sendRequest(String requesterUsername, String recipientEmail) {
        User requester = userRepository.findByUsername(requesterUsername)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user was not found."));
        User recipient = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new UserNotFoundException(
                        "No user is registered with that email address."
                ));

        if (requester.equals(recipient)) {
            throw new IllegalArgumentException("You cannot send a friend request to yourself.");
        }
        if (friendshipRepository.existsBetween(requester, recipient)) {
            throw new FriendshipAlreadyExistsException();
        }

        Friendship friendship = friendshipRepository.save(
                new Friendship(requester, recipient, FriendshipStatus.PENDING)
        );
        return toResponse(friendship);
    }

    @Transactional
    public void removeFriendship(Integer friendshipId, String currentUsername) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user was not found."));
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException(friendshipId));

        boolean isParticipant = friendship.getRequester().equals(currentUser)
                || friendship.getRecipient().equals(currentUser);
        if (!isParticipant) {
            throw new AccessDeniedException("You are not part of this friendship");
        }

        friendshipRepository.delete(friendship);
    }

    private FriendshipResponse toResponse(Friendship friendship) {
        User requester = friendship.getRequester();
        User recipient = friendship.getRecipient();
        return new FriendshipResponse(
                friendship.getId(),
                new FriendDto(requester.getId(), requester.getUsername()),
                new FriendDto(recipient.getId(), recipient.getUsername()),
                friendship.getStatus()
        );
    }
}
