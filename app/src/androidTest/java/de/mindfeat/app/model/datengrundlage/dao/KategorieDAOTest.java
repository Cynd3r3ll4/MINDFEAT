package de.mindfeat.app.model.datengrundlage.dao;

import android.database.sqlite.SQLiteConstraintException;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import de.mindfeat.app.model.datengrundlage.TestDatenbank;
import de.mindfeat.app.model.datengrundlage.entitaeten.Kategorie;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)

public class KategorieDAOTest {

    private TestDatenbank testDatenbank;
    private KategorieDAO kategorieDAO;

    // Vor jedem Test eine neue In-Memory-Datenbank erstellen
    @Before
    public void setUp() {
        testDatenbank = new TestDatenbank();
        kategorieDAO = testDatenbank.getDatenbank().kategorieDAO();
    }

    // Nach jedem Test die Datenbank schließen
    @After
    public void tearDown() {
        if (testDatenbank != null) {
            testDatenbank.schliessen();
        }
    }

    // Prüfen, ob eine Kategorie gespeichert und anhand ihrer ID abgerufen werden kann
    @Test
    public void einfuegenUndNachIdAbrufen() {
        Kategorie kategorie = new Kategorie("deftig");
        long id = kategorieDAO.einfuegen(kategorie);
        Kategorie gespeichert = kategorieDAO.nachIdAbrufen((int) id);
        assertNotNull(gespeichert);
        assertEquals("deftig", gespeichert.getName());
    }

    // Prüfen, ob eine vorhandene Kategorie aktualisiert werden kann
    @Test
    public void aktualisieren() {
        Kategorie kategorie = new Kategorie("deftig");
        long id = kategorieDAO.einfuegen(kategorie);
        kategorie.setId((int) id);
        kategorie.setName("italienisch");
        int anzahl = kategorieDAO.aktualisieren(kategorie);
        Kategorie aktualisiert = kategorieDAO.nachIdAbrufen((int) id);
        assertEquals(1, anzahl);
        assertEquals("italienisch", aktualisiert.getName());
    }

    // Prüfen, ob eine Kategorie gelöscht werden kann
    @Test
    public void loeschen() {
        Kategorie kategorie = new Kategorie("herzhaft");
        long id = kategorieDAO.einfuegen(kategorie);
        kategorie.setId((int) id);
        int anzahl = kategorieDAO.loeschen(kategorie);
        assertEquals(1, anzahl);
        assertNull(kategorieDAO.nachIdAbrufen((int) id));
    }

    // Prüfen, ob alle Kategorien alphabetisch und ohne Beachtung der Großschreibung sortiert werden
    @Test
    public void alleAbrufenSortiertAlphabetisch() {
        kategorieDAO.einfuegen(new Kategorie("deftig"));
        kategorieDAO.einfuegen(new Kategorie("Nachtisch"));
        kategorieDAO.einfuegen(new Kategorie("herzhaft"));
        List<Kategorie> kategorien = kategorieDAO.alleAbrufen();
        assertEquals(3, kategorien.size());
        assertEquals("deftig", kategorien.get(0).getName());
        assertEquals("herzhaft", kategorien.get(1).getName());
        assertEquals("Nachtisch", kategorien.get(2).getName());
    }

    // Prüfen, ob eine Kategorie anhand ihres Namens gefunden werden kann
    @Test
    public void nachNameAbrufen() {
        kategorieDAO.einfuegen(new Kategorie("deftig"));
        Kategorie gefunden = kategorieDAO.nachNameAbrufen("deftig");
        assertNotNull(gefunden);
        assertEquals("deftig", gefunden.getName());
    }

    // Prüfen, ob die Suche nach dem Kategorienamen unabhängig von Groß- und Kleinschreibung funktioniert
    @Test
    public void nachNameAbrufenIgnoriertGrossUndKleinschreibung() {
        kategorieDAO.einfuegen(new Kategorie("deftig"));
        Kategorie gefunden = kategorieDAO.nachNameAbrufen("DEFTIG");
        assertNotNull(gefunden);
        assertEquals("deftig", gefunden.getName());
    }

    // Prüfen, ob bei einer unbekannten ID null zurückgegeben wird
    @Test
    public void nachIdAbrufenNichtVorhanden() {
        Kategorie gefunden = kategorieDAO.nachIdAbrufen(999);
        assertNull(gefunden);
    }

    // Prüfen, ob doppelte Kategorienamen auch bei unterschiedlicher Großschreibung verhindert werden
    @Test(expected = SQLiteConstraintException.class)
    public void doppeltenNamenNichtEinfuegen() {
        kategorieDAO.einfuegen(new Kategorie("deftig"));
        kategorieDAO.einfuegen(new Kategorie("DEFTIG"));
    }
}
