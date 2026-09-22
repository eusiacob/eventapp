package com.example.eventapp.controller;

import com.example.eventapp.config.ApplicationMailProperties;
import com.example.eventapp.dto.BreadcrumbDTO;
import com.example.eventapp.dto.ContactForm;
import jakarta.validation.Valid;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class ContactController {

    private final JavaMailSender mailSender;
    private final ApplicationMailProperties mailProperties;

    public ContactController(
            JavaMailSender mailSender,
            ApplicationMailProperties mailProperties
    ) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    @GetMapping("/contact")
    public String showContactPage(Model model) {

        model.addAttribute("contactForm", new ContactForm());
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Acasă", "/businesses"),
                new BreadcrumbDTO("Contact", null)
        ));

        return "contact";
    }

    @PostMapping("/contact")
    public String sendContactMessage(
            @Valid @ModelAttribute("contactForm") ContactForm contactForm,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "contact";
        }

        try {

            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(mailProperties.getFormattedFromAddress());
            message.setTo(mailProperties.getContactAddress());

            message.setReplyTo(contactForm.getEmail());

            message.setSubject(
                    "[M-Event Contact] " + contactForm.getSubject()
            );

            message.setText(
                    "Ai primit un mesaj nou de pe M-Event.\n\n" +
                            "Nume: " + contactForm.getName() + "\n" +
                            "Email: " + contactForm.getEmail() + "\n" +
                            "Subiect: " + contactForm.getSubject() + "\n\n" +
                            "Mesaj:\n" +
                            contactForm.getMessage()
            );

            mailSender.send(message);

            return "redirect:/contact?success";

        } catch (Exception e) {

            model.addAttribute(
                    "mailError",
                    "Mesajul nu a putut fi trimis. Te rugăm să încerci din nou."
            );

            return "contact";
        }
    }
}
