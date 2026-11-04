package de.mindfeat.app.model.datengrundlage.dao;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import de.mindfeat.app.model.datengrundlage.entitaeten.Kategorie;

@Database(
        entities = {Kategorie.class},
        version = 1,
        exportSchema = false
)

public abstract class KategorieDAOTestDatenbank extends RoomDatabase {

    // Zugriff auf den Kategorie-DAO
    public abstract KategorieDAO kategorieDAO();
}
