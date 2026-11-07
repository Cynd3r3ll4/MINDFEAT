package de.mindfeat.app.model.datengrundlage;

import androidx.room.Database;
import androidx.room.RoomDatabase;

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
                Einkaufslistenelement.class
        },
        version = 1,
        exportSchema = false
)

public abstract class ZentraleDatenbankV1Test extends RoomDatabase {

    public abstract RezeptDAO rezeptDAO();

    public abstract ZutatDAO zutatDAO();

    public abstract RezeptZutatDAO rezeptZutatDAO();

    public abstract KategorieDAO kategorieDAO();

    public abstract RezeptKategorieDAO rezeptKategorieDAO();

    public abstract ZubereitungsschrittDAO zubereitungsschrittDAO();

    public abstract EssensplanDAO essensplanDAO();

    public abstract InhaltEssensplanDAO inhaltEssensplanDAO();

    public abstract EinkaufslistenelementDAO einkaufslistenelementDAO();
}
