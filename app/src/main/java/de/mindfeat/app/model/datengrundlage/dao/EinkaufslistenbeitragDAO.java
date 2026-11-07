package de.mindfeat.app.model.datengrundlage.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import de.mindfeat.app.model.datengrundlage.entitaeten.Einkaufslistenbeitrag;

import java.util.List;

@Dao
public interface EinkaufslistenbeitragDAO {

    // CRUD-Operationen

    // Einen neuen Einkaufslistenbeitrag speichern
    @Insert
    long einfuegen(Einkaufslistenbeitrag einkaufslistenbeitrag);

    // Einen vorhandenen Einkaufslistenbeitrag aktualisieren
    @Update
    int aktualisieren(Einkaufslistenbeitrag einkaufslistenbeitrag);

    // Einen vorhandenen Einkaufslistenbeitrag löschen
    @Delete
    int loeschen(Einkaufslistenbeitrag einkaufslistenbeitrag);

    // Alle Einkaufslistenbeiträge abrufen
    @Query("SELECT * FROM einkaufslistenbeitraege ORDER BY id ASC")
    List<Einkaufslistenbeitrag> alleAbrufen();

    // Einen Einkaufslistenbeitrag anhand seiner ID abrufen
    @Query("SELECT * FROM einkaufslistenbeitraege WHERE id = :id")
    Einkaufslistenbeitrag nachIdAbrufen(int id);

    // Alle Beiträge eines Einkaufslistenelements abrufen
    @Query("SELECT * FROM einkaufslistenbeitraege " +
            "WHERE einkaufslistenelementId = :einkaufslistenelementId " +
            "ORDER BY id ASC")
    List<Einkaufslistenbeitrag> nachEinkaufslistenelementIdAbrufen(int einkaufslistenelementId);

    // Alle Beiträge eines Essensplans abrufen
    @Query("SELECT * FROM einkaufslistenbeitraege " +
            "WHERE essensplanId = :essensplanId ORDER BY id ASC")
    List<Einkaufslistenbeitrag> nachEssensplanIdAbrufen(int essensplanId);

    // Beiträge eines Einkaufslistenelements und Essensplans abrufen
    @Query("SELECT * FROM einkaufslistenbeitraege " +
            "WHERE einkaufslistenelementId = :einkaufslistenelementId " +
            "AND essensplanId IS :essensplanId ORDER BY id ASC")
    List<Einkaufslistenbeitrag> nachElementIdUndEssensplanIdAbrufen(int einkaufslistenelementId, Integer essensplanId);
}
