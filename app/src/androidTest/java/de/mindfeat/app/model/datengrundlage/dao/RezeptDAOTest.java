package de.mindfeat.app.model.datengrundlage.dao;

import android.content.Context;

import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)

public class RezeptDAOTest {

    private RezeptDAOTestDatenbank datenbank;
    private RezeptDAO rezeptDAO;

    // Eine In-Memory-Datenbank für die Tests erstellen
    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        datenbank = Room.inMemoryDatabaseBuilder(context, RezeptDAOTestDatenbank.class).allowMainThreadQueries().build();
        rezeptDAO = datenbank.rezeptDAO();
    }

    // Die Datenbank nach jedem Test schließen
    @After
    public void tearDown() {
        if (datenbank != null) {
            datenbank.close();
        }
    }

    // Ein Rezept wird gespeichert und kann anhand seiner ID abgerufen werden
    @Test
    public void rezeptEinfuegenUndAbrufen() {
        Rezept rezept = new Rezept("Graupeneintopf", null, 4);
        long id = rezeptDAO.einfuegen(rezept);
        Rezept gespeichertesRezept = rezeptDAO.nachIdAbrufen((int) id);
        assertNotNull(gespeichertesRezept);
        assertEquals("Graupeneintopf", gespeichertesRezept.getTitel());
        assertEquals(4, gespeichertesRezept.getPortionen());
    }

    // Ein vorhandenes Rezept wird aktualisiert
    @Test
    public void rezeptAktualisieren() {
        Rezept rezept = new Rezept("Graupeneintopf", null, 4);
        long id = rezeptDAO.einfuegen(rezept);
        rezept.setId((int) id);
        rezept.setTitel("Gerstensuppe");
        rezept.setPortionen(2);
        int anzahlAktualisierterRezepte = rezeptDAO.aktualisieren(rezept);
        Rezept aktualisiertesRezept = rezeptDAO.nachIdAbrufen((int) id);
        assertEquals(1, anzahlAktualisierterRezepte);
        assertEquals("Gerstensuppe", aktualisiertesRezept.getTitel());
        assertEquals(2, aktualisiertesRezept.getPortionen());
    }

    // Ein vorhandenes Rezept wird gelöscht
    @Test
    public void rezeptLoeschen() {
        Rezept rezept = new Rezept("Graupeneintopf", null, 4);
        long id = rezeptDAO.einfuegen(rezept);
        rezept.setId((int) id);
        int anzahlGeloeschterRezepte = rezeptDAO.loeschen(rezept);
        assertEquals(1, anzahlGeloeschterRezepte);
        assertNull(rezeptDAO.nachIdAbrufen((int) id));
    }

    // Alle gespeicherten Rezepte werden alphabetisch sortiert abgerufen
    @Test
    public void alleRezepteAbrufen() {
        rezeptDAO.einfuegen(new Rezept("Schnitzel", null, 2));
        rezeptDAO.einfuegen(new Rezept("Graupeneintopf", null, 4));
        rezeptDAO.einfuegen(new Rezept("Apfelpfannkuchen", null, 3));
        List<Rezept> rezepte = rezeptDAO.alleAbrufen();
        assertEquals(3, rezepte.size());
        assertEquals("Apfelpfannkuchen", rezepte.get(0).getTitel());
        assertEquals("Graupeneintopf", rezepte.get(1).getTitel());
        assertEquals("Schnitzel", rezepte.get(2).getTitel());
    }

    // Eine nicht vorhandene Rezept-ID liefert kein Ergebnis
    @Test
    public void nichtVorhandenesRezeptAbrufen() {
        Rezept rezept = rezeptDAO.nachIdAbrufen(999);
        assertNull(rezept);
    }
}
