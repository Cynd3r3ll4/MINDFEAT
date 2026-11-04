package de.mindfeat.app.datengrundlage.dao;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import de.mindfeat.app.datengrundlage.entitaeten.Essensplan;

@Database(
        entities = {
                Essensplan.class
        },
        version = 1,
        exportSchema = false
)
public abstract class EssensplanDAOTestDatenbank extends RoomDatabase{

    // Zugriff auf den Essensplan-DAO
    public abstract EssensplanDAO essensplanDAO();
}
