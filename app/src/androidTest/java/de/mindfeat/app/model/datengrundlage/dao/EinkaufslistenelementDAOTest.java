package de.mindfeat.app.model.datengrundlage.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import de.mindfeat.app.model.datengrundlage.TestDatenbank;
import de.mindfeat.app.model.datengrundlage.entitaeten.Einkaufslistenelement;
import de.mindfeat.app.model.datengrundlage.entitaeten.Zutat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class EinkaufslistenelementDAOTest {

    private TestDatenbank testDatenbank;
    private EinkaufslistenbeitragDAO einkaufslistenbeitragDAO;
    private EinkaufslistenelementDAO einkaufslistenelementDAO;
    private EssensplanDAO essensplanDAO;
    private ZutatDAO zutatDAO;

    // Vor jedem Test eine neue In-Memory-Datenbank erstellen
    @Before
    public void setUp() {
        testDatenbank = new TestDatenbank();
        einkaufslistenbeitragDAO = testDatenbank.getDatenbank().einkaufslistenbeitragDAO();
        einkaufslistenelementDAO = testDatenbank.getDatenbank().einkaufslistenelementDAO();
        essensplanDAO = testDatenbank.getDatenbank().essensplanDAO();
        zutatDAO = testDatenbank.getDatenbank().zutatDAO();
    }

    // Nach jedem Test die Datenbank schließen
    @After
    public void tearDown() {
        if (testDatenbank != null) {
            testDatenbank.schliessen();
        }
    }

    // Eine Zutat erstellen und ihre generierte ID zurückgeben
    private int zutatErstellen(String name) {
        Zutat zutat = new Zutat(name);
        return (int) zutatDAO.einfuegen(zutat);
    }

    // Einen neuen Einkaufslisteneintrag einfügen und anhand seiner ID abrufen
    @Test
    public void einfuegenUndNachIdAbrufen() {
        int zutatId = zutatErstellen("Zwiebeln");
        Einkaufslistenelement eintrag = new Einkaufslistenelement(zutatId, 500.0, "g", false);
        long id = einkaufslistenelementDAO.einfuegen(eintrag);
        Einkaufslistenelement gespeichert = einkaufslistenelementDAO.nachIdAbrufen((int) id);
        assertEquals(zutatId, gespeichert.getZutatId());
        assertEquals(500.0, gespeichert.getMenge(), 0.001);
        assertEquals("g", gespeichert.getEinheit());
        assertFalse(gespeichert.isErledigt());
    }

    // Einen vorhandenen Einkaufslisteneintrag aktualisieren
    @Test
    public void aktualisieren() {
        int zutatId = zutatErstellen("Zwiebeln");
        Einkaufslistenelement eintrag = new Einkaufslistenelement(zutatId, 500.0, "g", false);
        long id = einkaufslistenelementDAO.einfuegen(eintrag);
        Einkaufslistenelement aktualisiert = new Einkaufslistenelement(zutatId, 750.0, "g", true);
        aktualisiert.setId((int) id);
        int anzahl = einkaufslistenelementDAO.aktualisieren(aktualisiert);
        Einkaufslistenelement gespeichert = einkaufslistenelementDAO.nachIdAbrufen((int) id);
        assertEquals(1, anzahl);
        assertEquals(750.0, gespeichert.getMenge(), 0.001);
        assertTrue(gespeichert.isErledigt());
    }

    // Einen vorhandenen Einkaufslisteneintrag löschen
    @Test
    public void loeschen() {
        int zutatId = zutatErstellen("Zwiebeln");
        Einkaufslistenelement eintrag = new Einkaufslistenelement(zutatId, 500.0, "g", false);
        long id = einkaufslistenelementDAO.einfuegen(eintrag);
        eintrag.setId((int) id);
        int anzahl = einkaufslistenelementDAO.loeschen(eintrag);
        assertEquals(1, anzahl);
        assertNull(einkaufslistenelementDAO.nachIdAbrufen((int) id));
    }

    // Alle Einkaufslisteneinträge nach ID aufsteigend abrufen
    @Test
    public void alleAbrufenSortiertNachId() {
        int zutatId1 = zutatErstellen("Zwiebeln");
        int zutatId2 = zutatErstellen("Kartoffeln");
        Einkaufslistenelement eintrag1 = new Einkaufslistenelement(zutatId1, 500.0, "g", false);
        Einkaufslistenelement eintrag2 = new Einkaufslistenelement(zutatId2, 2.0, "kg", false);
        long id1 = einkaufslistenelementDAO.einfuegen(eintrag1);
        long id2 = einkaufslistenelementDAO.einfuegen(eintrag2);
        List<Einkaufslistenelement> eintraege = einkaufslistenelementDAO.alleAbrufen();
        assertEquals(2, eintraege.size());
        assertEquals((int) id1, eintraege.get(0).getId());
        assertEquals((int) id2, eintraege.get(1).getId());
    }

    // Alle Einkaufslisteneinträge einer bestimmten Zutat abrufen
    @Test
    public void nachZutatIdAbrufen() {
        int zutatId1 = zutatErstellen("Zwiebeln");
        int zutatId2 = zutatErstellen("Kartoffeln");
        einkaufslistenelementDAO.einfuegen(new Einkaufslistenelement(zutatId1, 500.0, "g", false));
        einkaufslistenelementDAO.einfuegen(new Einkaufslistenelement(zutatId1, 2.0, "Stück", true));
        einkaufslistenelementDAO.einfuegen(new Einkaufslistenelement(zutatId2, 1.0, "kg", false));
        List<Einkaufslistenelement> eintraege = einkaufslistenelementDAO.nachZutatIdAbrufen(zutatId1);
        assertEquals(2, eintraege.size());
        assertEquals(zutatId1, eintraege.get(0).getZutatId());
        assertEquals(zutatId1, eintraege.get(1).getZutatId());
    }

    // Einkaufslisteneinträge nach Zutat und Einheit abrufen
    @Test
    public void nachZutatIdUndEinheitAbrufen() {
        int zutatId = zutatErstellen("Zwiebeln");
        einkaufslistenelementDAO.einfuegen(new Einkaufslistenelement(zutatId, 500.0, "g", false));
        einkaufslistenelementDAO.einfuegen(new Einkaufslistenelement(zutatId, 250.0, "g", false));
        einkaufslistenelementDAO.einfuegen(new Einkaufslistenelement(zutatId, 2.0, "Stück", false));
        List<Einkaufslistenelement> eintraege = einkaufslistenelementDAO.nachZutatIdUndEinheitAbrufen(zutatId, "g");
        assertEquals(2, eintraege.size());
        assertEquals("g", eintraege.get(0).getEinheit());
        assertEquals("g", eintraege.get(1).getEinheit());
    }

    // Einkaufslisteneinträge mit null als Einheit abrufen
    @Test
    public void nachZutatIdUndEinheitNullAbrufen() {
        int zutatId = zutatErstellen("Eier");
        einkaufslistenelementDAO.einfuegen(new Einkaufslistenelement(zutatId, null, null, false));
        einkaufslistenelementDAO.einfuegen(new Einkaufslistenelement(zutatId, 6.0, "Stück", false));
        List<Einkaufslistenelement> eintraege = einkaufslistenelementDAO.nachZutatIdUndEinheitAbrufen(zutatId, null);
        assertEquals(1, eintraege.size());
        assertNull(eintraege.get(0).getEinheit());
    }

    // Bei einer nicht vorhandenen ID null zurückgeben
    @Test
    public void nachIdAbrufenNichtVorhanden() {
        Einkaufslistenelement gespeichert = einkaufslistenelementDAO.nachIdAbrufen(999);
        assertNull(gespeichert);
    }

    // Beim Löschen einer Zutat ihre Einkaufslisteneinträge mitlöschen
    @Test
    public void loeschenZutatLoeschtEinkaufslistenelementeMit() {
        Zutat zutat = new Zutat("Zwiebeln");
        long zutatId = zutatDAO.einfuegen(zutat);
        zutat.setId((int) zutatId);
        einkaufslistenelementDAO.einfuegen(new Einkaufslistenelement((int) zutatId, 500.0, "g", false));
        int anzahl = zutatDAO.loeschen(zutat);
        List<Einkaufslistenelement> eintraege = einkaufslistenelementDAO.alleAbrufen();
        assertEquals(1, anzahl);
        assertTrue(eintraege.isEmpty());
    }
}
