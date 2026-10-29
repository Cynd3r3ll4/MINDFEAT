package de.mindfeat.app.datengrundlage.entitaeten;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

public class RezeptKategorieUnitTest {

    // Eine gültige Zuordnung wird korrekt erstellt
    @Test
    public void rezeptKategorieMitGueltigenWertenErstellen() {
        RezeptKategorie zuordnung = new RezeptKategorie(1, 2);
        assertNotNull(zuordnung);
        assertEquals(1, zuordnung.getRezeptId());
        assertEquals(2, zuordnung.getKategorieId());
    }

    // Eine Rezept-ID von 0 wird nicht akzeptiert
    @Test
    public void ungueltigeRezeptIdWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new RezeptKategorie(0, 2));
    }

    // Eine negative Rezept-ID wird nicht akzeptiert
    @Test
    public void negativeRezeptIdWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new RezeptKategorie(-1, 2));
    }

    // Eine Kategorie-ID von 0 wird nicht akzeptiert
    @Test
    public void ungueltigeKategorieIdWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new RezeptKategorie(1, 0));
    }

    // Eine negative Kategorie-ID wird nicht akzeptiert
    @Test
    public void negativeKategorieIdWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new RezeptKategorie(1, -1));
    }

    // Eine gültige Rezept-ID kann geändert werden
    @Test
    public void gueltigeRezeptIdAendern() {
        RezeptKategorie zuordnung = new RezeptKategorie(1, 2);
        zuordnung.setRezeptId(3);
        assertEquals(3, zuordnung.getRezeptId());
    }

    // Eine gültige Kategorie-ID kann geändert werden
    @Test
    public void gueltigeKategorieIdAendern() {
        RezeptKategorie zuordnung = new RezeptKategorie(1, 2);
        zuordnung.setKategorieId(4);
        assertEquals(4, zuordnung.getKategorieId());
    }

    // Eine ungültige Rezept-ID wird auch nachträglich nicht akzeptiert
    @Test
    public void ungueltigeRezeptIdBeimAendernWirdAbgewiesen() {
        RezeptKategorie zuordnung = new RezeptKategorie(1, 2);
        assertThrows(IllegalArgumentException.class, () ->
                zuordnung.setRezeptId(0));
    }

    // Eine ungültige Kategorie-ID wird auch nachträglich nicht akzeptiert
    @Test
    public void ungueltigeKategorieIdBeimAendernWirdAbgewiesen() {
        RezeptKategorie zuordnung = new RezeptKategorie(1, 2);
        assertThrows(IllegalArgumentException.class, () ->
                zuordnung.setKategorieId(0));
    }
}
