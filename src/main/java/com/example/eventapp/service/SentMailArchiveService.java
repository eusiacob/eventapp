package com.example.eventapp.service;

import com.example.eventapp.config.ApplicationMailProperties;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Properties;

@Service
public class SentMailArchiveService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SentMailArchiveService.class);
    private final ApplicationMailProperties properties;
    private final String smtpHost;
    private final String smtpUsername;
    private final String smtpPassword;

    public SentMailArchiveService(ApplicationMailProperties properties,
            @Value("${spring.mail.host:}") String smtpHost,
            @Value("${spring.mail.username:}") String smtpUsername,
            @Value("${spring.mail.password:}") String smtpPassword) {
        this.properties = properties;
        this.smtpHost = smtpHost;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
    }

    /** Best-effort archive only. SMTP delivery must never be retried due to an IMAP failure. */
    public void archive(SimpleMailMessage message) {
        var config = properties.getSentCopy();
        if (!config.isEnabled()) return;
        Store store = null;
        try {
            String host = fallback(config.getHost(), smtpHost);
            String username = fallback(config.getUsername(), smtpUsername);
            String password = fallback(config.getPassword(), smtpPassword);
            if (host.isBlank() || username.isBlank() || password.isBlank()
                    || config.getPort() < 1 || config.getPort() > 65535 || config.getTimeoutMs() < 1) {
                throw new IllegalArgumentException("Configurația IMAP este incompletă.");
            }
            Properties settings = new Properties();
            settings.setProperty("mail.imaps.ssl.enable", "true");
            settings.setProperty("mail.imaps.ssl.checkserveridentity", "true");
            for (String key : new String[]{"connectiontimeout", "timeout", "writetimeout"}) {
                settings.setProperty("mail.imaps." + key, Integer.toString(config.getTimeoutMs()));
            }
            Session session = Session.getInstance(settings);
            store = createStore(session);
            store.connect(host, config.getPort(), username, password);
            Folder folder = findSentFolder(store, config.getFolder());
            folder.appendMessages(new Message[]{copyMessage(session, message)});
            LOGGER.info("Copie email salvată în folderul Sent prin IMAP.");
        } catch (MessagingException | RuntimeException exception) {
            // Exception messages may expose email addresses or other sensitive server responses.
            LOGGER.warn("SMTP a reușit, dar copia emailului nu a fost salvată în Sent ({}).",
                    exception.getClass().getSimpleName());
        } finally {
            if (store != null) {
                try {
                    if (store.isConnected()) store.close();
                } catch (MessagingException | RuntimeException exception) {
                    LOGGER.warn("Închiderea conexiunii IMAP a eșuat ({}).", exception.getClass().getSimpleName());
                }
            }
        }
    }

    protected Store createStore(Session session) throws MessagingException {
        return session.getStore("imaps");
    }

    private Folder findSentFolder(Store store, String configuredFolder) throws MessagingException {
        if (configuredFolder != null && !configuredFolder.isBlank()) {
            Folder folder = store.getFolder(configuredFolder.trim());
            if (!folder.exists() || (folder.getType() & Folder.HOLDS_MESSAGES) == 0) {
                throw new MessagingException("Folderul IMAP configurat nu există sau nu poate păstra mesaje.");
            }
            return folder;
        }
        for (Folder folder : store.getDefaultFolder().list("*")) {
            if (folder instanceof IMAPFolder imapFolder) {
                for (String attribute : imapFolder.getAttributes()) {
                    if ("\\Sent".equalsIgnoreCase(attribute) && (folder.getType() & Folder.HOLDS_MESSAGES) != 0) {
                        return folder;
                    }
                }
            }
        }
        for (String name : new String[]{"Sent", "INBOX.Sent", "Sent Messages", "INBOX.Sent Messages"}) {
            Folder folder = store.getFolder(name);
            if (folder.exists() && (folder.getType() & Folder.HOLDS_MESSAGES) != 0) return folder;
        }
        Folder folder = store.getFolder("Sent");
        if (!folder.create(Folder.HOLDS_MESSAGES)) {
            throw new MessagingException("Folderul Sent nu a putut fi creat.");
        }
        folder.setSubscribed(true);
        return folder;
    }

    private MimeMessage copyMessage(Session session, SimpleMailMessage original) throws MessagingException {
        MimeMessage copy = new MimeMessage(session);
        copy.setFrom(new InternetAddress(original.getFrom()));
        if (original.getReplyTo() != null) copy.setReplyTo(InternetAddress.parse(original.getReplyTo()));
        addRecipients(copy, Message.RecipientType.TO, original.getTo());
        addRecipients(copy, Message.RecipientType.CC, original.getCc());
        addRecipients(copy, Message.RecipientType.BCC, original.getBcc());
        copy.setSubject(original.getSubject(), "UTF-8");
        copy.setText(original.getText(), "UTF-8");
        copy.setSentDate(original.getSentDate() == null ? new Date() : original.getSentDate());
        copy.saveChanges();
        copy.setFlag(Flags.Flag.SEEN, true);
        return copy;
    }

    private void addRecipients(MimeMessage copy, Message.RecipientType type, String[] recipients)
            throws MessagingException {
        if (recipients != null) {
            for (String recipient : recipients) copy.addRecipients(type, recipient);
        }
    }

    private String fallback(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
