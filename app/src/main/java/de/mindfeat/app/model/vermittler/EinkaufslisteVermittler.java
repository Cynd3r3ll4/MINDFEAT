package de.mindfeat.app.model.vermittler;

import de.mindfeat.app.model.datengrundlage.ZentraleDatenbank;
import de.mindfeat.app.model.datengrundlage.dao.EinkaufslistenbeitragDAO;
import de.mindfeat.app.model.datengrundlage.dao.EinkaufslistenelementDAO;
import de.mindfeat.app.model.datengrundlage.dao.ZutatDAO;

import de.mindfeat.app.model.datengrundlage.entitaeten.Einkaufslistenbeitrag;
import de.mindfeat.app.model.datengrundlage.entitaeten.Einkaufslistenelement;
import de.mindfeat.app.model.datengrundlage.entitaeten.RezeptZutat;
import de.mindfeat.app.model.datengrundlage.entitaeten.Zutat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class EinkaufslisteVermittler {

    private final EinkaufslistenelementDAO einkaufslistenelementDAO;
    private final EinkaufslistenbeitragDAO einkaufslistenbeitragDAO;
    private final ZutatDAO zutatDAO;
    private final EssensplanVermittler essensplanVermittler;
    private final ZentraleDatenbank datenbank;


    // Konstruktor bezieht benötigte DAOs aus der zentralen Datenbank
    public EinkaufslisteVermittler(ZentraleDatenbank datenbank) {
        if (datenbank == null) {
            throw new IllegalArgumentException("Eine Datenbank muss gewählt werden.");
        }
        this.datenbank = datenbank;
        einkaufslistenelementDAO = datenbank.einkaufslistenelementDAO();
        einkaufslistenbeitragDAO = datenbank.einkaufslistenbeitragDAO();
        zutatDAO = datenbank.zutatDAO();
        essensplanVermittler = new EssensplanVermittler(datenbank);
    }

    // Die gesamte Einkaufsliste einschließlich Zutaten und Beiträgen abrufen
    public List<EinkaufslistenDetails> alleAbrufen() {
        List<Einkaufslistenelement> elemente = einkaufslistenelementDAO.alleAbrufen();
        List<EinkaufslistenDetails> ergebnis = new ArrayList<>();
        for (Einkaufslistenelement element : elemente) {
            Zutat zutat = zutatDAO.nachIdAbrufen(element.getZutatId());
            List<Einkaufslistenbeitrag> beitraege = einkaufslistenbeitragDAO.nachEinkaufslistenelementIdAbrufen(element.getId());
            if (zutat != null) {
                ergebnis.add(new EinkaufslistenDetails(element, zutat, beitraege));
            }
        }
        return ergebnis;
    }

    // Ein neues Element manuell zur Einkaufsliste hinzufügen inkl. Berechnung der Menge
    public int manuellHinzufuegen(String zutatName, Double menge, String einheit) {
        if (zutatName == null || zutatName.trim().isEmpty()) {
            throw new IllegalArgumentException("Bitte gib einen Namen für das Element an.");
        }
        if (menge != null && (!Double.isFinite(menge) || menge <= 0)) {
            throw new IllegalArgumentException("Bitte gib eine Menge ein.");
        }
        String name = zutatName.trim();
        String einheitBereinigt = einheit == null || einheit.trim().isEmpty() ? null : einheit.trim();
        final int[] elementId = new int[1];
        datenbank.runInTransaction(() -> {
            Zutat zutat = zutatDAO.nachNameAbrufen(name); // Vorhandene Zutat wiederverwenden oder neu anlegen
            if (zutat == null) {
                zutat = new Zutat(name);
                long zutatId = zutatDAO.einfuegen(zutat);
                zutat.setId((int) zutatId);
            }
            Einkaufslistenelement element = elementFinden(zutat.getId(), einheitBereinigt); // Vorhandenes Element mit derselben Zutat und Einheit suchen
            if (element == null) {
                element = new Einkaufslistenelement(zutat.getId(), null, einheitBereinigt, false);
                long id = einkaufslistenelementDAO.einfuegen(element);
                element.setId((int) id);
            }
            // Vorhandenen manuellen Beitrag suchen
            List<Einkaufslistenbeitrag> manuelleBeitraege = einkaufslistenbeitragDAO.nachElementIdUndEssensplanIdAbrufen(element.getId(), null);
            if (manuelleBeitraege.isEmpty()) {
                Einkaufslistenbeitrag beitrag = new Einkaufslistenbeitrag(element.getId(), null, menge); // Neuen manuellen Beitrag speichern
                einkaufslistenbeitragDAO.einfuegen(beitrag);
            } else {
                Einkaufslistenbeitrag beitrag = manuelleBeitraege.get(0); // Vorhandenen manuellen Beitrag ergänzen
                beitrag.setMenge(mengenAddieren(beitrag.getMenge(), menge));
                einkaufslistenbeitragDAO.aktualisieren(beitrag);
            }
            element.setErledigt(false);
            elementAktualisieren(element.getId()); // Gesamtmenge aus allen Beiträgen neu berechnen
            elementId[0] = element.getId();
        });
        return elementId[0];
    }

    // Zutaten eines Essensplans vollständig in die Einkaufsliste übernehmen
    public void ausEssensplanHinzufuegen(int essensplanId) {
        pruefeId(essensplanId, "Essensplan-ID");
        EssensplanVermittler.EssensplanDetails essensplanDetails = essensplanVermittler.nachIdMitDetailsAbrufen(essensplanId);
        if (essensplanDetails == null) {
            throw new IllegalArgumentException("Dieser Essensplan existiert nicht.");
        }
        Map<ZutatEinheitSchluessel, Double> benoetigteZutaten = zutatenMengenErmitteln(essensplanDetails);
        datenbank.runInTransaction(() -> {
            List<Einkaufslistenbeitrag> alteBeitraege = einkaufslistenbeitragDAO.nachEssensplanIdAbrufen(essensplanId); // Bereits vorhandene Beiträge dieses Plans abrufen
            Set<Integer> betroffeneElemente = new HashSet<>(); // Beiträge entfernen, deren Zutaten nicht mehr benötigt werden
            for (Einkaufslistenbeitrag beitrag : alteBeitraege) {
                Einkaufslistenelement element = einkaufslistenelementDAO.nachIdAbrufen(beitrag.getEinkaufslistenelementId());
                if (element == null) {
                    continue;
                }
                ZutatEinheitSchluessel schluessel = new ZutatEinheitSchluessel(element.getZutatId(), element.getEinheit());
                if (!benoetigteZutaten.containsKey(schluessel)) {
                    einkaufslistenbeitragDAO.loeschen(beitrag);
                    betroffeneElemente.add(element.getId());
                }
            }
            for (Map.Entry<ZutatEinheitSchluessel, Double> eintrag : benoetigteZutaten.entrySet()) { // Alle benötigten Zutaten übernehmen bzw. aktualisieren
                ZutatEinheitSchluessel schluessel = eintrag.getKey();
                double menge = eintrag.getValue();
                Einkaufslistenelement element = elementFinden(schluessel.zutatId, schluessel.einheit); // Passendes Einkaufslistenelement suchen
                if (element == null) {
                    element = new Einkaufslistenelement(schluessel.zutatId, null, schluessel.einheit, false);
                    long id = einkaufslistenelementDAO.einfuegen(element);
                    element.setId((int) id);
                }
                // Prüfen, ob für diesen Plan bereits ein Beitrag existiert
                List<Einkaufslistenbeitrag> vorhandeneBeitraege = einkaufslistenbeitragDAO.nachElementIdUndEssensplanIdAbrufen(element.getId(), essensplanId);
                if (vorhandeneBeitraege.isEmpty()) {
                    Einkaufslistenbeitrag beitrag = new Einkaufslistenbeitrag(element.getId(), essensplanId, menge); // Neuen Beitrag für diesen Essensplan anlegen
                    einkaufslistenbeitragDAO.einfuegen(beitrag);
                } else {
                    Einkaufslistenbeitrag beitrag = vorhandeneBeitraege.get(0); // Bestehenden Beitrag aktualisieren
                    beitrag.setMenge(menge);
                    einkaufslistenbeitragDAO.aktualisieren(beitrag);
                }
                element.setErledigt(false);
                einkaufslistenelementDAO.aktualisieren(element);
                betroffeneElemente.add(element.getId());
            }
            for (Integer elementId : betroffeneElemente) { // Mengen aller betroffenen Elemente neu berechnen
                elementAktualisieren(elementId);
            }
        });
    }

    // Alle Beiträge eines Essensplans aus der Einkaufsliste entfernen
    public void ausEssensplanEntfernen(int essensplanId) {
        pruefeId(essensplanId, "Essensplan-ID");
        datenbank.runInTransaction(() -> {
            List<Einkaufslistenbeitrag> beitraege = einkaufslistenbeitragDAO.nachEssensplanIdAbrufen(essensplanId);
            Set<Integer> betroffeneElemente = new HashSet<>();
            for (Einkaufslistenbeitrag beitrag : beitraege) {
                betroffeneElemente.add(beitrag.getEinkaufslistenelementId());
                einkaufslistenbeitragDAO.loeschen(beitrag);
            }
            for (Integer elementId : betroffeneElemente) {
                elementAktualisieren(elementId);
            }
        });
    }

    // Einen Essensplan inkl. Einkaufslistenbeiträge löschen
    public void essensplanLoeschen(int essensplanId) {
        pruefeId(essensplanId, "Essensplan-ID");
        datenbank.runInTransaction(() -> {
            ausEssensplanEntfernen(essensplanId);
            essensplanVermittler.loeschen(essensplanId);
        });
    }

    // Einen einzelnen Einkaufslistenbeitrag entfernen
    public void beitragEntfernen(int beitragId) {
        pruefeId(beitragId, "Beitrags-ID");
        datenbank.runInTransaction(() -> {
            Einkaufslistenbeitrag beitrag = einkaufslistenbeitragDAO.nachIdAbrufen(beitragId);
            if (beitrag == null) {
                throw new IllegalArgumentException("Diese Mahlzeit liefert keinen Beitrag zur Mengenberechnung.");
            }
            int elementId = beitrag.getEinkaufslistenelementId();
            einkaufslistenbeitragDAO.loeschen(beitrag);
            elementAktualisieren(elementId);
        });
    }

    // Den Erledigt-Status eines Einkaufslistenelements ändern
    public void erledigtSetzen(int elementId, boolean erledigt) {
        pruefeId(elementId, "Element-ID");
        Einkaufslistenelement element = einkaufslistenelementDAO.nachIdAbrufen(elementId);
        if (element == null) {
            throw new IllegalArgumentException("Dieses Einkaufslistenelement existiert nicht.");
        }
        element.setErledigt(erledigt);
        einkaufslistenelementDAO.aktualisieren(element);
    }

    // Ein Einkaufslistenelement vollständig löschen (inkl. zugehöriger Beiträge)
    public void loeschen(int elementId) {
        pruefeId(elementId, "Element-ID");
        Einkaufslistenelement element = einkaufslistenelementDAO.nachIdAbrufen(elementId);
        if (element == null) {
            throw new IllegalArgumentException("Dieses Einkaufslistenelement existiert nicht.");
        }
        einkaufslistenelementDAO.loeschen(element);
    }

    // Die Zutatenmengen eines Essensplans anhand der Planportionen berechnen
    private Map<ZutatEinheitSchluessel, Double> zutatenMengenErmitteln(
            EssensplanVermittler.EssensplanDetails essensplanDetails) {
        Map<ZutatEinheitSchluessel, Double> zutatenMengen = new HashMap<>();
        for (EssensplanVermittler.InhaltMitRezept inhaltMitRezept : essensplanDetails.getEintraege()) {
            int planPortionen = inhaltMitRezept.getEintrag().getPortionen();
            int rezeptPortionen = inhaltMitRezept.getEintrag().getRezeptPortionen();
            if (planPortionen < 1 || rezeptPortionen < 1) {
                throw new IllegalStateException("Die Portionsanzahl eines Planeintrags ist ungültig.");
            }
            double faktor = (double) planPortionen / rezeptPortionen; // Verhältnis zwischen gewünschter Planmenge und ursprünglicher Rezeptmenge berechnen
            for (RezeptZutat rezeptZutat : inhaltMitRezept.getRezeptDetails().getRezeptZutaten()) {
                ZutatEinheitSchluessel schluessel = new ZutatEinheitSchluessel(rezeptZutat.getZutatId(), rezeptZutat.getEinheit());
                Double angepassteMenge = rezeptZutat.getMenge() * faktor;
                if (zutatenMengen.containsKey(schluessel)) {
                    zutatenMengen.compute(schluessel, (k, bisherigeMenge) -> mengenAddieren(bisherigeMenge, angepassteMenge));
                } else {
                    zutatenMengen.put(schluessel, angepassteMenge);
                }
            }
        }

        return zutatenMengen;
    }

    // Ein Einkaufslistenelement anhand von Zutat und Einheit suchen
    private Einkaufslistenelement elementFinden(int zutatId, String einheit) {
        List<Einkaufslistenelement> elemente = einkaufslistenelementDAO.nachZutatIdUndEinheitAbrufen(zutatId, einheit);

        if (elemente.isEmpty()) {
            return null;
        }
        return elemente.get(0);
    }

    // Gesamtmenge eines Elements anhand seiner Beiträge neu berechnen (ohne Beiträge wird das Element entfernt)
    private void elementAktualisieren(int elementId) {
        Einkaufslistenelement element = einkaufslistenelementDAO.nachIdAbrufen(elementId);
        if (element == null) {
            return;
        }
        List<Einkaufslistenbeitrag> beitraege = einkaufslistenbeitragDAO.nachEinkaufslistenelementIdAbrufen(elementId);
        if (beitraege.isEmpty()) {
            einkaufslistenelementDAO.loeschen(element);
            return;
        }
        double gesamtmenge = 0;
        boolean mengeUnbekannt = false;
        for (Einkaufslistenbeitrag beitrag : beitraege) {
            if (beitrag.getMenge() == null) {
                mengeUnbekannt = true;
            } else {
                gesamtmenge += beitrag.getMenge();
            }
        }
        element.setMenge(
                mengeUnbekannt ? null : gesamtmenge);
        einkaufslistenelementDAO.aktualisieren(element);
    }

    // Zwei Mengen addieren (null = unbekannte Menge)
    private Double mengenAddieren(Double menge1, Double menge2) {
        if (menge1 == null || menge2 == null) {
            return null;
        }
        return menge1 + menge2;
    }

    private void pruefeId(int id, String bezeichnung) {
        if (id < 1) {
            throw new IllegalArgumentException(
                    bezeichnung + " muss größer als 0 sein.");
        }
    }

    // Objekt zur Zusammenfassung gleicher Zutaten mit gleicher Einheit
    private static class ZutatEinheitSchluessel {

        private final int zutatId;
        private final String einheit;

        private ZutatEinheitSchluessel(int zutatId, String einheit) {
            this.zutatId = zutatId;
            this.einheit = einheit;
        }

        @Override
        public boolean equals(Object objekt) {
            if (this == objekt) {
                return true;
            }
            if (!(objekt instanceof ZutatEinheitSchluessel)) {
                return false;
            }
            ZutatEinheitSchluessel anderer = (ZutatEinheitSchluessel) objekt;
            return zutatId == anderer.zutatId && Objects.equals(einheit, anderer.einheit);
        }

        @Override
        public int hashCode() {
            return Objects.hash(zutatId, einheit);
        }
    }


    // Ergebnisobjekt für ein Einkaufslistenelement mit seinen Details
    public static class EinkaufslistenDetails {

        private final Einkaufslistenelement element;
        private final Zutat zutat;
        private final List<Einkaufslistenbeitrag> beitraege;

        public EinkaufslistenDetails(Einkaufslistenelement element, Zutat zutat, List<Einkaufslistenbeitrag> beitraege) {
            this.element = element;
            this.zutat = zutat;
            this.beitraege = new ArrayList<>(beitraege);
        }

        public Einkaufslistenelement getElement() {
            return element;
        }

        public Zutat getZutat() {
            return zutat;
        }

        public List<Einkaufslistenbeitrag> getBeitraege() {
            return Collections.unmodifiableList(beitraege);
        }
    }
}
