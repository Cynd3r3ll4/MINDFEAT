package de.mindfeat.app.datengrundlage.entitaeten;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

public class RezeptZutatUnitTest {

    // Eine gültige Zuordnung wird korrekt erstellt
    @Test
    public void rezeptZutatMitGueltigenWertenErstellen() {
        RezeptZutat rezeptZutat = new RezeptZutat(1, 2, 300.0, "g");
        assertNotNull(rezeptZutat);
        assertEquals(1, rezeptZutat.getRezeptId());
        assertEquals(2, rezeptZutat.getZutatId());
        assertEquals(300.0, rezeptZutat.getMenge(), 0.001);
        assertEquals("g", rezeptZutat.getEinheit());
    }

    // Die Menge darf auch einen Dezimalwert enthalten (z.B. 0,5)
    @Test
    public void dezimalmengeWirdAkzeptiert() {
        RezeptZutat rezeptZutat = new RezeptZutat(1, 2, 0.5, "TL");
        assertEquals(0.5, rezeptZutat.getMenge(), 0.001);
    }

    // Eine Rezept-ID kleiner als 1 wird nicht akzeptiert
    @Test
    public void ungueltigeRezeptIdWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () -> new RezeptZutat(0, 2, 300.0, "g"));
    }

    // Eine Zutaten-ID kleiner als 1 wird nicht akzeptiert
    @Test
    public void ungueltigeZutatIdWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () -> new RezeptZutat(1, 0, 300.0, "g"));
    }

    // Eine Menge von 0 wird nicht akzeptiert
    @Test
    public void nullmengeWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () -> new RezeptZutat(1, 2, 0.0, "g"));
    }

    // Negative Mengen werden nicht akzeptiert
    @Test
    public void negativeMengeWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () -> new RezeptZutat(1, 2, -5.0, "g"));
    }

    // Unendliche Mengen werden nicht akzeptiert
    @Test
    public void unendlicheMengeWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () -> new RezeptZutat(1, 2, Double.POSITIVE_INFINITY, "g"));
    }

    // Eine leere Einheit wird nicht akzeptiert
    @Test
    public void leereEinheitWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () -> new RezeptZutat(1, 2, 300.0, ""));
    }

    // Eine fehlende Einheit wird nicht akzeptiert
    @Test
    public void fehlendeEinheitWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () -> new RezeptZutat(1, 2, 300.0, null));
    }

    // Leerzeichen vor/hinter der Einheit werden entfernt
    @Test
    public void einheitWirdVonLeerzeichenBereinigt() {
        RezeptZutat rezeptZutat = new RezeptZutat(1, 2, 300.0, "  g  ");
        assertEquals("g", rezeptZutat.getEinheit());
    }

    // Eine gültige Mengenänderung wird akzeptiert
    @Test
    public void gueltigeMengenAenderungWirdUebernommen() {
        RezeptZutat rezeptZutat = new RezeptZutat(1, 2, 300.0, "g");
        rezeptZutat.setMenge(450.0);
        assertEquals(450.0, rezeptZutat.getMenge(), 0.001);
    }
}
