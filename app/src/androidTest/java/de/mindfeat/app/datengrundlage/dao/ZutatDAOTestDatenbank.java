package de.mindfeat.app.datengrundlage.dao;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import de.mindfeat.app.datengrundlage.entitaeten.Zutat;

// Testdatenbank für die Zutatenverwaltung
@Database(
        entities = {Zutat.class},
        version = 1,
        exportSchema = false
)

public abstract class ZutatDAOTestDatenbank extends RoomDatabase{

    public abstract ZutatDAO zutatDAO();
}
