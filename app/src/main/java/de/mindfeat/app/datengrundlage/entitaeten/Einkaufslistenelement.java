package de.mindfeat.app.datengrundlage.entitaeten;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "einkaufslistenelemente",
        foreignKeys = {
                @ForeignKey(
                        entity = Zutat.class,
                        parentColumns = "id",
                        childColumns = "zutatId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = "zutatId")
        }
)

public class Einkaufslistenelement {

    // Attribute
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int zutatId;
    private Double menge;
    private String einheit;
    private boolean erledigt;

    // vollständiger Konstruktor für einen neuen Einkaufslisteneintrag (mit Settern für die darin enthaltenen Validierungen)
    public Einkaufslistenelement(int zutatId, Double menge, String einheit, boolean erledigt) {
        setZutatId(zutatId);
        setMenge(menge);
        setEinheit(einheit);
        setErledigt(erledigt);
    }

    // Getter und Setter

    // ID
    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException(
                    "Die ID darf nicht negativ sein.");
        }
        this.id = id;
    }

    // Zutaten-ID
    public int getZutatId() {
        return zutatId;
    }

    public void setZutatId(int zutatId) {
        if (zutatId < 1) {
            throw new IllegalArgumentException(
                    "Es muss mindestens eine Zutat zugeordnet sein.");
        }
        this.zutatId = zutatId;
    }

    // Menge
    public Double getMenge() {
        return menge;
    }

    public void setMenge(Double menge) {
        if (menge != null
                && (!Double.isFinite(menge) || menge <= 0)) {
            throw new IllegalArgumentException(
                    "Bitte gib eine Menge an. Sie muss größer als 0 sein.");
        }
        this.menge = menge;
    }

    // Einheit
    public String getEinheit() {
        return einheit;
    }

    public void setEinheit(String einheit) {
        if (einheit == null || einheit.trim().isEmpty()) {
            this.einheit = null;
        } else {
            this.einheit = einheit.trim();
        }
    }

    // Erledigt-Status (gibt an, ob der Artikel abgehakt wurde)
    public boolean isErledigt() {
        return erledigt;
    }

    public void setErledigt(boolean erledigt) {
        this.erledigt = erledigt;
    }
}
