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
@RequestMapping("/admin/support")
public class AdminSupportController {

    private final SupportService supportService;
    private final UserService userService;

    public AdminSupportController(
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
    public String supportTickets(
            Model model
    ) {

        List<SupportTicket> tickets =
                supportService.getAllTickets();

        model.addAttribute(
                "tickets",
                tickets
        );

        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Dashboard", "/admin"),
                new BreadcrumbDTO("Suport", null)
        ));

        return "admin/support/index";
    }


    // =========================
    // FILTRARE DUPĂ STATUS
    // =========================

    @GetMapping("/status/{status}")
    public String supportTicketsByStatus(
            @PathVariable SupportTicket.SupportStatus status,
            Model model
    ) {

        List<SupportTicket> tickets =
                supportService.getTicketsByStatus(
                        status
                );

        model.addAttribute(
                "tickets",
                tickets
        );

        model.addAttribute(
                "selectedStatus",
                status
        );

        return "admin/support/index";
    }


    // =========================
    // VIZUALIZARE TICHET
    // =========================

    @GetMapping("/{id}")
    public String viewTicket(
            @PathVariable Long id,
            Model model
    ) {

        SupportTicket ticket =
                supportService.getAdminTicket(id);

        List<SupportMessage> messages =
                supportService.getMessages(ticket);

        model.addAttribute(
                "ticket",
                ticket
        );

        model.addAttribute(
                "messages",
                messages
        );

        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Dashboard", "/admin"),
                new BreadcrumbDTO("Suport", "/admin/support"),
                new BreadcrumbDTO("View ticket", null)
        ));

        return "admin/support/view";
    }


    // =========================
    // RĂSPUNS ADMIN
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

        User admin =
                userService.findByEmail(
                        userDetails.getUsername()
                );

        try {

            supportService.addAdminMessage(
                    id,
                    admin,
                    message
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "supportError",
                    e.getMessage()
            );
        }

        return "redirect:/admin/support/" + id;
    }


    // =========================
    // SCHIMBARE STATUS
    // =========================

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,

            @RequestParam("status")
            SupportTicket.SupportStatus status,

            RedirectAttributes redirectAttributes
    ) {

        try {

            supportService.updateTicketStatus(
                    id,
                    status
            );

            redirectAttributes.addFlashAttribute(
                    "supportSuccess",
                    "Statusul solicitării a fost actualizat."
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "supportError",
                    e.getMessage()
            );
        }

        return "redirect:/admin/support/" + id;
    }
}