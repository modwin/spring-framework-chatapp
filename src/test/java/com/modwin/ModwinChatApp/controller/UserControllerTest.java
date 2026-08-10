package com.modwin.ModwinChatApp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modwin.ModwinChatApp.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerProfileLogoutAndLoginFlowUsesSessionAuthentication() throws Exception {
        CsrfSession registrationCsrf = csrfSession();
        Map<String, String> registration = Map.of(
                "email", "Alice@Example.com",
                "username", "alice",
                "name", "Alice Example",
                "password", "securePassword123"
        );

        mockMvc.perform(post("/api/auth/register")
                        .session(registrationCsrf.session())
                        .header(registrationCsrf.headerName(), registrationCsrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registration)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(get("/api/users/me").session(registrationCsrf.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Example"));

        CsrfSession loginCsrf = csrfSession();
        mockMvc.perform(post("/api/auth/login")
                        .session(loginCsrf.session())
                        .header(loginCsrf.headerName(), loginCsrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "alice@example.com",
                                "password", "securePassword123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));

        mockMvc.perform(post("/api/auth/logout")
                        .session(loginCsrf.session())
                        .header(loginCsrf.headerName(), loginCsrf.token()))
                .andExpect(status().isNoContent());
    }

    @Test
    void invalidCredentialsAndAnonymousProfileReturnUnauthorizedProblem() throws Exception {
        CsrfSession csrf = csrfSession();

        mockMvc.perform(post("/api/auth/login")
                        .session(csrf.session())
                        .header(csrf.headerName(), csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"missing@example.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.detail").value("Invalid email or password."));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Authentication is required."));
    }

    @Test
    void mutatingRequestsRequireCsrfAndValidationErrorsAreStructured() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Forbidden"));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    @WithMockUser(username = "alice")
    void removedAuthenticationBypassIsNotMapped() throws Exception {
        mockMvc.perform(post("/api/users/login/auth").with(csrf()))
                .andExpect(status().isNotFound());
    }

    private CsrfSession csrfSession() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return new CsrfSession(
                (MockHttpSession) result.getRequest().getSession(false),
                body.get("headerName").asText(),
                body.get("token").asText()
        );
    }

    private record CsrfSession(MockHttpSession session, String headerName, String token) {
    }
}
