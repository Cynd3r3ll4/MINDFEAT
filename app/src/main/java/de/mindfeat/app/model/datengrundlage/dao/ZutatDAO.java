package de.mindfeat.app.model.datengrundlage.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import de.mindfeat.app.model.datengrundlage.entitaeten.Zutat;

import java.util.List;

@Dao
public interface ZutatDAO {

    // CRUD-Operationen

    // Eine neue Zutat in der Datenbank speichern
    @Insert
    long einfuegen(Zutat zutat);

    // Eine vorhandene Zutat aktualisieren
    @Update
    int aktualisieren(Zutat zutat);

    // Eine vorhandene Zutat löschen
    @Delete
    int loeschen(Zutat zutat);

    // Alle gespeicherten Zutaten alphabetisch abrufen
    @Query("SELECT * FROM zutaten ORDER BY name COLLATE NOCASE ASC")
    List<Zutat> alleAbrufen();

    // Eine Zutat anhand ihrer ID abrufen
    @Query("SELECT * FROM zutaten WHERE id = :id")
    Zutat nachIdAbrufen(int id);

    // Eine Zutat anhand ihres Namens abrufen (Groß-/Kleinschreibung ignorieren)
    @Query("SELECT * FROM zutaten WHERE name = :name COLLATE NOCASE LIMIT 1")
    Zutat nachNameAbrufen(String name);
}