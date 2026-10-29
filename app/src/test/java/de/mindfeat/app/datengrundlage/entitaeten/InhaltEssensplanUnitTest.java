package de.mindfeat.app.datengrundlage.entitaeten;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class InhaltEssensplanUnitTest {

    // Eine gültige Rezeptzuordnung wird erstellt
    @Test
    public void gueltigeWerte_ErstellenInhaltEssensplan() {
        InhaltEssensplan inhalt = new InhaltEssensplan(1, 2, "2026-10-26");
        assertEquals(1, inhalt.getEssensplanId());
        assertEquals(2, inhalt.getRezeptId());
        assertEquals("2026-10-26", inhalt.getDatum());
    }

    // Eine Essensplan-ID kleiner als 1 wird nicht akzeptiert
    @Test
    public void essensplanIdUngueltig_WirftException() {
        assertThrows(IllegalArgumentException.class, () ->
                new InhaltEssensplan(0, 2, "2026-10-26"));
    }

    // Eine negative Essensplan-ID wird nicht akzeptiert
    @Test
    public void essensplanIdNegativ_WirftException() {
        assertThrows(IllegalArgumentException.class, () ->
                new InhaltEssensplan(-1, 2, "2026-10-26"));
    }

    // Eine Rezept-ID kleiner als 1 wird nicht akzeptiert
    @Test
    public void rezeptIdUngueltig_WirftException() {
        assertThrows(IllegalArgumentException.class, () ->
                new InhaltEssensplan(1, 0, "2026-10-26"));
    }

    // Eine negative Rezept-ID wird nicht akzeptiert
    @Test
    public void rezeptIdNegativ_WirftException() {
        assertThrows(IllegalArgumentException.class, () ->
                new InhaltEssensplan(1, -1, "2026-10-26"));
    }

    // Ein gültiges Datum im ISO-Format YYYY-MM-DD wird akzeptiert
    @Test
    public void datumGueltig_AkzeptiertDatum() {
        InhaltEssensplan inhalt = new InhaltEssensplan(1, 2, "2026-10-26");
        assertEquals("2026-10-26", inhalt.getDatum());
    }

    // Ein ungültig formatiertes Datum wird nicht akzeptiert
    @Test
    public void datumUngueltigFormatiert_WirftException() {
        assertThrows(IllegalArgumentException.class, () ->
                new InhaltEssensplan(1, 2, "26.10.2026"));
    }

    // Ein nicht existierendes Kalenderdatum wird nicht akzeptiert
    @Test
    public void datumNichtExistierend_WirftException() {
        assertThrows(IllegalArgumentException.class, () ->
                new InhaltEssensplan(1, 2, "2026-02-30"));
    }

    // Ein fehlendes Datum wird nicht akzeptiert
    @Test
    public void datumNull_WirftException() {
        assertThrows(IllegalArgumentException.class, () ->
                new InhaltEssensplan(1, 2, null));
    }

    // Ein leeres Datum wird nicht akzeptiert
    @Test
    public void datumLeer_WirftException() {
        assertThrows(IllegalArgumentException.class, () ->
                new InhaltEssensplan(1, 2, ""));
    }

    // Ein gültiges Datum kann nachträglich gesetzt werden
    @Test
    public void datumSetzen_AktualisiertDatum() {
        InhaltEssensplan inhalt = new InhaltEssensplan(1, 2, "2026-10-26");
        inhalt.setDatum("2026-10-27");
        assertEquals("2026-10-27", inhalt.getDatum());
    }

    // Ein ungültiges Datum wird auch beim nachträglichen Setzen nicht akzeptiert
    @Test
    public void datumSetzenUngueltig_WirftException() {
        InhaltEssensplan inhalt = new InhaltEssensplan(1, 2, "2026-10-26");
        assertThrows(IllegalArgumentException.class, () ->
                inhalt.setDatum("31.10.2026"));
    }

    // Eine gültige Essensplan-ID kann nachträglich gesetzt werden
    @Test
    public void essensplanIdSetzen_AktualisiertId() {
        InhaltEssensplan inhalt = new InhaltEssensplan(1, 2, "2026-10-26");
        inhalt.setEssensplanId(3);
        assertEquals(3, inhalt.getEssensplanId());
    }

    // Eine gültige Rezept-ID kann nachträglich gesetzt werden
    @Test
    public void rezeptIdSetzen_AktualisiertId() {
        InhaltEssensplan inhalt = new InhaltEssensplan(1, 2, "2026-10-26");
        inhalt.setRezeptId(4);
        assertEquals(4, inhalt.getRezeptId());
    }

    // Eine negative Eintrags-ID wird nicht akzeptiert
    @Test
    public void idNegativ_WirftException() {
        InhaltEssensplan inhalt = new InhaltEssensplan(1, 2, "2026-10-26");
        assertThrows(IllegalArgumentException.class, () ->
                inhalt.setId(-1));
    }
}
