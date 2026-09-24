package com.example.eventapp.model;

import lombok.Getter;
import java.util.Arrays;
import java.util.List;

@Getter
public enum BusinessServiceType {
    FOTOGRAF_FOTOGRAFIE_DE_EVENIMENT(BusinessCategory.FOTOGRAF, "Fotografie de eveniment"),
    FOTOGRAF_SEDINTE_FOTO(BusinessCategory.FOTOGRAF, "Ședințe foto"),
    FOTOGRAF_FOTOGRAFIE_DE_PRODUS(BusinessCategory.FOTOGRAF, "Fotografie de produs"),
    FOTOGRAF_CABINA_FOTO(BusinessCategory.FOTOGRAF, "Cabină foto"),
    FOTOGRAF_OGLINDA_FOTO(BusinessCategory.FOTOGRAF, "Oglindă foto"),
    FOTOGRAF_ALT_SERVICIU(BusinessCategory.FOTOGRAF, "Alt serviciu"),
    VIDEOGRAF_FILMARE_DE_EVENIMENT(BusinessCategory.VIDEOGRAF, "Filmare de eveniment"),
    VIDEOGRAF_FILMARE_CU_DRONA(BusinessCategory.VIDEOGRAF, "Filmare cu drona"),
    VIDEOGRAF_CLIPURI_DE_PREZENTARE(BusinessCategory.VIDEOGRAF, "Clipuri de prezentare"),
    VIDEOGRAF_TRANSMISIE_LIVE(BusinessCategory.VIDEOGRAF, "Transmisie live"),
    VIDEOGRAF_VIDEO_BOOTH_360(BusinessCategory.VIDEOGRAF, "Video booth 360°"),
    VIDEOGRAF_ALT_SERVICIU(BusinessCategory.VIDEOGRAF, "Alt serviciu"),
    DJ_DJ_DE_EVENIMENT(BusinessCategory.DJ, "DJ de eveniment"),
    DJ_MC_PREZENTATOR(BusinessCategory.DJ, "MC / Prezentator"),
    DJ_SONORIZARE(BusinessCategory.DJ, "Sonorizare"),
    DJ_LUMINI_PENTRU_EVENIMENTE(BusinessCategory.DJ, "Lumini pentru evenimente"),
    DJ_ALT_SERVICIU(BusinessCategory.DJ, "Alt serviciu"),
    MUZICA_LIVE_SOLIST_SOLISTA(BusinessCategory.MUZICA_LIVE, "Solist / Solistă"),
    MUZICA_LIVE_FORMATIE(BusinessCategory.MUZICA_LIVE, "Formație"),
    MUZICA_LIVE_INSTRUMENTIST(BusinessCategory.MUZICA_LIVE, "Instrumentist"),
    MUZICA_LIVE_ANSAMBLU_FOLCLORIC(BusinessCategory.MUZICA_LIVE, "Ansamblu folcloric"),
    MUZICA_LIVE_CVARTET_ANSAMBLU_INSTRUMENTAL(BusinessCategory.MUZICA_LIVE, "Cvartet / Ansamblu instrumental"),
    MUZICA_LIVE_COR(BusinessCategory.MUZICA_LIVE, "Cor"),
    MUZICA_LIVE_ALT_SERVICIU(BusinessCategory.MUZICA_LIVE, "Alt serviciu"),
    RESTAURANT_SALON_DE_EVENIMENTE(BusinessCategory.RESTAURANT, "Salon de evenimente"),
    RESTAURANT_RESTAURANT_CU_SALA_PRIVATA(BusinessCategory.RESTAURANT, "Restaurant cu sală privată"),
    RESTAURANT_TERASA_PENTRU_EVENIMENTE(BusinessCategory.RESTAURANT, "Terasă pentru evenimente"),
    RESTAURANT_ALT_SERVICIU(BusinessCategory.RESTAURANT, "Alt serviciu"),
    CANDY_BAR_CANDY_BAR(BusinessCategory.CANDY_BAR, "Candy bar"),
    CANDY_BAR_TORTURI_PERSONALIZATE(BusinessCategory.CANDY_BAR, "Torturi personalizate"),
    CANDY_BAR_PRAJITURI_SI_DESERTURI(BusinessCategory.CANDY_BAR, "Prăjituri și deserturi"),
    CANDY_BAR_GELATO_INGHETATA(BusinessCategory.CANDY_BAR, "Gelato / Înghețată"),
    CANDY_BAR_FANTANA_DE_CIOCOLATA(BusinessCategory.CANDY_BAR, "Fântână de ciocolată"),
    CANDY_BAR_ALT_SERVICIU(BusinessCategory.CANDY_BAR, "Alt serviciu"),
    CATERING_MENIU_COMPLET(BusinessCategory.CATERING, "Meniu complet"),
    CATERING_BUFET_SUEDEZ(BusinessCategory.CATERING, "Bufet suedez"),
    CATERING_FINGER_FOOD(BusinessCategory.CATERING, "Finger food"),
    CATERING_LIVE_COOKING_GRATAR(BusinessCategory.CATERING, "Live cooking / Grătar"),
    CATERING_COCKTAIL_BAR(BusinessCategory.CATERING, "Cocktail bar"),
    CATERING_COFFEE_BAR(BusinessCategory.CATERING, "Coffee bar"),
    CATERING_ALT_SERVICIU(BusinessCategory.CATERING, "Alt serviciu"),
    FLORIST_BUCHETE(BusinessCategory.FLORIST, "Buchete"),
    FLORIST_ARANJAMENTE_PENTRU_MESE(BusinessCategory.FLORIST, "Aranjamente pentru mese"),
    FLORIST_ARCADE_FLORALE(BusinessCategory.FLORIST, "Arcade florale"),
    FLORIST_LUMANARI_DE_EVENIMENT(BusinessCategory.FLORIST, "Lumânări de eveniment"),
    FLORIST_COCARDE_SI_BRATARI_FLORALE(BusinessCategory.FLORIST, "Cocarde și brățări florale"),
    FLORIST_ALT_SERVICIU(BusinessCategory.FLORIST, "Alt serviciu"),
    DECOR_DECOR_CU_BALOANE(BusinessCategory.DECOR, "Decor cu baloane"),
    DECOR_DECOR_TEMATIC(BusinessCategory.DECOR, "Decor tematic"),
    DECOR_PANOU_FOTO_PHOTO_CORNER(BusinessCategory.DECOR, "Panou foto / Photo corner"),
    DECOR_ARANJAREA_MESELOR(BusinessCategory.DECOR, "Aranjarea meselor"),
    DECOR_INCHIRIERE_MOBILIER_SI_ACCESORII(BusinessCategory.DECOR, "Închiriere mobilier și accesorii"),
    DECOR_LITERE_LUMINOASE(BusinessCategory.DECOR, "Litere luminoase"),
    DECOR_ALT_SERVICIU(BusinessCategory.DECOR, "Alt serviciu"),
    DIVERTISMENT_ANIMATORI_PENTRU_COPII(BusinessCategory.DIVERTISMENT, "Animatori pentru copii"),
    DIVERTISMENT_MAGICIENI(BusinessCategory.DIVERTISMENT, "Magicieni"),
    DIVERTISMENT_DANSATORI(BusinessCategory.DIVERTISMENT, "Dansatori"),
    DIVERTISMENT_URSITOARE(BusinessCategory.DIVERTISMENT, "Ursitoare"),
    DIVERTISMENT_MASCOTE(BusinessCategory.DIVERTISMENT, "Mascote"),
    DIVERTISMENT_FACE_PAINTING(BusinessCategory.DIVERTISMENT, "Face painting"),
    DIVERTISMENT_CARICATURI(BusinessCategory.DIVERTISMENT, "Caricaturi"),
    DIVERTISMENT_ALT_SERVICIU(BusinessCategory.DIVERTISMENT, "Alt serviciu"),
    LOCATII_SALA_DE_EVENIMENTE(BusinessCategory.LOCATII, "Sală de evenimente"),
    LOCATII_GRADINA_LOCATIE_IN_AER_LIBER(BusinessCategory.LOCATII, "Grădină / Locație în aer liber"),
    LOCATII_CORT_PENTRU_EVENIMENTE(BusinessCategory.LOCATII, "Cort pentru evenimente"),
    LOCATII_DOMENIU_CONAC(BusinessCategory.LOCATII, "Domeniu / Conac"),
    LOCATII_HOTEL_PENSIUNE(BusinessCategory.LOCATII, "Hotel / Pensiune"),
    LOCATII_SPATIU_PENTRU_CONFERINTE(BusinessCategory.LOCATII, "Spațiu pentru conferințe"),
    LOCATII_ALT_SERVICIU(BusinessCategory.LOCATII, "Alt serviciu"),
    ORGANIZATOR_ORGANIZARE_COMPLETA(BusinessCategory.ORGANIZATOR, "Organizare completă"),
    ORGANIZATOR_ORGANIZARE_PARTIALA(BusinessCategory.ORGANIZATOR, "Organizare parțială"),
    ORGANIZATOR_COORDONARE_IN_ZIUA_EVENIMENTULUI(BusinessCategory.ORGANIZATOR, "Coordonare în ziua evenimentului"),
    ORGANIZATOR_CONSULTANTA(BusinessCategory.ORGANIZATOR, "Consultanță"),
    ORGANIZATOR_ALT_SERVICIU(BusinessCategory.ORGANIZATOR, "Alt serviciu"),
    TRANSPORT_MASINA_CU_SOFER(BusinessCategory.TRANSPORT, "Mașină cu șofer"),
    TRANSPORT_LIMUZINA(BusinessCategory.TRANSPORT, "Limuzină"),
    TRANSPORT_MASINA_DE_EPOCA(BusinessCategory.TRANSPORT, "Mașină de epocă"),
    TRANSPORT_MICROBUZ(BusinessCategory.TRANSPORT, "Microbuz"),
    TRANSPORT_AUTOCAR(BusinessCategory.TRANSPORT, "Autocar"),
    TRANSPORT_CALEASCA(BusinessCategory.TRANSPORT, "Caleașcă"),
    TRANSPORT_ALT_SERVICIU(BusinessCategory.TRANSPORT, "Alt serviciu"),
    ARTIFICII_FOC_DE_ARTIFICII(BusinessCategory.ARTIFICII, "Foc de artificii"),
    ARTIFICII_ARTIFICII_DE_SCENA_SCANTEI_RECI(BusinessCategory.ARTIFICII, "Artificii de scenă / Scântei reci"),
    ARTIFICII_FUM_GREU(BusinessCategory.ARTIFICII, "Fum greu"),
    ARTIFICII_CONFETTI_SI_SERPENTINE(BusinessCategory.ARTIFICII, "Confetti și serpentine"),
    ARTIFICII_ALT_SERVICIU(BusinessCategory.ARTIFICII, "Alt serviciu"),
    ALTELE_INVITATII_SI_PAPETARIE(BusinessCategory.ALTELE, "Invitații și papetărie"),
    ALTELE_MARTURII_SI_CADOURI(BusinessCategory.ALTELE, "Mărturii și cadouri"),
    ALTELE_MAKE_UP(BusinessCategory.ALTELE, "Make-up"),
    ALTELE_COAFURA(BusinessCategory.ALTELE, "Coafură"),
    ALTELE_INCHIRIERE_ECHIPAMENTE(BusinessCategory.ALTELE, "Închiriere echipamente"),
    ALTELE_PERSONAL_PENTRU_EVENIMENTE(BusinessCategory.ALTELE, "Personal pentru evenimente"),
    ALTELE_ALT_SERVICIU(BusinessCategory.ALTELE, "Alt serviciu");

    private final BusinessCategory category;
    private final String displayName;

    BusinessServiceType(BusinessCategory category, String displayName) {
        this.category = category;
        this.displayName = displayName;
    }

    public boolean isOther() {
        return name().endsWith("_ALT_SERVICIU");
    }

    public static List<BusinessServiceType> forCategory(BusinessCategory category) {
        return Arrays.stream(values()).filter(type -> type.category == category).toList();
    }
}

