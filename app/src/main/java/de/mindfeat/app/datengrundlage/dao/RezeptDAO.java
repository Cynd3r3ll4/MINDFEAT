package de.mindfeat.app.datengrundlage.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import de.mindfeat.app.datengrundlage.entitaeten.Rezept;

import java.util.List;

@Dao
public interface RezeptDAO {

    // CRUD-Operationen

    // Ein neues Rezept in der Datenbank speichern (long, weil Room standardmäßig long für IDs nutzt)
    @Insert
    long einfuegen(Rezept rezept);

    // Ein vorhandenes Rezept aktualisieren
    @Update
    int aktualisieren(Rezept rezept);

    // Ein Rezept aus der Datenbank löschen
    @Delete
    int loeschen(Rezept rezept);

    // Alle gespeicherten Rezepte abrufen
    @Query("SELECT * FROM rezepte ORDER BY titel ASC")
    List<Rezept> alleAbrufen();

    // Ein Rezept anhand seiner ID abrufen
    @Query("SELECT * FROM rezepte WHERE id = :id")
    Rezept nachIdAbrufen(int id);
}
