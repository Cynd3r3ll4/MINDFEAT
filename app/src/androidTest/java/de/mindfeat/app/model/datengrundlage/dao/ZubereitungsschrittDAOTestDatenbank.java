package de.mindfeat.app.model.datengrundlage.dao;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;
import de.mindfeat.app.model.datengrundlage.entitaeten.Zubereitungsschritt;

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
