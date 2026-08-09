package com.modwin.ModwinChatApp.service;

import com.modwin.ModwinChatApp.dto.UserDto;
import com.modwin.ModwinChatApp.exception.UserAlreadyExistsException;
import com.modwin.ModwinChatApp.exception.UserNotFoundException;
import com.modwin.ModwinChatApp.persistence.model.Role;
import com.modwin.ModwinChatApp.persistence.model.User;
import com.modwin.ModwinChatApp.persistence.repository.RoleRepository;
import com.modwin.ModwinChatApp.persistence.repository.UserRepository;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    private Validator validator;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                roleRepository,
                validator,
                userRepository,
                passwordEncoder,
                authenticationManager
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerHashesPasswordAssignsDefaultRoleAndPersistsUser() {
        UserDto request = validUserDto();
        Role userRole = new Role("USER");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(validator.validate(request)).thenReturn(Set.of());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = userService.registerNewUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User persisted = userCaptor.getValue();
        assertThat(persisted.getEmail()).isEqualTo("alice@example.com");
        assertThat(persisted.getUsername()).isEqualTo("alice");
        assertThat(persisted.getName()).isEqualTo("Alice Example");
        assertThat(persisted.getPassword()).isEqualTo("encoded-password");
        assertThat(persisted.getRoles()).containsExactly(userRole);
        assertThat(registered).isSameAs(persisted);
    }

    @Test
    void registerRejectsDuplicateEmailBeforeEncodingOrSaving() {
        UserDto request = validUserDto();
        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user(1, "existing", request.getEmail())));

        assertThatThrownBy(() -> userService.registerNewUser(request))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRejectsDuplicateUsername() {
        UserDto request = validUserDto();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(validator.validate(request)).thenReturn(Set.of());
        when(userRepository.findByUsername(request.getUsername()))
                .thenReturn(Optional.of(user(2, request.getUsername(), "someone@example.com")));

        assertThatThrownBy(() -> userService.registerNewUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("username");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticationRejectsWrongPasswordWithoutCallingAuthenticationManager() {
        User user = user(1, "alice", "alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", user.getPassword())).thenReturn(false);

        boolean authenticated = userService.authenticateUser("alice@example.com", "wrong-password");

        assertThat(authenticated).isFalse();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void authenticationStoresSuccessfulAuthenticationInSecurityContext() {
        User user = user(1, "alice", "alice@example.com");
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", user.getPassword())).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        boolean authenticated = userService.authenticateUser("alice@example.com", "correct-password");

        assertThat(authenticated).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(authentication);
    }

    @Test
    void getUsersUsesDomainErrorForEmptyResult() {
        when(userRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> userService.getUsers())
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("No registered users");
    }

    @Test
    void mappedUserResponsePreservesDisplayNameAndNeverContainsPassword() {
        User user = user(1, "alice", "alice@example.com");
        user.setName("Alice Example");
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        UserDto response = userService.getUserDTOById(1);

        assertThat(response.getName()).isEqualTo("Alice Example");
        assertThat(response.getPassword()).isNull();
    }

    private static UserDto validUserDto() {
        return UserDto.builder()
                .email("alice@example.com")
                .username("alice")
                .name("Alice Example")
                .password("securePassword123")
                .build();
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
