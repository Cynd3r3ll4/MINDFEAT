package de.mindfeat.app.datengrundlage.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import de.mindfeat.app.datengrundlage.entitaeten.Einkaufslistenelement;

import java.util.List;

@Dao
public interface EinkaufslistenelementDAO {

    // CRUD-Operationen

    // Ein neues Einkaufslistenelement in der Datenbank speichern
    @Insert
    long einfuegen(Einkaufslistenelement einkaufslistenelement);

    // Ein vorhandenes Einkaufslistenelement aktualisieren
    @Update
    int aktualisieren(Einkaufslistenelement einkaufslistenelement);

    // Ein vorhandenes Einkaufslistenelement löschen
    @Delete
    int loeschen(Einkaufslistenelement einkaufslistenelement);

    // Alle Einkaufslistenelemente abrufen
    @Query("SELECT * FROM einkaufslistenelemente ORDER BY id ASC")
    List<Einkaufslistenelement> alleAbrufen();

    // Ein Einkaufslistenelement anhand seiner ID abrufen
    @Query("SELECT * FROM einkaufslistenelemente WHERE id = :id")
    Einkaufslistenelement nachIdAbrufen(int id);

    // Alle Einträge einer bestimmten Zutat abrufen
    @Query("SELECT * FROM einkaufslistenelemente WHERE zutatId = :zutatId ORDER BY id ASC")
    List<Einkaufslistenelement> nachZutatIdAbrufen(int zutatId);

    // Einträge einer Zutat mit derselben Einheit abrufen
    @Query("SELECT * FROM einkaufslistenelemente WHERE zutatId = :zutatId AND einheit IS :einheit ORDER BY id ASC")
    List<Einkaufslistenelement> nachZutatIdUndEinheitAbrufen(int zutatId, String einheit);
}
