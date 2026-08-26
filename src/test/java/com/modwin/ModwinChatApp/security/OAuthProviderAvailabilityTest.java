package com.modwin.ModwinChatApp.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthProviderAvailabilityTest {

    @Mock
    private ObjectProvider<ClientRegistrationRepository> clientRegistrations;
    @Mock
    private ClientRegistrationRepository registrationRepository;

    @Test
    void googleIsUnavailableWithoutAClientRegistrationRepository() {
        OAuthProviderAvailability availability = new OAuthProviderAvailability(clientRegistrations);

        assertThat(availability.isGoogleConfigured()).isFalse();
    }

    @Test
    void googleIsAvailableOnlyWhenItsRegistrationExists() {
        when(clientRegistrations.getIfAvailable()).thenReturn(registrationRepository);
        when(registrationRepository.findByRegistrationId("google"))
                .thenReturn(mock(ClientRegistration.class));
        OAuthProviderAvailability availability = new OAuthProviderAvailability(clientRegistrations);

        assertThat(availability.isGoogleConfigured()).isTrue();
    }
}
