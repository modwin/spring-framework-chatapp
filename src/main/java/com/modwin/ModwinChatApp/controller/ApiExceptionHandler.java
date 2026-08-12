package com.modwin.ModwinChatApp.controller;

import com.modwin.ModwinChatApp.exception.AccessDeniedException;
import com.modwin.ModwinChatApp.exception.FriendshipAlreadyExistsException;
import com.modwin.ModwinChatApp.exception.FriendshipNotFoundException;
import com.modwin.ModwinChatApp.exception.UserAlreadyExistsException;
import com.modwin.ModwinChatApp.exception.UserNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({
            FriendshipAlreadyExistsException.class,
            UserAlreadyExistsException.class,
            DataIntegrityViolationException.class,
            IllegalStateException.class
    })
    ProblemDetail handleConflict(RuntimeException exception) {
        String detail = exception instanceof DataIntegrityViolationException
                ? "The request conflicts with existing data."
                : exception.getMessage();
        return problem(HttpStatus.CONFLICT, detail);
    }

    @ExceptionHandler({FriendshipNotFoundException.class, UserNotFoundException.class})
    ProblemDetail handleNotFound(RuntimeException exception) {
        return problem(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleAccessDenied(AccessDeniedException exception) {
        return problem(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleInvalidBusinessRequest(IllegalArgumentException exception) {
        return problem(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    ProblemDetail handleBadCredentials() {
        return problem(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, "One or more fields are invalid.");
        detail.setProperty("errors", errors);
        return detail;
    }

    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail handleResponseStatus(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        return problem(status, exception.getReason());
    }

    private ProblemDetail problem(HttpStatus status, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        return problem;
    }
}
