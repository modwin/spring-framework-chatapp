package com.modwin.ModwinChatApp.security;

import com.modwin.ModwinChatApp.service.CustomOidcUserService;
import com.modwin.ModwinChatApp.service.LocalUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOidcUserService customOidcUserService;
    private final LocalUserDetailsService userDetailsService;
    public SecurityConfig(CustomOidcUserService customOidcUserService, LocalUserDetailsService userDetailsService) {
        this.customOidcUserService = customOidcUserService;
        this.userDetailsService = userDetailsService;
    }
    @Bean
    public AuthenticationManager authenticationManager(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(daoAuthenticationProvider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/users/**", "/", "/home.html", "/register.html", "/login/auth",
                                "/css/**", "/favicon.ico", "/oauth2/**", "/terms-and-conditions.html",
                                "/h2-console", "/error", "/api/users/register"
                        ).permitAll()
                        .requestMatchers("/profile.html", "/api/users/addFriend","/api/users/removeFriend").authenticated()
                        .anyRequest().permitAll()
                )
                .formLogin(login -> login
                        .loginPage("/login.html").permitAll()
                        .defaultSuccessUrl("/profile.html"))

                .oauth2Login(oauth -> oauth
                        .defaultSuccessUrl("/profile.html")
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(customOidcUserService))
                )
                .logout(logoutAction -> logoutAction
                        .logoutUrl("/api/users/logout")
                        .logoutSuccessUrl("/home.html").permitAll()
                        .invalidateHttpSession(true))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }


}


