package de.mindfeat.app.model.vermittler;

import static org.junit.Assert.assertEquals;
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

import de.mindfeat.app.model.datengrundlage.dao.KategorieDAO;
import de.mindfeat.app.model.datengrundlage.dao.RezeptDAO;
import de.mindfeat.app.model.datengrundlage.dao.RezeptKategorieDAO;
import de.mindfeat.app.model.datengrundlage.dao.RezeptZutatDAO;
import de.mindfeat.app.model.datengrundlage.TestDatenbank;
import de.mindfeat.app.model.datengrundlage.dao.ZubereitungsschrittDAO;
import de.mindfeat.app.model.datengrundlage.dao.ZutatDAO;
import de.mindfeat.app.model.datengrundlage.entitaeten.Kategorie;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;
import de.mindfeat.app.model.datengrundlage.entitaeten.Zutat;

@RunWith(AndroidJUnit4.class)
public class RezeptVermittlerTest {
    private TestDatenbank testDatenbank;
    private RezeptVermittler rezeptVermittler;
    private RezeptDAO rezeptDAO;
    private RezeptZutatDAO rezeptZutatDAO;
    private RezeptKategorieDAO rezeptKategorieDAO;
    private ZubereitungsschrittDAO zubereitungsschrittDAO;
    private KategorieDAO kategorieDAO;
    private ZutatDAO zutatDAO;

    @Before
    public void setUp() {
        testDatenbank = new TestDatenbank();
        rezeptVermittler = new RezeptVermittler(testDatenbank.getDatenbank());
        rezeptDAO = testDatenbank.getDatenbank().rezeptDAO();
        rezeptZutatDAO = testDatenbank.getDatenbank().rezeptZutatDAO();
        rezeptKategorieDAO = testDatenbank.getDatenbank().rezeptKategorieDAO();
        zubereitungsschrittDAO = testDatenbank.getDatenbank().zubereitungsschrittDAO();
        kategorieDAO = testDatenbank.getDatenbank().kategorieDAO();
        zutatDAO = testDatenbank.getDatenbank().zutatDAO();
    }

    @After
    public void tearDown() {
        if (testDatenbank != null) {
            testDatenbank.schliessen();
        }
    }

    // Ein vollständiges Rezept mitsamt Kategorien und Zubereitungsschritten anlegen
    @Test
    public void rezeptMitZutatenKategorienUndSchrittenAnlegen() {
        // Zutaten vorbereiten
        List<RezeptVermittler.ZutatAngabe> zutaten = Arrays.asList(
                new RezeptVermittler.ZutatAngabe("Zwiebeln", 200, "g"),
                new RezeptVermittler.ZutatAngabe("Kartoffeln", 150, "g"));
        // Kategorien vorbereiten
        List<String> kategorien = Arrays.asList("Herbstlich", "Hauptgericht");
        // Zubereitungsschritte vorbereiten
        List<String> schritte = Arrays.asList("Wasser zum Kochen bringen.", "Kartoffeln kochen.", "Zwiebeln schneiden und anbraten.");
        // Rezept anlegen
        Rezept rezept = new Rezept("Kartoffelsuppe", null, 2);
        int rezeptId = rezeptVermittler.anlegen(rezept, zutaten, kategorien, schritte);
        assertTrue(rezeptId > 0); // Überprüfen, ob das Rezept gespeichert wurde
        assertNotNull(rezeptDAO.nachIdAbrufen(rezeptId));
        assertEquals(2, rezeptZutatDAO.nachRezeptIdAbrufen(rezeptId).size()); // Überprüfen, ob die Zuordnungen gespeichert wurden
        assertEquals(2, rezeptKategorieDAO.nachRezeptIdAbrufen(rezeptId).size());
        assertEquals(3, zubereitungsschrittDAO.nachRezeptIdAbrufen(rezeptId).size());
    }

    // Ein vollständiges Rezept ohne die optionalen Angaben anlegen
    @Test
    public void rezeptOhneOptionaleAngabenAnlegen() {
        Rezept rezept = new Rezept("Einfaches Rezept", null, 1);
        int rezeptId = rezeptVermittler.anlegen(rezept, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertTrue(rezeptId > 0);
        assertNotNull(rezeptDAO.nachIdAbrufen(rezeptId));
        assertEquals(0, rezeptZutatDAO.nachRezeptIdAbrufen(rezeptId).size());
        assertEquals(0, rezeptKategorieDAO.nachRezeptIdAbrufen(rezeptId).size());
        assertEquals(0, zubereitungsschrittDAO.nachRezeptIdAbrufen(rezeptId).size());
    }

    // Nur ein Kategorie-Datensatz der gleichen Art darf existieren, unabhängig von der Groß- und Kleinschreibung
    @Test
    public void gleicheKategorieMitUnterschiedlicherSchreibweiseNurEinmalSpeichern() {
        Rezept rezept = new Rezept("Herbstsuppe", null, 2);
        int rezeptId = rezeptVermittler.anlegen(rezept, Collections.emptyList(), Arrays.asList("Herbstlich", "herbstlich"), Collections.emptyList());
        assertEquals(1, kategorieDAO.alleAbrufen().size());
        assertEquals(1, rezeptKategorieDAO.nachRezeptIdAbrufen(rezeptId).size());
        assertEquals(kategorieDAO.nachNameAbrufen("Herbstlich").getId(), kategorieDAO.nachNameAbrufen("herbstlich").getId());
    }

    // Ein vollständiges Rezept mit bereits vorhandenen Zutaten/ Kategorien anlegen (und abweichender Schreibweise)
    @Test
    public void vorhandeneZutatenUndKategorienWiederverwenden() {
        Zutat vorhandeneZutat = new Zutat("Kartoffeln"); // Vorhandene Zutat anlegen
        int zutatId = (int) zutatDAO.einfuegen(vorhandeneZutat);
        vorhandeneZutat.setId(zutatId);
        Kategorie vorhandeneKategorie = new Kategorie("Herbstlich"); // Vorhandene Kategorie anlegen
        int kategorieId = (int) kategorieDAO.einfuegen(vorhandeneKategorie);
        vorhandeneKategorie.setId(kategorieId);
        Rezept rezept = new Rezept("Kartoffelsuppe", null, 2); // Neues Rezept mit abweichender Groß-/Kleinschreibung anlegen
        int rezeptId = rezeptVermittler.anlegen(rezept,
                Collections.singletonList(new RezeptVermittler.ZutatAngabe("kartoffeln", 500, "g")),
                Collections.singletonList("HERBSTLICH"),
                Collections.emptyList());
        assertEquals(1, zutatDAO.alleAbrufen().size());
        assertEquals(1, kategorieDAO.alleAbrufen().size());
        assertEquals(zutatId, rezeptZutatDAO.nachRezeptIdAbrufen(rezeptId).get(0).getZutatId());
        assertEquals(kategorieId, rezeptKategorieDAO.nachRezeptIdAbrufen(rezeptId).get(0).getKategorieId());
    }

    // Backroll bei der Übergabe ungültiger Daten
    @Test
    public void beiUngueltigenZubereitungsschrittenAllesZurueckrollen() {
        Rezept rezept = new Rezept("Testrezept", null, 2);
        List<RezeptVermittler.ZutatAngabe> zutaten = Collections.singletonList(new RezeptVermittler.ZutatAngabe("Kartoffeln", 500, "g"));
        List<String> kategorien = Collections.singletonList("Herbstlich");
        List<String> schritte = Arrays.asList("Kartoffeln schälen.", "   "); // ungültige Eingabe
        boolean fehlerAusgeloest = false;
        try {
            rezeptVermittler.anlegen(rezept, zutaten, kategorien, schritte);
        } catch (IllegalArgumentException e) {
            fehlerAusgeloest = true;
        }
        assertTrue(fehlerAusgeloest);
        assertEquals(0, rezeptDAO.alleAbrufen().size());
        assertEquals(0, zutatDAO.alleAbrufen().size());
        assertEquals(0, kategorieDAO.alleAbrufen().size());
        assertEquals(0, rezeptZutatDAO.nachRezeptIdAbrufen(rezept.getId()).size());
        assertEquals(0, rezeptKategorieDAO.nachRezeptIdAbrufen(rezept.getId()).size());
        assertEquals(0, zubereitungsschrittDAO.nachRezeptIdAbrufen(rezept.getId()).size());
    }

    // Ein vorhandenes Rezept bearbeiten
    @Test
    public void rezeptMitAllenAngabenBearbeiten() {
        Rezept rezept = new Rezept("Kartoffelsuppe", null, 2);
        int rezeptId = rezeptVermittler.anlegen(rezept,
                Collections.singletonList(new RezeptVermittler.ZutatAngabe("Kartoffeln", 500, "g")),
                Collections.singletonList("Hauptgericht"),
                Collections.singletonList("Kartoffeln kochen."));
        rezept.setTitel("Cremige Kartoffelsuppe");
        List<RezeptVermittler.ZutatAngabe> neueZutaten = Arrays.asList(
                new RezeptVermittler.ZutatAngabe("Kartoffeln", 600, "g"),
                new RezeptVermittler.ZutatAngabe("Sahne", 200, "ml"));
        List<String> neueKategorien = Arrays.asList("Hauptgericht", "Herbstlich");
        List<String> neueSchritte = Arrays.asList("Kartoffeln schälen und schneiden.", "Kartoffeln kochen.", "Sahne hinzugeben und pürieren.");
        rezeptVermittler.bearbeiten(rezept, neueZutaten, neueKategorien, neueSchritte);
        RezeptVermittler.RezeptDetails details = rezeptVermittler.nachIdMitDetailsAbrufen(rezeptId);
        assertNotNull(details);
        assertEquals("Cremige Kartoffelsuppe", details.getRezept().getTitel());
        assertEquals(2, details.getRezeptZutaten().size());
        assertEquals(2, details.getRezeptKategorien().size());
        assertEquals(3, details.getZubereitungsschritte().size());
    }


    // Ein Rezept vollständig löschen
    @Test
    public void rezeptMitAllenZugehoerigenDatenLoeschen() {
        Rezept rezept = new Rezept("Kartoffelsuppe", null, 2);
        int rezeptId = rezeptVermittler.anlegen(rezept,
                Collections.singletonList(new RezeptVermittler.ZutatAngabe("Kartoffeln", 500, "g")),
                Collections.singletonList("Hauptgericht"),
                Arrays.asList("Kartoffeln schälen.", "Kartoffeln kochen."));
        boolean geloescht = rezeptVermittler.loeschen(rezeptId);
        assertTrue(geloescht);
        assertNull(rezeptDAO.nachIdAbrufen(rezeptId));
        assertEquals(0, rezeptZutatDAO.nachRezeptIdAbrufen(rezeptId).size());
        assertEquals(0, rezeptKategorieDAO.nachRezeptIdAbrufen(rezeptId).size());
        assertEquals(0, zubereitungsschrittDAO.nachRezeptIdAbrufen(rezeptId).size());
    }


    // Rezeptdetails und Liste aufrufen
    @Test
    public void rezeptdetailsUndRezeptlisteGezieltAbrufen() {
        Rezept rezept1 = new Rezept("Kartoffelsuppe", null, 2);
        int rezeptId1 = rezeptVermittler.anlegen(rezept1,
                Collections.singletonList(new RezeptVermittler.ZutatAngabe("Kartoffeln", 500, "g")),
                Collections.singletonList("Hauptgericht"),
                Collections.singletonList("Kartoffeln kochen."));
        Rezept rezept2 = new Rezept("Gulasch", null, 4);
        rezeptVermittler.anlegen(rezept2,
                Collections.singletonList(new RezeptVermittler.ZutatAngabe("Zwiebeln", 300, "g")),
                Collections.singletonList("Eintopf"),
                Collections.singletonList("Zwiebeln schneiden."));
        RezeptVermittler.RezeptDetails details = rezeptVermittler.nachIdMitDetailsAbrufen(rezeptId1);
        assertNotNull(details);
        assertEquals("Kartoffelsuppe", details.getRezept().getTitel());
        assertEquals(1, details.getZutaten().size());
        assertEquals(1, details.getKategorien().size());
        assertEquals(1, details.getZubereitungsschritte().size());
        List<Rezept> rezepte = rezeptVermittler.alleAbrufen();
        assertEquals(2, rezepte.size());
    }
}
