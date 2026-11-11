package de.mindfeat.app.model.vermittler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.mindfeat.app.model.datengrundlage.TestDatenbank;
import de.mindfeat.app.model.datengrundlage.dao.EinkaufslistenbeitragDAO;
import de.mindfeat.app.model.datengrundlage.dao.EinkaufslistenelementDAO;
import de.mindfeat.app.model.datengrundlage.dao.EssensplanDAO;
import de.mindfeat.app.model.datengrundlage.dao.ZutatDAO;
import de.mindfeat.app.model.datengrundlage.entitaeten.Einkaufslistenbeitrag;
import de.mindfeat.app.model.datengrundlage.entitaeten.Einkaufslistenelement;
import de.mindfeat.app.model.datengrundlage.entitaeten.Essensplan;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;

@RunWith(AndroidJUnit4.class)
public class EinkaufslisteVermittlerTest {

    private TestDatenbank testDatenbank;
    private EinkaufslisteVermittler einkaufslisteVermittler;
    private EssensplanVermittler essensplanVermittler;
    private RezeptVermittler rezeptVermittler;
    private EinkaufslistenbeitragDAO beitragDAO;
    private EinkaufslistenelementDAO elementDAO;
    private EssensplanDAO essensplanDAO;
    private ZutatDAO zutatDAO;

    // Eine Testdatenbank für die Tests erstellen
    @Before
    public void setUp() {
        testDatenbank = new TestDatenbank();
        einkaufslisteVermittler = new EinkaufslisteVermittler(testDatenbank.getDatenbank());
        essensplanVermittler = new EssensplanVermittler(testDatenbank.getDatenbank());
        rezeptVermittler = new RezeptVermittler(testDatenbank.getDatenbank());
        beitragDAO = testDatenbank.getDatenbank().einkaufslistenbeitragDAO();
        elementDAO = testDatenbank.getDatenbank().einkaufslistenelementDAO();
        essensplanDAO = testDatenbank.getDatenbank().essensplanDAO();
        zutatDAO = testDatenbank.getDatenbank().zutatDAO();
    }

    // Die Testdatenbank nach den Tests schließen
    @After
    public void tearDown() {
        if (testDatenbank != null) {
            testDatenbank.schliessen();
        }
    }

    // Hilfsmethode: einen Essensplan für Tests anlegen
    private int essensplanAnlegen() {
        Essensplan essensplan = new Essensplan("Test-Essensplan", "2026-11-01", "2026-11-07", 6);
        return (int) essensplanVermittler.anlegen(essensplan);
    }

    // Hilfsmethode: ein Rezept mit Zutaten anlegen
    private int rezeptAnlegen(String titel, List<RezeptVermittler.ZutatAngabe> zutaten, int portionen) {
        Rezept rezept = new Rezept(titel, null, portionen);
        return rezeptVermittler.anlegen(rezept, zutaten, Collections.emptyList(), Collections.emptyList());
    }

    // Hilfsmethode: ein Einkaufslistenelement anhand von Zutat und Einheit abrufen
    private Einkaufslistenelement elementAbrufen(String zutatName, String einheit) {
        de.mindfeat.app.model.datengrundlage.entitaeten.Zutat zutat = zutatDAO.nachNameAbrufen(zutatName);
        assertNotNull("Bitte gib eine Zutat an.", zutat);
        List<Einkaufslistenelement> elemente = elementDAO.nachZutatIdUndEinheitAbrufen(zutat.getId(), einheit);
        assertEquals(1, elemente.size());
        return elemente.get(0);
    }

    // Einen manuellen Einkaufslisteneintrag anlegen
    @Test
    public void manuellenEintragHinzufuegen() {
        einkaufslisteVermittler.manuellHinzufuegen("Kartoffeln", 500.0, "g");
        Einkaufslistenelement element = elementAbrufen("Kartoffeln", "g");
        assertEquals(Double.valueOf(500.0), element.getMenge());
        assertFalse(element.isErledigt());
        List<Einkaufslistenbeitrag> beitraege = beitragDAO.nachEinkaufslistenelementIdAbrufen(element.getId());
        assertEquals(1, beitraege.size());
        assertNull(beitraege.get(0).getEssensplanId());
        assertEquals(Double.valueOf(500.0), beitraege.get(0).getMenge());
    }

    // Gleiche Zutat und Einheit werden in einem Element zusammengeführt
    @Test
    public void gleicheZutatUndEinheitZusammenfuehren() {
        einkaufslisteVermittler.manuellHinzufuegen("Kartoffeln", 500.0, "g");
        einkaufslisteVermittler.manuellHinzufuegen("Kartoffeln", 250.0, "g");
        Einkaufslistenelement element = elementAbrufen("Kartoffeln", "g");
        assertEquals(Double.valueOf(750.0), element.getMenge());
        List<Einkaufslistenbeitrag> beitraege = beitragDAO.nachEinkaufslistenelementIdAbrufen(element.getId());
        assertEquals(1, beitraege.size()); // Manuelle Beiträge werden zu einem Beitrag zusammengeführt
        assertNull(beitraege.get(0).getEssensplanId());
    }

    // Unterschiedliche Einheiten bleiben getrennte Einkaufslistenelemente
    @Test
    public void gleicheZutatMitUnterschiedlichenEinheitenGetrenntHalten() {
        einkaufslisteVermittler.manuellHinzufuegen("Kartoffeln", 500.0, "g");
        einkaufslisteVermittler.manuellHinzufuegen("Kartoffeln", 2.0, "Stück");
        de.mindfeat.app.model.datengrundlage.entitaeten.Zutat zutat = zutatDAO.nachNameAbrufen("Kartoffeln");
        assertEquals(2, elementDAO.nachZutatIdAbrufen(zutat.getId()).size());
    }

    // Zutaten eines Essensplans in die Einkaufsliste übernehmen
    @Test
    public void zutatenAusEssensplanUebernehmen() {
        int essensplanId = essensplanAnlegen();
        int rezeptId = rezeptAnlegen("Kartoffelsuppe", Arrays.asList(
                new RezeptVermittler.ZutatAngabe("Kartoffeln", 500.0, "g"),
                new RezeptVermittler.ZutatAngabe("Möhren", 200.0, "g")), 2);
        essensplanVermittler.inhaltHinzufuegen(essensplanId, rezeptId, "2026-11-03");
        einkaufslisteVermittler.ausEssensplanHinzufuegen(essensplanId);
        Einkaufslistenelement kartoffeln = elementAbrufen("Kartoffeln", "g");
        Einkaufslistenelement moehren = elementAbrufen("Möhren", "g");
        assertEquals(Double.valueOf(500.0), kartoffeln.getMenge());
        assertEquals(Double.valueOf(200.0), moehren.getMenge());
        assertEquals(2, beitragDAO.nachEssensplanIdAbrufen(essensplanId).size());
    }

    // Die erneute Übernahme desselben Plans darf keine doppelten Beiträge erzeugen
    @Test
    public void essensplanMehrfachUebernehmenErzeugtKeineDoppeltenBeitraege() {
        int essensplanId = essensplanAnlegen();
        int rezeptId = rezeptAnlegen("Kartoffelsuppe", Collections.singletonList(new RezeptVermittler.ZutatAngabe("Kartoffeln", 500.0, "g")), 2);
        essensplanVermittler.inhaltHinzufuegen(essensplanId, rezeptId, "2026-11-03");
        einkaufslisteVermittler.ausEssensplanHinzufuegen(essensplanId);
        einkaufslisteVermittler.ausEssensplanHinzufuegen(essensplanId);
        Einkaufslistenelement element = elementAbrufen("Kartoffeln", "g");
        assertEquals(Double.valueOf(500.0), element.getMenge());
        assertEquals(1, beitragDAO.nachEssensplanIdAbrufen(essensplanId).size());
    }

    // Beim Entfernen eines Plans bleiben die manuellen Beiträge erhalten
    @Test
    public void essensplanEntfernenErhaeltManuelleBeitraege() {
        int essensplanId = essensplanAnlegen();
        int rezeptId = rezeptAnlegen("Kartoffelsuppe", Collections.singletonList(new RezeptVermittler.ZutatAngabe("Kartoffeln", 500.0, "g")), 2);
        essensplanVermittler.inhaltHinzufuegen(essensplanId, rezeptId, "2026-11-03");
        einkaufslisteVermittler.manuellHinzufuegen("Kartoffeln", 200.0, "g");
        einkaufslisteVermittler.ausEssensplanHinzufuegen(essensplanId);
        Einkaufslistenelement element = elementAbrufen("Kartoffeln", "g");
        assertEquals(Double.valueOf(700.0), element.getMenge());
        einkaufslisteVermittler.ausEssensplanEntfernen(essensplanId);
        element = elementAbrufen("Kartoffeln", "g");
        assertEquals(Double.valueOf(200.0), element.getMenge());
        List<Einkaufslistenbeitrag> beitraege = beitragDAO.nachEinkaufslistenelementIdAbrufen(element.getId());
        assertEquals(1, beitraege.size());
        assertNull(beitraege.get(0).getEssensplanId());
    }

    // Einen Einkaufslisteneintrag als erledigt markieren
    @Test
    public void eintragAlsErledigtMarkieren() {
        einkaufslisteVermittler.manuellHinzufuegen("Kartoffeln", 500.0, "g");
        Einkaufslistenelement element = elementAbrufen("Kartoffeln", "g");
        einkaufslisteVermittler.erledigtSetzen(element.getId(), true);
        assertTrue(elementDAO.nachIdAbrufen(element.getId()).isErledigt());
    }

    // Einen Einkaufslisteneintrag vollständig löschen
    @Test
    public void eintragLoeschenEntferntAuchSeineBeitraege() {
        einkaufslisteVermittler.manuellHinzufuegen("Kartoffeln", 500.0, "g");
        Einkaufslistenelement element = elementAbrufen("Kartoffeln", "g");
        einkaufslisteVermittler.loeschen(element.getId());
        assertNull(elementDAO.nachIdAbrufen(element.getId()));
        assertTrue(beitragDAO.nachEinkaufslistenelementIdAbrufen(element.getId()).isEmpty());
    }

    // Beim Entfernen des letzten Beitrags wird das Einkaufslistenelement gelöscht
    @Test
    public void letzterBeitragEntfernenLoeschtEinkaufslistenelement() {
        einkaufslisteVermittler.manuellHinzufuegen("Kartoffeln", 500.0, "g");
        Einkaufslistenelement element = elementAbrufen("Kartoffeln", "g");
        Einkaufslistenbeitrag beitrag = beitragDAO.nachEinkaufslistenelementIdAbrufen(element.getId()).get(0);
        einkaufslisteVermittler.beitragEntfernen(beitrag.getId());
        assertNull(elementDAO.nachIdAbrufen(element.getId()));
    }

    // Einen Essensplan und seine Einkaufslistenbeiträge vollständig entfernen
    @Test
    public void essensplanLoeschenBereinigtAuchEinkaufslistenelemente() {
        int essensplanId = essensplanAnlegen();
        int rezeptId = rezeptAnlegen("Kartoffelsuppe", Collections.singletonList(new RezeptVermittler.ZutatAngabe("Kartoffeln", 500.0, "g")), 2);
        essensplanVermittler.inhaltHinzufuegen(essensplanId, rezeptId, "2026-11-03");
        einkaufslisteVermittler.ausEssensplanHinzufuegen(essensplanId);
        Einkaufslistenelement element = elementAbrufen("Kartoffeln", "g");
        einkaufslisteVermittler.essensplanLoeschen(essensplanId);
        assertNull(essensplanDAO.nachIdAbrufen(essensplanId));
        assertNull(elementDAO.nachIdAbrufen(element.getId()));
        assertTrue(beitragDAO.nachEssensplanIdAbrufen(essensplanId).isEmpty());
    }

    // Skalierung der Portionsgröße
    @Test
    public void mengenWerdenAnPlanportionenAngepasst() {
        int essensplanId = essensplanAnlegen();
        int rezeptId = rezeptAnlegen("Kartoffelsuppe", Collections.singletonList
                        (new RezeptVermittler.ZutatAngabe("Kartoffeln", 500.0, "g")), 2);
        long eintragId = essensplanVermittler.inhaltHinzufuegen(essensplanId, rezeptId, "2026-11-03");
        essensplanVermittler.portionenAendern((int) eintragId, 4);
        einkaufslisteVermittler.ausEssensplanHinzufuegen(essensplanId);
        Einkaufslistenelement element = elementAbrufen("Kartoffeln", "g");
        assertEquals(Double.valueOf(1000.0), element.getMenge());
    }
}
