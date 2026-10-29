package de.mindfeat.app.datengrundlage.entitaeten;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "rezept_zutaten",
        foreignKeys = {
                @ForeignKey(
                        entity = Rezept.class,
                        parentColumns = "id",
                        childColumns = "rezeptId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(
                        entity = Zutat.class,
                        parentColumns = "id",
                        childColumns = "zutatId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index(value = {"rezeptId"}),
                @Index(value = {"zutatId"})}
)

public class RezeptZutat {

    // Attribute
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int rezeptId;
    private int zutatId;
    private double menge;
    private String einheit;

    // vollständiger Konstruktor für eine Rezept-Zutat-Zuordnung (mit Settern für darin enthaltene Validierungen)
    public RezeptZutat(int rezeptId, int zutatId, double menge, String einheit) {
        setRezeptId(rezeptId);
        setZutatId(zutatId);
        setMenge(menge);
        setEinheit(einheit);
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
    public double getMenge() {
        return menge;
    }

    public void setMenge(double menge) {
        if (!Double.isFinite(menge) || menge <= 0) {
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
            throw new IllegalArgumentException(
                    "Bitte gib eine passende Einheit an.");
        }
        this.einheit = einheit.trim();
    }
}
