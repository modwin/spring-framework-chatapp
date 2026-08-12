package com.modwin.ModwinChatApp.service;

import com.modwin.ModwinChatApp.dto.RegisterUserRequest;
import com.modwin.ModwinChatApp.dto.UserResponse;
import com.modwin.ModwinChatApp.exception.UserAlreadyExistsException;
import com.modwin.ModwinChatApp.exception.UserNotFoundException;
import com.modwin.ModwinChatApp.persistence.model.Role;
import com.modwin.ModwinChatApp.persistence.model.User;
import com.modwin.ModwinChatApp.persistence.repository.RoleRepository;
import com.modwin.ModwinChatApp.persistence.repository.UserRepository;
import com.modwin.ModwinChatApp.util.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserService {

    private static final String DEFAULT_ROLE = "USER";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterUserRequest request) {
        String email = normalizeEmail(request.email());
        String username = request.username().trim();

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("A user is already registered with that email address.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("A user is already registered with that username.");
        }

        User user = new User(
                email,
                username,
                request.name().trim(),
                passwordEncoder.encode(request.password())
        );
        user.getRoles().add(getOrCreateDefaultRole());
        return userRepository.save(user);
    }

    @Transactional
    public User provisionOidcUser(String emailClaim, String nameClaim) {
        String email = normalizeEmail(emailClaim);
        return userRepository.findByEmail(email).orElseGet(() -> {
            String username = uniqueUsernameFor(email);
            String displayName = nameClaim == null || nameClaim.isBlank() ? username : nameClaim.trim();
            User user = new User(email, username, displayName, null);
            user.getRoles().add(getOrCreateDefaultRole());
            return userRepository.save(user);
        });
    }

    @Transactional(readOnly = true)
    public User requireByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user was not found."));
    }

    @Transactional(readOnly = true)
    public User requireByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new UserNotFoundException("Authenticated user was not found."));
    }

    @Transactional(readOnly = true)
    public UserResponse responseByUsername(String username) {
        return UserMapper.toResponse(requireByUsername(username));
    }

    private Role getOrCreateDefaultRole() {
        return roleRepository.findByName(DEFAULT_ROLE)
                .orElseGet(() -> roleRepository.save(new Role(DEFAULT_ROLE)));
    }

    private String uniqueUsernameFor(String email) {
        String localPart = email.substring(0, email.indexOf('@'))
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^[._-]+|[._-]+$", "");
        if (localPart.length() < 3) {
            localPart = "user-" + localPart;
        }
        String base = localPart.substring(0, Math.min(localPart.length(), 16));
        String candidate = base;
        int suffix = 2;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
