package com.modwin.ModwinChatApp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modwin.ModwinChatApp.dto.FriendDto;
import com.modwin.ModwinChatApp.dto.FriendshipRequest;
import com.modwin.ModwinChatApp.dto.FriendshipResponse;
import com.modwin.ModwinChatApp.exception.AccessDeniedException;
import com.modwin.ModwinChatApp.exception.FriendshipAlreadyExistsException;
import com.modwin.ModwinChatApp.exception.FriendshipNotFoundException;
import com.modwin.ModwinChatApp.service.FriendshipService;
import com.modwin.ModwinChatApp.util.FriendshipStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FriendshipController.class)
class FriendshipControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FriendshipService friendshipService;

    @Test
    @WithMockUser(username = "alice")
    void authenticatedUserCanSendFriendRequest() throws Exception {
        FriendshipResponse response = new FriendshipResponse(
                10,
                new FriendDto(1, "alice"),
                new FriendDto(2, "bob"),
                FriendshipStatus.PENDING
        );
        when(friendshipService.sendRequest("alice", "bob@example.com")).thenReturn(response);

        mockMvc.perform(post("/api/friendships")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FriendshipRequest("bob@example.com")
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.requester.id").value(1))
                .andExpect(jsonPath("$.requester.username").value("alice"))
                .andExpect(jsonPath("$.recipient.id").value(2))
                .andExpect(jsonPath("$.recipient.username").value("bob"))
                .andExpect(jsonPath("$.recipient.email").doesNotExist());

        verify(friendshipService).sendRequest("alice", "bob@example.com");
    }

    @Test
    @WithMockUser(username = "alice")
    void invalidRecipientEmailIsRejectedBeforeBusinessLayer() throws Exception {
        mockMvc.perform(post("/api/friendships")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipientEmail\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(friendshipService);
    }

    @Test
    @WithMockUser(username = "alice")
    void authenticatedParticipantCanDeleteFriendship() throws Exception {
        mockMvc.perform(delete("/api/friendships/{friendshipId}", 10)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(friendshipService).removeFriendship(10, "alice");
    }

    @Test
    @WithMockUser(username = "alice")
    void duplicateFriendRequestReturnsConflict() throws Exception {
        when(friendshipService.sendRequest("alice", "bob@example.com"))
                .thenThrow(new FriendshipAlreadyExistsException());

        mockMvc.perform(post("/api/friendships")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipientEmail\":\"bob@example.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value(
                        "A friendship or pending friend request already exists between these users."
                ));
    }

    @Test
    @WithMockUser(username = "alice")
    void missingFriendshipReturnsNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new FriendshipNotFoundException(99))
                .when(friendshipService).removeFriendship(99, "alice");

        mockMvc.perform(delete("/api/friendships/{friendshipId}", 99)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Friendship not found: 99"));
    }

    @Test
    @WithMockUser(username = "mallory")
    void nonParticipantReceivesForbiddenResponse() throws Exception {
        org.mockito.Mockito.doThrow(new AccessDeniedException("You are not part of this friendship"))
                .when(friendshipService).removeFriendship(10, "mallory");

        mockMvc.perform(delete("/api/friendships/{friendshipId}", 10)
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("You are not part of this friendship"));
    }

    @Test
    void anonymousUserCannotCreateFriendRequest() throws Exception {
        mockMvc.perform(post("/api/friendships")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipientEmail\":\"bob@example.com\"}"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/oauth2/authorization/google"));

        verifyNoInteractions(friendshipService);
    }
}
