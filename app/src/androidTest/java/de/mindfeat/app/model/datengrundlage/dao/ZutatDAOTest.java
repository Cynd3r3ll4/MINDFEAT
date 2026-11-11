package de.mindfeat.app.model.datengrundlage.dao;

import android.database.sqlite.SQLiteConstraintException;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import de.mindfeat.app.model.datengrundlage.TestDatenbank;
import de.mindfeat.app.model.datengrundlage.entitaeten.Zutat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)

public class ZutatDAOTest {

    private TestDatenbank testDatenbank;
    private ZutatDAO zutatDAO;

    // Eine In-Memory-Datenbank für die Tests erstellen
    @Before
    public void setUp() {
        testDatenbank = new TestDatenbank();
        zutatDAO = testDatenbank.getDatenbank().zutatDAO();
    }

    // Die Datenbank nach jedem Test schließen
    @After
    public void tearDown() {
        if (testDatenbank != null) {
            testDatenbank.schliessen();
        }
    }

    // Eine Zutat wird gespeichert und kann anhand ihrer ID abgerufen werden
    @Test
    public void zutatEinfuegenUndAbrufen() {
        Zutat zutat = new Zutat("Kartoffeln");
        long id = zutatDAO.einfuegen(zutat);
        Zutat gespeicherteZutat = zutatDAO.nachIdAbrufen((int) id);
        assertNotNull(gespeicherteZutat);
        assertEquals("Kartoffeln", gespeicherteZutat.getName());
    }

    // Eine vorhandene Zutat wird aktualisiert
    @Test
    public void zutatAktualisieren() {
        Zutat zutat = new Zutat("Tomaten");
        long id = zutatDAO.einfuegen(zutat);
        zutat.setId((int) id);
        zutat.setName("Cherrytomaten");
        int anzahlAktualisierterZutaten = zutatDAO.aktualisieren(zutat);
        Zutat aktualisierteZutat = zutatDAO.nachIdAbrufen((int) id);
        assertEquals(1, anzahlAktualisierterZutaten);
        assertEquals("Cherrytomaten", aktualisierteZutat.getName());
    }

    // Eine vorhandene Zutat wird gelöscht
    @Test
    public void zutatLoeschen() {
        Zutat zutat = new Zutat("Kartoffeln");
        long id = zutatDAO.einfuegen(zutat);
        zutat.setId((int) id);
        int anzahlGeloeschterZutaten = zutatDAO.loeschen(zutat);
        assertEquals(1, anzahlGeloeschterZutaten);
        assertNull(zutatDAO.nachIdAbrufen((int) id));
    }

    // Alle gespeicherten Zutaten werden alphabetisch abgerufen
    @Test
    public void alleZutatenAbrufen() {
        zutatDAO.einfuegen(new Zutat("Zucchini"));
        zutatDAO.einfuegen(new Zutat("Kartoffeln"));
        zutatDAO.einfuegen(new Zutat("Äpfel"));
        List<Zutat> zutaten = zutatDAO.alleAbrufen();
        assertEquals(3, zutaten.size());
        assertEquals("Kartoffeln", zutaten.get(0).getName());
        assertEquals("Zucchini", zutaten.get(1).getName());
        assertEquals("Äpfel", zutaten.get(2).getName());
    }

    // Eine Zutat kann anhand ihres Namens abgerufen werden
    @Test
    public void zutatNachNameAbrufen() {
        zutatDAO.einfuegen(new Zutat("Kartoffeln"));
        Zutat gefundeneZutat = zutatDAO.nachNameAbrufen("Kartoffeln");
        assertNotNull(gefundeneZutat);
        assertEquals("Kartoffeln", gefundeneZutat.getName());
    }

    // Eine Zutat wird auch bei abweichender Groß- und Kleinschreibung gefunden
    @Test
    public void zutatNachNameOhneBeachtungDerGrossUndKleinschreibungAbrufen() {
        zutatDAO.einfuegen(new Zutat("Kartoffeln"));
        Zutat gefundeneZutat = zutatDAO.nachNameAbrufen("kartoffeln");
        assertNotNull(gefundeneZutat);
        assertEquals("Kartoffeln", gefundeneZutat.getName());
    }

    // Eine nicht vorhandene Zutat liefert kein Ergebnis
    @Test
    public void nichtVorhandeneZutatAbrufen() {
        Zutat zutat = zutatDAO.nachIdAbrufen(999);
        assertNull(zutat);
    }

    // Doppelte Zutatennamen auch bei unterschiedlicher Groß- und Kleinschreibung verhindern
    @Test(expected = SQLiteConstraintException.class)
    public void doppeltenNamenNichtEinfuegen() {
        zutatDAO.einfuegen(new Zutat("Kartoffeln"));
        zutatDAO.einfuegen(new Zutat("KARTOFFELN"));
    }
}
