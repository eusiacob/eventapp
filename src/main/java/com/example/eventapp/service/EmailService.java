package com.example.eventapp.service;

import com.example.eventapp.config.ApplicationMailProperties;
import com.example.eventapp.model.LegalDocumentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;
import org.springframework.web.util.HtmlUtils;

@Slf4j
@Service
public class EmailService {

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
        sendEmailOrThrow(recipient, subject, greeting(firstName) + intro +
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
            log.warn("Emailul de notificare nu a fost trimis deoarece lipsește destinatarul.");
            return;
        }

        try {
            sendEmailOrThrow(recipient, subject, content);
        } catch (MailException exception) {
            log.warn("Emailul de notificare nu a putut fi trimis.", exception);
        }
    }

    private void sendEmailOrThrow(String recipient, String subject, String content) {
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("Lipsește destinatarul emailului.");
        }
        String plainContent = content + applicationFooter();
        try {
            var mimeMessage = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setFrom(mailProperties.getFormattedFromAddress());
            helper.setReplyTo(mailProperties.getReplyToAddress());
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setSentDate(new java.util.Date());
            helper.setText(plainContent, htmlEmail(content));
            mailSender.send(mimeMessage);
        } catch (jakarta.mail.MessagingException exception) {
            throw new org.springframework.mail.MailSendException("Emailul HTML nu a putut fi pregătit.", exception);
        }
        SimpleMailMessage archivedMessage = new SimpleMailMessage();
        archivedMessage.setFrom(mailProperties.getFormattedFromAddress());
        archivedMessage.setReplyTo(mailProperties.getReplyToAddress());
        archivedMessage.setTo(recipient);
        archivedMessage.setSubject(subject);
        archivedMessage.setText(plainContent);
        archivedMessage.setSentDate(new java.util.Date());
        try {
            sentMailArchiveService.archive(archivedMessage);
        } catch (RuntimeException exception) {
            log.warn("SMTP a reușit, dar arhivarea copiei email a eșuat ({}).",
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
                "Instagram: https://www.instagram.com/m.eventtt\n\n" +
                "© 2026 M-Event. Toate drepturile rezervate.";
    }

    private String htmlEmail(String content) {
        String body = HtmlUtils.htmlEscape(content).replace("\r\n", "\n").replace("\n", "<br>\n");
        String logo = appBaseUrl + "/images/logo_transparent.png";
        String facebookIcon = appBaseUrl + "/images/email-social-facebook.png";
        String instagramIcon = appBaseUrl + "/images/email-social-instagram.png";
        String tiktokIcon = appBaseUrl + "/images/email-social-tiktok.png";
        return "<!doctype html><html lang=\"ro\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"></head>" +
                "<body style=\"margin:0;padding:0;background:#faf8f5;color:#352a2c;font-family:Arial,Helvetica,sans-serif;-webkit-text-size-adjust:100%;\">" +
                "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#faf8f5;border-collapse:collapse;width:100%;\"><tr><td align=\"center\" style=\"padding:22px 10px;\">" +
                "<table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%;max-width:600px;background:#fff;border:1px solid #e8e1e4;border-radius:14px;border-collapse:separate;\"><tr><td style=\"padding:28px 24px;font-size:16px;line-height:1.65;overflow-wrap:anywhere;\">" +
                body +
                "</td></tr><tr><td align=\"center\" style=\"padding:18px 20px 22px;background:#f7f3f5;border-top:1px solid #e8e1e4;border-radius:0 0 14px 14px;\">" +
                "<a href=\"" + appBaseUrl + "\" style=\"text-decoration:none;\"><img src=\"" + logo + "\" width=\"76\" alt=\"M-Event\" style=\"display:block;width:76px;height:auto;border:0;margin:0 auto 12px;\"></a>" +
                "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:0 auto 12px;border-collapse:collapse;\"><tr>" +
                socialIcon(facebookIcon, "Facebook", "https://www.facebook.com/m.eventtt") +
                socialIcon(instagramIcon, "Instagram", "https://www.instagram.com/m.eventtt") +
                socialIcon(tiktokIcon, "TikTok", "https://www.tiktok.com/@m.eventtt") +
                "</tr></table>" +
                "<p style=\"margin:0 0 8px;color:#534666;font-size:13px;line-height:1.5;font-weight:bold;\">M-Event · servicii și experiențe pentru evenimente</p>" +
                "<p style=\"margin:0 0 8px;color:#625b63;font-size:12px;line-height:1.5;\">Acest email a fost generat automat. Te rugăm să nu răspunzi. Pentru ajutor: <a href=\"mailto:" + HtmlUtils.htmlEscape(mailProperties.getContactAddress()) + "\" style=\"color:#138086;\">" + HtmlUtils.htmlEscape(mailProperties.getContactAddress()) + "</a></p>" +
                "<p style=\"margin:0;color:#625b63;font-size:12px;line-height:1.6;\"><a href=\"" + appBaseUrl + "/privacy\" style=\"color:#534666;\">Confidențialitate</a> &nbsp;·&nbsp; <a href=\"" + appBaseUrl + "/terms\" style=\"color:#534666;\">Termeni</a></p>" +
                "<p style=\"margin:8px 0 0;color:#817a82;font-size:11px;line-height:1.5;\">© 2026 M-Event. Toate drepturile rezervate.</p>" +
                "</td></tr></table></td></tr></table></body></html>";
    }

    private String socialIcon(String image, String label, String url) {
        return "<td align=\"center\" style=\"padding:0 9px;\"><a href=\"" + url + "\" aria-label=\"M-Event pe " + label + "\" style=\"display:inline-block;text-decoration:none;\"><img src=\"" + image + "\" width=\"30\" height=\"30\" alt=\"" + label + "\" style=\"display:block;width:30px;height:30px;border:0;\"></a></td>";
    }

}
