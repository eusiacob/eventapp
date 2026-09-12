package com.example.eventapp.controller;

import com.example.eventapp.dto.BreadcrumbDTO;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.SupportTicketRepository;
import com.example.eventapp.service.BusinessProfileService;
import com.example.eventapp.service.EmailService;
import com.example.eventapp.service.ReviewService;
import com.example.eventapp.service.SubscriptionService;
import com.example.eventapp.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

@Controller
public class ProfileController {

    private final UserService userService;
    private final EmailService emailService;
    private final BusinessProfileService businessProfileService;
    private final ReviewService reviewService;
    private final SubscriptionService subscriptionService;
    private final SupportTicketRepository supportTicketRepository;

    public ProfileController(UserService userService,
                             EmailService emailService,
                             BusinessProfileService businessProfileService,
                             SupportTicketRepository supportTicketRepository,
                             ReviewService reviewService, SubscriptionService subscriptionService) {
        this.userService = userService;
        this.emailService = emailService;
        this.businessProfileService = businessProfileService;
        this.reviewService = reviewService;
        this.subscriptionService = subscriptionService;
        this.supportTicketRepository = supportTicketRepository;
    }

    @GetMapping("/profile")
    public String profile(Model model,
                          @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByEmail(userDetails.getUsername());

        User userEmail = userService.findById(user.getId());

        int favoriteCount = user.getFavoriteBusinesses() != null
                ? user.getFavoriteBusinesses().size() : 0;

        int businessCount = businessProfileService.findByUser(user).size();

        Long reviewCount = reviewService.countByUser(user);

        model.addAttribute("user", userEmail);
        model.addAttribute("favoriteCount", favoriteCount);
        model.addAttribute("activeSubscription",
                subscriptionService.findActiveSubscription(user));
        model.addAttribute("pendingSubscription",
                subscriptionService.findPendingSubscription(user));
        model.addAttribute("subscriptionHistory",
                subscriptionService.findAllByUser(user));
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("businessCount", businessCount);
        model.addAttribute("ownTickets",
                supportTicketRepository.findAllByUserOrderByUpdatedAtDesc(user).size());
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Acasă", "/businesses"),
                new BreadcrumbDTO("Profil", null)));
        return "profile";
    }

    @GetMapping("/profile/ratings")
    public String userRatings(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {

        User user = userService.findByEmail(userDetails.getUsername());
        model.addAttribute("reviews", reviewService.findUserReviews(user));
        model.addAttribute("averageRating", reviewService.getUserAverageRating(user));
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Acasă", "/businesses"),
                new BreadcrumbDTO("Recenziile mele", null)));
        return "user-ratings";
    }

    @PostMapping("/profile/email")
    public String changeEmail(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String currentPassword,
            @RequestParam String newEmail,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            userService.changeEmail(user, currentPassword, newEmail);
            emailService.sendEmailChangedEmail(user.getEmail(), user.getFirstName());

            new SecurityContextLogoutHandler().logout(request, response, authentication);
            return "redirect:/login?emailChanged";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("accountError", exception.getMessage());
            return "redirect:/profile";
        }
    }

    @PostMapping("/profile/password")
    public String changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            userService.changePassword(user, currentPassword, newPassword, confirmPassword);
            emailService.sendPasswordChangedEmail(
                    userDetails.getUsername(),
                    user.getFirstName()
            );
            redirectAttributes.addFlashAttribute(
                    "accountSuccess",
                    "Parola a fost schimbată cu succes."
            );
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("accountError", exception.getMessage());
        }

        return "redirect:/profile";
    }

    @PostMapping("/profile/delete")
    public String deleteAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String currentPassword,
            @RequestParam String deleteConfirmation,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            userService.deleteAccount(user, currentPassword, deleteConfirmation);
            emailService.sendAccountDeletedEmail(
                    userDetails.getUsername(),
                    user.getFirstName()
            );

            new SecurityContextLogoutHandler().logout(request, response, authentication);
            return "redirect:/login?accountDeleted";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("accountError", exception.getMessage());
            return "redirect:/profile";
        }
    }
}
