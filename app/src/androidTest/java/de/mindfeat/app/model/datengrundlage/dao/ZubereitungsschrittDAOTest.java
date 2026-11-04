package de.mindfeat.app.model.datengrundlage.dao;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;
import de.mindfeat.app.model.datengrundlage.entitaeten.Zubereitungsschritt;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class ZubereitungsschrittDAOTest {

    private ZubereitungsschrittDAOTestDatenbank datenbank;
    private RezeptDAO rezeptDAO;
    private ZubereitungsschrittDAO zubereitungsschrittDAO;

    // Vor jedem Test eine neue In-Memory-Datenbank erstellen
    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        datenbank = Room.inMemoryDatabaseBuilder(context, ZubereitungsschrittDAOTestDatenbank.class).allowMainThreadQueries().build();
        rezeptDAO = datenbank.rezeptDAO();
        zubereitungsschrittDAO = datenbank.zubereitungsschrittDAO();
    }

    // Nach jedem Test die Datenbank schließen
    @After
    public void tearDown() {
        datenbank.close();
    }

    // Hilfsmethode zum Erstellen eines gespeicherten Rezepts
    private int rezeptErstellen(String titel) {
        Rezept rezept = new Rezept(titel, null, 2);
        return (int) rezeptDAO.einfuegen(rezept);
    }

    // Einen Zubereitungsschritt speichern und anhand seiner ID abrufen
    @Test
    public void einfuegenUndNachIdAbrufen() {
        int rezeptId = rezeptErstellen("Gulasch");
        Zubereitungsschritt schritt = new Zubereitungsschritt(rezeptId, 1, "Zwiebeln, Knoblauch und Paprika schneiden.");
        long id = zubereitungsschrittDAO.einfuegen(schritt);
        Zubereitungsschritt gespeichert = zubereitungsschrittDAO.nachIdAbrufen((int) id);
        assertNotNull(gespeichert);
        assertEquals(rezeptId, gespeichert.getRezeptId());
        assertEquals(1, gespeichert.getReihenfolge());
        assertEquals("Zwiebeln, Knoblauch und Paprika schneiden.", gespeichert.getBeschreibung());
    }

    // Einen vorhandenen Zubereitungsschritt aktualisieren
    @Test
    public void aktualisieren() {
        int rezeptId = rezeptErstellen("Gulasch");
        Zubereitungsschritt schritt = new Zubereitungsschritt(rezeptId, 1, "Zwiebeln und Knoblauch schneiden.");
        long id = zubereitungsschrittDAO.einfuegen(schritt);
        schritt.setId((int) id);
        schritt.setBeschreibung("Zwiebeln, Knoblauch und Paprika schneiden.");
        int anzahl = zubereitungsschrittDAO.aktualisieren(schritt);
        Zubereitungsschritt aktualisiert = zubereitungsschrittDAO.nachIdAbrufen((int) id);
        assertEquals(1, anzahl);
        assertEquals("Zwiebeln, Knoblauch und Paprika schneiden.", aktualisiert.getBeschreibung());
    }

    // Ein Zubereitungsschritt gelöscht werden kann
    @Test
    public void loeschen() {
        int rezeptId = rezeptErstellen("Gulasch");
        Zubereitungsschritt schritt = new Zubereitungsschritt(rezeptId, 1, "Gulasch mit Soßenbinder andicken.");
        long id = zubereitungsschrittDAO.einfuegen(schritt);
        schritt.setId((int) id);
        int anzahl = zubereitungsschrittDAO.loeschen(schritt);
        assertEquals(1, anzahl);
        assertNull(zubereitungsschrittDAO.nachIdAbrufen((int) id));
    }

    // Die Zubereitungsschritte eines Rezepts in der richtigen Reihenfolge abrufen
    @Test
    public void nachRezeptIdAbrufenSortiert() {
        int rezeptId = rezeptErstellen("Gulasch");
        int anderesRezeptId = rezeptErstellen("Baumstriezel");
        // Schritte in umgekehrter Reihenfolge einfügen
        zubereitungsschrittDAO.einfuegen(new Zubereitungsschritt(rezeptId, 2, "Zwiebeln andünsten."));
        zubereitungsschrittDAO.einfuegen(new Zubereitungsschritt(rezeptId, 1, "Zwiebeln, Knoblauch und Paprika schneiden."));
        // Schritt eines anderen Rezepts darf nicht enthalten sein
        zubereitungsschrittDAO.einfuegen(new Zubereitungsschritt(anderesRezeptId, 1, "Striezel direkt in Zimt-Zucker wälzen."));
        List<Zubereitungsschritt> schritte = zubereitungsschrittDAO.nachRezeptIdAbrufen(rezeptId);
        assertEquals(2, schritte.size());
        assertEquals(1, schritte.get(0).getReihenfolge());
        assertEquals(2, schritte.get(1).getReihenfolge());
        assertEquals("Zwiebeln, Knoblauch und Paprika schneiden.", schritte.get(0).getBeschreibung());
        assertEquals("Zwiebeln andünsten.", schritte.get(1).getBeschreibung());
    }

    // Bei einer unbekannten ID null zurückgeben
    @Test
    public void nachIdAbrufenNichtVorhanden() {
        Zubereitungsschritt gefunden = zubereitungsschrittDAO.nachIdAbrufen(999);
        assertNull(gefunden);
    }

    // Innerhalb eines Rezepts keine doppelte Reihenfolgenummer speichern
    @Test(expected = SQLiteConstraintException.class)
    public void doppelteReihenfolgeNichtEinfuegen() {
        int rezeptId = rezeptErstellen("Gulasch");
        zubereitungsschrittDAO.einfuegen(new Zubereitungsschritt(rezeptId, 1, "Zwiebeln, Knoblauch und Paprika schneiden."));
        zubereitungsschrittDAO.einfuegen(new Zubereitungsschritt(rezeptId, 1, "Zwiebeln andünsten."));
    }

    // Beim Löschen eines Rezepts dessen Zubereitungsschritte ebenfalls löschen
    @Test
    public void loeschenRezeptLoeschtZubereitungsschritteMit() {
        int rezeptId = rezeptErstellen("Gulasch");
        zubereitungsschrittDAO.einfuegen(new Zubereitungsschritt(rezeptId, 1, "Zwiebeln, Knoblauch und Paprika schneiden."));
        zubereitungsschrittDAO.einfuegen(new Zubereitungsschritt(rezeptId, 2, "Zwiebeln andünsten."));
        Rezept rezept = rezeptDAO.nachIdAbrufen(rezeptId);
        rezeptDAO.loeschen(rezept);
        List<Zubereitungsschritt> schritte = zubereitungsschrittDAO.nachRezeptIdAbrufen(rezeptId);
        assertEquals(0, schritte.size());
    }
}
