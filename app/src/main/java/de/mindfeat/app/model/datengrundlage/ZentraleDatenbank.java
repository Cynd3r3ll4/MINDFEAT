package de.mindfeat.app.model.datengrundlage;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import de.mindfeat.app.model.datengrundlage.dao.RezeptDAO;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;

@Database(
        entities = {Rezept.class},
        version = 1,
        exportSchema = false
)
public abstract class ZentraleDatenbank extends RoomDatabase {

    private static volatile ZentraleDatenbank instanz;

    // Zugriff auf das Rezept-DAO bereitstellen
    public abstract RezeptDAO rezeptDAO();

    // Eine zentrale Datenbankinstanz für die App bereitstellen
    public static ZentraleDatenbank getInstance(Context context) {

        if (instanz == null) {
            synchronized (ZentraleDatenbank.class) {
                if (instanz == null) {
                    instanz = Room.databaseBuilder(context.getApplicationContext(), ZentraleDatenbank.class, "mindfeat_datenbank").build();
                }
            }
        }
        return instanz;
    }
}
