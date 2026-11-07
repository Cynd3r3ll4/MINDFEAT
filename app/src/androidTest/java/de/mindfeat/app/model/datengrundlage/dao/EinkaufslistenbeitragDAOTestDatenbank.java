package de.mindfeat.app.model.datengrundlage.dao;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import de.mindfeat.app.model.datengrundlage.entitaeten.Einkaufslistenbeitrag;
import de.mindfeat.app.model.datengrundlage.entitaeten.Einkaufslistenelement;
import de.mindfeat.app.model.datengrundlage.entitaeten.Essensplan;
import de.mindfeat.app.model.datengrundlage.entitaeten.Zutat;

@Database(
        entities = {
                Zutat.class,
                Essensplan.class,
                Einkaufslistenelement.class,
                Einkaufslistenbeitrag.class
        },
        version = 1,
        exportSchema = false
)

public abstract class EinkaufslistenbeitragDAOTestDatenbank extends RoomDatabase {

    // Zugriff auf den Zutat-DAO
    public abstract ZutatDAO zutatDAO();

    // Zugriff auf den Essensplan-DAO
    public abstract EssensplanDAO essensplanDAO();

    // Zugriff auf den Einkaufslistenelement-DAO
    public abstract EinkaufslistenelementDAO einkaufslistenelementDAO();

    // Zugriff auf den Einkaufslistenbeitrag-DAO
    public abstract EinkaufslistenbeitragDAO einkaufslistenbeitragDAO();
}
