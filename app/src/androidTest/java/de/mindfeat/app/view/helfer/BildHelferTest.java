package de.mindfeat.app.view.helfer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNotSame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.exifinterface.media.ExifInterface;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class BildHelferTest {

    private File testVerzeichnis;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        testVerzeichnis = new File(context.getCacheDir(), "bild_helfer_tests");
        testVerzeichnis.mkdirs();
    }

    @After
    public void tearDown() {
        if (testVerzeichnis != null && testVerzeichnis.exists()) {
            File[] dateien = testVerzeichnis.listFiles();
            if (dateien != null) {
                for (File datei : dateien) {
                    datei.delete();
                }
            }
            testVerzeichnis.delete();
        }
    }

    // Ein JPEG-Testbild mit der gewünschten EXIF-Orientierung erstellen
    private File testBildErstellen(int breite, int hoehe, int orientierung) throws IOException {
        File datei = new File(testVerzeichnis, "testbild_" + breite + "x" + hoehe + "_" + orientierung + ".jpg");
        Bitmap bitmap = Bitmap.createBitmap(breite, hoehe, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.RED);
        try (FileOutputStream ausgabe = new FileOutputStream(datei)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ausgabe);
        } finally {
            bitmap.recycle();
        }
        ExifInterface exif = new ExifInterface(datei.getAbsolutePath());
        exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(orientierung));
        exif.saveAttributes();
        return datei;
    }

    // Ein Bitmap ohne EXIF-Korrektur direkt erstellen
    private Bitmap bitmapErstellen() {
        Bitmap bitmap = Bitmap.createBitmap(40, 20, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.RED);
        return bitmap;
    }

    // Bei einem null-Bitmap wird null zurückgegeben
    @Test
    public void orientierenMitNullBitmapGibtNullZurueck() {
        assertNull(BildHelfer.orientieren(null, "ungueltig.jpg"));
    }

    // Bei einem leeren Bildpfad bleibt das Original-Bitmap erhalten
    @Test
    public void orientierenMitLeeremPfadGibtOriginalZurueck() {
        Bitmap bitmap = bitmapErstellen();
        Bitmap ergebnis = BildHelfer.orientieren(bitmap, "   ");
        assertSame(bitmap, ergebnis);
        bitmap.recycle();
    }

    // Bei normaler EXIF-Orientierung bleibt das Original-Bitmap erhalten
    @Test
    public void orientierenBeiNormalerOrientierung() throws IOException {
        File datei = testBildErstellen(40, 20, ExifInterface.ORIENTATION_NORMAL);
        Bitmap bitmap = bitmapErstellen();
        Bitmap ergebnis = BildHelfer.orientieren(bitmap, datei.getAbsolutePath());
        assertSame(bitmap, ergebnis);
        bitmap.recycle();
    }

    // Eine EXIF-Drehung um 90 Grad vertauscht Breite und Höhe
    @Test
    public void orientierenDrehtUm90Grad() throws IOException {
        File datei = testBildErstellen(40, 20, ExifInterface.ORIENTATION_ROTATE_90);
        Bitmap bitmap = bitmapErstellen();
        Bitmap ergebnis = BildHelfer.orientieren(bitmap, datei.getAbsolutePath());
        assertNotNull(ergebnis);
        assertNotSame(bitmap, ergebnis);
        assertEquals(20, ergebnis.getWidth());
        assertEquals(40, ergebnis.getHeight());
        ergebnis.recycle();
    }

    // Eine EXIF-Drehung um 180 Grad behält die Abmessungen bei
    @Test
    public void orientierenDrehtUm180Grad() throws IOException {
        File datei = testBildErstellen(40, 20, ExifInterface.ORIENTATION_ROTATE_180);
        Bitmap bitmap = bitmapErstellen();
        Bitmap ergebnis = BildHelfer.orientieren(bitmap, datei.getAbsolutePath());
        assertNotNull(ergebnis);
        assertEquals(40, ergebnis.getWidth());
        assertEquals(20, ergebnis.getHeight());
        ergebnis.recycle();
    }

    // Eine EXIF-Drehung um 270 Grad vertauscht Breite und Höhe
    @Test
    public void orientierenDrehtUm270Grad() throws IOException {
        File datei = testBildErstellen(40, 20, ExifInterface.ORIENTATION_ROTATE_270);
        Bitmap bitmap = bitmapErstellen();
        Bitmap ergebnis = BildHelfer.orientieren(bitmap, datei.getAbsolutePath());
        assertNotNull(ergebnis);
        assertEquals(20, ergebnis.getWidth());
        assertEquals(40, ergebnis.getHeight());
        ergebnis.recycle();
    }

    // Eine horizontale Spiegelung behält die Abmessungen bei
    @Test
    public void orientierenSpiegeltHorizontal() throws IOException {
        File datei = testBildErstellen(40, 20, ExifInterface.ORIENTATION_FLIP_HORIZONTAL);
        Bitmap bitmap = bitmapErstellen();
        Bitmap ergebnis = BildHelfer.orientieren(bitmap, datei.getAbsolutePath());
        assertNotNull(ergebnis);
        assertEquals(40, ergebnis.getWidth());
        assertEquals(20, ergebnis.getHeight());
        ergebnis.recycle();
    }

    // Eine vertikale Spiegelung behält die Abmessungen bei
    @Test
    public void orientierenSpiegeltVertikal() throws IOException {
        File datei = testBildErstellen(40, 20, ExifInterface.ORIENTATION_FLIP_VERTICAL);
        Bitmap bitmap = bitmapErstellen();
        Bitmap ergebnis = BildHelfer.orientieren(bitmap, datei.getAbsolutePath());
        assertNotNull(ergebnis);
        assertEquals(40, ergebnis.getWidth());
        assertEquals(20, ergebnis.getHeight());
        ergebnis.recycle();
    }

    // Bei einer nicht vorhandenen Bilddatei bleibt das Original erhalten
    @Test
    public void orientierenMitNichtVorhandenerDateiGibtOriginalZurueck() {
        Bitmap bitmap = bitmapErstellen();
        File datei = new File(testVerzeichnis, "nicht_vorhanden.jpg");
        Bitmap ergebnis = BildHelfer.orientieren(bitmap, datei.getAbsolutePath());
        assertSame(bitmap, ergebnis);
        bitmap.recycle();
    }

    // Ein Bild wird mit reduzierter Auflösung geladen
    @Test
    public void skaliertLadenLaedtVerkleinertesBild() throws IOException {
        File datei = testBildErstellen(400, 200, ExifInterface.ORIENTATION_NORMAL);
        Bitmap ergebnis = BildHelfer.skaliertLaden(datei.getAbsolutePath(), 100, 50);
        assertNotNull(ergebnis);
        assertEquals(100, ergebnis.getWidth());
        assertEquals(50, ergebnis.getHeight());
        ergebnis.recycle();
    }

    // Ein null-Bildpfad liefert null
    @Test
    public void skaliertLadenMitNullPfadGibtNullZurueck() {
        assertNull(BildHelfer.skaliertLaden(null, 100, 100));
    }

    // Ein leerer Bildpfad liefert null
    @Test
    public void skaliertLadenMitLeeremPfadGibtNullZurueck() {
        assertNull(BildHelfer.skaliertLaden("   ", 100, 100));
    }

    // Eine nicht vorhandene Bilddatei liefert null
    @Test
    public void skaliertLadenMitNichtVorhandenerDateiGibtNullZurueck() {
        File datei = new File(testVerzeichnis, "nicht_vorhanden.jpg");
        assertNull(BildHelfer.skaliertLaden(datei.getAbsolutePath(), 100, 100));
    }

    // Eine Zielbreite von null ist ungültig
    @Test
    public void skaliertLadenMitZielbreiteNullGibtNullZurueck() throws IOException {
        File datei = testBildErstellen(400, 200, ExifInterface.ORIENTATION_NORMAL);
        assertNull(BildHelfer.skaliertLaden(datei.getAbsolutePath(), 0, 100));
    }

    // Eine negative Zielhöhe ist ungültig
    @Test
    public void skaliertLadenMitNegativerZielhoeheGibtNullZurueck() throws IOException {
        File datei = testBildErstellen(400, 200, ExifInterface.ORIENTATION_NORMAL);
        assertNull(BildHelfer.skaliertLaden(datei.getAbsolutePath(), 100, -1));
    }

    // Eine beschädigte Bilddatei liefert null
    @Test
    public void skaliertLadenMitUngueltigerBilddateiGibtNullZurueck() throws IOException {
        File datei = new File(testVerzeichnis, "ungueltig.jpg");
        try (FileOutputStream ausgabe = new FileOutputStream(datei)) {
            ausgabe.write("Kein gültiges Bild".getBytes());
        }
        assertNull(BildHelfer.skaliertLaden(datei.getAbsolutePath(), 100, 100));
    }

    // EXIF-Transpose dreht und spiegelt das Bild
    @Test
    public void orientierenBeiTranspose() throws IOException {
        File datei = testBildErstellen(40, 20, ExifInterface.ORIENTATION_TRANSPOSE);
        Bitmap bitmap = bitmapErstellen();
        Bitmap ergebnis = BildHelfer.orientieren(bitmap, datei.getAbsolutePath());
        assertNotNull(ergebnis);
        assertEquals(20, ergebnis.getWidth());
        assertEquals(40, ergebnis.getHeight());
        ergebnis.recycle();
    }

    // EXIF-Transverse dreht und spiegelt das Bild
    @Test
    public void orientierenBeiTransverse() throws IOException {
        File datei = testBildErstellen(40, 20, ExifInterface.ORIENTATION_TRANSVERSE);
        Bitmap bitmap = bitmapErstellen();
        Bitmap ergebnis = BildHelfer.orientieren(bitmap, datei.getAbsolutePath());
        assertNotNull(ergebnis);
        assertEquals(20, ergebnis.getWidth());
        assertEquals(40, ergebnis.getHeight());
        ergebnis.recycle();
    }

    // Bei einer nicht unterstützten EXIF-Orientierung bleibt das Original erhalten
    @Test
    public void orientierenBeiUnbekannterOrientierungGibtOriginalZurueck() throws IOException {
        File datei = testBildErstellen(40, 20, 99);
        Bitmap bitmap = bitmapErstellen();
        Bitmap ergebnis = BildHelfer.orientieren(bitmap, datei.getAbsolutePath());
        assertSame(bitmap, ergebnis);
        bitmap.recycle();
    }
}
