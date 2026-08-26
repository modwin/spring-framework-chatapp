package com.modwin.ModwinChatApp.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

@Component
public class OAuthProviderAvailability {

    private static final String GOOGLE_REGISTRATION_ID = "google";

    private final ObjectProvider<ClientRegistrationRepository> clientRegistrations;

    public OAuthProviderAvailability(ObjectProvider<ClientRegistrationRepository> clientRegistrations) {
        this.clientRegistrations = clientRegistrations;
    }

    public boolean isGoogleConfigured() {
        ClientRegistrationRepository registrations = clientRegistrations.getIfAvailable();
        return registrations != null
                && registrations.findByRegistrationId(GOOGLE_REGISTRATION_ID) != null;
    }
}
