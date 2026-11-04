package de.mindfeat.app.model.datengrundlage.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import de.mindfeat.app.model.datengrundlage.entitaeten.Essensplan;

import java.util.List;

@Dao
public interface EssensplanDAO {

    // CRUD-Operationen

    // Einen neuen Essensplan in der Datenbank speichern
    @Insert
    long einfuegen(Essensplan essensplan);

    // Einen vorhandenen Essensplan aktualisieren
    @Update
    int aktualisieren(Essensplan essensplan);

    // Einen vorhandenen Essensplan löschen
    @Delete
    int loeschen(Essensplan essensplan);

    // Alle gespeicherten Essenspläne nach Startdatum sortiert abrufen
    @Query("SELECT * FROM essensplaene ORDER BY startDatum ASC, id ASC")
    List<Essensplan> alleAbrufen();

    // Einen Essensplan anhand seiner ID abrufen
    @Query("SELECT * FROM essensplaene WHERE id = :id")
    Essensplan nachIdAbrufen(int id);
}
