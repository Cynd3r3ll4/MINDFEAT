package de.mindfeat.app.model.datengrundlage.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import de.mindfeat.app.model.datengrundlage.entitaeten.InhaltEssensplan;

import java.util.List;

@Dao
public interface InhaltEssensplanDAO {

    // CRUD-Operationen

    // Einen neuen Eintrag im Essensplan speichern
    @Insert
    long einfuegen(InhaltEssensplan inhaltEssensplan);

    // Einen vorhandenen Eintrag im Essensplan aktualisieren
    @Update
    int aktualisieren(InhaltEssensplan inhaltEssensplan);

    // Einen vorhandenen Eintrag aus dem Essensplan löschen
    @Delete
    int loeschen(InhaltEssensplan inhaltEssensplan);

    // Alle Einträge nach Essensplan, Datum und ID sortiert abrufen
    @Query("SELECT * FROM inhalt_essensplan ORDER BY essensplanId ASC, datum ASC, id ASC")
    List<InhaltEssensplan> alleAbrufen();

    // Einen Eintrag anhand seiner ID abrufen
    @Query("SELECT * FROM inhalt_essensplan WHERE id = :id")
    InhaltEssensplan nachIdAbrufen(int id);

    // Alle Einträge eines Essensplans chronologisch abrufen
    @Query("SELECT * FROM inhalt_essensplan WHERE essensplanId = :essensplanId ORDER BY datum ASC, id ASC")
    List<InhaltEssensplan> nachEssensplanIdAbrufen(int essensplanId);

    // Alle Einträge für ein bestimmtes Datum abrufen
    @Query("SELECT * FROM inhalt_essensplan WHERE datum = :datum ORDER BY essensplanId ASC, id ASC")
    List<InhaltEssensplan> nachDatumAbrufen(String datum);
}
