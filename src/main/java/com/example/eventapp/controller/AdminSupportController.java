package com.example.eventapp.controller;

import com.example.eventapp.dto.BreadcrumbDTO;
import com.example.eventapp.model.SupportMessage;
import com.example.eventapp.model.SupportTicket;
import com.example.eventapp.model.User;
import com.example.eventapp.service.AdminAuditService;
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
    private final AdminAuditService adminAuditService;

    public AdminSupportController(
            SupportService supportService,
            UserService userService,
            AdminAuditService adminAuditService
    ) {
        this.supportService = supportService;
        this.userService = userService;
        this.adminAuditService = adminAuditService;
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

            SupportTicket ticket = supportService.getAdminTicket(id);

            supportService.addAdminMessage(
                    id,
                    admin,
                    message
            );
            adminAuditService.record(admin.getId(), ticket.getUser().getId(), "SUPPORT_REPLY_SENT",
                    "SUPPORT_TICKET", id.toString(), "Răspuns trimis la solicitarea de suport.");

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

            @AuthenticationPrincipal UserDetails userDetails,

            RedirectAttributes redirectAttributes
    ) {

        try {

            SupportTicket ticket = supportService.getAdminTicket(id);
            SupportTicket.SupportStatus oldStatus = ticket.getStatus();

            supportService.updateTicketStatus(
                    id,
                    status
            );

            if (oldStatus != status) {
                User admin = userService.findByEmail(userDetails.getUsername());
                adminAuditService.record(admin.getId(), ticket.getUser().getId(), "SUPPORT_STATUS_CHANGED",
                        "SUPPORT_TICKET", id.toString(), "Statusul solicitării de suport a fost schimbat.");
            }

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
