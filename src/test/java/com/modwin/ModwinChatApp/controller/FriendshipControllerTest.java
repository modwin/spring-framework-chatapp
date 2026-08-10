package com.modwin.ModwinChatApp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modwin.ModwinChatApp.dto.FriendDto;
import com.modwin.ModwinChatApp.dto.FriendshipResponse;
import com.modwin.ModwinChatApp.exception.FriendshipAlreadyExistsException;
import com.modwin.ModwinChatApp.persistence.model.User;
import com.modwin.ModwinChatApp.service.AuthenticatedUserProvider;
import com.modwin.ModwinChatApp.service.FriendshipService;
import com.modwin.ModwinChatApp.util.FriendshipStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FriendshipController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class FriendshipControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FriendshipService friendshipService;
    @MockitoBean
    private AuthenticatedUserProvider authenticatedUserProvider;

    private User alice;

    @BeforeEach
    void setUp() {
        alice = User.builder().id(1).email("alice@example.com").username("alice").name("Alice").build();
        when(authenticatedUserProvider.require(org.mockito.ArgumentMatchers.any())).thenReturn(alice);
    }

    @Test
    void listsCurrentUsersFriendships() throws Exception {
        when(friendshipService.listFor(alice)).thenReturn(List.of(response(FriendshipStatus.PENDING)));

        mockMvc.perform(get("/api/friendships").principal(principal()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].recipient.name").value("Bob"));
    }

    @Test
    void createsAndAcceptsFriendRequest() throws Exception {
        when(friendshipService.sendRequest(alice, "bob@example.com"))
                .thenReturn(response(FriendshipStatus.PENDING));
        when(friendshipService.accept(10, alice))
                .thenReturn(response(FriendshipStatus.ACCEPTED));

        mockMvc.perform(post("/api/friendships")
                        .principal(principal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipientEmail\":\"bob@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(patch("/api/friendships/10/accept").principal(principal()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/friendships/10").principal(principal()))
                .andExpect(status().isNoContent());

        verify(friendshipService).remove(10, alice);
    }

    @Test
    void validationAndConflictUseProblemDetails() throws Exception {
        mockMvc.perform(post("/api/friendships")
                        .principal(principal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipientEmail\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.recipientEmail").exists());
        verifyNoInteractions(friendshipService);

        when(friendshipService.sendRequest(alice, "bob@example.com"))
                .thenThrow(new FriendshipAlreadyExistsException());
        mockMvc.perform(post("/api/friendships")
                        .principal(principal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("recipientEmail", "bob@example.com")
                        )))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
    }

    private UsernamePasswordAuthenticationToken principal() {
        return UsernamePasswordAuthenticationToken.authenticated("alice", "n/a", List.of());
    }

    private FriendshipResponse response(FriendshipStatus status) {
        return new FriendshipResponse(
                10,
                new FriendDto(1, "alice", "Alice"),
                new FriendDto(2, "bob", "Bob"),
                status
        );
    }
}
