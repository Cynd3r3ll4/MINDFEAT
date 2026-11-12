package de.mindfeat.app.controller;

import de.mindfeat.app.model.datengrundlage.entitaeten.Essensplan;
import de.mindfeat.app.model.vermittler.EssensplanVermittler;
import de.mindfeat.app.model.vermittler.RezeptVermittler;

import java.util.List;
import java.util.concurrent.Executor;

public class EssensplanController {

    private final EssensplanVermittler essensplanVermittler;
    private final Executor hintergrundExecutor;
    private final Executor rueckgabeExecutor;

    public EssensplanController(EssensplanVermittler essensplanVermittler, Executor hintergrundExecutor, Executor rueckgabeExecutor) {
        if (essensplanVermittler == null
                || hintergrundExecutor == null
                || rueckgabeExecutor == null) {
            throw new IllegalArgumentException("Vermittler und Executoren müssen angegeben werden.");
        }
        this.essensplanVermittler = essensplanVermittler;
        this.hintergrundExecutor = hintergrundExecutor;
        this.rueckgabeExecutor = rueckgabeExecutor;
    }

    // Einen neuen Essensplan anlegen
    public void anlegen(Essensplan essensplan, Callback<Long> callback) {
        ausfuehren(() -> essensplanVermittler.anlegen(essensplan), callback);
    }

    // Einen vorhandenen Essensplan bearbeiten
    public void bearbeiten(Essensplan essensplan, Callback<Void> callback
    ) {
        ausfuehren(() -> {essensplanVermittler.bearbeiten(essensplan);return null;}, callback);
    }

    // Alle Essenspläne abrufen
    public void alleAbrufen(Callback<List<Essensplan>> callback) {
        ausfuehren(() -> essensplanVermittler.alleAbrufen(), callback);
    }

    // Einen Essensplan mit allen Einträgen und Rezeptdetails abrufen
    public void nachIdMitDetailsAbrufen(int essensplanId, Callback<EssensplanVermittler.EssensplanDetails> callback) {
        ausfuehren(() -> essensplanVermittler.nachIdMitDetailsAbrufen(essensplanId), callback);
    }

    // Ein Rezept an einem bestimmten Datum zum Essensplan hinzufügen
    public void inhaltHinzufuegen(int essensplanId, int rezeptId, String datum, Callback<Long> callback) {
        ausfuehren(() -> essensplanVermittler.inhaltHinzufuegen(essensplanId, rezeptId, datum), callback);
    }

    // Einen einzelnen Essensplaneintrag entfernen
    public void inhaltEntfernen(int eintragId, Callback<Void> callback) {
        ausfuehren(() -> {essensplanVermittler.inhaltEntfernen(eintragId);return null;}, callback);
    }

    // Einen gesamten Essensplan löschen
    public void loeschen(int essensplanId, Callback<Void> callback) {
        ausfuehren(() -> {essensplanVermittler.loeschen(essensplanId);return null;}, callback);
    }

    // Rezeptvorschläge für einen Essensplan ermitteln
    public void vorschlaegeErmitteln(int essensplanId, List<Integer> kategorieIds, boolean nachhaltigkeitAktiviert, Callback<List<RezeptVermittler.RezeptDetails>> callback) {
        ausfuehren(() -> essensplanVermittler.vorschlaegeErmitteln(essensplanId, kategorieIds, nachhaltigkeitAktiviert), callback);
    }

    // Einen einzelnen zufälligen Rezeptvorschlag ermitteln
    public void zufaelligenVorschlagErmitteln(int essensplanId, List<Integer> kategorieIds, boolean nachhaltigkeitAktiviert, Callback<RezeptVermittler.RezeptDetails> callback) {
        ausfuehren(() -> essensplanVermittler.zufaelligenVorschlagErmitteln(essensplanId, kategorieIds, nachhaltigkeitAktiviert), callback);
    }

    // Die Portionsanzahl eines Essensplaneintrags ändern
    public void portionenAendern(int eintragId, int portionen, Callback<Void> callback) {
        ausfuehren(() -> {essensplanVermittler.portionenAendern(eintragId, portionen);return null;}, callback);
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
