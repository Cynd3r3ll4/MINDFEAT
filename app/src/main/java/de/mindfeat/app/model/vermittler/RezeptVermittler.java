package de.mindfeat.app.model.vermittler;

import de.mindfeat.app.model.datengrundlage.ZentraleDatenbank;
import de.mindfeat.app.model.datengrundlage.dao.KategorieDAO;
import de.mindfeat.app.model.datengrundlage.dao.RezeptDAO;
import de.mindfeat.app.model.datengrundlage.dao.RezeptKategorieDAO;
import de.mindfeat.app.model.datengrundlage.dao.RezeptZutatDAO;
import de.mindfeat.app.model.datengrundlage.dao.ZubereitungsschrittDAO;
import de.mindfeat.app.model.datengrundlage.dao.ZutatDAO;

import de.mindfeat.app.model.datengrundlage.entitaeten.Kategorie;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;
import de.mindfeat.app.model.datengrundlage.entitaeten.RezeptKategorie;
import de.mindfeat.app.model.datengrundlage.entitaeten.RezeptZutat;
import de.mindfeat.app.model.datengrundlage.entitaeten.Zubereitungsschritt;
import de.mindfeat.app.model.datengrundlage.entitaeten.Zutat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class RezeptVermittler {

    private final RezeptDAO rezeptDAO;
    private final ZutatDAO zutatDAO;
    private final RezeptZutatDAO rezeptZutatDAO;
    private final KategorieDAO kategorieDAO;
    private final RezeptKategorieDAO rezeptKategorieDAO;
    private final ZubereitungsschrittDAO zubereitungsschrittDAO;
    private final ZentraleDatenbank datenbank;


    // Konstruktor bezieht benötigte DAOs aus der zentralen Datenbank
    public RezeptVermittler(ZentraleDatenbank datenbank) {
        this.datenbank = datenbank;
        if (datenbank == null) {
            throw new IllegalArgumentException("Eine Datenbank muss gewählt werden.");
        }
        rezeptDAO = datenbank.rezeptDAO();
        zutatDAO = datenbank.zutatDAO();
        rezeptZutatDAO = datenbank.rezeptZutatDAO();
        kategorieDAO = datenbank.kategorieDAO();
        rezeptKategorieDAO = datenbank.rezeptKategorieDAO();
        zubereitungsschrittDAO = datenbank.zubereitungsschrittDAO();
    }


    // Ein Rezept einschließlich seiner zugehörigen Daten abrufen
    public RezeptDetails nachIdMitDetailsAbrufen(int rezeptId) {
        if (rezeptId < 1) {
            throw new IllegalArgumentException("Es muss ein Rezept ausgewählt sein.");
        }
        Rezept rezept = rezeptDAO.nachIdAbrufen(rezeptId);
        if (rezept == null) { // wenn kein Rezept mit dieser ID vorhanden ist
            return null;
        }
        List<RezeptZutat> rezeptZutaten = rezeptZutatDAO.nachRezeptIdAbrufen(rezeptId); // Zutaten-Zuordnungen und zugehörige Zutaten abrufen
        List<Zutat> zutaten = new ArrayList<>();
        for (RezeptZutat rezeptZutat : rezeptZutaten) {
            Zutat zutat = zutatDAO.nachIdAbrufen(rezeptZutat.getZutatId());
            if (zutat != null) {
                zutaten.add(zutat);
            }
        }
        List<RezeptKategorie> rezeptKategorien = rezeptKategorieDAO.nachRezeptIdAbrufen(rezeptId); // Kategorie-Zuordnungen und zugehörige Kategorien abrufen
        List<Kategorie> kategorien = new ArrayList<>();
        for (RezeptKategorie rezeptKategorie : rezeptKategorien) {
            Kategorie kategorie = kategorieDAO.nachIdAbrufen(rezeptKategorie.getKategorieId());
            if (kategorie != null) {
                kategorien.add(kategorie);
            }
        }
        List<Zubereitungsschritt> zubereitungsschritte = zubereitungsschrittDAO.nachRezeptIdAbrufen(rezeptId); // Zubereitungsschritte in der gespeicherten Reihenfolge abrufen
        return new RezeptDetails(rezept, rezeptZutaten, zutaten, rezeptKategorien, kategorien, zubereitungsschritte);
    }

    // Ergebnisobjekt für ein vollständiges Rezept
    public static class RezeptDetails {
        private final Rezept rezept;
        private final List<RezeptZutat> rezeptZutaten;
        private final List<Zutat> zutaten;
        private final List<RezeptKategorie> rezeptKategorien;
        private final List<Kategorie> kategorien;
        private final List<Zubereitungsschritt> zubereitungsschritte;

        private RezeptDetails(Rezept rezept, List<RezeptZutat> rezeptZutaten, List<Zutat> zutaten, List<RezeptKategorie> rezeptKategorien, List<Kategorie> kategorien, List<Zubereitungsschritt> zubereitungsschritte) {
            this.rezept = rezept;
            this.rezeptZutaten = rezeptZutaten;
            this.zutaten = zutaten;
            this.rezeptKategorien = rezeptKategorien;
            this.kategorien = kategorien;
            this.zubereitungsschritte = zubereitungsschritte;
        }

        public Rezept getRezept() {
            return rezept;
        }
        public List<RezeptZutat> getRezeptZutaten() {
            return Collections.unmodifiableList(rezeptZutaten);
        }
        public List<Zutat> getZutaten() {
            return Collections.unmodifiableList(zutaten);
        }
        public List<RezeptKategorie> getRezeptKategorien() {
            return Collections.unmodifiableList(rezeptKategorien);
        }
        public List<Kategorie> getKategorien() {
            return Collections.unmodifiableList(kategorien);
        }
        public List<Zubereitungsschritt> getZubereitungsschritte() {
            return Collections.unmodifiableList(zubereitungsschritte);
        }
    }

    // Eingabedaten einer Zutat beim Anlegen eines Rezepts
    public static class ZutatAngabe {
        private final String name;
        private final double menge;
        private final String einheit;

        public ZutatAngabe(String name, double menge, String einheit) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Bitte benenne die Zutat.");
            }
            if (!Double.isFinite(menge) || menge <= 0) {
                throw new IllegalArgumentException("Die Menge muss größer als 0 sein.");
            }
            if (einheit == null || einheit.trim().isEmpty()) {
                throw new IllegalArgumentException("Bitte gib eine Einheit an.");
            }
            this.name = name.trim();
            this.menge = menge;
            this.einheit = einheit.trim();
        }
        public String getName() {
            return name;
        }
        public double getMenge() {
            return menge;
        }
        public String getEinheit() {
            return einheit;
        }
    }

    // Ein vollständiges Rezept einschließlich aller zugehörigen Daten anlegen
    public int anlegen(Rezept rezept, List<ZutatAngabe> zutaten, List<String> kategorien, List<String> zubereitungsschritte) {
        if (rezept == null) {
            throw new IllegalArgumentException("Ein Rezept muss ausgewählt sein.");
        }
        datenbank.runInTransaction(() -> { // 1. Rezept speichern und generierte ID übernehmen
            long rezeptId = rezeptDAO.einfuegen(rezept);
            rezept.setId((int) rezeptId);
            if (zutaten != null) { // 2. Zutaten wiederverwenden oder neu anlegen
                for (ZutatAngabe angabe : zutaten) {
                    if (angabe == null) {
                        throw new IllegalArgumentException("Eine Zutat muss ausgewählt sein.");
                    }
                    Zutat zutat = zutatDAO.nachNameAbrufen(angabe.getName());
                    if (zutat == null) { // wenn Zutat noch nicht existiert
                        zutat = new Zutat(angabe.getName());
                        long zutatId = zutatDAO.einfuegen(zutat);
                        zutat.setId((int) zutatId);
                    }
                    // Rezept-Zutat-Zuordnung speichern
                    RezeptZutat rezeptZutat = new RezeptZutat(rezept.getId(), zutat.getId(), angabe.getMenge(), angabe.getEinheit());
                    rezeptZutatDAO.einfuegen(rezeptZutat);
                }
            }
            if (kategorien != null) {
                Set<Integer> bereitsZugeordneteKategorien = new HashSet<>();
                for (String name : kategorien) {
                    if (name == null || name.trim().isEmpty()) {
                        throw new IllegalArgumentException("Ein Kategoriename muss ausgewählt sein.");
                    }
                    Kategorie kategorie = kategorieDAO.nachNameAbrufen(name.trim()); // Vorhandene Kategorie suchen
                    if (kategorie == null) { // Falls nicht vorhanden, neu anlegen
                        kategorie = new Kategorie(name.trim());
                        long kategorieId = kategorieDAO.einfuegen(kategorie);
                        kategorie.setId((int) kategorieId);
                    }
                    if (bereitsZugeordneteKategorien.add(kategorie.getId())) { // Kategorie nur einmal diesem Rezept zuordnen
                        RezeptKategorie rezeptKategorie = new RezeptKategorie(rezept.getId(), kategorie.getId());
                        rezeptKategorieDAO.einfuegen(rezeptKategorie);
                    }
                }
            }
            if (zubereitungsschritte != null) {// 4. Zubereitungsschritte speichern
                for (int i = 0; i < zubereitungsschritte.size(); i++) {
                    Zubereitungsschritt schritt = new Zubereitungsschritt(rezept.getId(), i + 1, zubereitungsschritte.get(i));
                    zubereitungsschrittDAO.einfuegen(schritt);
                }
            }
        });
        return rezept.getId();
    }

    // Ein vollständiges Rezept bearbeiten
    public void bearbeiten(Rezept rezept, List<ZutatAngabe> zutaten, List<String> kategorien, List<String> zubereitungsschritte) {
        if (rezept == null) {
            throw new IllegalArgumentException("Rezept darf nicht null sein.");
        }
        int rezeptId = rezept.getId();
        if (rezeptId < 1 || rezeptDAO.nachIdAbrufen(rezeptId) == null) {
            throw new IllegalArgumentException("Rezept wurde nicht gefunden.");
        }
        datenbank.runInTransaction(() -> {
            rezeptDAO.aktualisieren(rezept);
            List<RezeptZutat> alteZutaten = rezeptZutatDAO.nachRezeptIdAbrufen(rezeptId);
            for (RezeptZutat rezeptZutat : alteZutaten) {
                rezeptZutatDAO.loeschen(rezeptZutat);
            }
            List<RezeptKategorie> alteKategorien =
                    rezeptKategorieDAO.nachRezeptIdAbrufen(rezeptId);
            for (RezeptKategorie rezeptKategorie : alteKategorien) {
                rezeptKategorieDAO.loeschen(rezeptKategorie);
            }
            List<Zubereitungsschritt> alteSchritte =
                    zubereitungsschrittDAO.nachRezeptIdAbrufen(rezeptId);

            for (Zubereitungsschritt schritt : alteSchritte) {
                zubereitungsschrittDAO.loeschen(schritt);
            }
            if (zutaten != null) {
                for (ZutatAngabe zutatAngabe : zutaten) {
                    Zutat zutat = zutatDAO.nachNameAbrufen(zutatAngabe.getName());
                    if (zutat == null) {
                        zutat = new Zutat(zutatAngabe.getName());
                        long zutatId = zutatDAO.einfuegen(zutat);
                        zutat.setId((int) zutatId);
                    }
                    RezeptZutat rezeptZutat = new RezeptZutat(rezeptId, zutat.getId(), zutatAngabe.getMenge(), zutatAngabe.getEinheit());
                    rezeptZutatDAO.einfuegen(rezeptZutat);
                }
            }
            if (kategorien != null) {
                Set<Integer> bereitsZugeordneteKategorien = new HashSet<>();
                for (String kategorienName : kategorien) {
                    Kategorie kategorie = kategorieDAO.nachNameAbrufen(kategorienName);
                    if (kategorie == null) {
                        kategorie = new Kategorie(kategorienName);
                        long kategorieId = kategorieDAO.einfuegen(kategorie);
                        kategorie.setId((int) kategorieId);
                    }
                    if (bereitsZugeordneteKategorien.add(kategorie.getId())) {
                        RezeptKategorie rezeptKategorie = new RezeptKategorie(rezeptId, kategorie.getId());
                        rezeptKategorieDAO.einfuegen(rezeptKategorie);
                    }
                }
            }
            if (zubereitungsschritte != null) {
                for (int i = 0; i < zubereitungsschritte.size(); i++) {
                    Zubereitungsschritt schritt = new Zubereitungsschritt(rezeptId, i + 1, zubereitungsschritte.get(i));
                    zubereitungsschrittDAO.einfuegen(schritt);
                }
            }
        });
    }

    // Liste aller Rezepte abrufen
    public List<Rezept> alleAbrufen() {
        return rezeptDAO.alleAbrufen();
    }


    // Ein Rezept vollständig löschen
    public boolean loeschen(int rezeptId) {
        if (rezeptId < 1) {
            throw new IllegalArgumentException(
                    "Ein Rezept muss ausgewählt sein.");
        }
        Rezept rezept = rezeptDAO.nachIdAbrufen(rezeptId);
        if (rezept == null) {
            return false;
        }
        return rezeptDAO.loeschen(rezept) > 0;
    }
}
