package com.modwin.ModwinChatApp.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "GOOGLE_CLIENT_ID=test-google-client",
        "GOOGLE_CLIENT_SECRET=test-google-secret"
})
@ActiveProfiles("oauth")
@AutoConfigureMockMvc
class OAuthProviderConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void configuredGoogleClientIsAdvertised() throws Exception {
        mockMvc.perform(get("/api/auth/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providers.length()").value(2))
                .andExpect(jsonPath("$.providers[0]").value("LOCAL"))
                .andExpect(jsonPath("$.providers[1]").value("GOOGLE"));
    }
}
