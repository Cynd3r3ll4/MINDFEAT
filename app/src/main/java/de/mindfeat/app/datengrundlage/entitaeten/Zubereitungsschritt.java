package de.mindfeat.app.datengrundlage.entitaeten;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "zubereitungsschritte",
        foreignKeys = {
                @ForeignKey(
                        entity = Rezept.class,
                        parentColumns = "id",
                        childColumns = "rezeptId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = {"rezeptId", "reihenfolge"}, unique = true)
        }
)

public class Zubereitungsschritt {

    // Attribute
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int rezeptId;
    private int reihenfolge;
    private String beschreibung;

    // vollständiger Konstruktor für einen neuen Zubereitungsschritt (mit Settern für darin enthaltene Validierungen)
    public Zubereitungsschritt(int rezeptId, int reihenfolge, String beschreibung) {
        setRezeptId(rezeptId);
        setReihenfolge(reihenfolge);
        setBeschreibung(beschreibung);
    }

    // Getter und Setter

    // ID
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Rezept-ID
    public int getRezeptId() {
        return rezeptId;
    }

    public void setRezeptId(int rezeptId) {
        if (rezeptId < 1) {
            throw new IllegalArgumentException(
                    "Es muss mindestens ein Rezept zugeordnet sein.");
        }
        this.rezeptId = rezeptId;
    }

    // Reihenfolge
    public int getReihenfolge() {
        return reihenfolge;
    }

    public void setReihenfolge(int reihenfolge) {
        if (reihenfolge < 1) {
            throw new IllegalArgumentException(
                    "Es muss mindestens eine Zutat zugeordnet sein.");
        }
        this.reihenfolge = reihenfolge;
    }

    // Beschreibung
    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        if (beschreibung == null || beschreibung.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Bitte beschreibe den Zubereitungsschritt.");
        }
        this.beschreibung = beschreibung.trim();
    }
}
