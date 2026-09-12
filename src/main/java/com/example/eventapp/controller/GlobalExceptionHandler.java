package com.example.eventapp.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException exception, Model model, HttpServletResponse response) {
        return renderError(model, response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatus(ResponseStatusException exception, Model model, HttpServletResponse response) {
        int statusCode = exception.getStatusCode().value();
        response.setStatus(statusCode);
        FriendlyErrorAttributes.addToModel(model, statusCode);
        return FriendlyErrorAttributes.ERROR_VIEW;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleBadRequest(IllegalArgumentException exception, Model model, HttpServletResponse response) {
        return renderError(model, response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntime(RuntimeException exception, Model model, HttpServletResponse response) {
        if (looksLikeNotFound(exception)) {
            return renderError(model, response, HttpStatus.NOT_FOUND);
        }

        return renderError(model, response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception exception, Model model, HttpServletResponse response) {
        return renderError(model, response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String renderError(Model model, HttpServletResponse response, HttpStatus status) {
        response.setStatus(status.value());
        FriendlyErrorAttributes.addToModel(model, status.value());
        return FriendlyErrorAttributes.ERROR_VIEW;
    }

    private boolean looksLikeNotFound(RuntimeException exception) {
        String message = exception.getMessage();

        if (message == null) {
            return false;
        }

        String normalizedMessage = message.toLowerCase();

        return normalizedMessage.contains("not found")
                || normalizedMessage.contains("nu există")
                || normalizedMessage.contains("nu exista")
                || normalizedMessage.contains("nu a fost găsit")
                || normalizedMessage.contains("nu a fost gasit");
    }
}
