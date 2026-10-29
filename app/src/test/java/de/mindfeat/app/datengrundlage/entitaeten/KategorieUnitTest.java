package de.mindfeat.app.datengrundlage.entitaeten;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

public class KategorieUnitTest {

    // Eine Kategorie mit gültigem Namen wird korrekt erstellt
    @Test
    public void kategorieMitGueltigemNamenErstellen() {
        Kategorie kategorie = new Kategorie("zweites Frühstück");

        assertNotNull(kategorie);
        assertEquals("zweites Frühstück", kategorie.getName());
    }

    // Leerzeichen davor/dahinter im Namen werden entfernt
    @Test
    public void nameWirdVonLeerzeichenBereinigt() {
        Kategorie kategorie = new Kategorie("  zweites Frühstück  ");
        assertEquals("zweites Frühstück", kategorie.getName());
    }

    // Ein leerer Name wird nicht akzeptiert
    @Test
    public void leererNameWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new Kategorie(""));
    }

    // Ein Name, der nur aus Leerzeichen besteht, wird nicht akzeptiert
    @Test
    public void nameNurMitLeerzeichenWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new Kategorie("   "));
    }

    // Ein fehlender Name wird nicht akzeptiert
    @Test
    public void fehlenderNameWirdAbgewiesen() {
        assertThrows(IllegalArgumentException.class, () ->
                new Kategorie(null));
    }

    // Auch beim Ändern wird ein leerer Name nicht akzeptiert
    @Test
    public void leererNameBeimAendernWirdAbgewiesen() {
        Kategorie kategorie = new Kategorie("zweites Frühstück");
        assertThrows(IllegalArgumentException.class, () ->
                kategorie.setName(""));
    }

    // Eine gültige Namensänderung wird akzeptiert
    @Test
    public void gueltigeNamensaenderungWirdUebernommen() {
        Kategorie kategorie = new Kategorie("zweites Frühstück");
        kategorie.setName("Elf-Uhr-Imbiss");
        assertEquals("Elf-Uhr-Imbiss", kategorie.getName());
    }
}
