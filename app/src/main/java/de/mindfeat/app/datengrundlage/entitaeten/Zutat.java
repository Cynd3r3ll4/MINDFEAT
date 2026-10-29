package de.mindfeat.app.datengrundlage.entitaeten;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "zutaten",
        indices = {
                @Index(value = {"name"}, unique = true)
        }
)

public class Zutat {

    // Attribute
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(collate = ColumnInfo.NOCASE) // Zutaten sollen auch bei unterschiedlicher Groß- & Kleinschreibung als derselbe Eintrag erkannt werden
    private String name;

    // vollständiger Konstruktor für eine neue Zutat (per Setter für enthaltene Validierung)
    public Zutat(String name) {
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
                    "Bitte gib der Zutat einen Namen."
            );
        }

        this.name = name.trim();
    }
}
