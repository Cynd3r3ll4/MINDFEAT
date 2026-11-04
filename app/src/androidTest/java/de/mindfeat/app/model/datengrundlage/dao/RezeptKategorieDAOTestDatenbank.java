package de.mindfeat.app.model.datengrundlage.dao;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;
import de.mindfeat.app.model.datengrundlage.entitaeten.Kategorie;
import de.mindfeat.app.model.datengrundlage.entitaeten.RezeptKategorie;

@Database(
        entities = {
                Rezept.class,
                Kategorie.class,
                RezeptKategorie.class
        },
        version = 1,
        exportSchema = false
)

public abstract class RezeptKategorieDAOTestDatenbank extends RoomDatabase{

    // Zugriff auf den Rezept-DAO
    public abstract RezeptDAO rezeptDAO();

    // Zugriff auf den Kategorie-DAO
    public abstract KategorieDAO kategorieDAO();

    // Zugriff auf den Rezept-Kategorie-DAO
    public abstract RezeptKategorieDAO rezeptKategorieDAO();
}
