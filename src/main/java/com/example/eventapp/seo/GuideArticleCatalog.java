package com.example.eventapp.seo;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class GuideArticleCatalog {
    private final List<GuideArticle> articles = List.of(
            new GuideArticle(
                    "cum-alegi-fotograful-pentru-nunta",
                    "Cum alegi fotograful potrivit pentru nuntă",
                    "Criterii practice pentru alegerea unui fotograf de nuntă: stil, portofoliu, disponibilitate și comunicare.",
                    "Fotografiile păstrează emoția evenimentului mult timp după ce petrecerea s-a încheiat. De aceea, alegerea fotografului merită făcută cu atenție, nu doar după preț.",
                    "fotograf", "Fotografi",
                    List.of(
                            new GuideArticle.GuideSection("Începe cu stilul fotografic", "Privește mai multe galerii complete, nu doar imaginile preferate de pe rețelele sociale. Caută un stil care se potrivește atmosferei pe care o dorești.", List.of("documentar și natural", "editorial și elegant", "luminos, cu culori calde", "fotografie de familie și portrete")),
                            new GuideArticle.GuideSection("Verifică disponibilitatea și aria de acoperire", "Un fotograf poate lucra doar în anumite județe sau poate accepta evenimente în toată țara. Stabilește data și locația înainte de a cere o ofertă.", List.of("data evenimentului", "județul și locația", "numărul de ore", "livrabilele incluse")),
                            new GuideArticle.GuideSection("Pune întrebările importante", "O discuție scurtă îți arată dacă există compatibilitate și dacă furnizorul a înțeles ce este important pentru voi.", List.of("când primiți fotografiile", "cum sunt livrate și arhivate", "ce se întâmplă dacă apare o situație neprevăzută", "dacă există un contract și un avans"))
                    )),
            new GuideArticle(
                    "cum-alegi-dj-ul-pentru-eveniment",
                    "Cum alegi DJ-ul pentru un eveniment reușit",
                    "Află ce să verifici la un DJ pentru nuntă, botez sau petrecere: muzică, echipament, experiență și comunicare.",
                    "DJ-ul influențează ritmul întregii seri. Un furnizor bun combină muzica potrivită cu echipamente sigure și cu atenția la invitați.",
                    "dj", "DJ",
                    List.of(
                            new GuideArticle.GuideSection("Potrivește repertoriul cu publicul", "Discută din timp despre genurile preferate, melodiile obligatorii și piesele pe care vrei să le eviți.", List.of("stilurile muzicale preferate", "momentele speciale", "muzica pentru toate generațiile", "lista de piese interzise")),
                            new GuideArticle.GuideSection("Verifică echipamentul", "Sunetul și lumina trebuie dimensionate pentru spațiul evenimentului. Întreabă ce se întâmplă în cazul unei defecțiuni.", List.of("sonorizare potrivită locației", "microfon pentru discursuri", "lumini ambientale", "echipament de rezervă")),
                            new GuideArticle.GuideSection("Clarifică programul", "Stabiliți ora de sosire, durata programului și cine coordonează momentele importante din seară.", List.of("montaj și soundcheck", "pauze", "comunicarea cu organizatorul", "prelungirea programului"))
                    )),
            new GuideArticle(
                    "checklist-organizare-eveniment",
                    "Checklist pentru organizarea unui eveniment",
                    "Un checklist simplu pentru organizarea unei nunți, a unui botez sau a unei petreceri fără stres inutil.",
                    "Organizarea devine mai ușoară când împarți deciziile pe etape și notezi ce este deja rezolvat. Folosește această listă ca punct de pornire.",
                    null, null,
                    List.of(
                            new GuideArticle.GuideSection("Cu câteva luni înainte", "Începe cu deciziile care influențează toate celelalte servicii.", List.of("stabilește data și bugetul", "alege locația", "estimează numărul de invitați", "rezervă foto, video și muzică")),
                            new GuideArticle.GuideSection("În luna evenimentului", "Închide detaliile logistice și confirmă furnizorii.", List.of("confirmă programul cu fiecare furnizor", "transmite locația și persoana de contact", "definitivează meniul și decorul", "pregătește lista momentelor importante")),
                            new GuideArticle.GuideSection("În ziua evenimentului", "Păstrează o singură persoană de contact pentru furnizori, astfel încât tu să te poți bucura de eveniment.", List.of("ține la îndemână contactele", "verifică accesul și ora montajului", "lasă coordonarea unei persoane de încredere", "păstrează timp pentru invitați"))
                    )),
            new GuideArticle(
                    "cum-alegi-locatia-pentru-eveniment",
                    "Cum alegi locația potrivită pentru eveniment",
                    "Descoperă ce criterii contează când alegi o locație pentru nuntă, botez, aniversare sau eveniment corporate.",
                    "Locația stabilește atmosfera, logistica și o parte importantă din buget. O vizită bine pregătită te ajută să compari opțiunile corect.",
                    "locatii", "Locații",
                    List.of(
                            new GuideArticle.GuideSection("Compară spațiul și capacitatea", "Numărul de invitați trebuie raportat la configurația meselor, ringul de dans și spațiile auxiliare.", List.of("capacitatea reală", "spațiu pentru dans", "terasă sau grădină", "parcare și acces")),
                            new GuideArticle.GuideSection("Întreabă despre ce este inclus", "Două locații cu prețuri asemănătoare pot avea servicii incluse foarte diferite.", List.of("meniu și băuturi", "mobilier și decor de bază", "personal și curățenie", "taxe suplimentare")),
                            new GuideArticle.GuideSection("Verifică restricțiile", "Clarifică din timp programul muzical, furnizorii acceptați și regulile pentru decor sau artificii.", List.of("ora până la care poate continua muzica", "accesul furnizorilor", "decorul permis", "planul pentru vreme nefavorabilă"))
                    ))
    );

    public List<GuideArticle> all() {
        return articles;
    }

    public Optional<GuideArticle> find(String slug) {
        return articles.stream().filter(article -> article.slug().equals(slug)).findFirst();
    }
}
