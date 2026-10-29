package de.mindfeat.app.datengrundlage.entitaeten;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rezepte")

public class Rezept {

    // Attribute
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String titel;
    private String bildPfad;
    private int portionen;

    // vollständiger Konstruktor für ein neues Rezept (mit Settern für darin enthaltene Validierungen)
    public Rezept(String titel, String bildPfad, int portionen) {
        setTitel(titel);
        setBildPfad(bildPfad);
        setPortionen(portionen);
    }

    // Getter und Setter

    // ID
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Titel (Pflichtfeld, keine Leerzeichen davor/dahinter)
    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        if (titel == null || titel.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Bitte gib deinem Rezept einen Titel."
            );
        }
        this.titel = titel.trim();
    }

    // Bildpfad (optional)
    public String getBildPfad() {
        return bildPfad;
    }

    public void setBildPfad(String bildPfad) {
        this.bildPfad = bildPfad;
    }

    // Portionen (muss min. 1 sein)
    public int getPortionen() {
        return portionen;
    }

    public void setPortionen(int portionen) {
        if (portionen < 1) {
            throw new IllegalArgumentException(
                    "Die Anzahl der Portionen muss mindestens \"1\" sein."
            );
        }

        this.portionen = portionen;
    }
}
