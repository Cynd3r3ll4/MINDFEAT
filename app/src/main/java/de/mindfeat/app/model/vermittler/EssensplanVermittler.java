package de.mindfeat.app.model.vermittler;

import de.mindfeat.app.model.datengrundlage.ZentraleDatenbank;
import de.mindfeat.app.model.datengrundlage.dao.EssensplanDAO;
import de.mindfeat.app.model.datengrundlage.dao.InhaltEssensplanDAO;
import de.mindfeat.app.model.datengrundlage.entitaeten.Essensplan;
import de.mindfeat.app.model.datengrundlage.entitaeten.InhaltEssensplan;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;
import de.mindfeat.app.model.datengrundlage.entitaeten.RezeptKategorie;
import de.mindfeat.app.model.datengrundlage.entitaeten.RezeptZutat;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EssensplanVermittler {

    private final EssensplanDAO essensplanDAO;
    private final InhaltEssensplanDAO inhaltEssensplanDAO;
    private final RezeptVermittler rezeptVermittler;

    public EssensplanVermittler(ZentraleDatenbank datenbank) {
        if (datenbank == null) {
            throw new IllegalArgumentException("Eine Datenbank muss gewählt werden.");
        }
        this.essensplanDAO = datenbank.essensplanDAO();
        this.inhaltEssensplanDAO = datenbank.inhaltEssensplanDAO();
        this.rezeptVermittler = new RezeptVermittler(datenbank);
    }

    // Einen neuen Essensplan vollständig anlegen
    public long anlegen(Essensplan essensplan) {
        if (essensplan == null) {
            throw new IllegalArgumentException("Ein Essensplan muss ausgewählt sein.");
        }
        long id = essensplanDAO.einfuegen(essensplan);
        essensplan.setId((int) id);
        return id;
    }

    // Einen vorhandenen Plan bearbeiten
    public void bearbeiten(Essensplan essensplan) {
        if (essensplan == null) {
            throw new IllegalArgumentException("Ein Essensplan muss ausgewählt sein.");
        }
        if (essensplan.getId() < 1
                || essensplanDAO.nachIdAbrufen(essensplan.getId()) == null) {
            throw new IllegalArgumentException("Dieser Essensplan existiert nicht.");
        }
        LocalDate startDatum = parseDatum(essensplan.getStartDatum());
        LocalDate endDatum = parseDatum(essensplan.getEndDatum());
        List<InhaltEssensplan> eintraege = inhaltEssensplanDAO.nachEssensplanIdAbrufen(essensplan.getId());
        for (InhaltEssensplan eintrag : eintraege) {
            LocalDate eintragsDatum = parseDatum(eintrag.getDatum());
            if (eintragsDatum.isBefore(startDatum)
                    || eintragsDatum.isAfter(endDatum)) {
                throw new IllegalArgumentException(
                        "Der Zeitraum kann nicht geändert werden, da dadurch bestehende Essenseinträge außerhalb des Plan-Zeitraums liegen würden.");
            }
        }
        essensplanDAO.aktualisieren(essensplan);
    }

    //Liste aller gespeicherten Pläne
    public List<Essensplan> alleAbrufen() {
        return essensplanDAO.alleAbrufen();
    }

    // Einen Plan inkl. Mahlzeiten und Rezeptdetails aufrufen
    public EssensplanDetails nachIdMitDetailsAbrufen(int essensplanId) {
        pruefeId(essensplanId, "Essensplan-ID");
        Essensplan essensplan = essensplanDAO.nachIdAbrufen(essensplanId);
        if (essensplan == null) {
            return null;
        }
        List<InhaltEssensplan> eintraege = inhaltEssensplanDAO.nachEssensplanIdAbrufen(essensplanId);
        List<InhaltMitRezept> eintraegeMitRezept = new ArrayList<>();
        for (InhaltEssensplan eintrag : eintraege) {
            RezeptVermittler.RezeptDetails rezeptDetails = rezeptVermittler.nachIdMitDetailsAbrufen(eintrag.getRezeptId());
            if (rezeptDetails != null) {
                eintraegeMitRezept.add(new InhaltMitRezept(eintrag, rezeptDetails));
            }
        }
        return new EssensplanDetails(essensplan, eintraegeMitRezept);
    }

    // EIn Rezept an einem bestimmten Datum dem Plan hinzufügen
    public long inhaltHinzufuegen(int essensplanId, int rezeptId, String datum) {
        pruefeId(essensplanId, "Essensplan-ID");
        pruefeId(rezeptId, "Rezept-ID");
        Essensplan essensplan = essensplanDAO.nachIdAbrufen(essensplanId);
        if (essensplan == null) {
            throw new IllegalArgumentException("Dieser Essensplan existiert nicht.");
        }
        if (rezeptVermittler.nachIdMitDetailsAbrufen(rezeptId) == null) {
            throw new IllegalArgumentException("Dieses Rezept existiert nicht.");
        }
        LocalDate eintragsDatum = parseDatum(datum);
        LocalDate startDatum = parseDatum(essensplan.getStartDatum());
        LocalDate endDatum = parseDatum(essensplan.getEndDatum());
        if (eintragsDatum.isBefore(startDatum)
                || eintragsDatum.isAfter(endDatum)) {
            throw new IllegalArgumentException("Das Datum liegt außerhalb des Essensplanzeitraums.");
        }
        InhaltEssensplan inhaltEssensplan = new InhaltEssensplan(essensplanId, rezeptId, datum);
        return inhaltEssensplanDAO.einfuegen(inhaltEssensplan);
    }

    // Einen einzelnen Essensplan-Eintrag entfernen
    public void inhaltEntfernen(int eintragId) {
        pruefeId(eintragId, "Eintrags-ID");
        InhaltEssensplan eintrag = inhaltEssensplanDAO.nachIdAbrufen(eintragId);
        if (eintrag == null) {
            throw new IllegalArgumentException("Dieser Essensplaneintrag existiert nicht.");
        }
        inhaltEssensplanDAO.loeschen(eintrag);
    }

    // Einen Essensplan inkl. seiner Einträge löschen
    public void loeschen(int essensplanId) {
        pruefeId(essensplanId, "Essensplan-ID");
        Essensplan essensplan = essensplanDAO.nachIdAbrufen(essensplanId);
        if (essensplan == null) {
            throw new IllegalArgumentException("Dieser Essensplan existiert nicht.");
        }
        essensplanDAO.loeschen(essensplan);
    }

    // Einen Vorschlag für den gesamten Essensplan ermitteln (Default: random, ergänzt durch Kategorie-Filter + Nachhaltigkeit)
    public List<RezeptVermittler.RezeptDetails> vorschlaegeErmitteln(int essensplanId, List<Integer> kategorieIds, boolean nachhaltigkeitAktiviert) {
        pruefeId(essensplanId, "Essensplan-ID");
        if (essensplanDAO.nachIdAbrufen(essensplanId) == null) {
            throw new IllegalArgumentException("Dieser Essensplan existiert nicht.");
        }
        Set<Integer> ausgewaehlteKategorien = new HashSet<>();
        if (kategorieIds != null) {
            for (Integer kategorieId : kategorieIds) {
                if (kategorieId == null || kategorieId < 1) {
                    throw new IllegalArgumentException("Eine gültige Kategorie muss ausgewählt sein.");
                }
                ausgewaehlteKategorien.add(kategorieId);
            }
        }
        Set<Integer> bereitsVerwendeteZutaten = new HashSet<>();
        if (nachhaltigkeitAktiviert) {
            List<InhaltEssensplan> vorhandeneEintraege = inhaltEssensplanDAO.nachEssensplanIdAbrufen(essensplanId);
            for (InhaltEssensplan eintrag : vorhandeneEintraege) {
                RezeptVermittler.RezeptDetails details = rezeptVermittler.nachIdMitDetailsAbrufen(eintrag.getRezeptId());
                if (details == null) {
                    continue;
                }
                for (RezeptZutat rezeptZutat : details.getRezeptZutaten()) {
                    bereitsVerwendeteZutaten.add(rezeptZutat.getZutatId());
                }
            }
        }
        List<RezeptVermittler.RezeptDetails> vorschlaege = new ArrayList<>();
        List<Rezept> alleRezepte = rezeptVermittler.alleAbrufen();
        for (Rezept rezept : alleRezepte) {
            RezeptVermittler.RezeptDetails details = rezeptVermittler.nachIdMitDetailsAbrufen(rezept.getId());
            if (details == null) {
                continue;
            }
            boolean kategorieErfuellt = ausgewaehlteKategorien.isEmpty()
                            || hatEineKategorie(details, ausgewaehlteKategorien);
            boolean nachhaltigkeitErfuellt = !nachhaltigkeitAktiviert
                            || hatGemeinsameZutat(details, bereitsVerwendeteZutaten);
            if (kategorieErfuellt && nachhaltigkeitErfuellt) {
                vorschlaege.add(details);
            }
        }
        Collections.shuffle(vorschlaege);
        return vorschlaege;
    }

    // Einen Vorschlag für ein einzelnes Rezept machen
    public RezeptVermittler.RezeptDetails zufaelligenVorschlagErmitteln(int essensplanId, List<Integer> kategorieIds, boolean nachhaltigkeitAktiviert) {
        List<RezeptVermittler.RezeptDetails> vorschlaege = vorschlaegeErmitteln(essensplanId, kategorieIds, nachhaltigkeitAktiviert);
        if (vorschlaege.isEmpty()) {
            return null;
        }
        return vorschlaege.get(0);
    }

    // Ein Rezept mit min. einer Kategorie (mehrere werden mit ODER verknüpft)
    private boolean hatEineKategorie(RezeptVermittler.RezeptDetails rezeptDetails, Set<Integer> kategorieIds) {
        for (RezeptKategorie rezeptKategorie
                : rezeptDetails.getRezeptKategorien()) {
            if (kategorieIds.contains(rezeptKategorie.getKategorieId())) {
                return true;
            }
        }
        return false;
    }

    // Ein Rezept mit min. einer bereits verwendeten Zutat
    private boolean hatGemeinsameZutat(RezeptVermittler.RezeptDetails rezeptDetails, Set<Integer> verwendeteZutaten) {
        for (RezeptZutat rezeptZutat : rezeptDetails.getRezeptZutaten()) {
            if (verwendeteZutaten.contains(rezeptZutat.getZutatId())) {
                return true;
            }
        }
        return false;
    }


    private LocalDate parseDatum(String datum) {
        try {
            return LocalDate.parse(datum);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("Das Datum muss im Format JJJJ-MM-TT angegeben werden.", e);
        }
    }

    private void pruefeId(int id, String bezeichnung) {
        if (id < 1) {
            throw new IllegalArgumentException(bezeichnung + " muss größer als 0 sein.");
        }
    }

    // Objekt für einen Essensplan inkl. Rezeptdetails
    public static class EssensplanDetails {
        private final Essensplan essensplan;
        private final List<InhaltMitRezept> eintraege;

        public EssensplanDetails(Essensplan essensplan, List<InhaltMitRezept> eintraege) {
            this.essensplan = essensplan;
            this.eintraege = new ArrayList<>(eintraege);
        }

        public Essensplan getEssensplan() {
            return essensplan;
        }

        public List<InhaltMitRezept> getEintraege() {
            return Collections.unmodifiableList(eintraege);
        }
    }

    // Ein Plan-Eintrag mit vollständigen Rezept-Details
    public static class InhaltMitRezept {
        private final InhaltEssensplan eintrag;
        private final RezeptVermittler.RezeptDetails rezeptDetails;

        public InhaltMitRezept(InhaltEssensplan eintrag, RezeptVermittler.RezeptDetails rezeptDetails) {
            this.eintrag = eintrag;
            this.rezeptDetails = rezeptDetails;
        }

        public InhaltEssensplan getEintrag() {
            return eintrag;
        }

        public RezeptVermittler.RezeptDetails getRezeptDetails() {
            return rezeptDetails;
        }
    }
}
