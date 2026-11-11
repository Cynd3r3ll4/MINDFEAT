package de.mindfeat.app.model.datengrundlage.dao;

import android.database.sqlite.SQLiteConstraintException;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import de.mindfeat.app.model.datengrundlage.TestDatenbank;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;
import de.mindfeat.app.model.datengrundlage.entitaeten.Kategorie;
import de.mindfeat.app.model.datengrundlage.entitaeten.RezeptKategorie;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)

public class RezeptKategorieDAOTest {

    private TestDatenbank testDatenbank;
    private RezeptDAO rezeptDAO;
    private KategorieDAO kategorieDAO;
    private RezeptKategorieDAO rezeptKategorieDAO;

    // Eine neue In-Memory-Datenbank vor jedem Test erstellen
    @Before
    public void setUp() {
        testDatenbank = new TestDatenbank();
        rezeptDAO = testDatenbank.getDatenbank().rezeptDAO();
        kategorieDAO = testDatenbank.getDatenbank().kategorieDAO();
        rezeptKategorieDAO = testDatenbank.getDatenbank().rezeptKategorieDAO();
    }

    // Nach jedem Test die Datenbank schließen
    @After
    public void tearDown() {
        if (testDatenbank != null) {
            testDatenbank.schliessen();
        }
    }

    // Hilfsmethode zum Erstellen eines gespeicherten Rezepts
    private int rezeptErstellen(String titel) {
        Rezept rezept = new Rezept(titel, null, 2);
        return (int) rezeptDAO.einfuegen(rezept);
    }

    // Hilfsmethode zum Erstellen einer gespeicherten Kategorie
    private int kategorieErstellen(String name) {
        Kategorie kategorie = new Kategorie(name);
        return (int) kategorieDAO.einfuegen(kategorie);
    }

    // Prüfen, ob eine Rezept-Kategorie-Zuordnung gespeichert und anhand ihrer ID abgerufen werden kann
    @Test
    public void einfuegenUndNachIdAbrufen() {
        int rezeptId = rezeptErstellen("Kürbissuppe");
        int kategorieId = kategorieErstellen("herbstlich");
        RezeptKategorie zuordnung = new RezeptKategorie(rezeptId, kategorieId);
        long id = rezeptKategorieDAO.einfuegen(zuordnung);
        RezeptKategorie gespeichert = rezeptKategorieDAO.nachIdAbrufen((int) id);
        assertNotNull(gespeichert);
        assertEquals(rezeptId, gespeichert.getRezeptId());
        assertEquals(kategorieId, gespeichert.getKategorieId());
    }

    // Prüfen, ob eine vorhandene Rezept-Kategorie-Zuordnung aktualisiert werden kann
    @Test
    public void aktualisieren() {
        int rezeptId = rezeptErstellen("Kürbissuppe");
        int ersteKategorieId = kategorieErstellen("herbstlich");
        int zweiteKategorieId = kategorieErstellen("Hauptgericht");
        RezeptKategorie zuordnung = new RezeptKategorie(rezeptId, ersteKategorieId);
        long id = rezeptKategorieDAO.einfuegen(zuordnung);
        zuordnung.setId((int) id);
        zuordnung.setKategorieId(zweiteKategorieId);
        int anzahl = rezeptKategorieDAO.aktualisieren(zuordnung);
        RezeptKategorie aktualisiert = rezeptKategorieDAO.nachIdAbrufen((int) id);
        assertEquals(1, anzahl);
        assertEquals(zweiteKategorieId, aktualisiert.getKategorieId());
    }

    // Prüfen, ob eine Rezept-Kategorie-Zuordnung gelöscht werden kann
    @Test
    public void loeschen() {
        int rezeptId = rezeptErstellen("Kürbissuppe");
        int kategorieId = kategorieErstellen("herbstlich");
        RezeptKategorie zuordnung = new RezeptKategorie(rezeptId, kategorieId);
        long id = rezeptKategorieDAO.einfuegen(zuordnung);
        zuordnung.setId((int) id);
        int anzahl = rezeptKategorieDAO.loeschen(zuordnung);
        assertEquals(1, anzahl);
        assertNull(rezeptKategorieDAO.nachIdAbrufen((int) id));
    }

    // Prüfen, ob alle Kategorien eines Rezepts abgerufen werden und andere Rezepte ausgeschlossen bleiben
    @Test
    public void nachRezeptIdAbrufen() {
        int rezeptId = rezeptErstellen("Kürbissuppe");
        int anderesRezeptId = rezeptErstellen("Apfelkuchen");
        int herbstlichId = kategorieErstellen("herbstlich");
        int hauptgerichtId = kategorieErstellen("Hauptgericht");
        int nachtischId = kategorieErstellen("Nachtisch");
        rezeptKategorieDAO.einfuegen(new RezeptKategorie(rezeptId, herbstlichId));
        rezeptKategorieDAO.einfuegen(new RezeptKategorie(rezeptId, hauptgerichtId));
        rezeptKategorieDAO.einfuegen(new RezeptKategorie(anderesRezeptId, nachtischId));
        List<RezeptKategorie> zuordnungen = rezeptKategorieDAO.nachRezeptIdAbrufen(rezeptId);
        assertEquals(2, zuordnungen.size());
        assertEquals(herbstlichId, zuordnungen.get(0).getKategorieId());
        assertEquals(hauptgerichtId, zuordnungen.get(1).getKategorieId());
    }

    // Prüfen, ob alle Rezepte einer Kategorie abgerufen werden und andere Kategorien ausgeschlossen bleiben
    @Test
    public void nachKategorieIdAbrufen() {
        int suppeId = rezeptErstellen("Kürbissuppe");
        int schnitzelId = rezeptErstellen("Schnitzel");
        int kuchenId = rezeptErstellen("Apfelkuchen");
        int hauptgerichtId = kategorieErstellen("Hauptgericht");
        int nachtischId = kategorieErstellen("Nachtisch");
        rezeptKategorieDAO.einfuegen(new RezeptKategorie(suppeId, hauptgerichtId));
        rezeptKategorieDAO.einfuegen(new RezeptKategorie(schnitzelId, hauptgerichtId));
        rezeptKategorieDAO.einfuegen(new RezeptKategorie(kuchenId, nachtischId));
        List<RezeptKategorie> zuordnungen = rezeptKategorieDAO.nachKategorieIdAbrufen(hauptgerichtId);
        assertEquals(2, zuordnungen.size());
        assertEquals(suppeId, zuordnungen.get(0).getRezeptId());
        assertEquals(schnitzelId, zuordnungen.get(1).getRezeptId());
    }

    // Prüfen, ob bei einer unbekannten ID null zurückgegeben wird
    @Test
    public void nachIdAbrufenNichtVorhanden() {
        RezeptKategorie gefunden = rezeptKategorieDAO.nachIdAbrufen(999);
        assertNull(gefunden);
    }

    // Prüfen, ob eine Rezept-Kategorie-Zuordnung nicht doppelt gespeichert werden kann
    @Test(expected = SQLiteConstraintException.class)
    public void doppelteZuordnungNichtEinfuegen() {
        int rezeptId = rezeptErstellen("Kürbissuppe");
        int kategorieId = kategorieErstellen("herbstlich");
        rezeptKategorieDAO.einfuegen(new RezeptKategorie(rezeptId, kategorieId));
        rezeptKategorieDAO.einfuegen(new RezeptKategorie(rezeptId, kategorieId));
    }

    // Prüfen, ob beim Löschen eines Rezepts dessen Kategorie-Zuordnungen ebenfalls gelöscht werden
    @Test
    public void loeschenRezeptLoeschtZuordnungenMit() {
        int rezeptId = rezeptErstellen("Kürbissuppe");
        int kategorieId = kategorieErstellen("herbstlich");
        rezeptKategorieDAO.einfuegen(new RezeptKategorie(rezeptId, kategorieId));
        Rezept rezept = rezeptDAO.nachIdAbrufen(rezeptId);
        rezeptDAO.loeschen(rezept);
        List<RezeptKategorie> zuordnungen = rezeptKategorieDAO.nachKategorieIdAbrufen(kategorieId);
        assertEquals(0, zuordnungen.size());
    }

    // Prüfen, ob beim Löschen einer Kategorie deren Rezept-Zuordnungen ebenfalls gelöscht werden
    @Test
    public void loeschenKategorieLoeschtZuordnungenMit() {
        int rezeptId = rezeptErstellen("Kürbissuppe");
        int kategorieId = kategorieErstellen("herbstlich");
        rezeptKategorieDAO.einfuegen(new RezeptKategorie(rezeptId, kategorieId));
        Kategorie kategorie = kategorieDAO.nachIdAbrufen(kategorieId);
        kategorieDAO.loeschen(kategorie);
        List<RezeptKategorie> zuordnungen = rezeptKategorieDAO.nachRezeptIdAbrufen(rezeptId);
        assertEquals(0, zuordnungen.size());
    }
}
