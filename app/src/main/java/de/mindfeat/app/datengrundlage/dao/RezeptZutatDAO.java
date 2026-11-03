package de.mindfeat.app.datengrundlage.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import de.mindfeat.app.datengrundlage.entitaeten.RezeptZutat;

import java.util.List;

@Dao
public interface RezeptZutatDAO {

    // CRUD-Operationen

    // Eine neue Rezept-Zutat-Zuordnung in der Datenbank speichern
    @Insert
    long einfuegen(RezeptZutat rezeptZutat);

    // Eine vorhandene Rezept-Zutat-Zuordnung aktualisieren
    @Update
    int aktualisieren(RezeptZutat rezeptZutat);

    // Eine vorhandene Rezept-Zutat-Zuordnung löschen
    @Delete
    int loeschen(RezeptZutat rezeptZutat);

    // Alle Zutaten-Zuordnungen eines Rezepts abrufen
    @Query("SELECT * FROM rezept_zutaten WHERE rezeptId = :rezeptId ORDER BY id ASC")
    List<RezeptZutat> nachRezeptIdAbrufen(int rezeptId);

    // Alle Rezept-Zuordnungen einer Zutat abrufen
    @Query("SELECT * FROM rezept_zutaten WHERE zutatId = :zutatId ORDER BY id ASC")
    List<RezeptZutat> nachZutatIdAbrufen(int zutatId);

    // Eine Rezept-Zutat-Zuordnung anhand ihrer ID abrufen
    @Query("SELECT * FROM rezept_zutaten WHERE id = :id")
    RezeptZutat nachIdAbrufen(int id);
}
