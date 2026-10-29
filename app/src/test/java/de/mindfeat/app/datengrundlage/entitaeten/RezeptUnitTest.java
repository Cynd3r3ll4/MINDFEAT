package de.mindfeat.app.datengrundlage.entitaeten;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

public class RezeptUnitTest {

    // Ein Rezept mit gültigen Werten wird korrekt erstellt
    @Test
    public void rezeptMitGueltigenWertenErstellen() {
        Rezept rezept = new Rezept("Graupeneintopf", null, 4);
        assertNotNull(rezept);
        assertEquals("Graupeneintopf", rezept.getTitel());
        assertNull(rezept.getBildPfad());
        assertEquals(4, rezept.getPortionen());
    }

    // Leerzeichen davor/dahinter im Titel werden entfernt
    @Test
    public void titelWirdVonLeerzeichenBereinigt() {
        Rezept rezept = new Rezept("  Graupeneintopf  ", null, 2);
        assertEquals("Graupeneintopf", rezept.getTitel());
    }

    // Ein leerer Titel wird nicht akzeptiert
    @Test
    public void leererTitelWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new Rezept("", null, 2));
    }

    // Ein Titel, der nur aus Leerzeichen besteht, wird nicht akzeptiert
    @Test
    public void titelNurMitLeerzeichenWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new Rezept("   ", null, 2));
    }

    // Ein fehlender Titel wird nicht akzeptiert
    @Test
    public void fehlenderTitelWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new Rezept(null, null, 2));
    }

    // Ein Rezept mit null Portionen wird nicht akzeptiert
    @Test
    public void nullPortionenWerdenAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new Rezept("Graupeneintopf", null, 0));
    }

    // Negative Portionszahlen werden nicht akzeptiert
    @Test
    public void negativePortionenWerdenAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new Rezept("Graupeneintopf", null, -2));
    }

    // Auch beim nachträglichen Ändern werden ungültige Portionen nicht akzeptiert
    @Test
    public void ungueltigePortionsaenderungWirdAbgewiesen() {
        Rezept rezept = new Rezept("Graupeneintopf", null, 2);
        assertThrows(IllegalArgumentException.class, () ->
                rezept.setPortionen(0));
    }

    // Eine gültige Portionsänderung wird akzeptiert
    @Test
    public void gueltigePortionsaenderungWirdUebernommen() {
        Rezept rezept = new Rezept("Graupeneintopf", null, 2);
        rezept.setPortionen(6);
        assertEquals(6, rezept.getPortionen());
    }
}
