package de.mindfeat.app.datengrundlage.entitaeten;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

public class ZutatUnitTest {

    // Eine Zutat mit gültigem Namen wird korrekt erstellt
    @Test
    public void zutatMitGueltigemNamenErstellen() {
        Zutat zutat = new Zutat("Kartoffeln");
        assertNotNull(zutat);
        assertEquals("Kartoffeln", zutat.getName());
    }

    // Leerzeichen davor/dahinter im Namen werden entfernt
    @Test
    public void nameWirdVonLeerzeichenBereinigt() {
        Zutat zutat = new Zutat("  Kartoffeln  ");
        assertEquals("Kartoffeln", zutat.getName());
    }

    // Ein leerer Name wird nicht akzeptiert
    @Test
    public void leererNameWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new Zutat(""));
    }

    // Ein Name, der nur aus Leerzeichen besteht, wird nicht akzeptiert
    @Test
    public void nameNurMitLeerzeichenWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new Zutat("   "));
    }

    // Ein fehlender Name wird nicht akzeptiert
    @Test
    public void fehlenderNameWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new Zutat(null));
    }

    // Auch beim nachträglichen Ändern wird ein leerer Name nicht akzeptiert
    @Test
    public void leererNameBeimAendernWirdAbgewiesen() {
        Zutat zutat = new Zutat("Kartoffeln");
        assertThrows(IllegalArgumentException.class, () ->
                zutat.setName(""));
    }

    // Ein gültiger neuer Name wird akzeptiert
    @Test
    public void gueltigeNamensaenderungWirdUebernommen() {
        Zutat zutat = new Zutat("Kartoffeln");
        zutat.setName("Süßkartoffeln");
        assertEquals("Süßkartoffeln", zutat.getName());
    }
}
