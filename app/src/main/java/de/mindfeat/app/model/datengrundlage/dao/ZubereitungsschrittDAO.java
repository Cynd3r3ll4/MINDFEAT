package de.mindfeat.app.model.datengrundlage.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import de.mindfeat.app.model.datengrundlage.entitaeten.Zubereitungsschritt;

import java.util.List;

@Dao
public interface ZubereitungsschrittDAO {

    // CRUD-Operationen

    // Einen neuen Zubereitungsschritt in der Datenbank speichern
    @Insert
    long einfuegen(Zubereitungsschritt zubereitungsschritt);

    // Einen vorhandenen Zubereitungsschritt aktualisieren
    @Update
    int aktualisieren(Zubereitungsschritt zubereitungsschritt);

    // Einen vorhandenen Zubereitungsschritt löschen
    @Delete
    int loeschen(Zubereitungsschritt zubereitungsschritt);

    // Alle Zubereitungsschritte eines Rezepts in der richtigen Reihenfolge abrufen
    @Query("SELECT * FROM zubereitungsschritte WHERE rezeptId = :rezeptId ORDER BY reihenfolge ASC")
    List<Zubereitungsschritt> nachRezeptIdAbrufen(int rezeptId);

    // Einen Zubereitungsschritt anhand seiner ID abrufen
    @Query("SELECT * FROM zubereitungsschritte WHERE id = :id")
    Zubereitungsschritt nachIdAbrufen(int id);
}
