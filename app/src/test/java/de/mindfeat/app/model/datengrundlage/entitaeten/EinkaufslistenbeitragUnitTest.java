package de.mindfeat.app.model.datengrundlage.entitaeten;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;


public class EinkaufslistenbeitragUnitTest {

    // Einen Einkaufslistenbeitrag mit Essensplan erstellen
    @Test
    public void beitragErstellen() {
        Einkaufslistenbeitrag beitrag = new Einkaufslistenbeitrag(1, 2, 500.0);
        assertEquals(1, beitrag.getEinkaufslistenelementId());
        assertEquals(Integer.valueOf(2), beitrag.getEssensplanId());
        assertEquals(500.0, beitrag.getMenge(), 0.001);
    }

    // Einen manuellen Beitrag ohne Essensplan erstellen
    @Test
    public void manuellenBeitragErstellen() {
        Einkaufslistenbeitrag beitrag = new Einkaufslistenbeitrag(1, null, 250.0);
        assertNull(beitrag.getEssensplanId());
        assertEquals(250.0, beitrag.getMenge(), 0.001);
    }

    // Die ID eines Beitrags setzen
    @Test
    public void idSetzen() {
        Einkaufslistenbeitrag beitrag = new Einkaufslistenbeitrag(1, 2, 500.0);
        beitrag.setId(5);
        assertEquals(5, beitrag.getId());
    }

    // Eine negative ID darf nicht gesetzt werden
    @Test
    public void negativeIdVerhindern() {
        Einkaufslistenbeitrag beitrag = new Einkaufslistenbeitrag(1, 2, 500.0);
        assertThrows(IllegalArgumentException.class, () -> beitrag.setId(-1));
    }

    // Eine ungültige Einkaufslistenelement-ID darf nicht gesetzt werden
    @Test
    public void ungueltigeEinkaufslistenelementIdVerhindern() {
        assertThrows(IllegalArgumentException.class, () -> new Einkaufslistenbeitrag(0, 2, 500.0));
    }

    // Eine ungültige Essensplan-ID darf nicht gesetzt werden
    @Test
    public void ungueltigeEssensplanIdVerhindern() {
        assertThrows(IllegalArgumentException.class, () -> new Einkaufslistenbeitrag(1, 0, 500.0));
    }

    // Die Essensplan-ID darf auf null gesetzt werden
    @Test
    public void essensplanIdAufNullSetzen() {
        Einkaufslistenbeitrag beitrag = new Einkaufslistenbeitrag(1, 2, 500.0);
        beitrag.setEssensplanId(null);
        assertNull(beitrag.getEssensplanId());
    }

    // Eine Menge darf null sein
    @Test
    public void mengeAufNullSetzen() {
        Einkaufslistenbeitrag beitrag = new Einkaufslistenbeitrag(1, null, null);
        assertNull(beitrag.getMenge());
    }

    // Eine Menge muss positiv sein
    @Test
    public void ungueltigeMengeVerhindern() {
        assertThrows(IllegalArgumentException.class, () -> new Einkaufslistenbeitrag(1, 2, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new Einkaufslistenbeitrag(1, 2, -5.0));
    }

    // Unendliche und nicht endliche Mengen verhindern
    @Test
    public void nichtEndlicheMengeVerhindern() {
        assertThrows(IllegalArgumentException.class, () -> new Einkaufslistenbeitrag(1, 2, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new Einkaufslistenbeitrag(1, 2, Double.NaN));
    }

    // Die Menge eines vorhandenen Beitrags aktualisieren
    @Test
    public void mengeAktualisieren() {
        Einkaufslistenbeitrag beitrag = new Einkaufslistenbeitrag(1, 2, 500.0);
        beitrag.setMenge(750.0);
        assertEquals(750.0, beitrag.getMenge(), 0.001);
    }
}
