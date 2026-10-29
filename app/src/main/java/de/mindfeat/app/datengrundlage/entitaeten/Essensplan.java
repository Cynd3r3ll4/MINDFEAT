package de.mindfeat.app.datengrundlage.entitaeten;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Entity(tableName = "essensplaene")

public class Essensplan {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String titel;
    private String startDatum;
    private String endDatum;
    private int wochenstartTag;

    // vollständiger Konstruktor für einen neuen Essensplan (mit Settern für darin enthaltene Validierungen)
    public Essensplan(String titel, String startDatum, String endDatum, int wochenstartTag) {
        setTitel(titel);
        setStartDatum(startDatum);
        setEndDatum(endDatum);
        setWochenstartTag(wochenstartTag);
    }

    // Getter und Setter

    // ID
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Optionaler Titel
    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        if (titel == null || titel.trim().isEmpty()) {
            this.titel = null;
        } else {
            this.titel = titel.trim();
        }
    }

    // Startdatum
    public String getStartDatum() {
        return startDatum;
    }

    public void setStartDatum(String startDatum) {
        pruefeDatum(startDatum);
        if (endDatum != null
                && LocalDate.parse(startDatum)
                .isAfter(LocalDate.parse(endDatum))) {
            throw new IllegalArgumentException(
                    "Das Startdatum darf nicht nach dem Enddatum liegen.");
        }
        this.startDatum = startDatum;
    }

    // Enddatum
    public String getEndDatum() {
        return endDatum;
    }

    public void setEndDatum(String endDatum) {
        pruefeDatum(endDatum);
        if (startDatum != null
                && LocalDate.parse(endDatum)
                .isBefore(LocalDate.parse(startDatum))) {
            throw new IllegalArgumentException(
                    "Das Enddatum darf nicht vor dem Startdatum liegen.");
        }
        this.endDatum = endDatum;
    }

    // Wochenbeginn: ISO-Wochentag von 1 (Montag) bis 7 (Sonntag)
    public int getWochenstartTag() {
        return wochenstartTag;
    }

    public void setWochenstartTag(int wochenstartTag) {
        if (wochenstartTag < 1 || wochenstartTag > 7) {
            throw new IllegalArgumentException(
                    "Der Wochenstart muss zwischen 1 (Montag) " + "und 7 (Sonntag) liegen.");
        }
        this.wochenstartTag = wochenstartTag;
    }

    // Prüft, ob ein Datum im ISO-Format JJJJ-MM-TT gültig ist.
    private void pruefeDatum(String datum) {
        if (datum == null || datum.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Bitte gib ein Datum ein.");
        }
        try {
            LocalDate.parse(datum);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "Bitte gib ein gültiges Datum im Format JJJJ-MM-TT an.",
                    exception);
        }
    }
}
