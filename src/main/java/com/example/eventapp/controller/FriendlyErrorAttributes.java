package com.example.eventapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;

final class FriendlyErrorAttributes {

    static final String ERROR_VIEW = "error/friendly-error";

    private FriendlyErrorAttributes() {
    }

    static void addToModel(Model model, int statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode);

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorTitle", titleFor(status));
        model.addAttribute("errorMessage", messageFor(status));
        model.addAttribute("errorActionText", actionTextFor(status));
        model.addAttribute("errorActionHref", actionHrefFor(status));
    }

    private static String titleFor(HttpStatus status) {
        if (status == null) {
            return "A apărut o problemă";
        }

        return switch (status) {
            case BAD_REQUEST -> "Cererea nu poate fi procesată";
            case UNAUTHORIZED -> "Trebuie să te autentifici";
            case FORBIDDEN -> "Nu ai acces aici";
            case NOT_FOUND -> "Pagina nu a fost găsită";
            case METHOD_NOT_ALLOWED -> "Acțiunea nu este disponibilă";
            default -> status.is5xxServerError()
                    ? "A apărut o eroare"
                    : "A apărut o problemă";
        };
    }

    private static String messageFor(HttpStatus status) {
        if (status == null) {
            return "Nu am putut finaliza acțiunea. Te rugăm să încerci din nou.";
        }

        return switch (status) {
            case BAD_REQUEST -> "Datele trimise nu sunt valide sau cererea nu mai este disponibilă.";
            case UNAUTHORIZED -> "Pentru această pagină este nevoie să intri în cont.";
            case FORBIDDEN -> "Contul tău nu are permisiunea necesară pentru această pagină sau acțiune.";
            case NOT_FOUND -> "Pagina sau resursa căutată nu există, a fost mutată sau nu mai este disponibilă.";
            case METHOD_NOT_ALLOWED -> "Această acțiune nu poate fi executată din pagina curentă.";
            default -> status.is5xxServerError()
                    ? "A apărut o eroare neașteptată. Echipa tehnică poate verifica detaliile în loguri."
                    : "Nu am putut finaliza acțiunea. Te rugăm să încerci din nou.";
        };
    }

    private static String actionTextFor(HttpStatus status) {
        if (status == HttpStatus.UNAUTHORIZED) {
            return "Autentifică-te";
        }

        return "Înapoi la pagina principală";
    }

    private static String actionHrefFor(HttpStatus status) {
        if (status == HttpStatus.UNAUTHORIZED) {
            return "/login";
        }

        return "/";
    }
}
