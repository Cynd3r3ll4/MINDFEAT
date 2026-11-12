package de.mindfeat.app.controller;

import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;
import de.mindfeat.app.model.vermittler.RezeptVermittler;

import java.util.List;
import java.util.concurrent.Executor;

public class RezeptController {

    private final RezeptVermittler rezeptVermittler;
    private final Executor hintergrundExecutor;
    private final Executor rueckgabeExecutor;

    // Konstruktor (Vermittler: Fachlogik, Executor: asynchrone Ausführung)
    public RezeptController(RezeptVermittler rezeptVermittler, Executor hintergrundExecutor, Executor rueckgabeExecutor) {
        if (rezeptVermittler == null) {
            throw new IllegalArgumentException("Der Rezeptvermittler darf nicht null sein.");
        }
        if (hintergrundExecutor == null || rueckgabeExecutor == null) {
            throw new IllegalArgumentException("Die Executor dürfen nicht null sein.");
        }
        this.rezeptVermittler = rezeptVermittler;
        this.hintergrundExecutor = hintergrundExecutor;
        this.rueckgabeExecutor = rueckgabeExecutor;
    }

    // Alle gespeicherten Rezepte abrufen
    public void alleAbrufen(Callback<List<Rezept>> callback) {
        ausfuehren(rezeptVermittler::alleAbrufen, callback);
    }

     // Ein Rezept einschließlich Zutaten, Kategorien und Zubereitungsschritten abrufen.
    public void nachIdMitDetailsAbrufen(int rezeptId, Callback<RezeptVermittler.RezeptDetails> callback) {
        ausfuehren(() -> rezeptVermittler.nachIdMitDetailsAbrufen(rezeptId), callback);
    }

    // Ein neues Rezept einschließlich seiner zugehörigen Zutaten, Kategorien und Zubereitungsschritte anlegen
    public void anlegen(Rezept rezept, List<RezeptVermittler.ZutatAngabe> zutaten, List<String> kategorien, List<String> zubereitungsschritte, Callback<Integer> callback) {
        ausfuehren(() -> rezeptVermittler.anlegen(rezept, zutaten, kategorien, zubereitungsschritte), callback);
    }

    // Ein vorhandenes Rezept einschließlich seiner zugehörigen Daten bearbeiten
    public void bearbeiten(Rezept rezept, List<RezeptVermittler.ZutatAngabe> zutaten, List<String> kategorien, List<String> zubereitungsschritte, Callback<Void> callback) {
        ausfuehren(() -> {rezeptVermittler.bearbeiten(rezept, zutaten, kategorien, zubereitungsschritte);return null;}, callback);
    }

    // Ein Rezept löschen
    public void loeschen(int rezeptId, Callback<Boolean> callback) {
        ausfuehren(() -> rezeptVermittler.loeschen(rezeptId), callback);
    }

    // Eine Operation im Hintergrund ausführen und anschließend Ergebnis/ Fehler melden
    private <T> void ausfuehren(Operation<T> operation, Callback<T> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Ein Callback muss angegeben werden.");
        }
        hintergrundExecutor.execute(() -> {T ergebnis = null;Exception fehler = null;
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

    // Rückmeldung über den Ausgang einer Controller-Operation
    public interface Callback<T> {
        void beiErfolg(T ergebnis);
        void beiFehler(Exception exception);
    }

    // Interne Schnittstelle für auszuführende Operationen
    private interface Operation<T> {
        T ausfuehren();
    }
}
