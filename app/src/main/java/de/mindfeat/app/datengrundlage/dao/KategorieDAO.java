package de.mindfeat.app.datengrundlage.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import de.mindfeat.app.datengrundlage.entitaeten.Kategorie;

import java.util.List;

@Dao
public interface KategorieDAO {

    // CRUD-Operationen

    // Eine neue Kategorie in der Datenbank speichern
    @Insert
    long einfuegen(Kategorie kategorie);

    // Eine vorhandene Kategorie aktualisieren
    @Update
    int aktualisieren(Kategorie kategorie);

    // Eine vorhandene Kategorie löschen
    @Delete
    int loeschen(Kategorie kategorie);


    // Alle Kategorien alphabetisch abrufen
    @Query("SELECT * FROM kategorien ORDER BY name COLLATE NOCASE ASC")
    List<Kategorie> alleAbrufen();

    // Eine Kategorie anhand ihrer ID abrufen
    @Query("SELECT * FROM kategorien WHERE id = :id")
    Kategorie nachIdAbrufen(int id);

    // Eine Kategorie anhand ihres Namens abrufen (Groß-/Kleinschreibung ignorieren)
    @Query("SELECT * FROM kategorien WHERE name = :name COLLATE NOCASE LIMIT 1")
    Kategorie nachNameAbrufen(String name);
}
