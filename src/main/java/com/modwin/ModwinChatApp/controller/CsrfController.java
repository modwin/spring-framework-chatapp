package com.modwin.ModwinChatApp.controller;

import com.modwin.ModwinChatApp.dto.CsrfTokenResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/csrf")
public class CsrfController {

    @GetMapping
    public CsrfTokenResponse token(CsrfToken token) {
        return new CsrfTokenResponse(token.getToken(), token.getHeaderName(), token.getParameterName());
    }
}
