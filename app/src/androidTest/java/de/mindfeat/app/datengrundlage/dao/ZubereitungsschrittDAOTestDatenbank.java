package de.mindfeat.app.datengrundlage.dao;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import de.mindfeat.app.datengrundlage.entitaeten.Rezept;
import de.mindfeat.app.datengrundlage.entitaeten.Zubereitungsschritt;

@Database(
        entities = {
                Rezept.class,
                Zubereitungsschritt.class
        },
        version = 1,
        exportSchema = false
)

public abstract class ZubereitungsschrittDAOTestDatenbank extends RoomDatabase {

    // Zugriff auf den Rezept-DAO
    public abstract RezeptDAO rezeptDAO();

    // Zugriff auf den Zubereitungsschritt-DAO
    public abstract ZubereitungsschrittDAO zubereitungsschrittDAO();
}
