package com.modwin.ModwinChatApp.service;

import com.modwin.ModwinChatApp.dto.FriendshipResponse;
import com.modwin.ModwinChatApp.exception.AccessDeniedException;
import com.modwin.ModwinChatApp.exception.FriendshipAlreadyExistsException;
import com.modwin.ModwinChatApp.exception.FriendshipNotFoundException;
import com.modwin.ModwinChatApp.persistence.model.Friendship;
import com.modwin.ModwinChatApp.persistence.model.User;
import com.modwin.ModwinChatApp.persistence.repository.FriendshipRepository;
import com.modwin.ModwinChatApp.persistence.repository.UserRepository;
import com.modwin.ModwinChatApp.util.FriendshipStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
    void sendRequestCreatesPendingFriendship() {
        User alice = user(1, "alice", "alice@example.com");
        User bob = user(2, "bob", "bob@example.com");
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(bob));
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(invocation -> {
            Friendship friendship = invocation.getArgument(0);
            friendship.setId(10);
            return friendship;
        });

        FriendshipResponse response = friendshipService.sendRequest(alice, " BOB@example.com ");

        assertThat(response.id()).isEqualTo(10);
        assertThat(response.status()).isEqualTo(FriendshipStatus.PENDING);
        assertThat(response.requester().name()).isEqualTo("alice");
        assertThat(response.recipient().username()).isEqualTo("bob");
    }

    @Test
    void selfAndDuplicateRequestsAreRejected() {
        User alice = user(1, "alice", "alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));

        assertThatThrownBy(() -> friendshipService.sendRequest(alice, "alice@example.com"))
                .isInstanceOf(IllegalArgumentException.class);

        User bob = user(2, "bob", "bob@example.com");
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(bob));
        when(friendshipRepository.existsBetween(alice, bob)).thenReturn(true);

        assertThatThrownBy(() -> friendshipService.sendRequest(alice, "bob@example.com"))
                .isInstanceOf(FriendshipAlreadyExistsException.class);
        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void recipientCanAcceptPendingRequest() {
        User alice = user(1, "alice", "alice@example.com");
        User bob = user(2, "bob", "bob@example.com");
        Friendship friendship = friendship(10, alice, bob, FriendshipStatus.PENDING);
        when(friendshipRepository.findById(10)).thenReturn(Optional.of(friendship));

        FriendshipResponse response = friendshipService.accept(10, bob);

        assertThat(response.status()).isEqualTo(FriendshipStatus.ACCEPTED);
        assertThat(friendship.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
    }

    @Test
    void requesterAndOutsiderCannotAcceptRequest() {
        User alice = user(1, "alice", "alice@example.com");
        User bob = user(2, "bob", "bob@example.com");
        User mallory = user(3, "mallory", "mallory@example.com");
        Friendship friendship = friendship(10, alice, bob, FriendshipStatus.PENDING);
        when(friendshipRepository.findById(10)).thenReturn(Optional.of(friendship));

        assertThatThrownBy(() -> friendshipService.accept(10, alice))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> friendshipService.accept(10, mallory))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void acceptedFriendshipCannotBeAcceptedAgain() {
        User alice = user(1, "alice", "alice@example.com");
        User bob = user(2, "bob", "bob@example.com");
        Friendship friendship = friendship(10, alice, bob, FriendshipStatus.ACCEPTED);
        when(friendshipRepository.findById(10)).thenReturn(Optional.of(friendship));

        assertThatThrownBy(() -> friendshipService.accept(10, bob))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void participantsCanRemoveButOutsidersCannot() {
        User alice = user(1, "alice", "alice@example.com");
        User bob = user(2, "bob", "bob@example.com");
        User mallory = user(3, "mallory", "mallory@example.com");
        Friendship friendship = friendship(10, alice, bob, FriendshipStatus.ACCEPTED);
        when(friendshipRepository.findById(10)).thenReturn(Optional.of(friendship));

        friendshipService.remove(10, alice);
        verify(friendshipRepository).delete(friendship);

        assertThatThrownBy(() -> friendshipService.remove(10, mallory))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void listReturnsAllFriendshipsForCurrentUser() {
        User alice = user(1, "alice", "alice@example.com");
        User bob = user(2, "bob", "bob@example.com");
        when(friendshipRepository.findAllForUser(1)).thenReturn(List.of(
                friendship(10, alice, bob, FriendshipStatus.PENDING)
        ));

        assertThat(friendshipService.listFor(alice)).singleElement()
                .satisfies(response -> assertThat(response.id()).isEqualTo(10));
    }

    @Test
    void missingFriendshipUsesDomainError() {
        User alice = user(1, "alice", "alice@example.com");
        when(friendshipRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendshipService.remove(99, alice))
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

    private static Friendship friendship(
            int id,
            User requester,
            User recipient,
            FriendshipStatus status
    ) {
        Friendship friendship = new Friendship(requester, recipient, status);
        friendship.setId(id);
        return friendship;
    }
}
