package de.mindfeat.app.datengrundlage.entitaeten;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "rezept_kategorien",
        foreignKeys = {
                @ForeignKey(
                        entity = Rezept.class,
                        parentColumns = "id",
                        childColumns = "rezeptId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Kategorie.class,
                        parentColumns = "id",
                        childColumns = "kategorieId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = {"rezeptId", "kategorieId"}, unique = true),
                @Index(value = {"kategorieId"})
        }
)

public class RezeptKategorie {

    // Attribute
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int rezeptId;
    private int kategorieId;

    // vollständiger Konstruktor für eine Rezept-Kategorie-Zuordnung (mit Settern für darin enthaltene Validierungen)
    public RezeptKategorie(int rezeptId, int kategorieId) {
        setRezeptId(rezeptId);
        setKategorieId(kategorieId);
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

    // Kategorie-ID
    public int getKategorieId() {
        return kategorieId;
    }

    public void setKategorieId(int kategorieId) {
        if (kategorieId < 1) {
            throw new IllegalArgumentException(
                    "Es muss mindestens eine Zutat zugeordnet sein.");
        }
        this.kategorieId = kategorieId;
    }
}
