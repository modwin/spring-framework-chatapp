package com.modwin.ModwinChatApp.service;

import com.modwin.ModwinChatApp.dto.RegisterUserRequest;
import com.modwin.ModwinChatApp.dto.UserResponse;
import com.modwin.ModwinChatApp.exception.UserAlreadyExistsException;
import com.modwin.ModwinChatApp.persistence.model.Role;
import com.modwin.ModwinChatApp.persistence.model.User;
import com.modwin.ModwinChatApp.persistence.repository.RoleRepository;
import com.modwin.ModwinChatApp.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(roleRepository, userRepository, passwordEncoder);
    }

    @Test
    void registrationNormalizesEmailHashesPasswordAndAssignsRole() {
        RegisterUserRequest request = new RegisterUserRequest(
                " Alice@Example.COM ", "alice", " Alice Example ", "securePassword123"
        );
        Role role = new Role("USER");
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = userService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("alice@example.com");
        assertThat(captor.getValue().getName()).isEqualTo("Alice Example");
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded-password");
        assertThat(captor.getValue().getRoles()).containsExactly(role);
        assertThat(registered).isSameAs(captor.getValue());
    }

    @Test
    void duplicateEmailIsRejectedBeforePasswordHashing() {
        RegisterUserRequest request = validRequest();
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("email");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void duplicateUsernameIsRejected() {
        RegisterUserRequest request = validRequest();
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("username");
    }

    @Test
    void oidcProvisioningCreatesPasswordlessUserWithUniqueUsername() {
        Role role = new Role("USER");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(7);
            return user;
        });

        User user = userService.provisionOidcUser("Alice@Example.com", "Alice Example");

        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getPassword()).isNull();
        assertThat(user.getRoles()).containsExactly(role);
    }

    @Test
    void userResponseContainsRoleNamesAndNoPasswordField() {
        User user = user(1, "alice", "alice@example.com");
        user.getRoles().add(new Role("USER"));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        UserResponse response = userService.responseByUsername("alice");

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.roles()).containsExactly("USER");
    }

    private static RegisterUserRequest validRequest() {
        return new RegisterUserRequest(
                "alice@example.com", "alice", "Alice Example", "securePassword123"
        );
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
}
