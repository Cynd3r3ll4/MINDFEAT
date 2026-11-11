package de.mindfeat.app.model.datengrundlage.dao;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import de.mindfeat.app.model.datengrundlage.TestDatenbank;
import de.mindfeat.app.model.datengrundlage.entitaeten.Einkaufslistenbeitrag;
import de.mindfeat.app.model.datengrundlage.entitaeten.Einkaufslistenelement;
import de.mindfeat.app.model.datengrundlage.entitaeten.Essensplan;
import de.mindfeat.app.model.datengrundlage.entitaeten.Zutat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class EinkaufslistenbeitragDAOTest {

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

    // Eine Zutat erstellen und ihre ID zurückgeben
    private int zutatErstellen(String name) {
        return (int) zutatDAO.einfuegen(new Zutat(name));
    }

    // Einen Essensplan erstellen und seine ID zurückgeben
    private int essensplanErstellen() {
        Essensplan essensplan = new Essensplan("Wochenplan", "2026-11-02", "2026-11-08", 1);
        return (int) essensplanDAO.einfuegen(essensplan);
    }

    // Ein Einkaufslistenelement erstellen und seine ID zurückgeben
    private int einkaufslistenelementErstellen(String name) {
        int zutatId = zutatErstellen(name);
        Einkaufslistenelement element = new Einkaufslistenelement(zutatId, 500.0, "g", false);
        return (int) einkaufslistenelementDAO.einfuegen(element);
    }

    // Einen neuen Beitrag einfügen und anhand seiner ID abrufen
    @Test
    public void einfuegenUndNachIdAbrufen() {
        int elementId = einkaufslistenelementErstellen("Mehl");
        int essensplanId = essensplanErstellen();
        Einkaufslistenbeitrag beitrag = new Einkaufslistenbeitrag(elementId, essensplanId, 250.0);
        long id = einkaufslistenbeitragDAO.einfuegen(beitrag);
        Einkaufslistenbeitrag gespeichert = einkaufslistenbeitragDAO.nachIdAbrufen((int) id);
        assertEquals(elementId, gespeichert.getEinkaufslistenelementId());
        assertEquals(Integer.valueOf(essensplanId),
                gespeichert.getEssensplanId());
        assertEquals(250.0, gespeichert.getMenge(), 0.001);
    }

    // Einen vorhandenen Beitrag aktualisieren
    @Test
    public void aktualisieren() {
        int elementId = einkaufslistenelementErstellen("Mehl");
        int essensplanId = essensplanErstellen();
        Einkaufslistenbeitrag beitrag = new Einkaufslistenbeitrag(elementId, essensplanId, 250.0);
        long id = einkaufslistenbeitragDAO.einfuegen(beitrag);
        beitrag.setId((int) id);
        beitrag.setMenge(500.0);
        int anzahl = einkaufslistenbeitragDAO.aktualisieren(beitrag);
        Einkaufslistenbeitrag gespeichert = einkaufslistenbeitragDAO.nachIdAbrufen((int) id);
        assertEquals(1, anzahl);
        assertEquals(500.0, gespeichert.getMenge(), 0.001);
    }

    // Einen vorhandenen Beitrag löschen
    @Test
    public void loeschen() {
        int elementId = einkaufslistenelementErstellen("Mehl");
        int essensplanId = essensplanErstellen();
        Einkaufslistenbeitrag beitrag = new Einkaufslistenbeitrag(elementId, essensplanId, 250.0);
        long id = einkaufslistenbeitragDAO.einfuegen(beitrag);
        beitrag.setId((int) id);
        int anzahl = einkaufslistenbeitragDAO.loeschen(beitrag);
        assertEquals(1, anzahl);
        assertNull(einkaufslistenbeitragDAO.nachIdAbrufen((int) id));
    }

    // Alle Beiträge nach ID aufsteigend abrufen
    @Test
    public void alleAbrufenSortiertNachId() {
        int elementId = einkaufslistenelementErstellen("Mehl");
        int essensplanId = essensplanErstellen();
        long id1 = einkaufslistenbeitragDAO.einfuegen(new Einkaufslistenbeitrag(elementId, essensplanId, 250.0));
        long id2 = einkaufslistenbeitragDAO.einfuegen(new Einkaufslistenbeitrag(elementId, null, 100.0));
        List<Einkaufslistenbeitrag> beitraege = einkaufslistenbeitragDAO.alleAbrufen();
        assertEquals(2, beitraege.size());
        assertEquals((int) id1, beitraege.get(0).getId());
        assertEquals((int) id2, beitraege.get(1).getId());
    }

    // Alle Beiträge eines Einkaufslistenelements abrufen
    @Test
    public void nachEinkaufslistenelementIdAbrufen() {
        int elementId = einkaufslistenelementErstellen("Mehl");
        int essensplanId = essensplanErstellen();
        einkaufslistenbeitragDAO.einfuegen(new Einkaufslistenbeitrag(elementId, essensplanId, 250.0));
        einkaufslistenbeitragDAO.einfuegen(new Einkaufslistenbeitrag(elementId, null, 100.0));
        List<Einkaufslistenbeitrag> beitraege = einkaufslistenbeitragDAO.nachEinkaufslistenelementIdAbrufen(elementId);
        assertEquals(2, beitraege.size());
        assertEquals(elementId, beitraege.get(0).getEinkaufslistenelementId());
        assertEquals(elementId, beitraege.get(1).getEinkaufslistenelementId());
    }

    // Alle Beiträge eines Essensplans abrufen
    @Test
    public void nachEssensplanIdAbrufen() {
        int elementId = einkaufslistenelementErstellen("Mehl");
        int essensplanId = essensplanErstellen();
        einkaufslistenbeitragDAO.einfuegen(new Einkaufslistenbeitrag(elementId, essensplanId, 250.0));
        einkaufslistenbeitragDAO.einfuegen(new Einkaufslistenbeitrag(elementId, null, 100.0));
        List<Einkaufslistenbeitrag> beitraege = einkaufslistenbeitragDAO.nachEssensplanIdAbrufen(essensplanId);
        assertEquals(1, beitraege.size());
        assertEquals(Integer.valueOf(essensplanId), beitraege.get(0).getEssensplanId());
    }

    // Beiträge eines Elements mit null als Essensplan-ID abrufen
    @Test
    public void manuelleBeitraegeAbrufen() {
        int elementId = einkaufslistenelementErstellen("Spülmittel");
        int essensplanId = essensplanErstellen();
        einkaufslistenbeitragDAO.einfuegen(new Einkaufslistenbeitrag(elementId, null, 1.0));
        einkaufslistenbeitragDAO.einfuegen(new Einkaufslistenbeitrag(elementId, essensplanId, 2.0));
        List<Einkaufslistenbeitrag> beitraege = einkaufslistenbeitragDAO.nachElementIdUndEssensplanIdAbrufen(elementId, null);
        assertEquals(1, beitraege.size());
        assertNull(beitraege.get(0).getEssensplanId());
    }

    // Beiträge eines Elements und eines bestimmten Plans abrufen
    @Test
    public void nachElementIdUndEssensplanIdAbrufen() {
        int elementId = einkaufslistenelementErstellen("Mehl");
        int essensplanId = essensplanErstellen();
        einkaufslistenbeitragDAO.einfuegen(new Einkaufslistenbeitrag(elementId, essensplanId, 250.0));
        List<Einkaufslistenbeitrag> beitraege = einkaufslistenbeitragDAO.nachElementIdUndEssensplanIdAbrufen(elementId, essensplanId);
        assertEquals(1, beitraege.size());
        assertEquals(250.0, beitraege.get(0).getMenge(), 0.001);
    }

    // Einen nicht vorhandenen Beitrag abrufen
    @Test
    public void nachIdAbrufenNichtVorhanden() {
        assertNull(einkaufslistenbeitragDAO.nachIdAbrufen(999));
    }

    // Beim Löschen des Einkaufslistenelements seine Beiträge mitlöschen
    @Test
    public void loeschenEinkaufslistenelementLoeschtBeitraegeMit() {
        int elementId = einkaufslistenelementErstellen("Mehl");
        int essensplanId = essensplanErstellen();
        einkaufslistenbeitragDAO.einfuegen(new Einkaufslistenbeitrag(elementId, essensplanId, 250.0));
        Einkaufslistenelement element = einkaufslistenelementDAO.nachIdAbrufen(elementId);
        einkaufslistenelementDAO.loeschen(element);
        assertTrue(einkaufslistenbeitragDAO.nachEinkaufslistenelementIdAbrufen(elementId).isEmpty());
    }

    // Beim Löschen des Essensplans seine Beiträge mitlöschen
    @Test
    public void loeschenEssensplanLoeschtBeitraegeMit() {
        int elementId = einkaufslistenelementErstellen("Mehl");
        int essensplanId = essensplanErstellen();
        einkaufslistenbeitragDAO.einfuegen(new Einkaufslistenbeitrag(elementId, essensplanId, 250.0));
        Essensplan essensplan = essensplanDAO.nachIdAbrufen(essensplanId);
        essensplanDAO.loeschen(essensplan);
        assertTrue(einkaufslistenbeitragDAO.nachEssensplanIdAbrufen(essensplanId).isEmpty());
    }
}
