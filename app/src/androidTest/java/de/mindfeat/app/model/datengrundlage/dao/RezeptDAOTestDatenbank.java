package de.mindfeat.app.model.datengrundlage.dao;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;

// Testdatenbank für Test unabhängig von der Produktivdatenbank
@Database(
        entities = {Rezept.class},
        version = 1,
        exportSchema = false
)

public abstract class RezeptDAOTestDatenbank extends RoomDatabase {

    public abstract RezeptDAO rezeptDAO();
}
