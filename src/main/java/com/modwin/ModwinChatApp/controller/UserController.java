package com.modwin.ModwinChatApp.controller;

import com.modwin.ModwinChatApp.dto.UserDto;
import com.modwin.ModwinChatApp.exception.UserAlreadyExistsException;
import com.modwin.ModwinChatApp.exception.UserNotFoundException;
import com.modwin.ModwinChatApp.service.UserService;
import com.modwin.ModwinChatApp.persistence.model.User;
import com.modwin.ModwinChatApp.util.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@Data
@RestController
@Validated
@RequestMapping(path = "/api/users")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerNewUser(@Valid @RequestBody UserDto userDto, HttpServletRequest request) {
        try {
            User user = userService.registerNewUser(userDto);

            boolean authenticated = userService.authenticateUser(user.getEmail(), userDto.getPassword());
            if (authenticated) {
                HttpSession session = request.getSession(true);
                session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
                return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully.");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed.");
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private List<GrantedAuthority> getUserAuthorities(User user) {
        return user.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName())).collect(Collectors.toList());
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData, HttpServletRequest request) {
        String email = loginData.get("email");
        String password = loginData.get("password");
        UserDto userDto = userService.getUserDTOByEmail(email);
        if (userDto != null && userService.authenticateUser(email, password)) {
            HttpSession session = request.getSession(true);
            session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
            return ResponseEntity.status(HttpStatus.OK).body(userDto);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping(path = "/login/auth")
    public ResponseEntity<UserDto> loginUser(HttpServletRequest request, @RequestBody UserDto userDto) {

        User u = UserMapper.toEntity(userDto);

        Authentication auth = new UsernamePasswordAuthenticationToken(userDto.getUsername(), userDto.getPassword(), getUserAuthorities(u));

        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);

        HttpSession session = request.getSession(true);
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, sc);

        return ResponseEntity.status(HttpStatus.OK).body(UserMapper.toDTO(u));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/home.html")).build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping(path = "/profile")
    public ResponseEntity<UserDto> getUserProfile(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken authToken) {

            String email = authToken.getPrincipal().getAttribute("email");
            UserDto user = userService.getUserDTOByEmail(email);

            return ResponseEntity.status(HttpStatus.OK).body(user);
        } else if (principal instanceof UsernamePasswordAuthenticationToken localUser) {
            System.out.println("Local user authenticated!");
            UserDto userDto = userService.getUserDTOByUsername(localUser.getName());
            System.out.println("UserDto = " + userDto.getName());
            return ResponseEntity.status(HttpStatus.OK).body(userDto);
        } else if (principal instanceof Authentication auth) {

            UserDto dto = userService.getUserDTOByUsername(auth.getName());
            return ResponseEntity.status(HttpStatus.OK).body(dto);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/addFriend")
    public ResponseEntity<String> addFriend(@NotNull @RequestBody String email, Principal principal) {
        if(principal instanceof OAuth2AuthenticationToken oauthUser) {
            UserDto u = userService.getUserDTOByEmail(oauthUser.getPrincipal().getAttribute("email"));
            System.out.println("FOUND USER, ADDING EMAIL = " + email);
            userService.addFriend(u, email);
            return ResponseEntity.ok().body(email);
        }
        else if (principal instanceof UsernamePasswordAuthenticationToken localUser) {
            System.out.println("localUser = " + localUser);
            UserDto u = userService.getUserDTOByUsername(principal.getName());
            userService.addFriend(u, email);
            return ResponseEntity.ok().body(email);
        }
        return ResponseEntity.badRequest().body(email);
    }

    @PostMapping("/removeFriend")
    public ResponseEntity<?> removeFriend(@NotNull @RequestBody String email, Principal principal) {
        if(principal instanceof OAuth2AuthenticationToken oauthUser) {
            UserDto u = userService.getUserDTOByEmail(oauthUser.getPrincipal().getAttribute("email"));
            System.out.println("FOUND USER, ADDING EMAIL = " + email);
            userService.removeFriend(u, email);
            return ResponseEntity.ok().body(email);
        }
        else if (principal instanceof UsernamePasswordAuthenticationToken localUser) {
            System.out.println("localUser = " + localUser);
            System.out.println("ATTEMPTING TO REMOVE FRIEND = " + email);
            UserDto u = userService.getUserDTOByUsername(principal.getName());
            userService.removeFriend(u, email);
            return ResponseEntity.ok().body(email);
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/getUser/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable @Valid String username) {
        UserDto u = userService.getUserDTOByUsername(username);
        return ResponseEntity.ok(u);
    }

    @GetMapping(path = "/getUser/id/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable @Valid Integer id) {
        UserDto u = userService.getUserDTOById(id);
        return ResponseEntity.ok(u);
    }

    @GetMapping(path = "/allUsers")
    public ResponseEntity<Iterable<UserDto>> getUsers() {
         List<UserDto> users = userService.getUsers().stream().map(UserMapper::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }


}

