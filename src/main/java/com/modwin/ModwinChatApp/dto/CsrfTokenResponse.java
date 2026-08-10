package com.modwin.ModwinChatApp.dto;

public record CsrfTokenResponse(String token, String headerName, String parameterName) {
}
