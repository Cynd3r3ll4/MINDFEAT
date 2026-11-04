package de.mindfeat.app.model.datengrundlage.dao;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import de.mindfeat.app.model.datengrundlage.entitaeten.Essensplan;
import de.mindfeat.app.model.datengrundlage.entitaeten.InhaltEssensplan;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;

@Database(
        entities = {
                Essensplan.class,
                Rezept.class,
                InhaltEssensplan.class
        },
        version = 1,
        exportSchema = false
)

public abstract class InhaltEssensplanDAOTestDatenbank extends RoomDatabase {

    // Zugriff auf den Essensplan-DAO
    public abstract EssensplanDAO essensplanDAO();

    // Zugriff auf den Rezept-DAO
    public abstract RezeptDAO rezeptDAO();

    // Zugriff auf den InhaltEssensplan-DAO
    public abstract InhaltEssensplanDAO inhaltEssensplanDAO();
}
