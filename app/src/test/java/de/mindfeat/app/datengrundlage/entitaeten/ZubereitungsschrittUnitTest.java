package de.mindfeat.app.datengrundlage.entitaeten;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

public class ZubereitungsschrittUnitTest {

    // Ein gültiger Zubereitungsschritt wird korrekt erstellt
    @Test
    public void schrittMitGueltigenWertenErstellen() {
        Zubereitungsschritt schritt = new Zubereitungsschritt(1, 1, "Gemüse waschen und schneiden.");
        assertNotNull(schritt);
        assertEquals(1, schritt.getRezeptId());
        assertEquals(1, schritt.getReihenfolge());
        assertEquals("Gemüse waschen und schneiden.", schritt.getBeschreibung());
    }

    // Leerzeichen vor/hinter der Beschreibung werden entfernt
    @Test
    public void beschreibungWirdVonLeerzeichenBereinigt() {
        Zubereitungsschritt schritt = new Zubereitungsschritt(1, 1, "  Gemüse schneiden.  ");
        assertEquals("Gemüse schneiden.", schritt.getBeschreibung());
    }

    // Eine Rezept-ID von 0 wird nicht akzeptiert
    @Test
    public void ungueltigeRezeptIdWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new Zubereitungsschritt(0, 1, "Gemüse schneiden."));
    }

    // Eine negative Rezept-ID wird nicht akzeptiert
    @Test
    public void negativeRezeptIdWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new Zubereitungsschritt(-1, 1, "Gemüse schneiden."));
    }

    // Eine Reihenfolge von 0 wird nicht akzeptiert
    @Test
    public void ungueltigeReihenfolgeWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new Zubereitungsschritt(1, 0, "Gemüse schneiden."));
    }

    // Eine negative Reihenfolge wird nicht akzeptiert
    @Test
    public void negativeReihenfolgeWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new Zubereitungsschritt(1, -1, "Gemüse schneiden."));
    }

    // Eine leere Beschreibung wird nicht akzeptiert
    @Test
    public void leereBeschreibungWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new Zubereitungsschritt(1, 1, ""));
    }

    // Eine Beschreibung nur aus Leerzeichen wird nicht akzeptiert
    @Test
    public void beschreibungNurMitLeerzeichenWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new Zubereitungsschritt(1, 1, "   "));
    }

    // Eine fehlende Beschreibung wird nicht akzeptiert
    @Test
    public void fehlendeBeschreibungWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new Zubereitungsschritt(1, 1, null));
    }

    // Eine gültige Reihenfolgeänderung wird akzeptiert
    @Test
    public void gueltigeReihenfolgeAendern() {
        Zubereitungsschritt schritt = new Zubereitungsschritt(
                1, 1, "Gemüse schneiden.");
        schritt.setReihenfolge(2);
        assertEquals(2, schritt.getReihenfolge());
    }

    // Eine gültige Beschreibungsänderung wird akzeptiert
    @Test
    public void gueltigeBeschreibungAendern() {
        Zubereitungsschritt schritt = new Zubereitungsschritt(
                1, 1, "Gemüse schneiden.");
        schritt.setBeschreibung("Gemüse anbraten.");
        assertEquals("Gemüse anbraten.", schritt.getBeschreibung());
    }
}
