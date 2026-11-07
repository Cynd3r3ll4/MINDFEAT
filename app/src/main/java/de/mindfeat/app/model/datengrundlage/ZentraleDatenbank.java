package de.mindfeat.app.model.datengrundlage;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import de.mindfeat.app.model.datengrundlage.dao.EinkaufslistenbeitragDAO;
import de.mindfeat.app.model.datengrundlage.entitaeten.Einkaufslistenbeitrag;
import de.mindfeat.app.model.datengrundlage.dao.EinkaufslistenelementDAO;
import de.mindfeat.app.model.datengrundlage.dao.EssensplanDAO;
import de.mindfeat.app.model.datengrundlage.dao.InhaltEssensplanDAO;
import de.mindfeat.app.model.datengrundlage.dao.KategorieDAO;
import de.mindfeat.app.model.datengrundlage.dao.RezeptDAO;
import de.mindfeat.app.model.datengrundlage.dao.RezeptKategorieDAO;
import de.mindfeat.app.model.datengrundlage.dao.RezeptZutatDAO;
import de.mindfeat.app.model.datengrundlage.dao.ZubereitungsschrittDAO;
import de.mindfeat.app.model.datengrundlage.dao.ZutatDAO;

import de.mindfeat.app.model.datengrundlage.entitaeten.Einkaufslistenelement;
import de.mindfeat.app.model.datengrundlage.entitaeten.Essensplan;
import de.mindfeat.app.model.datengrundlage.entitaeten.InhaltEssensplan;
import de.mindfeat.app.model.datengrundlage.entitaeten.Kategorie;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;
import de.mindfeat.app.model.datengrundlage.entitaeten.RezeptKategorie;
import de.mindfeat.app.model.datengrundlage.entitaeten.RezeptZutat;
import de.mindfeat.app.model.datengrundlage.entitaeten.Zubereitungsschritt;
import de.mindfeat.app.model.datengrundlage.entitaeten.Zutat;

@Database(
        entities = {
                Rezept.class,
                Zutat.class,
                RezeptZutat.class,
                Kategorie.class,
                RezeptKategorie.class,
                Zubereitungsschritt.class,
                Essensplan.class,
                InhaltEssensplan.class,
                Einkaufslistenelement.class,
                Einkaufslistenbeitrag.class
        },
        version = 2,
        exportSchema = false
)
public abstract class ZentraleDatenbank extends RoomDatabase {

    private static volatile ZentraleDatenbank instanz;

    // Zugriff auf die einzelnen DAOs bereitstellen
    public abstract RezeptDAO rezeptDAO();

    public abstract ZutatDAO zutatDAO();

    public abstract RezeptZutatDAO rezeptZutatDAO();

    public abstract KategorieDAO kategorieDAO();

    public abstract RezeptKategorieDAO rezeptKategorieDAO();

    public abstract ZubereitungsschrittDAO zubereitungsschrittDAO();

    public abstract EssensplanDAO essensplanDAO();

    public abstract InhaltEssensplanDAO inhaltEssensplanDAO();

    public abstract EinkaufslistenelementDAO einkaufslistenelementDAO();

    public abstract EinkaufslistenbeitragDAO einkaufslistenbeitragDAO();


    // Eine zentrale Datenbankinstanz für die gesamte App bereitstellen

    public static ZentraleDatenbank getInstance(Context context) {
        if (instanz == null) {
            synchronized (ZentraleDatenbank.class) {
                if (instanz == null) {
                    instanz = Room.databaseBuilder(context.getApplicationContext(), ZentraleDatenbank.class, "mindfeat_datenbank").addMigrations(MIGRATION_1_2).build();
                }
            }
        }
        return instanz;
    }

    // Migration von Datenbankversion 1 auf 2
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `einkaufslistenbeitraege` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`einkaufslistenelementId` INTEGER NOT NULL, " +
                            "`essensplanId` INTEGER, " +
                            "`menge` REAL, " +
                            "FOREIGN KEY(`einkaufslistenelementId`) " +
                            "REFERENCES `einkaufslistenelemente`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE, " +
                            "FOREIGN KEY(`essensplanId`) " +
                            "REFERENCES `essensplaene`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE)");
            database.execSQL(
                    "CREATE INDEX IF NOT EXISTS " +
                            "`index_einkaufslistenbeitraege_einkaufslistenelementId` " +
                            "ON `einkaufslistenbeitraege` (`einkaufslistenelementId`)");
            database.execSQL(
                    "CREATE INDEX IF NOT EXISTS " +
                            "`index_einkaufslistenbeitraege_essensplanId` " +
                            "ON `einkaufslistenbeitraege` (`essensplanId`)");
            database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS " +
                            "`index_einkaufslistenbeitraege_einkaufslistenelementId_essensplanId` " +
                            "ON `einkaufslistenbeitraege` " +
                            "(`einkaufslistenelementId`, `essensplanId`)");
        }
    };
}
