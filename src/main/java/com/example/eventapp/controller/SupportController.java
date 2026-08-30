package com.example.eventapp.controller;

import com.example.eventapp.dto.BreadcrumbDTO;
import com.example.eventapp.model.SupportMessage;
import com.example.eventapp.model.SupportTicket;
import com.example.eventapp.model.User;
import com.example.eventapp.service.SupportService;
import com.example.eventapp.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/support")
public class SupportController {

    private final SupportService supportService;
    private final UserService userService;

    public SupportController(
            SupportService supportService,
            UserService userService
    ) {
        this.supportService = supportService;
        this.userService = userService;
    }


    // =========================
    // LISTA TICHETE
    // =========================

    @GetMapping
    public String support(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {

        User user =
                userService.findByEmail(
                        userDetails.getUsername()
                );

        List<SupportTicket> tickets =
                supportService.getUserTickets(user);

        model.addAttribute(
                "tickets",
                tickets
        );

        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Servicii", "/businesses"),
                new BreadcrumbDTO("Profil", "/profile"),
                new BreadcrumbDTO("Suport", null)
        ));

        return "support/index";
    }


    // =========================
    // FORMULAR TICHET NOU
    // =========================

    @GetMapping("/new")
    public String newTicket(
            Model model
    ) {

        if (!model.containsAttribute("ticket")) {

            model.addAttribute(
                    "ticket",
                    new SupportTicket()
            );
        }

        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Servicii", "/businesses"),
                new BreadcrumbDTO("Profil", "/profile"),
                new BreadcrumbDTO("Suport", "/support"),
                new BreadcrumbDTO("Tichet nou", null)
        ));

        return "support/new";
    }


    // =========================
    // CREARE TICHET
    // =========================

    @PostMapping("/new")
    public String createTicket(
            @ModelAttribute("ticket")
            SupportTicket ticket,

            @RequestParam("message")
            String message,

            @AuthenticationPrincipal
            UserDetails userDetails,

            RedirectAttributes redirectAttributes
    ) {

        User user =
                userService.findByEmail(
                        userDetails.getUsername()
                );

        try {

            SupportTicket savedTicket =
                    supportService.createTicket(
                            user,
                            ticket.getSubject(),
                            ticket.getCategory(),
                            message
                    );

            redirectAttributes.addFlashAttribute(
                    "supportSuccess",
                    "Solicitarea a fost trimisă cu succes."
            );

            return "redirect:/support/"
                    + savedTicket.getId();


        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "supportError",
                    e.getMessage()
            );

            redirectAttributes.addFlashAttribute(
                    "ticket",
                    ticket
            );

            redirectAttributes.addFlashAttribute(
                    "message",
                    message
            );

            return "redirect:/support/new";
        }
    }


    // =========================
    // VIZUALIZARE TICHET
    // =========================

    @GetMapping("/{id}")
    public String viewTicket(
            @PathVariable Long id,

            @AuthenticationPrincipal
            UserDetails userDetails,

            Model model
    ) {

        User user =
                userService.findByEmail(
                        userDetails.getUsername()
                );

        SupportTicket ticket =
                supportService.getUserTicket(
                        id,
                        user
                );

        List<SupportMessage> messages =
                supportService.getMessages(
                        ticket
                );

        model.addAttribute(
                "ticket",
                ticket
        );

        model.addAttribute(
                "messages",
                messages
        );

        return "support/view";
    }


    // =========================
    // TRIMITERE MESAJ
    // =========================

    @PostMapping("/{id}/message")
    public String addMessage(
            @PathVariable Long id,

            @RequestParam("message")
            String message,

            @AuthenticationPrincipal
            UserDetails userDetails,

            RedirectAttributes redirectAttributes
    ) {

        User user =
                userService.findByEmail(
                        userDetails.getUsername()
                );

        try {

            supportService.addMessage(
                    id,
                    user,
                    message
            );

            return "redirect:/support/"
                    + id;


        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "supportError",
                    e.getMessage()
            );

            return "redirect:/support/" + id;
        }
    }
}