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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public FriendshipService(FriendshipRepository friendshipRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<FriendshipResponse> listFor(User currentUser) {
        return friendshipRepository.findAllForUser(currentUser.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public FriendshipResponse sendRequest(User requester, String recipientEmail) {
        User recipient = userRepository.findByEmail(recipientEmail.trim().toLowerCase(Locale.ROOT))
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
    public FriendshipResponse accept(Integer friendshipId, User currentUser) {
        Friendship friendship = requireFriendship(friendshipId);
        if (!friendship.getRecipient().equals(currentUser)) {
            throw new AccessDeniedException("Only the recipient can accept this friend request.");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("Only pending friend requests can be accepted.");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        return toResponse(friendship);
    }

    @Transactional
    public void remove(Integer friendshipId, User currentUser) {
        Friendship friendship = requireFriendship(friendshipId);
        boolean isParticipant = friendship.getRequester().equals(currentUser)
                || friendship.getRecipient().equals(currentUser);
        if (!isParticipant) {
            throw new AccessDeniedException("You are not part of this friendship.");
        }
        friendshipRepository.delete(friendship);
    }

    private Friendship requireFriendship(Integer friendshipId) {
        return friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException(friendshipId));
    }

    private FriendshipResponse toResponse(Friendship friendship) {
        User requester = friendship.getRequester();
        User recipient = friendship.getRecipient();
        return new FriendshipResponse(
                friendship.getId(),
                new FriendDto(requester.getId(), requester.getUsername(), requester.getName()),
                new FriendDto(recipient.getId(), recipient.getUsername(), recipient.getName()),
                friendship.getStatus()
        );
    }
}
