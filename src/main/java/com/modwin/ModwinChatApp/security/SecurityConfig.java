package com.modwin.ModwinChatApp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modwin.ModwinChatApp.service.CustomOidcUserService;
import com.modwin.ModwinChatApp.service.LocalUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    private final ObjectProvider<CustomOidcUserService> customOidcUserService;
    private final LocalUserDetailsService userDetailsService;
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrations;
    private final boolean googleEnabled;
    private final String frontendUrl;

    public SecurityConfig(ObjectProvider<CustomOidcUserService> customOidcUserService,
                          LocalUserDetailsService userDetailsService,
                          ObjectProvider<ClientRegistrationRepository> clientRegistrations,
                          @Value("${app.auth.google-enabled:false}") boolean googleEnabled,
                          @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl) {
        this.customOidcUserService = customOidcUserService;
        this.userDetailsService = userDetailsService;
        this.clientRegistrations = clientRegistrations;
        this.googleEnabled = googleEnabled;
        this.frontendUrl = frontendUrl;
    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            ObjectMapper objectMapper,
            SecurityContextRepository securityContextRepository
    ) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET,
                                "/api/csrf",
                                "/api/auth/providers",
                                "/actuator/health",
                                "/actuator/health/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**", "/error").permitAll()
                        .requestMatchers("/api/users/**", "/api/friendships/**", "/api/auth/logout")
                        .authenticated()
                        .anyRequest().denyAll()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                writeProblem(response, objectMapper, HttpStatus.UNAUTHORIZED,
                                        "Authentication is required."))
                        .accessDeniedHandler((request, response, exception) ->
                                writeProblem(response, objectMapper, HttpStatus.FORBIDDEN,
                                        "You are not allowed to perform this operation."))
                )
                .requestCache(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpStatus.NO_CONTENT.value()))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )
                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository)
                        .requireExplicitSave(true)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                );

        if (googleEnabled && clientRegistrations.getIfAvailable() != null) {
            http.oauth2Login(oauth -> oauth
                    .defaultSuccessUrl(frontendUrl, true)
                    .userInfoEndpoint(userInfo -> userInfo.oidcUserService(customOidcUserService.getObject()))
            );
        }

        return http.build();
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    private void writeProblem(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            HttpStatus status,
            String detail
    ) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
