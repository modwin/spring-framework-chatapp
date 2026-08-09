package com.modwin.ModwinChatApp.persistence.repository;

import com.modwin.ModwinChatApp.persistence.model.Friendship;
import com.modwin.ModwinChatApp.persistence.model.User;
import com.modwin.ModwinChatApp.util.FriendshipStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FriendshipRepositoryTest {

    @Autowired
    private FriendshipRepository friendshipRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void existsBetweenDetectsFriendshipRegardlessOfDirection() {
        User alice = userRepository.save(user("alice", "alice@example.com"));
        User bob = userRepository.save(user("bob", "bob@example.com"));
        User charlie = userRepository.save(user("charlie", "charlie@example.com"));
        friendshipRepository.save(new Friendship(alice, bob, FriendshipStatus.PENDING));

        assertThat(friendshipRepository.existsBetween(alice, bob)).isTrue();
        assertThat(friendshipRepository.existsBetween(bob, alice)).isTrue();
        assertThat(friendshipRepository.existsBetween(alice, charlie)).isFalse();
    }

    private static User user(String username, String email) {
        return User.builder()
                .username(username)
                .email(email)
                .name(username)
                .password("encoded-password")
                .build();
    }
}
