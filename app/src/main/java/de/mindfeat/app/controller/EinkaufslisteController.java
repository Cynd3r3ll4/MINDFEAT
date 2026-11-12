package de.mindfeat.app.controller;

import de.mindfeat.app.model.vermittler.EinkaufslisteVermittler;

import java.util.List;
import java.util.concurrent.Executor;

public class EinkaufslisteController {

    private final EinkaufslisteVermittler einkaufslisteVermittler;
    private final Executor hintergrundExecutor;
    private final Executor rueckgabeExecutor;

    public EinkaufslisteController(EinkaufslisteVermittler einkaufslisteVermittler, Executor hintergrundExecutor, Executor rueckgabeExecutor) {
        if (einkaufslisteVermittler == null
                || hintergrundExecutor == null
                || rueckgabeExecutor == null) {
            throw new IllegalArgumentException("Vermittler und Executoren müssen angegeben werden.");
        }
        this.einkaufslisteVermittler = einkaufslisteVermittler;
        this.hintergrundExecutor = hintergrundExecutor;
        this.rueckgabeExecutor = rueckgabeExecutor;
    }

    // Die gesamte Einkaufsliste mit Zutaten und Beiträgen abrufen
    public void alleAbrufen(Callback<List<EinkaufslisteVermittler.EinkaufslistenDetails>> callback) {
        ausfuehren(() -> einkaufslisteVermittler.alleAbrufen(), callback);
    }

    // Ein Element manuell zur Einkaufsliste hinzufügen
    public void manuellHinzufuegen(String zutatName, Double menge, String einheit, Callback<Integer> callback) {
        ausfuehren(() -> einkaufslisteVermittler.manuellHinzufuegen(zutatName, menge, einheit), callback);
    }

    // Zutaten eines Essensplans in die Einkaufsliste übernehmen
    public void ausEssensplanHinzufuegen(int essensplanId, Callback<Void> callback) {
        ausfuehren(() -> {einkaufslisteVermittler.ausEssensplanHinzufuegen(essensplanId);return null;}, callback);
    }

    // Alle Beiträge eines Essensplans aus der Einkaufsliste entfernen
    public void ausEssensplanEntfernen(int essensplanId, Callback<Void> callback) {
        ausfuehren(() -> {einkaufslisteVermittler.ausEssensplanEntfernen(essensplanId);return null;}, callback);
    }

    // Einen Essensplan einschließlich seiner Einkaufslistenbeiträge löschen
    public void essensplanLoeschen(int essensplanId, Callback<Void> callback) {
        ausfuehren(() -> {einkaufslisteVermittler.essensplanLoeschen(essensplanId);return null;}, callback);
    }

    // Einen einzelnen Einkaufslistenbeitrag entfernen
    public void beitragEntfernen(int beitragId, Callback<Void> callback) {
        ausfuehren(() -> {einkaufslisteVermittler.beitragEntfernen(beitragId);return null;}, callback);
    }

    // Den Erledigt-Status eines Einkaufslistenelements ändern
    public void erledigtSetzen(int elementId, boolean erledigt, Callback<Void> callback) {
        ausfuehren(() -> {einkaufslisteVermittler.erledigtSetzen(elementId, erledigt);return null;}, callback);
    }

    // Ein Einkaufslistenelement einschließlich seiner Beiträge löschen
    public void loeschen(int elementId, Callback<Void> callback) {
        ausfuehren(() -> {einkaufslisteVermittler.loeschen(elementId);return null;}, callback);
    }

    // Führt eine Operation im Hintergrund aus und gibt das Ergebnis zurück
    private <T> void ausfuehren(Operation<T> operation, Callback<T> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Ein Callback muss angegeben werden.");
        }
        hintergrundExecutor.execute(() -> {
            T ergebnis = null;
            Exception fehler = null;
            try {
                ergebnis = operation.ausfuehren();
            } catch (Exception exception) {
                fehler = exception;
            }
            final T rueckgabe = ergebnis;
            final Exception rueckgabeFehler = fehler;
            rueckgabeExecutor.execute(() -> {
                if (rueckgabeFehler == null) {
                    callback.beiErfolg(rueckgabe);
                } else {
                    callback.beiFehler(rueckgabeFehler);
                }
            });
        });
    }

    // Callback für Ergebnisse und Fehler
    public interface Callback<T> {

        void beiErfolg(T ergebnis);
        void beiFehler(Exception exception);
    }

    // Funktionale Schnittstelle für die auszuführende Operation
    private interface Operation<T> {

        T ausfuehren();
    }
}
