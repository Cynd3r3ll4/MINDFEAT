package de.mindfeat.app.controller;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import de.mindfeat.app.model.datengrundlage.TestDatenbank;
import de.mindfeat.app.model.datengrundlage.dao.EssensplanDAO;
import de.mindfeat.app.model.datengrundlage.dao.InhaltEssensplanDAO;
import de.mindfeat.app.model.datengrundlage.entitaeten.Essensplan;
import de.mindfeat.app.model.datengrundlage.entitaeten.InhaltEssensplan;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;
import de.mindfeat.app.model.vermittler.EssensplanVermittler;
import de.mindfeat.app.model.vermittler.RezeptVermittler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class EssensplanControllerTest {

    private TestDatenbank testDatenbank;
    private EssensplanController essensplanController;
    private RezeptVermittler rezeptVermittler;
    private EssensplanDAO essensplanDAO;
    private InhaltEssensplanDAO inhaltEssensplanDAO;
    private final Executor direkterExecutor = Runnable::run;

    // Eine Testdatenbank für die Tests erstellen
    @Before
    public void setUp() {
        testDatenbank = new TestDatenbank();
        essensplanVermittlerInitialisieren();
    }

    private void essensplanVermittlerInitialisieren() {
        EssensplanVermittler essensplanVermittler = new EssensplanVermittler(testDatenbank.getDatenbank());
        rezeptVermittler = new RezeptVermittler(testDatenbank.getDatenbank());
        essensplanController = new EssensplanController(essensplanVermittler, direkterExecutor, direkterExecutor);
        essensplanDAO = testDatenbank.getDatenbank().essensplanDAO();
        inhaltEssensplanDAO = testDatenbank.getDatenbank().inhaltEssensplanDAO();
    }

    // Die Testdatenbank nach den Tests schließen
    @After
    public void tearDown() {
        if (testDatenbank != null) {
            testDatenbank.schliessen();
        }
    }

    // Hilfsmethode: einen Essensplan über den Controller anlegen
    private int essensplanAnlegen() {
        Essensplan essensplan = new Essensplan("Test-Essensplan", "2026-11-01", "2026-11-07", 6);
        ErgebnisCallback<Long> callback = new ErgebnisCallback<>();
        essensplanController.anlegen(essensplan, callback);
        assertNull(callback.fehler);
        assertNotNull(callback.ergebnis);
        return (int) (long) callback.ergebnis;
    }

    // Hilfsmethode: ein Testrezept anlegen
    private int rezeptAnlegen(String titel) {
        Rezept rezept = new Rezept(titel, null, 2);
        return rezeptVermittler.anlegen(rezept, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    // Einen Essensplan anlegen und alle Essenspläne abrufen
    @Test
    public void essensplanAnlegenUndAbrufen() {
        int essensplanId = essensplanAnlegen();
        ErgebnisCallback<List<Essensplan>> callback = new ErgebnisCallback<>();
        essensplanController.alleAbrufen(callback);
        assertNull(callback.fehler);
        assertNotNull(callback.ergebnis);
        assertEquals(1, callback.ergebnis.size());
        assertEquals(essensplanId, callback.ergebnis.get(0).getId());
    }

    // Einen Essensplan mit seinen Einträgen und Rezeptdetails abrufen
    @Test
    public void essensplanMitDetailsAbrufen() {
        int essensplanId = essensplanAnlegen();
        int rezeptId = rezeptAnlegen("Kartoffelsuppe");
        ErgebnisCallback<Long> hinzufuegenCallback = new ErgebnisCallback<>();
        essensplanController.inhaltHinzufuegen(essensplanId, rezeptId, "2026-11-03", hinzufuegenCallback);
        assertNull(hinzufuegenCallback.fehler);
        ErgebnisCallback<EssensplanVermittler.EssensplanDetails> detailsCallback = new ErgebnisCallback<>();
        essensplanController.nachIdMitDetailsAbrufen(essensplanId, detailsCallback);
        assertNull(detailsCallback.fehler);
        assertNotNull(detailsCallback.ergebnis);
        assertEquals(essensplanId, detailsCallback.ergebnis.getEssensplan().getId());
        assertEquals(1, detailsCallback.ergebnis.getEintraege().size());
        assertEquals("Kartoffelsuppe", detailsCallback.ergebnis.getEintraege().get(0).getRezeptDetails().getRezept().getTitel());
    }

    // Einen vorhandenen Essensplan bearbeiten
    @Test
    public void essensplanBearbeiten() {
        int essensplanId = essensplanAnlegen();
        Essensplan essensplan = essensplanDAO.nachIdAbrufen(essensplanId);
        essensplan.setTitel("Geänderter Essensplan");
        ErgebnisCallback<Void> callback = new ErgebnisCallback<>();
        essensplanController.bearbeiten(essensplan, callback);
        assertNull(callback.fehler);
        Essensplan gespeicherterPlan = essensplanDAO.nachIdAbrufen(essensplanId);
        assertNotNull(gespeicherterPlan);
        assertEquals("Geänderter Essensplan", gespeicherterPlan.getTitel());
    }

    // Ein Rezept zum Essensplan hinzufügen
    @Test
    public void rezeptZumEssensplanHinzufuegen() {
        int essensplanId = essensplanAnlegen();
        int rezeptId = rezeptAnlegen("Kartoffelsuppe");
        ErgebnisCallback<Long> callback = new ErgebnisCallback<>();
        essensplanController.inhaltHinzufuegen(essensplanId, rezeptId, "2026-11-03", callback);
        assertNull(callback.fehler);
        assertNotNull(callback.ergebnis);
        assertTrue(callback.ergebnis > 0);
        InhaltEssensplan eintrag = inhaltEssensplanDAO.nachIdAbrufen(callback.ergebnis.intValue());
        assertNotNull(eintrag);
        assertEquals(essensplanId, eintrag.getEssensplanId());
        assertEquals(rezeptId, eintrag.getRezeptId());
        assertEquals("2026-11-03", eintrag.getDatum());
    }

    // Die Anzahl der Portionen eines Essensplaneintrags ändern
    @Test
    public void portionenAendern() {
        int essensplanId = essensplanAnlegen();
        int rezeptId = rezeptAnlegen("Kartoffelsuppe");
        ErgebnisCallback<Long> hinzufuegenCallback = new ErgebnisCallback<>();
        essensplanController.inhaltHinzufuegen(essensplanId, rezeptId, "2026-11-03", hinzufuegenCallback);
        int eintragId = hinzufuegenCallback.ergebnis.intValue();
        InhaltEssensplan eintrag = inhaltEssensplanDAO.nachIdAbrufen(eintragId);
        assertEquals(2, eintrag.getPortionen());
        ErgebnisCallback<Void> aendernCallback = new ErgebnisCallback<>();
        essensplanController.portionenAendern(eintragId, 4, aendernCallback);
        assertNull(aendernCallback.fehler);
        InhaltEssensplan geaenderterEintrag = inhaltEssensplanDAO.nachIdAbrufen(eintragId);
        assertEquals(4, geaenderterEintrag.getPortionen());
    }

    // Einen einzelnen Essensplaneintrag entfernen
    @Test
    public void essensplaneintragEntfernen() {
        int essensplanId = essensplanAnlegen();
        int rezeptId = rezeptAnlegen("Kartoffelsuppe");
        ErgebnisCallback<Long> hinzufuegenCallback = new ErgebnisCallback<>();
        essensplanController.inhaltHinzufuegen(essensplanId, rezeptId, "2026-11-03", hinzufuegenCallback);
        int eintragId = hinzufuegenCallback.ergebnis.intValue();
        ErgebnisCallback<Void> entfernenCallback = new ErgebnisCallback<>();
        essensplanController.inhaltEntfernen(eintragId, entfernenCallback);
        assertNull(entfernenCallback.fehler);
        assertNull(inhaltEssensplanDAO.nachIdAbrufen(eintragId));
    }

    // Einen Essensplan einschließlich seiner Einträge löschen
    @Test
    public void essensplanLoeschen() {
        int essensplanId = essensplanAnlegen();
        int rezeptId = rezeptAnlegen("Kartoffelsuppe");
        ErgebnisCallback<Long> hinzufuegenCallback = new ErgebnisCallback<>();
        essensplanController.inhaltHinzufuegen(essensplanId, rezeptId, "2026-11-03", hinzufuegenCallback);
        ErgebnisCallback<Void> loeschenCallback = new ErgebnisCallback<>();
        essensplanController.loeschen(essensplanId, loeschenCallback);
        assertNull(loeschenCallback.fehler);
        assertNull(essensplanDAO.nachIdAbrufen(essensplanId));
        assertTrue(inhaltEssensplanDAO.nachEssensplanIdAbrufen(essensplanId).isEmpty());
    }

    // Rezeptvorschläge für einen Essensplan ermitteln
    @Test
    public void vorschlaegeErmitteln() {
        int essensplanId = essensplanAnlegen();
        rezeptAnlegen("Kartoffelsuppe");
        rezeptAnlegen("Gulasch");
        ErgebnisCallback<List<RezeptVermittler.RezeptDetails>> callback = new ErgebnisCallback<>();
        essensplanController.vorschlaegeErmitteln(essensplanId, Collections.emptyList(), false, callback);
        assertNull(callback.fehler);
        assertNotNull(callback.ergebnis);
        assertEquals(2, callback.ergebnis.size());
    }

    // Einen einzelnen zufälligen Rezeptvorschlag ermitteln
    @Test
    public void zufaelligenVorschlagErmitteln() {
        int essensplanId = essensplanAnlegen();
        rezeptAnlegen("Kartoffelsuppe");
        ErgebnisCallback<RezeptVermittler.RezeptDetails> callback = new ErgebnisCallback<>();
        essensplanController.zufaelligenVorschlagErmitteln(essensplanId, Collections.emptyList(), false, callback);
        assertNull(callback.fehler);
        assertNotNull(callback.ergebnis);
        assertEquals("Kartoffelsuppe", callback.ergebnis.getRezept().getTitel());
    }

    // Fehler bei einer ungültigen Essensplan-ID weitergeben
    @Test
    public void fehlerBeiUngueltigerEssensplanIdWirdWeitergegeben() {
        ErgebnisCallback<EssensplanVermittler.EssensplanDetails> callback = new ErgebnisCallback<>();
        essensplanController.nachIdMitDetailsAbrufen(0, callback);
        assertNull(callback.ergebnis);
        assertNotNull(callback.fehler);
        assertTrue(callback.fehler instanceof IllegalArgumentException);
    }

    // Ein nicht vorhandener Essensplan liefert kein Ergebnis
    @Test
    public void nichtVorhandenerEssensplanLiefertNull() {
        ErgebnisCallback<EssensplanVermittler.EssensplanDetails> callback = new ErgebnisCallback<>();
        essensplanController.nachIdMitDetailsAbrufen(999, callback);
        assertNull(callback.fehler);
        assertNull(callback.ergebnis);
    }

    // Einfacher Callback zum Speichern des Testergebnisses
    private static class ErgebnisCallback<T> implements EssensplanController.Callback<T> {

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
