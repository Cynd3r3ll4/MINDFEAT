package de.mindfeat.app.controller;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import de.mindfeat.app.model.datengrundlage.TestDatenbank;
import de.mindfeat.app.model.datengrundlage.dao.EinkaufslistenbeitragDAO;
import de.mindfeat.app.model.datengrundlage.dao.EinkaufslistenelementDAO;
import de.mindfeat.app.model.datengrundlage.dao.EssensplanDAO;
import de.mindfeat.app.model.datengrundlage.dao.ZutatDAO;
import de.mindfeat.app.model.datengrundlage.entitaeten.Einkaufslistenbeitrag;
import de.mindfeat.app.model.datengrundlage.entitaeten.Einkaufslistenelement;
import de.mindfeat.app.model.datengrundlage.entitaeten.Essensplan;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;
import de.mindfeat.app.model.datengrundlage.entitaeten.Zutat;
import de.mindfeat.app.model.vermittler.EinkaufslisteVermittler;
import de.mindfeat.app.model.vermittler.EssensplanVermittler;
import de.mindfeat.app.model.vermittler.RezeptVermittler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class EinkaufslisteControllerTest {

    private TestDatenbank testDatenbank;
    private EinkaufslisteController einkaufslisteController;
    private EinkaufslisteVermittler einkaufslisteVermittler;
    private EssensplanVermittler essensplanVermittler;
    private RezeptVermittler rezeptVermittler;
    private EinkaufslistenbeitragDAO beitragDAO;
    private EinkaufslistenelementDAO elementDAO;
    private EssensplanDAO essensplanDAO;
    private ZutatDAO zutatDAO;

    private final Executor direkterExecutor = Runnable::run;

    // Eine Testdatenbank für die Tests erstellen
    @Before
    public void setUp() {
        testDatenbank = new TestDatenbank();
        einkaufslisteVermittler = new EinkaufslisteVermittler(testDatenbank.getDatenbank());
        essensplanVermittler = new EssensplanVermittler(testDatenbank.getDatenbank());
        rezeptVermittler = new RezeptVermittler(testDatenbank.getDatenbank());
        einkaufslisteController = new EinkaufslisteController(einkaufslisteVermittler, direkterExecutor, direkterExecutor);
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
        Zutat zutat = zutatDAO.nachNameAbrufen(zutatName);
        assertNotNull("Die Zutat muss vorhanden sein.", zutat);
        List<Einkaufslistenelement> elemente = elementDAO.nachZutatIdUndEinheitAbrufen(zutat.getId(), einheit);
        assertEquals(1, elemente.size());
        return elemente.get(0);
    }

    // Hilfsmethode: einen Einkaufslistenbeitrag zu einem Element abrufen
    private Einkaufslistenbeitrag beitragAbrufen(int elementId) {
        List<Einkaufslistenbeitrag> beitraege = beitragDAO.nachEinkaufslistenelementIdAbrufen(elementId);
        assertEquals(1, beitraege.size());
        return beitraege.get(0);
    }

    // Einen manuellen Eintrag hinzufügen und die Einkaufsliste abrufen
    @Test
    public void manuellenEintragHinzufuegenUndAbrufen() {
        ErgebnisCallback<Integer> hinzufuegenCallback = new ErgebnisCallback<>();
        einkaufslisteController.manuellHinzufuegen("Kartoffeln", 500.0, "g", hinzufuegenCallback);
        assertNull(hinzufuegenCallback.fehler);
        assertNotNull(hinzufuegenCallback.ergebnis);
        assertTrue(hinzufuegenCallback.ergebnis > 0);
        ErgebnisCallback<List<EinkaufslisteVermittler.EinkaufslistenDetails>> abrufenCallback = new ErgebnisCallback<>();
        einkaufslisteController.alleAbrufen(abrufenCallback);
        assertNull(abrufenCallback.fehler);
        assertNotNull(abrufenCallback.ergebnis);
        assertEquals(1, abrufenCallback.ergebnis.size());
        EinkaufslisteVermittler.EinkaufslistenDetails details = abrufenCallback.ergebnis.get(0);
        assertEquals("Kartoffeln", details.getZutat().getName());
        assertEquals(Double.valueOf(500.0), details.getElement().getMenge());
        assertEquals(1, details.getBeitraege().size());
    }

    // Zutaten eines Essensplans in die Einkaufsliste übernehmen
    @Test
    public void zutatenAusEssensplanUebernehmen() {
        int essensplanId = essensplanAnlegen();
        int rezeptId = rezeptAnlegen("Kartoffelsuppe",
                Arrays.asList(
                        new RezeptVermittler.ZutatAngabe("Kartoffeln", 500.0, "g"),
                        new RezeptVermittler.ZutatAngabe("Möhren", 200.0, "g")), 2);
        essensplanVermittler.inhaltHinzufuegen(essensplanId, rezeptId, "2026-11-03");
        ErgebnisCallback<Void> callback = new ErgebnisCallback<>();
        einkaufslisteController.ausEssensplanHinzufuegen(essensplanId, callback);
        assertNull(callback.fehler);
        assertEquals(Double.valueOf(500.0), elementAbrufen("Kartoffeln", "g").getMenge());
        assertEquals(Double.valueOf(200.0), elementAbrufen("Möhren", "g").getMenge());
        assertEquals(2, beitragDAO.nachEssensplanIdAbrufen(essensplanId).size());
    }

    // Beiträge eines Essensplans entfernen, manuelle Beiträge aber erhalten
    @Test
    public void ausEssensplanEntfernenErhaeltManuelleBeitraege() {
        int essensplanId = essensplanAnlegen();
        int rezeptId = rezeptAnlegen("Kartoffelsuppe", Collections.singletonList(new RezeptVermittler.ZutatAngabe("Kartoffeln", 500.0, "g")), 2);
        essensplanVermittler.inhaltHinzufuegen(essensplanId, rezeptId, "2026-11-03");
        einkaufslisteVermittler.manuellHinzufuegen("Kartoffeln", 200.0, "g");
        ErgebnisCallback<Void> hinzufuegenCallback = new ErgebnisCallback<>();
        einkaufslisteController.ausEssensplanHinzufuegen(essensplanId, hinzufuegenCallback);
        assertNull(hinzufuegenCallback.fehler);
        assertEquals(Double.valueOf(700.0), elementAbrufen("Kartoffeln", "g").getMenge());
        ErgebnisCallback<Void> entfernenCallback = new ErgebnisCallback<>();
        einkaufslisteController.ausEssensplanEntfernen(essensplanId, entfernenCallback);
        assertNull(entfernenCallback.fehler);
        Einkaufslistenelement element = elementAbrufen("Kartoffeln", "g");
        assertEquals(Double.valueOf(200.0), element.getMenge());
        List<Einkaufslistenbeitrag> beitraege = beitragDAO.nachEinkaufslistenelementIdAbrufen(element.getId());
        assertEquals(1, beitraege.size());
        assertNull(beitraege.get(0).getEssensplanId());
    }

    // Einen Einkaufslistenbeitrag entfernen
    @Test
    public void beitragEntfernen() {
        ErgebnisCallback<Integer> hinzufuegenCallback = new ErgebnisCallback<>();
        einkaufslisteController.manuellHinzufuegen("Kartoffeln", 500.0, "g", hinzufuegenCallback);
        int elementId = hinzufuegenCallback.ergebnis;
        Einkaufslistenbeitrag beitrag = beitragAbrufen(elementId);
        ErgebnisCallback<Void> entfernenCallback = new ErgebnisCallback<>();
        einkaufslisteController.beitragEntfernen(beitrag.getId(), entfernenCallback);
        assertNull(entfernenCallback.fehler);
        assertNull(elementDAO.nachIdAbrufen(elementId));
        assertNull(beitragDAO.nachIdAbrufen(beitrag.getId()));
    }

    // Ein Einkaufslistenelement als erledigt markieren
    @Test
    public void erledigtStatusSetzen() {
        ErgebnisCallback<Integer> hinzufuegenCallback = new ErgebnisCallback<>();
        einkaufslisteController.manuellHinzufuegen("Kartoffeln", 500.0, "g", hinzufuegenCallback);
        int elementId = hinzufuegenCallback.ergebnis;
        ErgebnisCallback<Void> callback = new ErgebnisCallback<>();
        einkaufslisteController.erledigtSetzen(elementId, true, callback);
        assertNull(callback.fehler);
        assertTrue(elementDAO.nachIdAbrufen(elementId).isErledigt());
    }

    // Ein Einkaufslistenelement einschließlich seiner Beiträge löschen
    @Test
    public void einkaufslistenelementLoeschen() {
        ErgebnisCallback<Integer> hinzufuegenCallback = new ErgebnisCallback<>();
        einkaufslisteController.manuellHinzufuegen("Kartoffeln", 500.0, "g", hinzufuegenCallback);
        int elementId = hinzufuegenCallback.ergebnis;
        ErgebnisCallback<Void> callback = new ErgebnisCallback<>();
        einkaufslisteController.loeschen(elementId, callback);
        assertNull(callback.fehler);
        assertNull(elementDAO.nachIdAbrufen(elementId));
        assertTrue(beitragDAO.nachEinkaufslistenelementIdAbrufen(elementId).isEmpty());
    }

    // Einen Essensplan einschließlich seiner Einkaufslistenbeiträge löschen
    @Test
    public void essensplanLoeschen() {
        int essensplanId = essensplanAnlegen();
        int rezeptId = rezeptAnlegen("Kartoffelsuppe", Collections.singletonList(new RezeptVermittler.ZutatAngabe("Kartoffeln", 500.0, "g")), 2);
        essensplanVermittler.inhaltHinzufuegen(essensplanId, rezeptId, "2026-11-03");
        ErgebnisCallback<Void> hinzufuegenCallback = new ErgebnisCallback<>();
        einkaufslisteController.ausEssensplanHinzufuegen(essensplanId, hinzufuegenCallback);
        assertNull(hinzufuegenCallback.fehler);
        Einkaufslistenelement element = elementAbrufen("Kartoffeln", "g");
        ErgebnisCallback<Void> loeschenCallback = new ErgebnisCallback<>();
        einkaufslisteController.essensplanLoeschen(essensplanId, loeschenCallback);
        assertNull(loeschenCallback.fehler);
        assertNull(essensplanDAO.nachIdAbrufen(essensplanId));
        assertNull(elementDAO.nachIdAbrufen(element.getId()));
        assertTrue(beitragDAO.nachEssensplanIdAbrufen(essensplanId).isEmpty());
    }

    // Fehler bei einem ungültigen Beitrag weitergeben
    @Test
    public void fehlerBeiUngueltigerBeitragsIdWirdWeitergegeben() {
        ErgebnisCallback<Void> callback = new ErgebnisCallback<>();
        einkaufslisteController.beitragEntfernen(0, callback);
        assertNull(callback.ergebnis);
        assertNotNull(callback.fehler);
        assertTrue(callback.fehler instanceof IllegalArgumentException);
    }

    // Einfacher Callback zum Speichern des Testergebnisses
    private static class ErgebnisCallback<T> implements EinkaufslisteController.Callback<T> {
        private T ergebnis;
        private Exception fehler;

        @Override
        public void beiErfolg(T ergebnis) {
            this.ergebnis = ergebnis;
        }

        @Override
        public void beiFehler(Exception exception) {
            this.fehler = exception;
        }
    }
}
