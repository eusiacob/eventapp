package com.example.eventapp.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FriendlyErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        FriendlyErrorAttributes.addToModel(model, getStatusCode(request));
        return FriendlyErrorAttributes.ERROR_VIEW;
    }

    private int getStatusCode(HttpServletRequest request) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (statusCode instanceof Integer code) {
            return code;
        }

        return 500;
    }
}
