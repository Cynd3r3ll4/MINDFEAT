package de.mindfeat.app.datengrundlage.entitaeten;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class EinkaufslistenelementUnitTest {

    // Ein Einkaufslistenelement mit gültigen Werten wird erstellt
    @Test
    public void gueltigeWerte_ErstellenEinkaufslistenelement() {
        Einkaufslistenelement element = new Einkaufslistenelement(1, 500.0, "g", false);
        assertEquals(1, element.getZutatId());
        assertEquals(Double.valueOf(500.0), element.getMenge());
        assertEquals("g", element.getEinheit());
        assertFalse(element.isErledigt());
    }

    // Eine Zutaten-ID kleiner als 1 wird nicht akzeptiert
    @Test
    public void zutatIdUngueltig_WirftException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Einkaufslistenelement(0, 500.0, "g", false));
    }

    // Eine negative Zutaten-ID wird nicht akzeptiert
    @Test
    public void zutatIdNegativ_WirftException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Einkaufslistenelement(-1, 500.0, "g", false));
    }

    // Eine positive Menge wird akzeptiert
    @Test
    public void mengeGueltig_AkzeptiertMenge() {
        Einkaufslistenelement element =
                new Einkaufslistenelement(1, 2.0, "Stück", false);
        assertEquals(Double.valueOf(2.0), element.getMenge());
    }

    // Eine fehlende Mengenangabe wird akzeptiert
    @Test
    public void mengeNull_ErlaubtKeineMengenangabe() {
        Einkaufslistenelement element =
                new Einkaufslistenelement(1, null, null, false);
        assertNull(element.getMenge());
    }

    // Eine Menge von 0 wird nicht akzeptiert
    @Test
    public void mengeNullAlsZahlenwert_WirftException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Einkaufslistenelement(1, 0.0, "g", false));
    }

    // Eine negative Menge wird nicht akzeptiert
    @Test
    public void mengeNegativ_WirftException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Einkaufslistenelement(1, -2.0, "g", false));
    }

    // Eine unendliche Menge wird nicht akzeptiert
    @Test
    public void mengeUnendlich_WirftException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Einkaufslistenelement(
                        1, Double.POSITIVE_INFINITY, "g", false));
    }

    // Eine Einheit mit Leerzeichen davor und dahinter wird bereinigt
    @Test
    public void einheitMitLeerzeichen_EntferntLeerzeichen() {
        Einkaufslistenelement element =
                new Einkaufslistenelement(1, 500.0, "  g  ", false);
        assertEquals("g", element.getEinheit());
    }

    // Eine leere Einheit wird als nicht gesetzte Einheit behandelt
    @Test
    public void einheitLeer_ErlaubtKeineEinheit() {
        Einkaufslistenelement element =
                new Einkaufslistenelement(1, null, "", false);
        assertNull(element.getEinheit());
    }

    // Eine fehlende Einheit wird akzeptiert
    @Test
    public void einheitNull_ErlaubtKeineEinheit() {
        Einkaufslistenelement element =
                new Einkaufslistenelement(1, 2.0, null, false);
        assertNull(element.getEinheit());
    }

    // Eine negative ID wird nicht akzeptiert
    @Test
    public void idNegativ_WirftException() {
        Einkaufslistenelement element =
                new Einkaufslistenelement(1, 500.0, "g", false);
        assertThrows(IllegalArgumentException.class, () ->
                element.setId(-1));
    }

    // Eine gültige Menge kann nachträglich geändert werden
    @Test
    public void mengeSetzen_AktualisiertMenge() {
        Einkaufslistenelement element =
                new Einkaufslistenelement(1, 500.0, "g", false);
        element.setMenge(250.0);
        assertEquals(Double.valueOf(250.0), element.getMenge());
    }

    // Eine ungültige Menge wird auch beim nachträglichen Setzen nicht akzeptiert
    @Test
    public void mengeSetzenUngueltig_WirftException() {
        Einkaufslistenelement element =
                new Einkaufslistenelement(1, 500.0, "g", false);
        assertThrows(IllegalArgumentException.class, () ->
                element.setMenge(-1.0));
    }

    // Der Erledigt-Status kann nachträglich geändert werden
    @Test
    public void erledigtSetzen_AktualisiertStatus() {
        Einkaufslistenelement element =
                new Einkaufslistenelement(1, 500.0, "g", false);
        element.setErledigt(true);
        assertTrue(element.isErledigt());
    }
}
