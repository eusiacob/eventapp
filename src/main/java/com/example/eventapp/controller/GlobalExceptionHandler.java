package com.example.eventapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(AccessDeniedException exception, Model model, HttpServletRequest request, HttpServletResponse response) {
        return renderError(model, request, response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Object handleResponseStatus(ResponseStatusException exception, Model model, HttpServletRequest request, HttpServletResponse response) {
        int statusCode = exception.getStatusCode().value();

        if (wantsJson(request)) {
            return jsonError(statusCode);
        }

        if (response.isCommitted()) {
            return null;
        }

        FriendlyErrorAttributes.addToModel(model, statusCode);
        response.setStatus(statusCode);
        return FriendlyErrorAttributes.ERROR_VIEW;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleBadRequest(IllegalArgumentException exception, Model model, HttpServletRequest request, HttpServletResponse response) {
        return renderError(model, request, response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntime(RuntimeException exception, Model model, HttpServletRequest request, HttpServletResponse response) {
        if (looksLikeNotFound(exception)) {
            return renderError(model, request, response, HttpStatus.NOT_FOUND);
        }

        return renderError(model, request, response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception exception, Model model, HttpServletRequest request, HttpServletResponse response) {
        return renderError(model, request, response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Object renderError(Model model, HttpServletRequest request, HttpServletResponse response, HttpStatus status) {
        if (wantsJson(request)) {
            return jsonError(status.value());
        }

        if (response.isCommitted()) {
            return null;
        }

        response.setStatus(status.value());
        FriendlyErrorAttributes.addToModel(model, status.value());
        return FriendlyErrorAttributes.ERROR_VIEW;
    }

    private ResponseEntity<Map<String, Object>> jsonError(int statusCode) {
        HttpStatus status =
                HttpStatus.resolve(statusCode);

        String message =
                status != null && status.is4xxClientError()
                        ? "Cererea nu a putut fi procesată."
                        : "A apărut o eroare neașteptată.";

        return ResponseEntity
                .status(statusCode)
                .body(Map.of(
                        "success", false,
                        "message", message
                ));
    }

    private boolean wantsJson(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");
        String contentType = request.getContentType();
        String uri = request.getRequestURI();
        String method = request.getMethod();

        return (accept != null && accept.contains("application/json"))
                || "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (contentType != null && contentType.contains("application/json"))
                || isJsonEndpoint(method, uri);
    }

    private boolean isJsonEndpoint(String method, String uri) {
        if (method == null || uri == null) {
            return false;
        }

        return ("POST".equalsIgnoreCase(method) && (
                uri.matches("^/business/[^/]+/gallery/upload$")
                        || uri.matches("^/business/[^/]+/videos/upload$")
                        || uri.matches("^/business/videos/delete/[^/]+$")
                        || uri.matches("^/business/gallery/delete/.*$")
                        || uri.contains("/availability/")
                        || uri.contains("/favorites/")
        ))
                || ("GET".equalsIgnoreCase(method) &&
                uri.equals("/profile/data-export"));
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
