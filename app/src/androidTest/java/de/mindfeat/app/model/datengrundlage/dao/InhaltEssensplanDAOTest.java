package de.mindfeat.app.model.datengrundlage.dao;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import de.mindfeat.app.model.datengrundlage.entitaeten.Essensplan;
import de.mindfeat.app.model.datengrundlage.entitaeten.InhaltEssensplan;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

@RunWith(AndroidJUnit4.class)
public class InhaltEssensplanDAOTest {

    private InhaltEssensplanDAOTestDatenbank datenbank;
    private EssensplanDAO essensplanDAO;
    private RezeptDAO rezeptDAO;
    private InhaltEssensplanDAO inhaltEssensplanDAO;

    // Vor jedem Test eine neue In-Memory-Datenbank erstellen
    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        datenbank = Room.inMemoryDatabaseBuilder(context, InhaltEssensplanDAOTestDatenbank.class).allowMainThreadQueries().build();
        essensplanDAO = datenbank.essensplanDAO();
        rezeptDAO = datenbank.rezeptDAO();
        inhaltEssensplanDAO = datenbank.inhaltEssensplanDAO();
    }

    // Nach jedem Test die Datenbank schließen
    @After
    public void tearDown() {
        datenbank.close();
    }

    // Einen Essensplan für die Testdatenbank erstellen
    private int essensplanErstellen(String titel) {
        Essensplan essensplan = new Essensplan(titel, "2026-11-01", "2026-11-07", 1);
        return (int) essensplanDAO.einfuegen(essensplan);
    }

    // Ein Rezept für die Testdatenbank erstellen
    private int rezeptErstellen(String titel) {
        Rezept rezept = new Rezept(titel, null, 2);
        return (int) rezeptDAO.einfuegen(rezept);
    }

    // Einen Eintrag für einen Essensplan erstellen
    private InhaltEssensplan inhaltErstellen(int essensplanId, int rezeptId, String datum) {
        InhaltEssensplan inhalt = new InhaltEssensplan(essensplanId, rezeptId, datum);
        long id = inhaltEssensplanDAO.einfuegen(inhalt);
        inhalt.setId((int) id);
        return inhalt;
    }

    // Einen Eintrag speichern und anhand seiner ID abrufen
    @Test
    public void einfuegenUndNachIdAbrufen() {
        int essensplanId = essensplanErstellen("Wochenplan");
        int rezeptId = rezeptErstellen("Gulasch");
        InhaltEssensplan inhalt = new InhaltEssensplan(essensplanId, rezeptId, "2026-11-02");
        long id = inhaltEssensplanDAO.einfuegen(inhalt);
        InhaltEssensplan gespeichert = inhaltEssensplanDAO.nachIdAbrufen((int) id);
        assertEquals(essensplanId, gespeichert.getEssensplanId());
        assertEquals(rezeptId, gespeichert.getRezeptId());
        assertEquals("2026-11-02", gespeichert.getDatum());
    }

    // Einen vorhandenen Eintrag aktualisieren
    @Test
    public void aktualisieren() {
        int essensplanId = essensplanErstellen("Wochenplan");
        int rezeptId = rezeptErstellen("Gerstensuppe");
        InhaltEssensplan inhalt = inhaltErstellen(essensplanId, rezeptId, "2026-11-02");
        inhalt.setDatum("2026-11-03");
        int anzahl = inhaltEssensplanDAO.aktualisieren(inhalt);
        InhaltEssensplan aktualisiert = inhaltEssensplanDAO.nachIdAbrufen(inhalt.getId());
        assertEquals(1, anzahl);
        assertEquals("2026-11-03", aktualisiert.getDatum());
    }

    // Einen vorhandenen Eintrag löschen
    @Test
    public void loeschen() {
        int essensplanId = essensplanErstellen("Wochenplan");
        int rezeptId = rezeptErstellen("Kürbissuppe");
        InhaltEssensplan inhalt = inhaltErstellen(essensplanId, rezeptId, "2026-11-02");
        int anzahl = inhaltEssensplanDAO.loeschen(inhalt);
        assertEquals(1, anzahl);
        assertNull(inhaltEssensplanDAO.nachIdAbrufen(inhalt.getId()));
    }

    // Alle Einträge chronologisch sortiert abrufen
    @Test
    public void alleAbrufenSortiertNachEssensplanUndDatum() {
        int essensplanId = essensplanErstellen("Wochenplan");
        int rezeptId1 = rezeptErstellen("Kartoffelsuppe");
        int rezeptId2 = rezeptErstellen("Apfelpfannkuchen");
        inhaltErstellen(essensplanId, rezeptId1, "2026-11-05");
        inhaltErstellen(essensplanId, rezeptId2, "2026-11-02");
        List<InhaltEssensplan> inhalte = inhaltEssensplanDAO.alleAbrufen();
        assertEquals(2, inhalte.size());
        assertEquals("2026-11-02", inhalte.get(0).getDatum());
        assertEquals("2026-11-05", inhalte.get(1).getDatum());
    }

    // Nur die Einträge des ausgewählten Essensplans abrufen
    @Test
    public void nachEssensplanIdAbrufen() {
        int essensplanId1 = essensplanErstellen("Wochenplan 1");
        int essensplanId2 = essensplanErstellen("Wochenplan 2");
        int rezeptId1 = rezeptErstellen("Kartoffelsuppe");
        int rezeptId2 = rezeptErstellen("Apfelpfannkuchen");
        inhaltErstellen(essensplanId1, rezeptId1, "2026-11-02");
        inhaltErstellen(essensplanId1, rezeptId2, "2026-11-04");
        inhaltErstellen(essensplanId2, rezeptId1, "2026-11-03");
        List<InhaltEssensplan> inhalte = inhaltEssensplanDAO.nachEssensplanIdAbrufen(essensplanId1);
        assertEquals(2, inhalte.size());
        assertEquals(essensplanId1, inhalte.get(0).getEssensplanId());
        assertEquals(essensplanId1, inhalte.get(1).getEssensplanId());
        assertEquals("2026-11-02", inhalte.get(0).getDatum());
        assertEquals("2026-11-04", inhalte.get(1).getDatum());
    }

    // Einträge für ein bestimmtes Datum abrufen
    @Test
    public void nachDatumAbrufen() {
        int essensplanId = essensplanErstellen("Wochenplan");
        int rezeptId1 = rezeptErstellen("Gulasch");
        int rezeptId2 = rezeptErstellen("Kartoffelsalat");
        inhaltErstellen(essensplanId, rezeptId1, "2026-11-02");
        inhaltErstellen(essensplanId, rezeptId2, "2026-11-03");
        List<InhaltEssensplan> inhalte = inhaltEssensplanDAO.nachDatumAbrufen("2026-11-02");
        assertEquals(1, inhalte.size());
        assertEquals("2026-11-02", inhalte.get(0).getDatum());
        assertEquals(rezeptId1, inhalte.get(0).getRezeptId());
    }

    // Bei einer nicht vorhandenen ID null zurückgeben
    @Test
    public void nachIdAbrufenNichtVorhanden() {
        InhaltEssensplan inhalt = inhaltEssensplanDAO.nachIdAbrufen(999);
        assertNull(inhalt);
    }

    // Dieselbe Rezeptzuordnung am selben Tag nicht doppelt speichern
    @Test
    public void doppelteZuordnungAmGleichenTagNichtMoeglich() {
        int essensplanId = essensplanErstellen("Wochenplan");
        int rezeptId = rezeptErstellen("Gulasch");
        inhaltErstellen(essensplanId, rezeptId, "2026-11-02");
        InhaltEssensplan doppelt = new InhaltEssensplan(essensplanId, rezeptId, "2026-11-02");
        assertThrows(RuntimeException.class, () -> inhaltEssensplanDAO.einfuegen(doppelt));
    }

    // Beim Löschen eines Essensplans dessen Einträge mitlöschen
    @Test
    public void loeschenEssensplanLoeschtZugehoerigeInhalte() {
        int essensplanId = essensplanErstellen("Wochenplan");
        int rezeptId = rezeptErstellen("Gulasch");
        inhaltErstellen(essensplanId, rezeptId, "2026-11-02");
        Essensplan essensplan = essensplanDAO.nachIdAbrufen(essensplanId);
        essensplanDAO.loeschen(essensplan);
        List<InhaltEssensplan> inhalte = inhaltEssensplanDAO.alleAbrufen();
        assertEquals(0, inhalte.size());
    }

    // Beim Löschen eines Rezepts dessen Zuordnungen mitlöschen
    @Test
    public void loeschenRezeptLoeschtZugehoerigeInhalte() {
        int essensplanId = essensplanErstellen("Wochenplan");
        int rezeptId = rezeptErstellen("Gulasch");
        inhaltErstellen(essensplanId, rezeptId, "2026-11-02");
        Rezept rezept = rezeptDAO.nachIdAbrufen(rezeptId);
        rezeptDAO.loeschen(rezept);
        List<InhaltEssensplan> inhalte = inhaltEssensplanDAO.alleAbrufen();
        assertEquals(0, inhalte.size());
    }
}
