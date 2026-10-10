package com.example.eventapp.service;

import com.example.eventapp.config.ApplicationMailProperties;
import com.example.eventapp.model.LegalDocumentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String appBaseUrl;
    private final ApplicationMailProperties mailProperties;
    private final SentMailArchiveService sentMailArchiveService;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.base-url:http://localhost:8080}") String appBaseUrl,
            ApplicationMailProperties mailProperties,
            SentMailArchiveService sentMailArchiveService
    ) {
        this.mailSender = mailSender;
        this.appBaseUrl = appBaseUrl.replaceAll("/+$", "");
        this.mailProperties = mailProperties;
        this.sentMailArchiveService = sentMailArchiveService;
    }

    public void sendPasswordResetEmail(
            String recipient,
            String resetLink
    ) {

        sendEmail(
                recipient,
                "Resetarea parolei - M-Event",
                "Salutare,\n\n" +
                        "Ai solicitat resetarea parolei pentru contul tău M-Event.\n\n" +
                        "Pentru a seta o parolă nouă, accesează următorul link:\n\n" +
                        resetLink +
                        "\n\n" +
                        "Linkul este valabil timp de 15 minute.\n\n" +
                        "Dacă nu ai solicitat resetarea parolei, " +
                        "poți ignora acest mesaj."
        );
    }

    public String buildPasswordResetLink(String rawToken) {
        return UriComponentsBuilder
                .fromUriString(appBaseUrl)
                .path("/reset-password")
                .queryParam("token", rawToken)
                .build()
                .toUriString();
    }

    public String buildEmailVerificationLink(String rawToken) {
        return UriComponentsBuilder
                .fromUriString(appBaseUrl)
                .path("/verify-email")
                .queryParam("token", rawToken)
                .build()
                .toUriString();
    }

    public void sendUserEmailVerificationEmail(
            String recipient,
            String firstName,
            String verificationLink
    ) {
        sendEmail(
                recipient,
                "Verifică adresa de email - M-Event",
                greeting(firstName) +
                        "Pentru a confirma adresa de email a contului tău, " +
                        "accesează următorul link:\n\n" +
                        verificationLink +
                        "\n\n" +
                        "Linkul este valabil timp de 24 de ore.\n\n" +
                        "Dacă nu ai cerut această verificare, poți ignora mesajul."
        );
    }

    public void sendBusinessEmailVerificationEmail(
            String recipient,
            String businessName,
            String verificationLink
    ) {
        sendEmail(
                recipient,
                "Verifică emailul serviciului - M-Event",
                "Salut,\n\n" +
                        "Pentru a confirma adresa de email afișată public pentru serviciul \"" +
                        businessName +
                        "\", accesează următorul link:\n\n" +
                        verificationLink +
                        "\n\n" +
                        "Linkul este valabil timp de 24 de ore.\n\n" +
                        "După verificare, emailul va apărea ca verificat în pagina serviciului."
        );
    }

    public void sendAccountCreatedEmail(String recipient, String firstName) {
        sendEmail(
                recipient,
                "Cont creat cu succes - M-Event",
                greeting(firstName) +
                        "Contul tău M-Event a fost creat cu succes. " +
                        "Te poți autentifica și începe să planifici evenimentul tău sau să oferi servicii organizatorilor."
        );
    }

    public void sendEmailChangedEmail(String recipient, String firstName) {
        sendEmail(
                recipient,
                "Email actualizat - M-Event",
                greeting(firstName) +
                        "Adresa de email a contului tău a fost schimbată. " +
                        "De acum, autentifică-te folosind această adresă.\n\n" +
                        "Dacă nu ai făcut tu această modificare, contactează-ne imediat."
        );
    }

    public void sendPasswordChangedEmail(String recipient, String firstName) {
        sendEmail(
                recipient,
                "Parolă schimbată - M-Event",
                greeting(firstName) +
                        "Parola contului tău a fost schimbată cu succes.\n\n" +
                        "Dacă nu ai făcut tu această modificare, resetează parola și contactează-ne imediat."
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendPasswordResetConfirmationEmail(PasswordResetCompletedEvent event) {
        sendEmail(
                event.recipient(),
                "Parolă resetată cu succes - M-Event",
                greeting(event.firstName()) +
                        "Parola contului tău M-Event a fost resetată cu succes.\n\n" +
                        "Te poți autentifica folosind noua parolă:\n" +
                        appBaseUrl + "/login\n\n" +
                        "Dacă nu ai făcut tu această modificare, solicită imediat o nouă resetare " +
                        "a parolei și contactează-ne:\n" +
                        appBaseUrl + "/forgot\n" +
                        appBaseUrl + "/contact"
        );
    }

    public void sendAccountDeletedEmail(String recipient, String firstName) {
        sendEmail(
                recipient,
                "Cont șters - M-Event",
                greeting(firstName) +
                        "Contul tău M-Event a fost șters la cerere.\n\n" +
                        "Îți mulțumim că ai folosit M-Event."
        );
    }

    public void sendBusinessApprovedEmail(
            String recipient,
            String firstName,
            String businessName
    ) {
        sendEmail(
                recipient,
                "Serviciu aprobat - M-Event",
                greeting(firstName) +
                        "Serviciul tău \"" + businessName +
                        "\" a fost aprobat și este acum vizibil pe platformă."
        );
    }

    public void sendReviewApprovedEmail(
            String recipient,
            String firstName,
            String businessName
    ) {
        sendEmail(
                recipient,
                "Recenzie aprobată - M-Event",
                greeting(firstName) +
                        "Recenzia ta pentru \"" + businessName +
                        "\" a fost aprobată și este acum vizibilă public."
        );
    }

    public void sendReviewRejectedEmail(
            String recipient,
            String firstName,
            String businessName,
            String reason
    ) {
        String reasonText = reason == null || reason.isBlank()
                ? ""
                : "\n\nMotiv: " + reason.trim();

        sendEmail(
                recipient,
                "Recenzie respinsă - M-Event",
                greeting(firstName) +
                        "Recenzia ta pentru \"" + businessName +
                        "\" nu a fost aprobată." + reasonText +
                        "\n\nO poți modifica și trimite din nou spre verificare."
        );
    }

    public void sendSupportReplyEmail(
            String recipient,
            String firstName,
            String subject
    ) {
        sendEmail(
                recipient,
                "Răspuns nou de la Support - M-Event",
                greeting(firstName) +
                        "Ai primit un răspuns nou pentru solicitarea \"" + subject +
                        "\". Autentifică-te pentru a-l citi."
        );
    }

    public void sendPromotionReminderEmail(String recipient, String firstName, int stage) {
        String subject = switch (stage) {
            case 0 -> "Prezintă-ți serviciile pe M-Event";
            case 1 -> "Ai nevoie de ajutor pentru primul tău serviciu?";
            default -> "Profilul tău de furnizor te așteaptă pe M-Event";
        };
        String intro = switch (stage) {
            case 0 -> "Ai ales să îți promovezi serviciile pe M-Event, dar încă nu ai adăugat primul serviciu. " +
                    "Prezintă ce oferi și ajută persoanele care organizează un eveniment să te descopere.";
            case 1 -> "Pentru a începe, adaugă numele serviciului, o descriere, categoria, zona în care lucrezi " +
                    "și cel puțin o modalitate de contact. Poți completa prezentarea cu fotografii și videoclipuri scurte. " +
                    "Dacă ai nevoie de ajutor, scrie-ne la " + mailProperties.getContactAddress() + ".";
            default -> "Dacă dorești în continuare să îți promovezi activitatea, poți adăuga primul serviciu " +
                    "atunci când ești pregătit. Acesta este ultimul mesaj din seria noastră de reamintiri.";
        };
        sendEmailOrThrow(recipient, subject + " - M-Event", greeting(firstName) + intro +
                "\n\nAdaugă primul serviciu:\n" + appBaseUrl + "/business/create\n\n" +
                "Primești această reamintire deoarece ai ales promovarea serviciilor. " +
                "Dacă folosești platforma doar pentru a căuta servicii, poți schimba preferința din profil:\n" +
                appBaseUrl + "/profile");
    }

    /**
     * Propagates delivery failures so the durable queue can retry them.
     */
    public void sendLegalDocumentUpdatedEmail(String recipient, String firstName,
                                              LegalDocumentType type, String version, LocalDate lastUpdated) {
        boolean privacy = type == LegalDocumentType.PRIVACY_POLICY;
        String title = privacy ? "Politica de confidențialitate" : "Termenii și condițiile";
        sendEmailOrThrow(recipient, "Actualizare: " + title + " - M-Event",
                greeting(firstName) +
                        "Am actualizat documentul \"" + title + "\" al platformei M-Event.\n\n" +
                        "Versiune: " + version + "\n" +
                        "Data actualizării: " + lastUpdated.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + "\n\n" +
                        "Te rugăm să consulți documentul publicat aici:\n" +
                        appBaseUrl + (privacy ? "/privacy" : "/terms") + "\n\n" +
                        "Dacă versiunea acceptată în contul tău este diferită, îți vom solicita confirmarea " +
                        "la următoarea autentificare. Acest email este o informare și nu înregistrează acceptarea documentului.\n\n" +
                        "Primești acest mesaj deoarece ai un cont M-Event. Nu este un mesaj promoțional.");
    }

    private void sendEmail(
            String recipient,
            String subject,
            String content
    ) {
        if (recipient == null || recipient.isBlank()) {
            LOGGER.warn("Emailul de notificare nu a fost trimis deoarece lipsește destinatarul.");
            return;
        }

        try {
            sendEmailOrThrow(recipient, subject, content);
        } catch (MailException exception) {
            LOGGER.warn("Emailul de notificare nu a putut fi trimis.", exception);
        }
    }

    private void sendEmailOrThrow(String recipient, String subject, String content) {
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("Lipsește destinatarul emailului.");
        }
        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(mailProperties.getFormattedFromAddress());
        message.setReplyTo(mailProperties.getReplyToAddress());
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(content + applicationFooter());
        message.setSentDate(new java.util.Date());

        mailSender.send(message);
        try {
            sentMailArchiveService.archive(message);
        } catch (RuntimeException exception) {
            LOGGER.warn("SMTP a reușit, dar arhivarea copiei email a eșuat ({}).",
                    exception.getClass().getSimpleName());
        }
    }

    private String greeting(String firstName) {
        return "Salut" +
                (firstName == null || firstName.isBlank() ? "" : " " + firstName.trim()) +
                ",\n\n";
    }

    private String applicationFooter() {
        return "\n\n" +
                "────────────────────\n" +
                "M-Event\n" +
                "Descoperă servicii și experiențe pentru evenimentele tale.\n\n" +
                "Acesta este un mesaj generat automat. Te rugăm să nu răspunzi la acest email.\n" +
                "Pentru asistență, scrie-ne la " + mailProperties.getContactAddress() +
                " sau folosește formularul de contact: " + appBaseUrl + "/contact\n\n" +
                "Informații:\n" +
                "Confidențialitate: " + appBaseUrl + "/privacy\n" +
                "Termeni și condiții: " + appBaseUrl + "/terms\n" +
                "Facebook: https://www.facebook.com/m.eventtt\n" +
                "Instagram: https://www.instagram.com/\n\n" +
                "© 2026 M-Event. Toate drepturile rezervate.";
    }

}
