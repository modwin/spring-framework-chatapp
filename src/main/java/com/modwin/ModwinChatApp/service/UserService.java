package com.modwin.ModwinChatApp.service;

import com.modwin.ModwinChatApp.dto.UserDto;
import com.modwin.ModwinChatApp.exception.InvalidUserInputException;
import com.modwin.ModwinChatApp.exception.UserAlreadyExistsException;
import com.modwin.ModwinChatApp.exception.UserNotFoundException;
import com.modwin.ModwinChatApp.util.UserMapper;
import com.modwin.ModwinChatApp.persistence.repository.RoleRepository;
import com.modwin.ModwinChatApp.persistence.repository.UserRepository;
import com.modwin.ModwinChatApp.persistence.model.Role;
import com.modwin.ModwinChatApp.persistence.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@Service
public class UserService extends DefaultOAuth2UserService {

    private final RoleRepository roleRepository;
    private final Validator validator;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public UserService(RoleRepository roleRepository, Validator validator, UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager){
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.validator = validator;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }
    @Transactional
    public User registerNewUser(@Valid UserDto userDto) {
        Optional<User> optionalUser = userRepository.findByEmail(userDto.getEmail());
        if(optionalUser.isEmpty()){
            validateUserDTO(userDto);
            checkUsernameAvailability(userDto);
            User u = new User(userDto.getEmail(), userDto.getUsername(), userDto.getName(), passwordEncoder.encode(userDto.getPassword()), new ArrayList<>());
            return userRepository.save(addDefaultRoleToUser(u));
        }
        throw new UserAlreadyExistsException("User already exists");
//        return null;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");

        if (email != null && userRepository.findByEmail(email).isEmpty()) {
            User newUser = new User(email, oAuth2User.getName(), oAuth2User.getName(), passwordEncoder.encode(UUID.randomUUID().toString()), new ArrayList<>());
            newUser.getRoles().add(getOrCreateDefaultRole());
            userRepository.save(newUser);
        }
        return oAuth2User;
    }

    @Transactional
    public void addFriend(UserDto user, String friendEmail){
        Optional<User> optionalUser = userRepository.findByEmail(user.getEmail());
        Optional<User> friend = userRepository.findByEmail(friendEmail);
        if(friend.isPresent() && optionalUser.isPresent()){
            User u = optionalUser.get();
            User f = friend.get();
            u.getFriends().add(f);
            f.getFriends().add(u);
        }
        else throw new UserNotFoundException("No user registered associated with that email address.");
    }

    @Transactional
    public void removeFriend(UserDto userDto, String friendEmail){
        Optional<User> optionalUser = userRepository.findByEmail(userDto.getEmail());
        Optional<User> friend = userRepository.findByEmail(friendEmail);
        System.out.println("UserService#removeFriend");
        System.out.println("friend = " + friend);
        System.out.println("friendEmail = " + friendEmail);
        System.out.println("optionalUser = " + optionalUser);
        if(friend.isPresent() && optionalUser.isPresent()){
            User u = optionalUser.get();
            User f = friend.get();
            System.out.println("Removing friend " + f);
            System.out.println(" from user " + u);
            u.getFriends().remove(f);
            f.getFriends().remove(u);
        }
        else throw new UserNotFoundException("No user registered associated with that email address.");
    }

    public List<User> getUsers() {
        List<User> users = userRepository.findAll();
        if(users.isEmpty()){
            throw new UserNotFoundException("No registered users yet.");
        }
        return users;
    }

    public UserDto getUserDTOByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserMapper::toDTO)
                .orElseThrow(() -> new UserNotFoundException("No registered user with that username."));
    }

    public UserDto getUserDTOById(Integer id) {
        return userRepository.findById(id)
                .map(UserMapper::toDTO)
                .orElseThrow(() -> new UserNotFoundException("No registered user with that ID."));
    }

    public UserDto getUserDTOByEmail(String email){
        System.out.println("getUserDTOByEmail: SEARCHING FOR EMAIL IN DB = " + email);
        Optional<User> u = userRepository.findByEmail(email);
        if(u.isPresent()){
            System.out.println("FOUND USER = "+u);
            return UserMapper.toDTO(u.get());
        }
        throw new UserNotFoundException("No registered user with that email.");
    }


    public boolean authenticateUser(String email, String password){
        Optional<User> optionalUser = userRepository.findByEmail(email);;
        if(optionalUser.filter(value ->  passwordEncoder.matches(password, value.getPassword())).isPresent()){

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            optionalUser.get().getUsername(),
                            password,
                            getUserAuthorities(optionalUser.get())));

            SecurityContext sc  = SecurityContextHolder.getContext();
            sc.setAuthentication(auth);

            return auth.isAuthenticated();
        }
        return false;
    }

    private List<GrantedAuthority> getUserAuthorities(User user) {;
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }

    private void validateUserDTO(UserDto userDTO){
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDTO);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new InvalidUserInputException(errorMessage, violations);
        }
    }

    private void checkUsernameAvailability(UserDto userDTO) {
        if(userRepository.findByUsername(userDTO.getUsername()).isPresent()){
            throw new UserAlreadyExistsException("A user is already registered with that username.");
        }
    }

    private User addDefaultRoleToUser(User u) {
        u.getRoles().add(getOrCreateDefaultRole());
        return u;
    }

    private Role getOrCreateDefaultRole() {
        return roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(new Role("USER")));
    }

    public void loginUser(HttpServletRequest request, String username, String password){

        UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(username, password, getUserAuthorities(UserMapper.toEntity(getUserDTOByUsername(username))));
        Authentication auth = authenticationManager.authenticate(authReq);

        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(auth);

        HttpSession session = request.getSession(true);
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, securityContext);

    }

}
