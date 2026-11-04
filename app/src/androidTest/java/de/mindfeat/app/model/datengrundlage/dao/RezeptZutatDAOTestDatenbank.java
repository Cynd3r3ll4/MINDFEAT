package de.mindfeat.app.model.datengrundlage.dao;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;
import de.mindfeat.app.model.datengrundlage.entitaeten.RezeptZutat;
import de.mindfeat.app.model.datengrundlage.entitaeten.Zutat;

// Testdatenbank für die Rezept-Zutaten-Zuordnung
@Database(
        entities = {
                Rezept.class,
                Zutat.class,
                RezeptZutat.class
        },
        version = 1,
        exportSchema = false
)
public abstract class RezeptZutatDAOTestDatenbank extends RoomDatabase {

    public abstract RezeptDAO rezeptDAO();

    public abstract ZutatDAO zutatDAO();

    public abstract RezeptZutatDAO rezeptZutatDAO();
}
