package com.example.eventapp.service;

import com.example.eventapp.dto.LegalDocumentForm;
import com.example.eventapp.model.LegalDocument;
import com.example.eventapp.model.LegalDocumentType;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.LegalDocumentRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class LegalDocumentService {

    private static final String INITIAL_VERSION = "2026-09-13";

    private final LegalDocumentRepository legalDocumentRepository;

    public LegalDocumentService(LegalDocumentRepository legalDocumentRepository) {
        this.legalDocumentRepository = legalDocumentRepository;
    }

    @PostConstruct
    @Transactional
    public void createDefaultDocuments() {
        createIfMissing(LegalDocumentType.PRIVACY_POLICY, defaultPrivacyPolicy());
        createIfMissing(LegalDocumentType.TERMS_AND_CONDITIONS, defaultTermsAndConditions());
    }

    public LegalDocument getDocument(LegalDocumentType type) {
        return legalDocumentRepository.findByType(type)
                .orElseThrow(() -> new IllegalStateException("Documentul legal nu este configurat."));
    }

    public List<LegalDocument> getAllDocuments() {
        return legalDocumentRepository.findAll().stream()
                .sorted(Comparator.comparing(LegalDocument::getType))
                .toList();
    }

    public boolean hasCurrentAcceptances(User user) {
        return user != null &&
                hasAcceptedCurrentVersion(
                        user.getPrivacyPolicyVersion(),
                        user.getPrivacyPolicyAcceptedAt(),
                        LegalDocumentType.PRIVACY_POLICY
                ) && hasAcceptedCurrentVersion(
                        user.getTermsVersion(),
                        user.getTermsAcceptedAt(),
                        LegalDocumentType.TERMS_AND_CONDITIONS
                );
    }

    @Transactional
    public void acceptCurrentDocuments(User user) {
        user.setPrivacyPolicyVersion(getDocument(LegalDocumentType.PRIVACY_POLICY).getVersion());
        user.setPrivacyPolicyAcceptedAt(java.time.LocalDateTime.now());
        user.setTermsVersion(getDocument(LegalDocumentType.TERMS_AND_CONDITIONS).getVersion());
        user.setTermsAcceptedAt(java.time.LocalDateTime.now());
    }

    @Transactional
    public void updateDocument(LegalDocumentType type, LegalDocumentForm form) {
        LegalDocument document = getDocument(type);
        document.setContent(form.getContent().trim());
        document.setVersion(form.getVersion().trim());
        document.setLastUpdated(form.getLastUpdated());

        legalDocumentRepository.save(document);
    }

    private boolean hasAcceptedCurrentVersion(
            String acceptedVersion,
            java.time.LocalDateTime acceptedAt,
            LegalDocumentType type
    ) {
        return acceptedAt != null && getDocument(type).getVersion().equals(acceptedVersion);
    }

    private void createIfMissing(LegalDocumentType type, String content) {
        if (legalDocumentRepository.findByType(type).isPresent()) {
            return;
        }

        LegalDocument document = new LegalDocument();
        document.setType(type);
        document.setContent(content);
        document.setVersion(INITIAL_VERSION);
        document.setLastUpdated(LocalDate.of(2026, 9, 13));
        legalDocumentRepository.save(document);
    }

    private String defaultPrivacyPolicy() {
        return """
                1. Introducere
                Protejarea datelor cu caracter personal este importantă pentru M-Event. Această politică explică modul în care colectăm, utilizăm, stocăm și protejăm datele utilizatorilor.

                2. Date prelucrate
                Putem prelucra numele, datele de contact, datele contului, serviciile publicate, imaginile și videoclipurile încărcate, favoritele, recenziile, notificările și mesajele de suport.

                3. Securitate
                Parolele sunt stocate sub formă de hash. Emailul și numărul de telefon ale contului sunt criptate înainte de stocarea în baza de date.

                4. Scopuri și temeiuri
                Datele sunt folosite pentru administrarea contului, autentificare, servicii, recenzii, favorite, suport, notificări și securitatea platformei. Prelucrarea se bazează, după caz, pe executarea contractului, consimțământ, obligații legale sau interes legitim.

                5. Notificări și emailuri
                Putem transmite notificări în platformă și emailuri tranzacționale despre cont, securitate, aprobări, recenzii și suport.

                6. Partajare și retenție
                Datele nu sunt vândute. Furnizorii tehnici pot prelucra date strict pentru funcționarea serviciului. Datele sunt păstrate pe durata contului și cel mult 2 ani de la ultima autentificare, cu excepțiile prevăzute de lege.

                7. Drepturile utilizatorului
                Utilizatorul poate solicita acces, rectificare, ștergere, restricționare, portabilitate sau opoziție, în condițiile legii. Exportul de date este disponibil în Profil > Setări cont > Descarcă datele mele.

                8. Contact și reclamații
                Pentru solicitări privind datele personale, utilizează pagina de contact sau sistemul de suport. Utilizatorul se poate adresa și ANSPDCP: www.dataprotection.ro.
                """;
    }

    private String defaultTermsAndConditions() {
        return """
                1. Introducere
                Acești termeni reglementează utilizarea platformei M-Event. Prin utilizarea platformei, utilizatorul confirmă că i-a citit și acceptat.

                2. Contul utilizatorului
                Utilizatorul trebuie să furnizeze informații corecte și să păstreze confidențialitatea datelor de autentificare. Emailul, parola și ștergerea contului pot fi gestionate din Profil > Setări cont.

                3. Servicii și conținut publicat
                Utilizatorii care publică servicii sunt responsabili pentru corectitudinea, legalitatea și actualitatea informațiilor. Pentru imaginile și videoclipurile încărcate, utilizatorul declară că deține drepturile necesare de utilizare și publicare.

                4. Recenzii
                Recenziile trebuie să fie reale, relevante și respectuoase. Ele pot fi verificate înainte de publicare și pot fi respinse dacă nu respectă regulile platformei.

                5. Notificări și suport
                Platforma poate trimite notificări în aplicație și emailuri tranzacționale pentru evenimente importante. Solicitările de suport trebuie să fie relevante și să nu conțină conținut abuziv sau ilegal.

                6. Comportament interzis
                Este interzisă folosirea platformei pentru activități ilegale, fraudă, publicarea de conținut ofensator, colectarea neautorizată a datelor sau compromiterea securității platformei.

                7. Modificări
                Termenii pot fi actualizați atunci când serviciul sau legislația se schimbă. O versiune nouă poate necesita reconfirmarea acceptării la următoarea autentificare.
                """;
    }
}
