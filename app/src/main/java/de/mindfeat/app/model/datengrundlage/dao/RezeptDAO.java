package de.mindfeat.app.model.datengrundlage.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;

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

    // Rezepte anhand von Titel, Kategorie oder Zutat suchen (ohne Berücksichtigung von Groß- und Kleinschreibung)
    @Query("SELECT DISTINCT rezepte.* FROM rezepte " +
            "WHERE titel LIKE '%' || :suchtext || '%' COLLATE NOCASE " +
            "OR id IN (" +
            "SELECT rk.rezeptId FROM rezept_kategorien rk " +
            "INNER JOIN kategorien k ON k.id = rk.kategorieId " +
            "WHERE k.name LIKE '%' || :suchtext || '%' COLLATE NOCASE" +
            ") " +
            "OR id IN (" +
            "SELECT rz.rezeptId FROM rezept_zutaten rz " +
            "INNER JOIN zutaten z ON z.id = rz.zutatId " +
            "WHERE z.name LIKE '%' || :suchtext || '%' COLLATE NOCASE" +
            ") " +
            "ORDER BY titel COLLATE NOCASE ASC")
    List<Rezept> nachSuchtextAbrufen(String suchtext);

    // Ein Rezept anhand seiner ID abrufen
    @Query("SELECT * FROM rezepte WHERE id = :id")
    Rezept nachIdAbrufen(int id);
}
