package de.mindfeat.app.datengrundlage.entitaeten;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

public class EssensplanUnitTest {

    // Ein Essensplan mit gültigen Werten wird erstellt
    @Test
    public void gueltigeWerte_ErstellenEssensplan() {
        Essensplan essensplan = new Essensplan("Wochenplan", "2026-10-26", "2026-11-01", 1);
        assertEquals("Wochenplan", essensplan.getTitel());
        assertEquals("2026-10-26", essensplan.getStartDatum());
        assertEquals("2026-11-01", essensplan.getEndDatum());
        assertEquals(1, essensplan.getWochenstartTag());
    }

    // Prüft, ob der Titel optional ist und null akzeptiert wird
    @Test
    public void titelNull_ErlaubtLeerenTitel() {
        Essensplan essensplan = new Essensplan(null, "2026-10-26", "2026-11-01", 1);
        assertNull(essensplan.getTitel());
    }

    // Prüft, ob ein leerer Titel ebenfalls als nicht gesetzter Titel behandelt wird
    @Test
    public void titelLeer_ErlaubtLeerenTitel() {
        Essensplan essensplan = new Essensplan("", "2026-10-26", "2026-11-01", 1);
        assertNull(essensplan.getTitel());
    }

    // Leerzeichen davor/dahinter im Titel werden entfernt
    @Test
    public void titelMitLeerzeichen_EntferntLeerzeichen() {
        Essensplan essensplan = new Essensplan("  Wochenplan  ", "2026-10-26", "2026-11-01", 1);
        assertEquals("Wochenplan", essensplan.getTitel());
    }

    // Ein Startdatum im ISO-Format YYYY-MM-DD wird akzeptiert
    @Test
    public void startDatumGueltig_AkzeptiertDatum() {
        Essensplan essensplan = new Essensplan("Wochenplan", "2026-10-26", "2026-11-01", 1);
        assertEquals("2026-10-26", essensplan.getStartDatum());
    }

    // Ein ungültig formatiertes Startdatum wird nicht akzeptiert
    @Test
    public void startDatumUngueltig_WirftException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Essensplan("Wochenplan", "26.10.2026", "2026-11-01", 1));
    }

    // Ein nicht existierendes Kalenderdatum wird nicht akzeptiert
    @Test
    public void startDatumNichtExistierend_WirftException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Essensplan("Wochenplan", "2026-02-30", "2026-03-01", 1));
    }

    // Ein gültiges Enddatum wird akzeptiert
    @Test
    public void endDatumGueltig_AkzeptiertDatum() {
        Essensplan essensplan = new Essensplan("Wochenplan", "2026-10-26", "2026-11-01", 1);
        assertEquals("2026-11-01", essensplan.getEndDatum());
    }

    // Ein Enddatum vor dem Startdatum wird nicht akzeptiert
    @Test
    public void endDatumVorStartDatum_WirftException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Essensplan("Wochenplan", "2026-11-01", "2026-10-26", 1));
    }

    // Ein Essensplan mit demselben Start- und Enddatum wird akzeptiert
    @Test
    public void startDatumGleichEndDatum_ErlaubtEinzelnenTag() {
        Essensplan essensplan = new Essensplan("Tagesplan", "2026-10-26", "2026-10-26", 1);
        assertEquals(essensplan.getStartDatum(), essensplan.getEndDatum());
    }

    // Der ISO-Wochentag Montag (1) als Wochenstart wird akzeptiert
    @Test
    public void wochenstartMontag_AkzeptiertWertEins() {
        Essensplan essensplan = new Essensplan("Wochenplan", "2026-10-26", "2026-11-01", 1);
        assertEquals(1, essensplan.getWochenstartTag());
    }

    // Der ISO-Wochentag Sonntag (7) als Wochenstart wird akzeptiert
    @Test
    public void wochenstartSonntag_AkzeptiertWertSieben() {
        Essensplan essensplan = new Essensplan("Wochenplan", "2026-10-26", "2026-11-01", 7);
        assertEquals(7, essensplan.getWochenstartTag());
    }

    // Ein Wochenstart kleiner als 1 wird nicht akzeptiert
    @Test
    public void wochenstartZuKlein_WirftException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Essensplan("Wochenplan", "2026-10-26", "2026-11-01", 0));
    }

    // Ein Wochenstart größer als 7 wird nicht akzeptiert
    @Test
    public void wochenstartZuGross_WirftException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Essensplan("Wochenplan", "2026-10-26", "2026-11-01", 8));
    }

    // Ein ungültiger Datumsbereich wird auch beim Setzen des Startdatums nicht akzeptiert
    @Test
    public void startDatumNachEndDatumSetzen_WirftException() {
        Essensplan essensplan = new Essensplan("Wochenplan", "2026-10-26", "2026-11-01", 1);
        assertThrows(IllegalArgumentException.class, () ->
                essensplan.setStartDatum("2026-11-02"));
    }
}
