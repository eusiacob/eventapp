package com.example.eventapp.controller;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.BusinessUnavailableDate;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.BusinessUnavailableDateRepository;
import com.example.eventapp.service.BusinessProfileService;
import com.example.eventapp.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class BusinessAvailabilityController {

    private final BusinessProfileService businessProfileService;
    private final BusinessUnavailableDateRepository unavailableDateRepository;
    private final UserService userService;

    public BusinessAvailabilityController(BusinessProfileService businessProfileService,
                                          BusinessUnavailableDateRepository unavailableDateRepository,
                                          UserService userService) {
        this.businessProfileService = businessProfileService;
        this.unavailableDateRepository = unavailableDateRepository;
        this.userService = userService;
    }

    @PostMapping("/business/{businessId}/availability/toggle")
    @ResponseBody
    public Map<String, Object> toggleAvailability(@PathVariable Long businessId,
                                                  @RequestParam("date")
                                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                  LocalDate date,
                                                  @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByEmail(userDetails.getUsername());

        BusinessProfile businessProfile =
                businessProfileService.findByIdAndValidateOwner(businessId, user);

        Optional<BusinessUnavailableDate> existingDate =
                unavailableDateRepository.findByBusinessProfileAndUnavailableDate(
                        businessProfile,
                        date
                );

        boolean unavailable;

        if (existingDate.isPresent()) {
            unavailableDateRepository.delete(existingDate.get());
            unavailable = false;
        } else {
            BusinessUnavailableDate unavailableDate = new BusinessUnavailableDate();
            unavailableDate.setBusinessProfile(businessProfile);
            unavailableDate.setUnavailableDate(date);

            unavailableDateRepository.save(unavailableDate);
            unavailable = true;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("date", date.toString());
        response.put("unavailable", unavailable);
        response.put("message", unavailable ? "Date marked as unavailable" : "Date marked as available");

        return response;
    }
}