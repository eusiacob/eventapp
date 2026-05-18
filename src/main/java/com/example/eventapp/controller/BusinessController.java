package com.example.eventapp.controller;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.UserRepository;
import com.example.eventapp.service.BusinessProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class BusinessController {

    private final BusinessProfileService service;
    private final UserRepository userRepository;

    public BusinessController(BusinessProfileService service, UserRepository userRepository) {

        this.service = service;
        this.userRepository = userRepository;
    }

    private boolean isOwner(BusinessProfile profile, UserDetails userDetails) {

        return profile.getUser().getEmail().equals(userDetails.getUsername());
    }

    @GetMapping("/business/create")
    public String showForm(Model model) {
        model.addAttribute("profile", new BusinessProfile());
        return "business-form";
    }

    @PostMapping("/business/create")
    public String createProfile(@Valid @ModelAttribute("profile") BusinessProfile profile, BindingResult result,
                                @RequestParam("imageFile") MultipartFile file,
                                @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        if (result.hasErrors()) {
            return "business-form";
        }

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        if (!file.isEmpty()) {

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            Path uploadPath = Paths.get("uploads/");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String uploadDir = "uploads/";

            Path filePath = Paths.get(uploadDir + fileName);

            Files.write(filePath, file.getBytes());

            profile.setImagePath("/images/" + fileName);
        }

        profile.setUser(user);

        service.save(profile);

        return "redirect:/dashboard";
    }

    @GetMapping("/business/{id}")
    public String businessDetails(@PathVariable Long id, Model model) {

        BusinessProfile profile = service.findById(id);

        model.addAttribute("profile", profile);

        return "business-details";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        List<BusinessProfile> profiles = user.getBusinessProfiles();

        model.addAttribute("profiles", profiles);

        return "dashboard";
    }

    @GetMapping("/business/edit/{id}")
    public String editForm(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {

        BusinessProfile profile = service.findById(id);

        if (!isOwner(profile, userDetails)) {
            return "redirect:/";
        }

        model.addAttribute("profile", profile);

        return "business-edit";
    }

    @PostMapping("/business/edit/{id}")
    public String updateProfile(@PathVariable Long id, @Valid @ModelAttribute("profile") BusinessProfile updatedProfile, BindingResult result, @AuthenticationPrincipal UserDetails userDetails) {

        if (result.hasErrors()) {
            return "business-edit";
        }

        BusinessProfile profile = service.findById(id);

        if (!isOwner(profile, userDetails)) {
            return "redirect:/";
        }

        profile.setName(updatedProfile.getName());
        profile.setCategory(updatedProfile.getCategory());
        profile.setCity(updatedProfile.getCity());
        profile.setPhone(updatedProfile.getPhone());
        profile.setDescription(updatedProfile.getDescription());

        service.save(profile);

        return "redirect:/dashboard";
    }

    @PostMapping("/business/delete/{id}")
    public String deleteProfile(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {

        BusinessProfile profile = service.findById(id);

        if (!isOwner(profile, userDetails)) {
            return "redirect:/";
        }

        service.delete(id);

        return "redirect:/dashboard";
    }
}