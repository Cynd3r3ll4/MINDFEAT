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
import de.mindfeat.app.model.datengrundlage.dao.EssensplanDAO;
import de.mindfeat.app.model.datengrundlage.dao.InhaltEssensplanDAO;
import de.mindfeat.app.model.datengrundlage.dao.KategorieDAO;
import de.mindfeat.app.model.datengrundlage.entitaeten.Essensplan;
import de.mindfeat.app.model.datengrundlage.entitaeten.InhaltEssensplan;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;

@RunWith(AndroidJUnit4.class)
public class EssensplanVermittlerTest {

    private TestDatenbank testDatenbank;
    private EssensplanVermittler essensplanVermittler;
    private RezeptVermittler rezeptVermittler;

    private EssensplanDAO essensplanDAO;
    private InhaltEssensplanDAO inhaltEssensplanDAO;
    private KategorieDAO kategorieDAO;

    // Eine Testdatenbank für die Tests erstellen
    @Before
    public void setUp() {
        testDatenbank = new TestDatenbank();
        essensplanVermittler = new EssensplanVermittler(testDatenbank.getDatenbank());
        rezeptVermittler = new RezeptVermittler(testDatenbank.getDatenbank());
        essensplanDAO = testDatenbank.getDatenbank().essensplanDAO();
        inhaltEssensplanDAO = testDatenbank.getDatenbank().inhaltEssensplanDAO();
        kategorieDAO = testDatenbank.getDatenbank().kategorieDAO();
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

    // Hilfsmethode: ein einfaches Rezept ohne optionale Angaben anlegen
    private int rezeptAnlegen(String titel) {
        Rezept rezept = new Rezept(titel, null, 2);
        return rezeptVermittler.anlegen(rezept, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    // Hilfsmethode: ein Rezept mit Zutaten und Kategorien anlegen
    private int rezeptAnlegen(String titel, List<RezeptVermittler.ZutatAngabe> zutaten, List<String> kategorien) {
        Rezept rezept = new Rezept(titel, null, 2);
        return rezeptVermittler.anlegen(rezept, zutaten, kategorien, Collections.emptyList());
    }

    // Vorhandensein einer Rezept-ID in den Vorschlägen
    private boolean vorschlaegeEnthaltenRezept(List<RezeptVermittler.RezeptDetails> vorschlaege, int rezeptId) {
        for (RezeptVermittler.RezeptDetails details : vorschlaege) {
            if (details.getRezept().getId() == rezeptId) {
                return true;
            }
        }
        return false;
    }

    // Hilfsmethode: eine IllegalArgumentException erwarten
    private void erwarteIllegalArgumentException(Runnable aktion) {
        boolean fehlerAusgeloest = false;
        try {
            aktion.run();
        } catch (IllegalArgumentException e) {
            fehlerAusgeloest = true;
        }
        assertTrue("Es wurde eine IllegalArgumentException erwartet.", fehlerAusgeloest);
    }


    // Einen Essensplan anlegen und abrufen
    @Test
    public void essensplanAnlegenUndAbrufen() {
        int essensplanId = essensplanAnlegen();
        assertTrue(essensplanId > 0);
        assertNotNull(essensplanDAO.nachIdAbrufen(essensplanId));
        assertEquals(1, essensplanVermittler.alleAbrufen().size());
    }

    // Einen erstellten Essensplan bearbeiten
    @Test
    public void essensplanBearbeiten() {
        int essensplanId = essensplanAnlegen();
        Essensplan essensplan = essensplanDAO.nachIdAbrufen(essensplanId);
        essensplan.setTitel("Geänderter Essensplan");
        essensplanVermittler.bearbeiten(essensplan);
        Essensplan gespeicherterPlan = essensplanDAO.nachIdAbrufen(essensplanId);
        assertNotNull(gespeicherterPlan);
        assertEquals("Geänderter Essensplan", gespeicherterPlan.getTitel());
    }

    // Ein Plan-Zeitraum kann nicht geändert werden, wenn dabei bereits geplante Tage außerhalb des neuen Zeitraums liegen
    @Test
    public void essensplanZeitraumNichtUeberBestehendeEintraegeHinausVerkleinern() {
        int essensplanId = essensplanAnlegen();
        int rezeptId = rezeptAnlegen("Kartoffelsuppe");
        essensplanVermittler.inhaltHinzufuegen(essensplanId, rezeptId, "2026-11-05");
        Essensplan essensplan = essensplanDAO.nachIdAbrufen(essensplanId);
        essensplan.setEndDatum("2026-11-03");
        erwarteIllegalArgumentException(() -> essensplanVermittler.bearbeiten(essensplan));
        assertEquals("2026-11-07", essensplanDAO.nachIdAbrufen(essensplanId).getEndDatum());
    }


    // Einen Essensplan mit Einträgen abrufen
    @Test
    public void essensplanMitEintraegenUndRezeptdetailsAbrufen() {
        int essensplanId = essensplanAnlegen();
        int rezeptId = rezeptAnlegen("Kartoffelsuppe");
        essensplanVermittler.inhaltHinzufuegen(essensplanId, rezeptId, "2026-11-03");
        EssensplanVermittler.EssensplanDetails details = essensplanVermittler.nachIdMitDetailsAbrufen(essensplanId);
        assertNotNull(details);
        assertEquals(essensplanId, details.getEssensplan().getId());
        assertEquals(1, details.getEintraege().size());
        assertEquals("Kartoffelsuppe", details.getEintraege().get(0).getRezeptDetails().getRezept().getTitel());
    }

    // nicht-vorhandenen Plan abrufen
    @Test
    public void nichtVorhandenenEssensplanMitDetailsAbrufenLiefertNull() {
        assertNull(essensplanVermittler.nachIdMitDetailsAbrufen(999));
    }

    // Rezept zum Plan hinzufügen (an bestimmtem Tag)
    @Test
    public void rezeptZumEssensplanHinzufuegen() {
        int essensplanId = essensplanAnlegen();
        int rezeptId = rezeptAnlegen("Kartoffelsuppe");
        long eintragId = essensplanVermittler.inhaltHinzufuegen(essensplanId, rezeptId, "2026-11-03");
        assertTrue(eintragId > 0);
        InhaltEssensplan eintrag = inhaltEssensplanDAO.nachIdAbrufen((int) eintragId);
        assertNotNull(eintrag);
        assertEquals(essensplanId, eintrag.getEssensplanId());
        assertEquals(rezeptId, eintrag.getRezeptId());
        assertEquals("2026-11-03", eintrag.getDatum());
    }

    // Rezept zum Plan hinzufügen an einem Tag außerhalb des Plan-Zeitraums
    @Test
    public void rezeptAusserhalbDesPlanzeitraumsNichtHinzufuegen() {
        int essensplanId = essensplanAnlegen();
        int rezeptId = rezeptAnlegen("Kartoffelsuppe");
        erwarteIllegalArgumentException(() -> essensplanVermittler.inhaltHinzufuegen(essensplanId, rezeptId, "2026-11-10"));
        assertEquals(0, inhaltEssensplanDAO.nachEssensplanIdAbrufen(essensplanId).size());
    }


    // nicht-vorhandenes Rezept hinzufügen (wird nicht akzeptiert)
    @Test
    public void nichtVorhandenesRezeptNichtHinzufuegen() {
        int essensplanId = essensplanAnlegen();
        erwarteIllegalArgumentException(() -> essensplanVermittler.inhaltHinzufuegen(essensplanId, 999, "2026-11-03"));
    }

    // Einen einzelnen Eintrag aus einem Plan entfernen
    @Test
    public void einzelnenEssensplaneintragEntfernen() {
        int essensplanId = essensplanAnlegen();
        int rezeptId = rezeptAnlegen("Kartoffelsuppe");
        long eintragId = essensplanVermittler.inhaltHinzufuegen(essensplanId, rezeptId, "2026-11-03");
        essensplanVermittler.inhaltEntfernen((int) eintragId);
        assertNull(inhaltEssensplanDAO.nachIdAbrufen((int) eintragId));
        assertEquals(0, inhaltEssensplanDAO.nachEssensplanIdAbrufen(essensplanId).size());
    }


    // Gesamten Plan inkl. Zugehörigkeiten löschen
    @Test
    public void essensplanMitZugehoerigenEintraegenLoeschen() {
        int essensplanId = essensplanAnlegen();
        int rezeptId = rezeptAnlegen("Kartoffelsuppe");
        essensplanVermittler.inhaltHinzufuegen(essensplanId, rezeptId, "2026-11-03");
        essensplanVermittler.loeschen(essensplanId);
        assertNull(essensplanDAO.nachIdAbrufen(essensplanId));
        assertEquals(0, inhaltEssensplanDAO.nachEssensplanIdAbrufen(essensplanId).size());
    }

    // Einen random Vorschlag für einen Essensplan generieren
    @Test
    public void vorschlaegeOhneFilterBeruecksichtigenAlleRezepte() {
        int essensplanId = essensplanAnlegen();
        int rezept1 = rezeptAnlegen("Kartoffelsuppe");
        int rezept2 = rezeptAnlegen("Gulasch");
        int rezept3 = rezeptAnlegen("Schmorbraten");
        List<RezeptVermittler.RezeptDetails> vorschlaege = essensplanVermittler.vorschlaegeErmitteln(essensplanId, Collections.emptyList(), false);
        assertEquals(3, vorschlaege.size());
        assertTrue(vorschlaegeEnthaltenRezept(vorschlaege, rezept1));
        assertTrue(vorschlaegeEnthaltenRezept(vorschlaege, rezept2));
        assertTrue(vorschlaegeEnthaltenRezept(vorschlaege, rezept3));
    }

    // Einen Vorschlag nach Kategorien gefiltert generieren (mehrere mit ODER verknüpft)
    @Test
    public void kategoriefilterVerknuepftMehrereKategorienMitOder() {
        int essensplanId = essensplanAnlegen();
        int rezeptHauptgericht = rezeptAnlegen("Kartoffelsuppe", Collections.emptyList(), Collections.singletonList("Hauptgericht"));
        int rezeptHerbstlich = rezeptAnlegen("Gulasch", Collections.emptyList(), Collections.singletonList("Herbstlich"));
        int rezeptDessert = rezeptAnlegen("Tiramisu", Collections.emptyList(), Collections.singletonList("Dessert"));
        int hauptgerichtId = kategorieDAO.nachNameAbrufen("Hauptgericht").getId();
        int herbstlichId = kategorieDAO.nachNameAbrufen("Herbstlich").getId();
        List<RezeptVermittler.RezeptDetails> vorschlaege = essensplanVermittler.vorschlaegeErmitteln(essensplanId, Arrays.asList(hauptgerichtId, herbstlichId), false);
        assertEquals(2, vorschlaege.size());
        assertTrue(vorschlaegeEnthaltenRezept(vorschlaege, rezeptHauptgericht));
        assertTrue(vorschlaegeEnthaltenRezept(vorschlaege, rezeptHerbstlich));
        assertFalse(vorschlaegeEnthaltenRezept(vorschlaege, rezeptDessert));
    }

    // Bei den Vorschlägen die Nachhaltigkeit berücksichtigen (Vorschläge mit wiederverwendeten Zutaten)
    @Test
    public void nachhaltigkeitsfilterBeruecksichtigtWiederverwendeteZutaten() {
        int essensplanId = essensplanAnlegen();
        int rezeptImPlan = rezeptAnlegen("Kartoffelsuppe",
                Collections.singletonList(new RezeptVermittler.ZutatAngabe("Kartoffeln", 500, "g")),
                Collections.emptyList());
        int rezeptMitGemeinsamerZutat = rezeptAnlegen("Kartoffelsalat",
                Arrays.asList(
                        new RezeptVermittler.ZutatAngabe("Kartoffeln", 600, "g"),
                        new RezeptVermittler.ZutatAngabe("Mayonnaise", 200, "g")),
                Collections.emptyList());
        int rezeptOhneGemeinsameZutat = rezeptAnlegen("Gulasch",
                Collections.singletonList(new RezeptVermittler.ZutatAngabe("Zwiebeln", 400, "g")),
                Collections.emptyList());
        essensplanVermittler.inhaltHinzufuegen(essensplanId, rezeptImPlan, "2026-11-03");
        List<RezeptVermittler.RezeptDetails> vorschlaege = essensplanVermittler.vorschlaegeErmitteln(essensplanId, Collections.emptyList(), true);
        assertTrue(vorschlaegeEnthaltenRezept(vorschlaege, rezeptMitGemeinsamerZutat));
        assertFalse(vorschlaegeEnthaltenRezept(vorschlaege, rezeptOhneGemeinsameZutat));
    }

    // Bei den Vorschlägen die Nachhaltigkeit und Kategorien berücksichtigen
    @Test
    public void kategorieUndNachhaltigkeitsfilterWerdenKombiniert() {
        int essensplanId = essensplanAnlegen();
        int rezeptImPlan = rezeptAnlegen("Kartoffelsuppe",
                Collections.singletonList(new RezeptVermittler.ZutatAngabe("Kartoffeln", 500, "g")),
                Collections.singletonList("Hauptgericht"));
        int rezeptPassend = rezeptAnlegen("Kartoffelsalat",
                Collections.singletonList(new RezeptVermittler.ZutatAngabe("Kartoffeln", 600, "g")),
                Collections.singletonList("Hauptgericht"));
        int rezeptFalscheKategorie = rezeptAnlegen("Kartoffel-Krapfen", // Sir, das ist ein Krapfen, Sir
                Collections.singletonList(new RezeptVermittler.ZutatAngabe("Kartoffeln", 200, "g")),
                Collections.singletonList("Dessert"));
        int rezeptFalscheZutat = rezeptAnlegen("Graupeneintopf",
                Collections.singletonList(new RezeptVermittler.ZutatAngabe("Graupen", 300, "g")),
                Collections.singletonList("Hauptgericht"));
        essensplanVermittler.inhaltHinzufuegen(essensplanId, rezeptImPlan, "2026-11-03");
        int hauptgerichtId = kategorieDAO.nachNameAbrufen("Hauptgericht").getId();
        List<RezeptVermittler.RezeptDetails> vorschlaege = essensplanVermittler.vorschlaegeErmitteln(essensplanId, Collections.singletonList(hauptgerichtId), true);
        assertTrue(vorschlaegeEnthaltenRezept(vorschlaege, rezeptPassend));
        assertFalse(vorschlaegeEnthaltenRezept(vorschlaege, rezeptFalscheKategorie));
        assertFalse(vorschlaegeEnthaltenRezept(vorschlaege, rezeptFalscheZutat));
    }

    // Wenn noch keine Rezepte vorhanden sind, der Nachhaltigkeitsfilter aber aktiv ist, werden keine Vorschläge gemacht
    @Test
    public void nachhaltigkeitsfilterOhneBisherigeEintraegeLiefertKeineVorschlaege() {
        int essensplanId = essensplanAnlegen();
        rezeptAnlegen("Kartoffelsuppe",
                Collections.singletonList(new RezeptVermittler.ZutatAngabe("Kartoffeln", 500, "g")),
                Collections.emptyList());
        List<RezeptVermittler.RezeptDetails> vorschlaege = essensplanVermittler.vorschlaegeErmitteln(essensplanId, Collections.emptyList(), true);
        assertTrue(vorschlaege.isEmpty());
    }

    // Einen einzelnen random Vorschlag ermitteln
    @Test
    public void zufaelligenVorschlagErmitteln() {
        int essensplanId = essensplanAnlegen();
        rezeptAnlegen("Kartoffelsuppe");
        RezeptVermittler.RezeptDetails vorschlag = essensplanVermittler.zufaelligenVorschlagErmitteln(essensplanId, Collections.emptyList(), false);
        assertNotNull(vorschlag);
        assertEquals("Kartoffelsuppe", vorschlag.getRezept().getTitel());
    }


    // Wenn noch keine Rezepte vorhanden sind, werden keine Vorschläge gemacht
    @Test
    public void zufaelligenVorschlagOhnePassendeRezepteLiefertNull() {
        int essensplanId = essensplanAnlegen();
        RezeptVermittler.RezeptDetails vorschlag = essensplanVermittler.zufaelligenVorschlagErmitteln(essensplanId, Collections.emptyList(), false);
        assertNull(vorschlag);
    }
}
