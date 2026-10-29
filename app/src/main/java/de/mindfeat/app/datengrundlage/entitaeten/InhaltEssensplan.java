package de.mindfeat.app.datengrundlage.entitaeten;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

@Entity(
        tableName = "inhalt_essensplan",
        foreignKeys = {
                @ForeignKey(
                        entity = Essensplan.class,
                        parentColumns = "id",
                        childColumns = "essensplanId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Rezept.class,
                        parentColumns = "id",
                        childColumns = "rezeptId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = "essensplanId"),
                @Index(value = "rezeptId"),
                @Index(
                        value = {"essensplanId", "datum", "rezeptId"},
                        unique = true
                )
        }
)

public class InhaltEssensplan {

    // Attribute
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int essensplanId;
    private int rezeptId;
    private String datum;

    // vollständiger Konstruktor für den Inhalt eines Essensplans (mit Settern für darin enthaltene Validierungen)
    public InhaltEssensplan(int essensplanId, int rezeptId, String datum) {
        setEssensplanId(essensplanId);
        setRezeptId(rezeptId);
        setDatum(datum);
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

    // Essensplan-ID
    public int getEssensplanId() {
        return essensplanId;
    }

    public void setEssensplanId(int essensplanId) {
        if (essensplanId < 1) {
            throw new IllegalArgumentException(
                    "Es muss mindestens ein Essensplan zugeordnet sein.");
        }
        this.essensplanId = essensplanId;
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

    // Datum
    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        if (datum == null || datum.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Bitte gib ein Datum an.");
        }
        try {
            LocalDate.parse(datum);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(
                    "Das Datum muss ein gültiges Datum im Format YYYY-MM-DD sein.", e);
        }
        this.datum = datum;
    }
}
