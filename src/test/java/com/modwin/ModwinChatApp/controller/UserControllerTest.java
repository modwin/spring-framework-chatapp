package com.modwin.ModwinChatApp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modwin.ModwinChatApp.dto.UserDto;
import com.modwin.ModwinChatApp.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
    }

    @Test
    public void testUserRegistrationAndLoginFlow() throws Exception {
        // 1. Prepare registration request
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("email", "testuser@example.com");
        registerRequest.put("username", "testuser");
        registerRequest.put("name", "Test User");
        registerRequest.put("password", "securePassword123");

        // 2. Perform Registration
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully."));

        // 3. Perform Local Login
        Map<String, String> loginRequest = new HashMap<>();
        loginDataPut(loginRequest);

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.name").value("Test User"));

        // 4. Test invalid login
        Map<String, String> invalidLogin = new HashMap<>();
        invalidLogin.put("email", "testuser@example.com");
        invalidLogin.put("password", "wrongpassword");

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isUnauthorized());
    }

    private void loginDataPut(Map<String, String> loginRequest) {
        loginRequest.put("email", "testuser@example.com");
        loginRequest.put("password", "securePassword123");
    }
}
