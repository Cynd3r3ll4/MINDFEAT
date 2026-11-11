package de.mindfeat.app.model.datengrundlage;

import android.content.Context;
import android.database.Cursor;

import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.mindfeat.app.model.datengrundlage.entitaeten.Einkaufslistenbeitrag;
import de.mindfeat.app.model.datengrundlage.entitaeten.Einkaufslistenelement;
import de.mindfeat.app.model.datengrundlage.entitaeten.Zutat;

@RunWith(AndroidJUnit4.class)
public class ZentraleDatenbankMigrationTest {

    private static final String DATENBANKNAME = "mindfeat_migration_test.db";

    // bei der Migration von Version 1 auf 3 bleiben alle Daten erhalten und eine neue Tabelle wird erstellt
    @Test
    public void migrationVonVersion1AufVersion3ErhaeltBestehendeDatenUndErstelltNeueTabelle() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(DATENBANKNAME); // Eventuell vorhandene Testdatenbankdatei entfernen
        // 1. Datenbank mit dem alten Schema (Version 1) erstellen
        ZentraleDatenbankV1Test alteDatenbank = Room.databaseBuilder(context, ZentraleDatenbankV1Test.class, DATENBANKNAME).allowMainThreadQueries().build();
        // 2. Bestehende Daten in Version 1 speichern
        Zutat zutat = new Zutat("Paprika");
        int zutatId = (int) alteDatenbank.zutatDAO().einfuegen(zutat);
        Einkaufslistenelement einkaufslistenelement = new Einkaufslistenelement(zutatId, 2.0, "Stück", false);
        int einkaufslistenelementId = (int) alteDatenbank.einkaufslistenelementDAO().einfuegen(einkaufslistenelement);
        // 3. Alte Datenbank schließen
        alteDatenbank.close();
        // 4. Dieselbe Datenbankdatei mit Version 2 öffnen
        ZentraleDatenbank neueDatenbank = Room.databaseBuilder(context, ZentraleDatenbank.class, DATENBANKNAME).addMigrations(ZentraleDatenbank.MIGRATION_1_2,
                ZentraleDatenbank.MIGRATION_2_3).allowMainThreadQueries().build();
        try {
            Zutat gespeicherteZutat = neueDatenbank.zutatDAO().nachIdAbrufen(zutatId); // 5. Prüfen, ob die bisherigen Daten erhalten geblieben sind
            assertNotNull(gespeicherteZutat);
            assertEquals("Paprika", gespeicherteZutat.getName());
            Einkaufslistenelement gespeichertesElement = neueDatenbank.einkaufslistenelementDAO().nachIdAbrufen(einkaufslistenelementId);
            assertNotNull(gespeichertesElement);
            assertEquals(zutatId, gespeichertesElement.getZutatId());
            // 6. Prüfen, ob die neue Tabelle angelegt wurde
            Cursor cursor = neueDatenbank.getOpenHelper().getReadableDatabase().query(
                            "SELECT name FROM sqlite_master " +
                                    "WHERE type = 'table' " +
                                    "AND name = 'einkaufslistenbeitraege'");
            try {
                assertTrue("Die Tabelle einkaufslistenbeitraege wurde nicht angelegt.", cursor.moveToFirst());
            } finally {
                cursor.close();
            }
            // 7. Prüfen, ob der neue DAO verfügbar ist
            assertNotNull(neueDatenbank.einkaufslistenbeitragDAO());
            // 8. Neuen Beitrag nach der Migration speichern
            Einkaufslistenbeitrag beitrag = new Einkaufslistenbeitrag(einkaufslistenelementId, null, 1.0);
            neueDatenbank.einkaufslistenbeitragDAO().einfuegen(beitrag);
            // 9. Prüfen, ob der neue Beitrag tatsächlich gespeichert wurde
            Cursor beitragCursor = neueDatenbank.getOpenHelper().getReadableDatabase().query(
                            "SELECT COUNT(*) FROM einkaufslistenbeitraege " +
                                    "WHERE einkaufslistenelementId = ?",
                            new Object[]{einkaufslistenelementId});
            try {
                assertTrue(beitragCursor.moveToFirst());
                assertEquals(1, beitragCursor.getInt(0));
            } finally {
                beitragCursor.close();
            }
        } finally {
            // 10. Datenbank schließen und Testdatei entfernen
            neueDatenbank.close();
            context.deleteDatabase(DATENBANKNAME);
        }
    }
}
