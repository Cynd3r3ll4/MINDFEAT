package de.mindfeat.app.datengrundlage.entitaeten;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Index;

@Entity(
        tableName = "kategorien",
        indices = {
                @Index(value = {"name"}, unique = true)
        }
)

public class Kategorie {

    // Attribute
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(collate = ColumnInfo.NOCASE) // Kategorien sollen auch bei unterschiedlicher Groß- & Kleinschreibung als dieselbe erkannt werden
    private String name;

    // vollständiger Konstruktor für eine neue Kategorie (per Setter für enthaltene Validierung)
    public Kategorie(String name) {
        setName(name);
    }

    // Getter und Setter

    // ID
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Name (Pflichtfeld)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Bitte gib der Kategorie einen Namen.");
        }
        this.name = name.trim();
    }
}
