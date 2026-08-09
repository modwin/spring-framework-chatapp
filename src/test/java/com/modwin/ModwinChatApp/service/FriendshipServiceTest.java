package com.modwin.ModwinChatApp.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;
    @Mock
    private UserRepository userRepository;

    private FriendshipService friendshipService;

    @BeforeEach
    void setUp() {
        friendshipService = new FriendshipService(friendshipRepository, userRepository);
    }

    @Test
    void sendRequestCreatesPendingFriendshipAndReturnsPublicUserData() {
        User requester = user(1, "alice", "alice@example.com");
        User recipient = user(2, "bob", "bob@example.com");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(requester));
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(recipient));
        when(friendshipRepository.existsBetween(requester, recipient)).thenReturn(false);
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(invocation -> {
            Friendship friendship = invocation.getArgument(0);
            friendship.setId(10);
            return friendship;
        });

        FriendshipResponse response = friendshipService.sendRequest("alice", "bob@example.com");

        ArgumentCaptor<Friendship> friendshipCaptor = ArgumentCaptor.forClass(Friendship.class);
        verify(friendshipRepository).save(friendshipCaptor.capture());
        Friendship saved = friendshipCaptor.getValue();
        assertThat(saved.getRequester()).isSameAs(requester);
        assertThat(saved.getRecipient()).isSameAs(recipient);
        assertThat(saved.getStatus()).isEqualTo(FriendshipStatus.PENDING);
        assertThat(response.id()).isEqualTo(10);
        assertThat(response.requester().username()).isEqualTo("alice");
        assertThat(response.recipient().username()).isEqualTo("bob");
    }

    @Test
    void sendRequestRejectsSelfRequest() {
        User alice = user(1, "alice", "alice@example.com");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));

        assertThatThrownBy(() -> friendshipService.sendRequest("alice", "alice@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("yourself");

        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void sendRequestRejectsExistingRequestInEitherDirection() {
        User requester = user(1, "alice", "alice@example.com");
        User recipient = user(2, "bob", "bob@example.com");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(requester));
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(recipient));
        when(friendshipRepository.existsBetween(requester, recipient)).thenReturn(true);

        assertThatThrownBy(() -> friendshipService.sendRequest("alice", "bob@example.com"))
                .isInstanceOf(FriendshipAlreadyExistsException.class);

        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void sendRequestFailsWhenAuthenticatedUserDoesNotExist() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendshipService.sendRequest("missing", "bob@example.com"))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).findByEmail(any());
        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void sendRequestFailsWhenRecipientDoesNotExist() {
        User requester = user(1, "alice", "alice@example.com");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(requester));
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendshipService.sendRequest("alice", "missing@example.com"))
                .isInstanceOf(UserNotFoundException.class);

        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void participantCanRemoveFriendship() {
        User requester = user(1, "alice", "alice@example.com");
        User recipient = user(2, "bob", "bob@example.com");
        Friendship friendship = friendship(10, requester, recipient);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(requester));
        when(friendshipRepository.findById(10)).thenReturn(Optional.of(friendship));

        friendshipService.removeFriendship(10, "alice");

        verify(friendshipRepository).delete(friendship);
    }

    @Test
    void nonParticipantCannotRemoveFriendship() {
        User requester = user(1, "alice", "alice@example.com");
        User recipient = user(2, "bob", "bob@example.com");
        User outsider = user(3, "mallory", "mallory@example.com");
        Friendship friendship = friendship(10, requester, recipient);
        when(userRepository.findByUsername("mallory")).thenReturn(Optional.of(outsider));
        when(friendshipRepository.findById(10)).thenReturn(Optional.of(friendship));

        assertThatThrownBy(() -> friendshipService.removeFriendship(10, "mallory"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not part");

        verify(friendshipRepository, never()).delete(any());
    }

    @Test
    void removingMissingFriendshipReturnsDomainError() {
        User requester = user(1, "alice", "alice@example.com");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(requester));
        when(friendshipRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendshipService.removeFriendship(99, "alice"))
                .isInstanceOf(FriendshipNotFoundException.class)
                .hasMessageContaining("99");
    }

    private static User user(int id, String username, String email) {
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .name(username)
                .password("encoded-password")
                .build();
    }

    private static Friendship friendship(int id, User requester, User recipient) {
        Friendship friendship = new Friendship(requester, recipient, FriendshipStatus.ACCEPTED);
        friendship.setId(id);
        return friendship;
    }
}
