package de.mindfeat.app.model.vermittler;

import de.mindfeat.app.model.datengrundlage.ZentraleDatenbank;
import de.mindfeat.app.model.datengrundlage.dao.EssensplanDAO;
import de.mindfeat.app.model.datengrundlage.dao.InhaltEssensplanDAO;
import de.mindfeat.app.model.datengrundlage.dao.RezeptKategorieDAO;
import de.mindfeat.app.model.datengrundlage.dao.RezeptZutatDAO;
import de.mindfeat.app.model.datengrundlage.entitaeten.Essensplan;
import de.mindfeat.app.model.datengrundlage.entitaeten.InhaltEssensplan;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;
import de.mindfeat.app.model.datengrundlage.entitaeten.RezeptKategorie;
import de.mindfeat.app.model.datengrundlage.entitaeten.RezeptZutat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Vermittelt zwischen der Essensplan-Fachlogik und den Room-DAOs.
 * Die Auswahl von Vorschlägen bleibt zufällig; Kategorien und
 * Zutatenüberschneidungen schränken sie nur auf ausdrücklichen Wunsch ein.
 */
public class EssensplanVermittler {

    private final ZentraleDatenbank datenbank;
    private final EssensplanDAO essensplanDAO;
    private final InhaltEssensplanDAO inhaltEssensplanDAO;
    private final RezeptKategorieDAO rezeptKategorieDAO;
    private final RezeptZutatDAO rezeptZutatDAO;
    private final RezeptVermittler rezeptVermittler;

    public EssensplanVermittler(ZentraleDatenbank datenbank) {
        if (datenbank == null) {
            throw new IllegalArgumentException("Eine Datenbank muss gewählt werden.");
        }
        this.datenbank = datenbank;
        essensplanDAO = datenbank.essensplanDAO();
        inhaltEssensplanDAO = datenbank.inhaltEssensplanDAO();
        rezeptKategorieDAO = datenbank.rezeptKategorieDAO();
        rezeptZutatDAO = datenbank.rezeptZutatDAO();
        rezeptVermittler = new RezeptVermittler(datenbank);
    }

    // Einen neuen Essensplan anlegen.
    public int anlegen(Essensplan essensplan) {
        if (essensplan == null) {
            throw new IllegalArgumentException("Ein Essensplan muss angegeben werden.");
        }
        long id = essensplanDAO.einfuegen(essensplan);
        essensplan.setId((int) id);
        return (int) id;
    }

    // Einen vorhandenen Essensplan bearbeiten.
    public void bearbeiten(Essensplan essensplan) {
        if (essensplan == null || essensplan.getId() < 1) {
            throw new IllegalArgumentException("Ein gültiger Essensplan muss angegeben werden.");
        }
        if (essensplanDAO.nachIdAbrufen(essensplan.getId()) == null) {
            throw new IllegalArgumentException("Der Essensplan wurde nicht gefunden.");
        }
        if (essensplanDAO.aktualisieren(essensplan) == 0) {
            throw new IllegalStateException("Der Essensplan konnte nicht aktualisiert werden.");
        }
    }

    // Alle Essenspläne abrufen.
    public List<Essensplan> alleAbrufen() {
        return essensplanDAO.alleAbrufen();
    }

    // Einen Essensplan samt chronologisch sortierter Rezepte abrufen.
    public EssensplanDetails nachIdMitDetailsAbrufen(int essensplanId) {
        pruefeId(essensplanId, "Essensplan");
        Essensplan essensplan = essensplanDAO.nachIdAbrufen(essensplanId);
        if (essensplan == null) {
            return null;
        }
        List<InhaltMitRezept> inhalte = new ArrayList<>();
        for (InhaltEssensplan inhalt : inhaltEssensplanDAO.nachEssensplanIdAbrufen(essensplanId)) {
            RezeptVermittler.RezeptDetails rezeptDetails =
                    rezeptVermittler.nachIdMitDetailsAbrufen(inhalt.getRezeptId());
            if (rezeptDetails != null) {
                inhalte.add(new InhaltMitRezept(inhalt, rezeptDetails));
            }
        }
        return new EssensplanDetails(essensplan, inhalte);
    }

    // Ein Rezept einem Essensplan und Datum zuordnen.
    public int inhaltHinzufuegen(int essensplanId, int rezeptId, String datum) {
        pruefeId(essensplanId, "Essensplan");
        pruefeId(rezeptId, "Rezept");
        Essensplan essensplan = essensplanDAO.nachIdAbrufen(essensplanId);
        if (essensplan == null) {
            throw new IllegalArgumentException("Der Essensplan wurde nicht gefunden.");
        }
        if (rezeptVermittler.nachIdMitDetailsAbrufen(rezeptId) == null) {
            throw new IllegalArgumentException("Das Rezept wurde nicht gefunden.");
        }
        LocalDate ausgewaehltesDatum = parseDatum(datum);
        LocalDate start = LocalDate.parse(essensplan.getStartDatum());
        LocalDate ende = LocalDate.parse(essensplan.getEndDatum());
        if (ausgewaehltesDatum.isBefore(start) || ausgewaehltesDatum.isAfter(ende)) {
            throw new IllegalArgumentException("Das Datum muss innerhalb des Essensplan-Zeitraums liegen.");
        }
        InhaltEssensplan inhalt = new InhaltEssensplan(essensplanId, rezeptId, datum);
        return (int) inhaltEssensplanDAO.einfuegen(inhalt);
    }

    // Eine Rezeptzuordnung aus dem Essensplan entfernen.
    public boolean inhaltEntfernen(int inhaltId) {
        pruefeId(inhaltId, "Essensplaneintrag");
        InhaltEssensplan inhalt = inhaltEssensplanDAO.nachIdAbrufen(inhaltId);
        return inhalt != null && inhaltEssensplanDAO.loeschen(inhalt) > 0;
    }

    // Einen Essensplan löschen. Zugehörige Einträge werden durch CASCADE entfernt.
    public boolean loeschen(int essensplanId) {
        pruefeId(essensplanId, "Essensplan");
        Essensplan essensplan = essensplanDAO.nachIdAbrufen(essensplanId);
        return essensplan != null && essensplanDAO.loeschen(essensplan) > 0;
    }

    /**
     * Liefert alle passenden Rezepte in zufälliger Reihenfolge.
     * Leere/null Kategorien bedeuten: kein Kategorienfilter.
     * Bei mehreren Kategorien genügt eine Übereinstimmung (ODER).
     * Der Nachhaltigkeitsfilter verlangt mindestens eine Zutat, die bereits
     * in einem anderen Eintrag desselben Essensplans vorkommt.
     */
    public List<RezeptVermittler.RezeptDetails> vorschlaegeErmitteln(
            int essensplanId, List<Integer> kategorieIds, boolean nachhaltigkeitAktiv) {
        pruefeId(essensplanId, "Essensplan");
        if (essensplanDAO.nachIdAbrufen(essensplanId) == null) {
            throw new IllegalArgumentException("Der Essensplan wurde nicht gefunden.");
        }

        Set<Integer> ausgewaehlteKategorien = new HashSet<>();
        if (kategorieIds != null) {
            for (Integer kategorieId : kategorieIds) {
                if (kategorieId == null || kategorieId < 1) {
                    throw new IllegalArgumentException("Eine ausgewählte Kategorie ist ungültig.");
                }
                ausgewaehlteKategorien.add(kategorieId);
            }
        }

        Set<Integer> vorhandeneZutaten = new HashSet<>();
        if (nachhaltigkeitAktiv) {
            for (InhaltEssensplan inhalt : inhaltEssensplanDAO.nachEssensplanIdAbrufen(essensplanId)) {
                for (RezeptZutat rezeptZutat : rezeptZutatDAO.nachRezeptIdAbrufen(inhalt.getRezeptId())) {
                    vorhandeneZutaten.add(rezeptZutat.getZutatId());
                }
            }
        }

        List<RezeptVermittler.RezeptDetails> passendeRezepte = new ArrayList<>();
        for (Rezept rezept : rezeptVermittler.alleAbrufen()) {
            RezeptVermittler.RezeptDetails details =
                    rezeptVermittler.nachIdMitDetailsAbrufen(rezept.getId());
            if (details == null) {
                continue;
            }

            if (!ausgewaehlteKategorien.isEmpty()
                    && !hatEineKategorie(details, ausgewaehlteKategorien)) {
                continue;
            }

            if (nachhaltigkeitAktiv
                    && !hatGemeinsameZutat(details, vorhandeneZutaten)) {
                continue;
            }
            passendeRezepte.add(details);
        }

        Collections.shuffle(passendeRezepte);
        return passendeRezepte;
    }

    // Einen einzelnen zufälligen Vorschlag liefern; null, falls keiner passt.
    public RezeptVermittler.RezeptDetails zufaelligenVorschlagErmitteln(
            int essensplanId, List<Integer> kategorieIds, boolean nachhaltigkeitAktiv) {
        List<RezeptVermittler.RezeptDetails> vorschlaege =
                vorschlaegeErmitteln(essensplanId, kategorieIds, nachhaltigkeitAktiv);
        return vorschlaege.isEmpty() ? null : vorschlaege.get(0);
    }

    private boolean hatEineKategorie(RezeptVermittler.RezeptDetails rezept,
                                     Set<Integer> ausgewaehlteKategorien) {
        for (RezeptKategorie zuordnung : rezept.getRezeptKategorien()) {
            if (ausgewaehlteKategorien.contains(zuordnung.getKategorieId())) {
                return true;
            }
        }
        return false;
    }

    private boolean hatGemeinsameZutat(RezeptVermittler.RezeptDetails rezept,
                                       Set<Integer> vorhandeneZutaten) {
        for (RezeptZutat zuordnung : rezept.getRezeptZutaten()) {
            if (vorhandeneZutaten.contains(zuordnung.getZutatId())) {
                return true;
            }
        }
        return false;
    }

    private LocalDate parseDatum(String datum) {
        if (datum == null || datum.trim().isEmpty()) {
            throw new IllegalArgumentException("Bitte gib ein Datum an.");
        }
        try {
            return LocalDate.parse(datum);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Bitte gib ein gültiges Datum im Format JJJJ-MM-TT an.", exception);
        }
    }

    private void pruefeId(int id, String bezeichnung) {
        if (id < 1) {
            throw new IllegalArgumentException("Es muss ein gültiger " + bezeichnung + " ausgewählt sein.");
        }
    }

    public static class EssensplanDetails {
        private final Essensplan essensplan;
        private final List<InhaltMitRezept> inhalte;

        private EssensplanDetails(Essensplan essensplan, List<InhaltMitRezept> inhalte) {
            this.essensplan = essensplan;
            this.inhalte = inhalte;
        }

        public Essensplan getEssensplan() {
            return essensplan;
        }

        public List<InhaltMitRezept> getInhalte() {
            return Collections.unmodifiableList(inhalte);
        }
    }

    public static class InhaltMitRezept {
        private final InhaltEssensplan inhalt;
        private final RezeptVermittler.RezeptDetails rezeptDetails;

        private InhaltMitRezept(InhaltEssensplan inhalt,
                                RezeptVermittler.RezeptDetails rezeptDetails) {
            this.inhalt = inhalt;
            this.rezeptDetails = rezeptDetails;
        }

        public InhaltEssensplan getInhalt() {
            return inhalt;
        }

        public RezeptVermittler.RezeptDetails getRezeptDetails() {
            return rezeptDetails;
        }
    }
}
