package de.mindfeat.app.model.datengrundlage.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import de.mindfeat.app.model.datengrundlage.TestDatenbank;
import de.mindfeat.app.model.datengrundlage.entitaeten.Essensplan;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class EssensplanDAOTest {

    private TestDatenbank testDatenbank;
    private EssensplanDAO essensplanDAO;

    // Vor jedem Test eine neue In-Memory-Datenbank erstellen
    @Before
    public void setUp() {
        testDatenbank = new TestDatenbank();
        essensplanDAO = testDatenbank.getDatenbank().essensplanDAO();
    }

    // Nach jedem Test die Datenbank schließen
    @After
    public void tearDown() {
        if (testDatenbank != null) {
            testDatenbank.schliessen();
        }
    }

    // Einen neuen Essensplan einfügen und anhand seiner ID abrufen
    @Test
    public void einfuegenUndNachIdAbrufen() {
        Essensplan essensplan = new Essensplan("Wochenplan", "2026-11-02", "2026-11-08", 1);
        long id = essensplanDAO.einfuegen(essensplan);
        Essensplan gespeichert = essensplanDAO.nachIdAbrufen((int) id);
        assertNotNull(gespeichert);
        assertEquals("Wochenplan", gespeichert.getTitel());
        assertEquals("2026-11-02", gespeichert.getStartDatum());
        assertEquals("2026-11-08", gespeichert.getEndDatum());
        assertEquals(1, gespeichert.getWochenstartTag());
    }

    // Einen vorhandenen Essensplan aktualisieren
    @Test
    public void aktualisieren() {
        Essensplan essensplan = new Essensplan("Alter Plan", "2026-11-02", "2026-11-08", 1);
        long id = essensplanDAO.einfuegen(essensplan);
        essensplan.setId((int) id);
        Essensplan aktualisiert = new Essensplan("Neuer Plan", "2026-11-09", "2026-11-15", 2);
        aktualisiert.setId((int) id);
        int anzahl = essensplanDAO.aktualisieren(aktualisiert);
        Essensplan gespeichert = essensplanDAO.nachIdAbrufen((int) id);
        assertEquals(1, anzahl);
        assertEquals("Neuer Plan", gespeichert.getTitel());
        assertEquals("2026-11-09", gespeichert.getStartDatum());
        assertEquals("2026-11-15", gespeichert.getEndDatum());
        assertEquals(2, gespeichert.getWochenstartTag());
    }

    // Einen vorhandenen Essensplan löschen
    @Test
    public void loeschen() {
        Essensplan essensplan = new Essensplan("Wochenplan", "2026-11-02", "2026-11-08", 1);
        long id = essensplanDAO.einfuegen(essensplan);
        essensplan.setId((int) id);
        int anzahl = essensplanDAO.loeschen(essensplan);
        assertEquals(1, anzahl);
        assertNull(essensplanDAO.nachIdAbrufen((int) id));
    }

    // Alle Essenspläne nach Startdatum aufsteigend abrufen
    @Test
    public void alleAbrufenSortiertNachStartdatum() {
        Essensplan planSpaeter = new Essensplan("Späterer Plan", "2026-11-09", "2026-11-15", 1);
        Essensplan planFrueher = new Essensplan("Früherer Plan", "2026-11-02", "2026-11-08", 1);
        essensplanDAO.einfuegen(planSpaeter);
        essensplanDAO.einfuegen(planFrueher);
        List<Essensplan> plaene = essensplanDAO.alleAbrufen();
        assertEquals(2, plaene.size());
        assertEquals("Früherer Plan", plaene.get(0).getTitel());
        assertEquals("Späterer Plan", plaene.get(1).getTitel());
    }

    // Bei einer nicht vorhandenen ID null zurückgeben
    @Test
    public void nachIdAbrufenNichtVorhanden() {
        Essensplan gespeichert = essensplanDAO.nachIdAbrufen(999);
        assertNull(gespeichert);
    }

    // Einen Essensplan ohne Titel speichern
    @Test
    public void einfuegenMitOptionalemTitel() {
        Essensplan essensplan = new Essensplan(null, "2026-11-02", "2026-11-08", 1);
        long id = essensplanDAO.einfuegen(essensplan);
        Essensplan gespeichert = essensplanDAO.nachIdAbrufen((int) id);
        assertNotNull(gespeichert);
        assertNull(gespeichert.getTitel());
    }
}
