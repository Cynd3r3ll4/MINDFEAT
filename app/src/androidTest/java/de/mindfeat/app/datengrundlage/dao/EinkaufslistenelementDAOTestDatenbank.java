package de.mindfeat.app.datengrundlage.dao;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import de.mindfeat.app.datengrundlage.entitaeten.Einkaufslistenelement;
import de.mindfeat.app.datengrundlage.entitaeten.Zutat;

@Database(
        entities = {
                Zutat.class,
                Einkaufslistenelement.class
        },
        version = 1,
        exportSchema = false
)
public abstract class EinkaufslistenelementDAOTestDatenbank extends RoomDatabase{

    // Zugriff auf den Zutat-DAO
    public abstract ZutatDAO zutatDAO();

    // Zugriff auf den Einkaufslistenelement-DAO
    public abstract EinkaufslistenelementDAO einkaufslistenelementDAO();
}
