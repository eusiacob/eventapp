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
                POLITICA DE CONFIDENȚIALITATE M-EVENT

                Data ultimei actualizări: se consultă data afișată în partea de sus a acestei pagini.

                1. Cine prelucrează datele tale
                Operatorul de date este [DENUMIREA LEGALĂ A OPERATORULUI], cu sediul în [ADRESĂ], denumit în continuare „M-Event”, „noi” sau „platforma”. Pentru întrebări despre datele personale ne poți contacta la [EMAIL GDPR] sau prin pagina de contact și sistemul de suport al platformei.

                Înainte de lansarea comercială, aceste date de identificare și de contact trebuie completate de administrator cu informațiile reale ale operatorului.

                2. Domeniul politicii
                Această politică explică modul în care prelucrăm datele persoanelor care își creează cont, publică sau consultă servicii, trimit recenzii, folosesc favoritele, notificările și suportul în M-Event. Politica se aplică datelor prelucrate prin site și prin funcționalitățile asociate contului.

                3. Datele pe care le prelucrăm
                În funcție de modul în care folosești platforma, putem prelucra:
                - date de cont: nume, adresă de email, număr de telefon, rolul contului, identificatorul intern, data creării și ultima autentificare;
                - date de securitate și autentificare: parola stocată sub formă de hash, date tehnice necesare sesiunii și informații necesare prevenirii accesului neautorizat;
                - date despre servicii: denumire, descriere, categorie, localitate, date de contact, disponibilitate, prețuri sau alte informații introduse în profilul unui serviciu;
                - conținut media: imagini și videoclipuri încărcate pentru prezentarea serviciilor;
                - interacțiuni în platformă: servicii favorite, recenzii, răspunsuri la recenzii, notificări și preferințe aferente;
                - date de suport: subiectul, mesajele și datele necesare soluționării solicitărilor;
                - dovezi ale acceptării documentelor legale: versiunea Termenilor și a Politicii de confidențialitate acceptate, precum și data și ora acceptării.

                Te rugăm să nu introduci în câmpurile publice date personale ale altor persoane decât dacă ai un temei legal și dreptul de a le publica.

                4. Scopurile prelucrării
                Folosim datele pentru a crea și administra contul, a permite autentificarea, a publica și afișa servicii, a modera conținutul și recenziile, a gestiona favoritele și notificările, a răspunde solicitărilor de suport, a proteja securitatea platformei, a preveni abuzurile și a demonstra acceptarea documentelor legale.

                Putem trimite emailuri tranzacționale despre crearea contului, schimbarea emailului sau parolei, ștergerea contului, aprobarea ori respingerea serviciilor și recenziilor, precum și răspunsurile la solicitările de suport. Aceste mesaje sunt legate de funcționarea contului și nu reprezintă, prin ele însele, comunicări de marketing.

                5. Temeiurile juridice
                Prelucrăm datele, după caz, pentru executarea contractului sau pentru demersuri la cererea ta înainte de încheierea acestuia, pentru respectarea obligațiilor legale, pentru interesele noastre legitime privind securitatea, prevenirea fraudei, moderarea și funcționarea platformei ori în baza consimțământului tău atunci când acesta este cerut de lege. Îți poți retrage consimțământul în orice moment, fără a afecta legalitatea prelucrării anterioare retragerii.

                6. Caracterul obligatoriu al datelor
                Datele necesare creării și administrării contului sunt necesare pentru furnizarea funcționalităților corespunzătoare. Dacă nu le furnizezi, este posibil să nu putem crea contul sau să nu putem oferi anumite funcții. Publicarea informațiilor opționale despre un serviciu rămâne la alegerea ta, însă lipsa lor poate reduce utilitatea profilului public.

                7. Cui putem divulga datele
                Nu vindem date personale. Putem utiliza furnizori care prelucrează date numai în numele nostru și conform instrucțiunilor noastre, cum ar fi furnizorii de găzduire, infrastructură, bază de date, trimitere email și mentenanță tehnică. Putem divulga date autorităților sau altor destinatari atunci când legea ne obligă ori când este necesar pentru apărarea unui drept.

                Denumirea furnizorului de găzduire și, după caz, informațiile privind transferurile internaționale trebuie completate de administrator înainte de lansarea comercială.

                8. Date făcute publice
                Informațiile publicate în profilul unui serviciu, inclusiv imaginile, videoclipurile, descrierile și datele de contact pe care alegi să le afișezi, pot fi văzute de vizitatorii platformei. Recenziile aprobate și răspunsurile aferente pot fi, de asemenea, publice. Nu încărca materiale sau informații pe care nu dorești să fie accesibile publicului.

                9. Păstrarea datelor
                Păstrăm datele pe durata existenței contului. Conturile inactive sunt programate pentru ștergere după cel mult 2 ani de la ultima autentificare, împreună cu datele și fișierele asociate, cu excepția informațiilor pe care trebuie să le păstrăm mai mult timp pentru respectarea unei obligații legale, soluționarea unui litigiu sau apărarea unui drept. Poți solicita ștergerea contului din Profil > Setări cont; anumite date pot rămâne păstrate temporar doar în limitele impuse de lege sau necesare protejării drepturilor noastre.

                10. Securitatea datelor
                Aplicăm măsuri tehnice și organizatorice rezonabile pentru protejarea datelor. Parolele sunt stocate sub formă de hash, iar emailul și numărul de telefon din cont sunt criptate înainte de stocarea în baza de date. Accesul la funcțiile administrative este restricționat în funcție de rol. Totuși, nicio transmitere sau stocare electronică nu poate fi garantată ca fiind complet lipsită de riscuri.

                11. Drepturile tale
                În condițiile prevăzute de legislația aplicabilă, ai dreptul de acces, rectificare, ștergere, restricționare a prelucrării, portabilitate, opoziție și de a nu face obiectul unei decizii bazate exclusiv pe prelucrare automată care produce efecte juridice semnificative. Poți solicita o copie a datelor sau poți utiliza opțiunea Profil > Setări cont > Descarcă datele mele pentru un export în format JSON. Poți solicita modificarea sau ștergerea datelor și prin suport.

                În prezent, deciziile de aprobare sau respingere a serviciilor și recenziilor sunt luate prin moderare umană; platforma nu urmărește să adopte decizii exclusiv automatizate cu efect juridic sau similar semnificativ asupra ta.

                12. Cookie-uri și tehnologii similare
                Platforma poate utiliza cookie-uri sau mecanisme strict necesare pentru autentificare, securitate și funcționarea sesiunii. Dacă vor fi introduse cookie-uri de analiză, publicitate sau alte tehnologii neesențiale, politica și mecanismul de consimțământ vor fi actualizate înainte de utilizarea lor.

                13. Plângeri
                Dacă consideri că prelucrarea datelor tale încalcă legea, ne poți contacta mai întâi pentru soluționare. Ai dreptul să depui o plângere la Autoritatea Națională de Supraveghere a Prelucrării Datelor cu Caracter Personal (ANSPDCP), disponibilă la www.dataprotection.ro.

                14. Modificarea politicii
                Putem actualiza această politică atunci când modificăm platforma, procesele de prelucrare sau când legislația o impune. Data și versiunea actualizată sunt afișate în pagină. Pentru modificări importante, îți putem solicita să accepți din nou documentul la următoarea autentificare.
                """;
    }

    private String defaultTermsAndConditions() {
        return """
                TERMENI ȘI CONDIȚII DE UTILIZARE M-EVENT

                Data ultimei actualizări: se consultă data afișată în partea de sus a acestei pagini.

                1. Acceptarea termenilor
                Acești Termeni și condiții reglementează utilizarea platformei M-Event. Prin crearea unui cont, autentificare sau folosirea funcționalităților platformei, confirmi că ai citit, înțeles și acceptat acești termeni și Politica de confidențialitate. Dacă nu ești de acord cu ei, nu utiliza platforma.

                2. Rolul platformei
                M-Event este o platformă care permite prezentarea și descoperirea serviciilor pentru evenimente, precum și folosirea funcțiilor de favorite, recenzii, notificări și suport. M-Event nu este parte la eventualele înțelegeri, rezervări, plăți sau contracte încheiate între utilizatori și furnizorii serviciilor afișate, dacă nu este indicat expres altfel.

                3. Contul utilizatorului
                Pentru anumite funcții este necesar un cont. Ești responsabil să furnizezi date corecte, complete și actualizate și să păstrezi confidențialitatea datelor de autentificare. Nu permite altei persoane să folosească contul tău. Anunță-ne prin suport dacă suspectezi acces neautorizat. Poți modifica emailul, parola sau poți solicita ștergerea contului din Profil > Setări cont, în limitele funcționalităților disponibile.

                4. Publicarea serviciilor
                Utilizatorul care publică un serviciu este responsabil pentru corectitudinea, legalitatea, actualitatea și caracterul complet al informațiilor furnizate. Nu publica informații înșelătoare, date de contact care nu îți aparțin, prețuri sau disponibilități false ori conținut care încalcă drepturile altor persoane. Ești responsabil pentru relația cu persoanele care te contactează prin intermediul profilului tău.

                5. Imagini, videoclipuri și alte materiale
                Prin încărcarea de imagini, videoclipuri, texte sau alte materiale, declari că deții drepturile necesare pentru utilizarea și publicarea lor și că ai obținut consimțămintele necesare de la persoanele identificabile în materiale. Acordezi M-Event o licență neexclusivă, gratuită, limitată la durata afișării conținutului pe platformă, pentru stocarea, adaptarea tehnică și afișarea acestuia în scopul furnizării serviciului. Nu încărca materiale ilegale, ofensatoare, defăimătoare, explicite, care încalcă confidențialitatea sau drepturile de autor, marca ori imaginea unei alte persoane.

                6. Moderarea serviciilor și a conținutului
                Serviciile, fișierele media și alte materiale pot fi verificate înainte sau după publicare. Putem aproba, respinge, ascunde, modifica tehnic sau elimina conținutul care nu respectă acești termeni, legislația ori standardele de siguranță ale platformei. Aprobarea unui serviciu nu reprezintă o garanție privind calitatea, legalitatea sau disponibilitatea acestuia.

                7. Recenzii și răspunsuri
                Recenziile trebuie să fie reale, relevante, bazate pe o experiență autentică și formulate respectuos. Este interzisă publicarea de informații false, injurii, amenințări, date personale ale altor persoane, publicitate neautorizată sau conținut ilegal. Recenziile și răspunsurile pot fi moderate, aprobate, respinse sau eliminate dacă încalcă regulile. Furnizorii pot răspunde civilizat la recenziile care îi privesc.

                8. Favorite, notificări și emailuri
                Platforma îți poate permite să salvezi servicii la favorite și să primești notificări în aplicație. Putem trimite emailuri tranzacționale despre cont, securitate, modificarea datelor, deciziile de moderare, recenzii și solicitări de suport. Comunicările de marketing, dacă vor exista, vor fi gestionate separat și conform regulilor aplicabile.

                9. Suport
                Sistemul de suport este destinat întrebărilor și problemelor legate de platformă. Nu îl utiliza pentru mesaje abuzive, spam, amenințări, conținut ilegal sau divulgarea nejustificată a datelor personale. Răspunsurile de suport au caracter informativ și nu constituie consultanță juridică, financiară sau profesională.

                10. Utilizări interzise
                Este interzis să folosești platforma pentru activități ilegale sau frauduloase, să încerci accesarea neautorizată a conturilor ori sistemelor, să colectezi automat date fără acord, să distribui malware, să ocolești măsurile de securitate, să creezi conturi false, să manipulezi recenziile sau să afectezi funcționarea normală a platformei.

                11. Disponibilitate și limitarea răspunderii
                Depunem eforturi rezonabile pentru funcționarea platformei, însă nu garantăm disponibilitatea neîntreruptă, lipsa erorilor sau compatibilitatea cu orice dispozitiv. Informațiile publicate de utilizatori aparțin acestora, iar M-Event nu garantează exactitatea, calitatea, siguranța, legalitatea sau disponibilitatea serviciilor terțe. În limitele permise de lege, M-Event nu răspunde pentru prejudicii rezultate din relațiile directe dintre utilizatori și furnizorii serviciilor.

                12. Proprietate intelectuală
                Elementele de identitate, designul, codul, structura și conținutul creat de M-Event sunt protejate de drepturile aplicabile. Nu poți copia, reproduce, distribui sau utiliza aceste elemente în scop comercial fără acordul nostru scris, cu excepția cazurilor permise de lege.

                13. Suspendarea și încetarea accesului
                Putem restricționa sau suspenda accesul la cont, temporar sau permanent, dacă există suspiciuni rezonabile de încălcare a acestor termeni, risc de securitate, fraudă sau obligație legală. Poți înceta utilizarea platformei și poți solicita ștergerea contului conform opțiunilor disponibile și Politicii de confidențialitate.

                14. Modificarea termenilor
                Putem actualiza acești termeni când modificăm funcționalitățile platformei, procesele de moderare sau legislația aplicabilă. Versiunea și data actualizării sunt afișate pe această pagină. Pentru schimbări importante, îți putem solicita reconfirmarea acceptării la următoarea autentificare. Continuarea utilizării după acceptarea noii versiuni înseamnă acceptarea termenilor actualizați.

                15. Legea aplicabilă și contact
                Acești termeni sunt interpretați conform legislației române și normelor obligatorii aplicabile consumatorilor. Pentru întrebări sau sesizări, folosește pagina de contact sau sistemul de suport al M-Event.
                """;
    }
}
