package com.example.eventapp.controller;

import com.example.eventapp.service.BusinessAvailabilityService;
import com.example.eventapp.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@Controller
public class BusinessAvailabilityController {
    private final BusinessAvailabilityService availability;
    private final UserService users;

    public BusinessAvailabilityController(BusinessAvailabilityService availability, UserService users) {
        this.availability = availability;
        this.users = users;
    }

    @PostMapping("/business/{uuid}/availability")
    @ResponseBody
    public List<String> update(@PathVariable String uuid,
                              @RequestParam LocalDate start,
                              @RequestParam LocalDate end,
                              @RequestParam boolean unavailable,
                              @AuthenticationPrincipal UserDetails principal) {
        return availability.update(uuid, users.findByEmail(principal.getUsername()), start, end, unavailable);
    }
}
