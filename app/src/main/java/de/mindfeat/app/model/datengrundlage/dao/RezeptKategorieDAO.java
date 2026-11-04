package de.mindfeat.app.model.datengrundlage.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import de.mindfeat.app.model.datengrundlage.entitaeten.RezeptKategorie;

import java.util.List;

@Dao
public interface RezeptKategorieDAO {

    // CRUD-Operationen

    // Eine neue Rezept-Kategorie-Zuordnung in der Datenbank speichern
    @Insert
    long einfuegen(RezeptKategorie rezeptKategorie);

    // Eine vorhandene Rezept-Kategorie-Zuordnung aktualisieren
    @Update
    int aktualisieren(RezeptKategorie rezeptKategorie);

    // Eine vorhandene Rezept-Kategorie-Zuordnung löschen
    @Delete
    int loeschen(RezeptKategorie rezeptKategorie);

    // Alle Kategorie-Zuordnungen eines Rezepts abrufen
    @Query("SELECT * FROM rezept_kategorien WHERE rezeptId = :rezeptId ORDER BY id ASC")
    List<RezeptKategorie> nachRezeptIdAbrufen(int rezeptId);

    // Alle Rezept-Zuordnungen einer Kategorie abrufen
    @Query("SELECT * FROM rezept_kategorien WHERE kategorieId = :kategorieId ORDER BY id ASC")
    List<RezeptKategorie> nachKategorieIdAbrufen(int kategorieId);

    // Eine Rezept-Kategorie-Zuordnung anhand ihrer ID abrufen
    @Query("SELECT * FROM rezept_kategorien WHERE id = :id")
    RezeptKategorie nachIdAbrufen(int id);
}
