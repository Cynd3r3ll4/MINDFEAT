package de.mindfeat.app.controller;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import de.mindfeat.app.model.datengrundlage.TestDatenbank;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;
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
public class RezeptControllerTest {

    private TestDatenbank testDatenbank;
    private RezeptController rezeptController;
    private final Executor direkterExecutor = Runnable::run;

    // Eine Testdatenbank für die Tests erstellen
    @Before
    public void setUp() {
        testDatenbank = new TestDatenbank();
        RezeptVermittler rezeptVermittler = new RezeptVermittler(testDatenbank.getDatenbank());
        rezeptController = new RezeptController(rezeptVermittler, direkterExecutor, direkterExecutor);
    }

    @After
    public void tearDown() {
        if (testDatenbank != null) {
            testDatenbank.schliessen();
        }
    }

    @Test
    public void rezeptAnlegenUndAbrufen() {
        Rezept rezept = new Rezept("Gulasch", null, 2);
        List<RezeptVermittler.ZutatAngabe> zutaten =
                Arrays.asList(
                        new RezeptVermittler.ZutatAngabe("Paprika", 2, "Stück"),
                        new RezeptVermittler.ZutatAngabe("Zwiebeln", 5, "Stück"));
        List<String> kategorien = Collections.singletonList("Herbstlich");
        List<String> schritte = Arrays.asList("Paprika und Zwiebeln schneiden", "Zwiebeln anbraten");
        ErgebnisCallback<Integer> anlegenCallback = new ErgebnisCallback<>();
        rezeptController.anlegen(rezept, zutaten, kategorien, schritte, anlegenCallback);
        assertNull(anlegenCallback.fehler);
        assertNotNull(anlegenCallback.ergebnis);
        assertTrue(anlegenCallback.ergebnis > 0);
        int rezeptId = anlegenCallback.ergebnis;
        ErgebnisCallback<RezeptVermittler.RezeptDetails> detailsCallback = new ErgebnisCallback<>();
        rezeptController.nachIdMitDetailsAbrufen(rezeptId, detailsCallback);
        assertNull(detailsCallback.fehler);
        assertNotNull(detailsCallback.ergebnis);
        RezeptVermittler.RezeptDetails details = detailsCallback.ergebnis;
        assertEquals("Gulasch", details.getRezept().getTitel());
        assertEquals(2, details.getRezeptZutaten().size());
        assertEquals(2, details.getZutaten().size());
        assertEquals(1, details.getKategorien().size());
        assertEquals(2, details.getZubereitungsschritte().size());
    }

    @Test
    public void alleRezepteAbrufen() {
        Rezept rezept = new Rezept("Nudelauflauf", null, 4);
        rezeptController.anlegen(rezept, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), new ErgebnisCallback<>());
        ErgebnisCallback<List<Rezept>> callback = new ErgebnisCallback<>();
        rezeptController.alleAbrufen(callback);
        assertNull(callback.fehler);
        assertNotNull(callback.ergebnis);
        assertEquals(1, callback.ergebnis.size());
        assertEquals("Nudelauflauf", callback.ergebnis.get(0).getTitel());
    }

    @Test
    public void rezeptBearbeiten() {
        Rezept rezept = new Rezept("Kartoffelsuppe", null, 2);
        ErgebnisCallback<Integer> anlegenCallback = new ErgebnisCallback<>();
        rezeptController.anlegen(rezept, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), anlegenCallback);
        assertNull(anlegenCallback.fehler);
        rezept.setTitel("Oma's Kartoffelsuppe");
        rezept.setPortionen(3);
        ErgebnisCallback<Void> bearbeitenCallback = new ErgebnisCallback<>();
        rezeptController.bearbeiten(rezept, Collections.emptyList(), Collections.singletonList("Suppen und Eintöpfe"), Collections.singletonList("Kartoffeln kochen"), bearbeitenCallback);
        assertNull(bearbeitenCallback.fehler);
        ErgebnisCallback<RezeptVermittler.RezeptDetails> detailsCallback = new ErgebnisCallback<>();
        rezeptController.nachIdMitDetailsAbrufen(rezept.getId(), detailsCallback);
        assertNull(detailsCallback.fehler);
        assertEquals("Oma's Kartoffelsuppe", detailsCallback.ergebnis.getRezept().getTitel());
        assertEquals(3, detailsCallback.ergebnis.getRezept().getPortionen());
        assertEquals(1, detailsCallback.ergebnis.getKategorien().size());
        assertEquals(1, detailsCallback.ergebnis.getZubereitungsschritte().size());
    }

    @Test
    public void rezeptLoeschen() {
        Rezept rezept = new Rezept("Obstsalat", null, 2);
        ErgebnisCallback<Integer> anlegenCallback = new ErgebnisCallback<>();
        rezeptController.anlegen(rezept, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), anlegenCallback);
        assertNull(anlegenCallback.fehler);
        ErgebnisCallback<Boolean> loeschenCallback = new ErgebnisCallback<>();
        rezeptController.loeschen(anlegenCallback.ergebnis, loeschenCallback);
        assertNull(loeschenCallback.fehler);
        assertTrue(loeschenCallback.ergebnis);
        ErgebnisCallback<Boolean> erneutLoeschenCallback = new ErgebnisCallback<>();
        rezeptController.loeschen(anlegenCallback.ergebnis, erneutLoeschenCallback);
        assertNull(erneutLoeschenCallback.fehler);
        assertFalse(erneutLoeschenCallback.ergebnis);
    }

    @Test
    public void fehlerBeiUngueltigerRezeptIdWirdWeitergegeben() {
        ErgebnisCallback<RezeptVermittler.RezeptDetails> callback = new ErgebnisCallback<>();
        rezeptController.nachIdMitDetailsAbrufen(0, callback);
        assertNull(callback.ergebnis);
        assertNotNull(callback.fehler);
        assertTrue(callback.fehler instanceof IllegalArgumentException);
    }

    // Einfacher Callback zum Speichern des Testergebnisses
    private static class ErgebnisCallback<T> implements RezeptController.Callback<T> {
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
