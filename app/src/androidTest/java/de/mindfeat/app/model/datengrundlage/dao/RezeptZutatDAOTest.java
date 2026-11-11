package de.mindfeat.app.model.datengrundlage.dao;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import de.mindfeat.app.model.datengrundlage.TestDatenbank;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;
import de.mindfeat.app.model.datengrundlage.entitaeten.RezeptZutat;
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

public class RezeptZutatDAOTest {

    private TestDatenbank testDatenbank;
    private RezeptDAO rezeptDAO;
    private ZutatDAO zutatDAO;
    private RezeptZutatDAO rezeptZutatDAO;

    // Eine In-Memory-Datenbank für die Tests erstellen
    @Before
    public void setUp() {
        testDatenbank = new TestDatenbank();
        rezeptDAO = testDatenbank.getDatenbank().rezeptDAO();
        zutatDAO = testDatenbank.getDatenbank().zutatDAO();
        rezeptZutatDAO = testDatenbank.getDatenbank().rezeptZutatDAO();
    }

    // Die Datenbank nach jedem Test schließen
    @After
    public void tearDown() {
        if (testDatenbank != null) {
            testDatenbank.schliessen();
        }
    }

    // Ein Rezept für die Testdaten anlegen
    private Rezept rezeptErstellen(String titel) {
        Rezept rezept = new Rezept(titel, null, 2);
        long id = rezeptDAO.einfuegen(rezept);
        rezept.setId((int) id);
        return rezept;
    }

    // Eine Zutat für die Testdaten anlegen
    private Zutat zutatErstellen(String name) {
        Zutat zutat = new Zutat(name);
        long id = zutatDAO.einfuegen(zutat);
        zutat.setId((int) id);
        return zutat;
    }

    // Eine Rezept-Zutat-Zuordnung wird gespeichert und anhand ihrer ID abgerufen
    @Test
    public void rezeptZutatEinfuegenUndAbrufen() {
        Rezept rezept = rezeptErstellen("Kartoffelsuppe");
        Zutat zutat = zutatErstellen("Kartoffeln");
        RezeptZutat rezeptZutat = new RezeptZutat(rezept.getId(), zutat.getId(), 800.0, "g");
        long id = rezeptZutatDAO.einfuegen(rezeptZutat);
        RezeptZutat gespeicherteZuordnung = rezeptZutatDAO.nachIdAbrufen((int) id);
        assertNotNull(gespeicherteZuordnung);
        assertEquals(rezept.getId(), gespeicherteZuordnung.getRezeptId());
        assertEquals(zutat.getId(), gespeicherteZuordnung.getZutatId());
        assertEquals(800.0, gespeicherteZuordnung.getMenge(), 0.001);
        assertEquals("g", gespeicherteZuordnung.getEinheit());
    }

    // Eine vorhandene Rezept-Zutat-Zuordnung wird aktualisiert
    @Test
    public void rezeptZutatAktualisieren() {
        Rezept rezept = rezeptErstellen("Kartoffelsuppe");
        Zutat zutat = zutatErstellen("Kartoffeln");

        RezeptZutat rezeptZutat = new RezeptZutat(rezept.getId(), zutat.getId(), 800.0, "g");
        long id = rezeptZutatDAO.einfuegen(rezeptZutat);
        rezeptZutat.setId((int) id);
        rezeptZutat.setMenge(1.0);
        rezeptZutat.setEinheit("kg");
        int anzahlAktualisierterZuordnungen = rezeptZutatDAO.aktualisieren(rezeptZutat);
        RezeptZutat aktualisierteZuordnung = rezeptZutatDAO.nachIdAbrufen((int) id);
        assertEquals(1, anzahlAktualisierterZuordnungen);
        assertEquals(1.0, aktualisierteZuordnung.getMenge(), 0.001);
        assertEquals("kg", aktualisierteZuordnung.getEinheit());
    }

    // Eine vorhandene Rezept-Zutat-Zuordnung wird gelöscht
    @Test
    public void rezeptZutatLoeschen() {
        Rezept rezept = rezeptErstellen("Kartoffelsuppe");
        Zutat zutat = zutatErstellen("Kartoffeln");
        RezeptZutat rezeptZutat = new RezeptZutat(rezept.getId(), zutat.getId(), 800.0, "g");
        long id = rezeptZutatDAO.einfuegen(rezeptZutat);
        rezeptZutat.setId((int) id);
        int anzahlGeloeschterZuordnungen = rezeptZutatDAO.loeschen(rezeptZutat);
        assertEquals(1, anzahlGeloeschterZuordnungen);
        assertNull(rezeptZutatDAO.nachIdAbrufen((int) id));
    }

    // Alle Zutaten-Zuordnungen eines Rezepts werden abgerufen
    @Test
    public void nachRezeptIdAbrufen() {
        Rezept rezept = rezeptErstellen("Kartoffelsuppe");
        Zutat kartoffeln = zutatErstellen("Kartoffeln");
        Zutat zwiebeln = zutatErstellen("Zwiebel");
        Rezept anderesRezept = rezeptErstellen("Gemüsesuppe");
        Zutat karotte = zutatErstellen("Karotte");
        rezeptZutatDAO.einfuegen(new RezeptZutat(rezept.getId(), kartoffeln.getId(), 800.0, "g"));
        rezeptZutatDAO.einfuegen(new RezeptZutat(rezept.getId(), zwiebeln.getId(), 2.0, "Stück"));
        rezeptZutatDAO.einfuegen(new RezeptZutat(anderesRezept.getId(), karotte.getId(), 3.0, "Stück"));
        List<RezeptZutat> zuordnungen = rezeptZutatDAO.nachRezeptIdAbrufen(rezept.getId());
        assertEquals(2, zuordnungen.size());
        assertEquals(kartoffeln.getId(), zuordnungen.get(0).getZutatId());
        assertEquals(zwiebeln.getId(), zuordnungen.get(1).getZutatId());
    }

    // Alle Rezept-Zuordnungen einer Zutat werden abgerufen
    @Test
    public void nachZutatIdAbrufen() {
        Zutat kartoffel = zutatErstellen("Kartoffeln");
        Rezept rezept1 = rezeptErstellen("Kartoffelsuppe");
        Rezept rezept2 = rezeptErstellen("Kartoffelsalat");
        rezeptZutatDAO.einfuegen(new RezeptZutat(rezept1.getId(), kartoffel.getId(), 800.0, "g"));
        rezeptZutatDAO.einfuegen(new RezeptZutat(rezept2.getId(), kartoffel.getId(), 3.0, "Stück"));
        List<RezeptZutat> zuordnungen = rezeptZutatDAO.nachZutatIdAbrufen(kartoffel.getId());
        assertEquals(2, zuordnungen.size());
        assertEquals(rezept1.getId(), zuordnungen.get(0).getRezeptId());
        assertEquals(rezept2.getId(), zuordnungen.get(1).getRezeptId());
    }

    // Eine nicht vorhandene Rezept-Zutat-Zuordnung liefert kein Ergebnis
    @Test
    public void nichtVorhandeneZuordnungAbrufen() {
        RezeptZutat zuordnung = rezeptZutatDAO.nachIdAbrufen(999);
        assertNull(zuordnung);
    }

    // Beim Löschen eines Rezepts werden seine Zutaten-Zuordnungen mitgelöscht
    @Test
    public void loeschenEinesRezeptsLoeschtZuordnungen() {
        Rezept rezept = rezeptErstellen("Kartoffelsuppe");
        Zutat zutat = zutatErstellen("Kartoffeln");
        RezeptZutat rezeptZutat = new RezeptZutat(rezept.getId(), zutat.getId(), 800.0, "g");
        long id = rezeptZutatDAO.einfuegen(rezeptZutat);
        int anzahlGeloeschterRezepte = rezeptDAO.loeschen(rezept);
        assertEquals(1, anzahlGeloeschterRezepte);
        assertNull(rezeptZutatDAO.nachIdAbrufen((int) id));
    }
}
