package de.mindfeat.app.model.datengrundlage.entitaeten;

import androidx.room.ColumnInfo;
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
                @Index(value = "rezeptId")
        }
)

public class InhaltEssensplan {

    // Attribute
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int essensplanId;
    private int rezeptId;
    private String datum;
    @ColumnInfo(defaultValue = "1")
    private int portionen;
    @ColumnInfo(defaultValue = "1")
    private int rezeptPortionen;

    // vollständiger Konstruktor für den Inhalt eines Essensplans (mit Settern für darin enthaltene Validierungen)
    public InhaltEssensplan(int essensplanId, int rezeptId, String datum, int portionen, int rezeptPortionen) {
        setEssensplanId(essensplanId);
        setRezeptId(rezeptId);
        setDatum(datum);
        setPortionen(portionen);
        setRezeptPortionen(rezeptPortionen);
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

    // Planportionen
    public int getPortionen() {
        return portionen;
    }

    public void setPortionen(int portionen) {
        if (portionen < 1) {
            throw new IllegalArgumentException(
                    "Die Anzahl der Portionen muss mindestens 1 sein."
            );
        }
        this.portionen = portionen;
    }

    // Ursprüngliche Rezeptportionen
    public int getRezeptPortionen() {
        return rezeptPortionen;
    }

    public void setRezeptPortionen(int rezeptPortionen) {
        if (rezeptPortionen < 1) {
            throw new IllegalArgumentException(
                    "Die ursprüngliche Anzahl der Rezeptportionen muss mindestens 1 sein."
            );
        }
        this.rezeptPortionen = rezeptPortionen;
    }
}
