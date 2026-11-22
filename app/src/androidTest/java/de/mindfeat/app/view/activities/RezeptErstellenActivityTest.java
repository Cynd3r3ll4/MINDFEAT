package de.mindfeat.app.view.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import de.mindfeat.app.R;
import de.mindfeat.app.model.datengrundlage.TestDatenbank;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;
import de.mindfeat.app.model.vermittler.RezeptVermittler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class RezeptErstellenActivityTest {

    private ActivityScenario<RezeptErstellenActivity> scenario;
    private TestDatenbank testDatenbank;

    @Before
    public void setUp() {
        testDatenbank = new TestDatenbank();
        scenario = ActivityScenario.launch(RezeptErstellenActivity.class);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
        if (testDatenbank != null) {
            testDatenbank.schliessen();
        }
    }

    // Die Activity vollständig starten
    @Test
    public void activityWirdKorrektGestartet() {
        scenario.onActivity(activity -> {
            assertNotNull(activity.findViewById(R.id.etRezeptTitel));
            assertNotNull(activity.findViewById(R.id.etRezeptKategorien));
            assertNotNull(activity.findViewById(R.id.etRezeptPortionen));
            assertNotNull(activity.findViewById(R.id.btnRezeptSpeichern));
        });
    }

    // Die Bildvorschau ist zunächst nicht sichtbar
    @Test
    public void bildvorschauIstZuBeginnAusgeblendet() {
        scenario.onActivity(activity -> {
            View vorschau = activity.findViewById(R.id.cardRezeptVorschau);
            assertEquals(View.GONE, vorschau.getVisibility());
        });
    }

    // Zu Beginn nur eine Zutatenzeile anzeigen
    @Test
    public void eineZutatenzeileWirdZuBeginnAngelegt() {
        scenario.onActivity(activity -> {
            LinearLayout layout = activity.findViewById(R.id.layoutZutaten);
            assertEquals(1, layout.getChildCount());
        });
    }

    // Zu Beginn nur eine Arbeitsschrittzeile anzeigen
    @Test
    public void einArbeitsschrittWirdZuBeginnAngelegt() {
        scenario.onActivity(activity -> {
            LinearLayout layout = activity.findViewById(R.id.layoutArbeitsschritte);
            assertEquals(1, layout.getChildCount());
        });
    }

    // Weitere Zutatenzeilen hinzufügen
    @Test
    public void zutatHinzufuegenErzeugtWeitereEingabezeile() {
        scenario.onActivity(activity -> {
            LinearLayout layout = activity.findViewById(R.id.layoutZutaten);
            AppCompatImageButton button = activity.findViewById(R.id.btnZutatHinzufuegen);
            button.performClick();
            assertEquals(2, layout.getChildCount());
        });
    }

    // Weitere Arbeitsschrittzeilen hinzufügen
    @Test
    public void arbeitsschrittHinzufuegenErzeugtWeiteresEingabefeld() {
        scenario.onActivity(activity -> {
            LinearLayout layout = activity.findViewById(R.id.layoutArbeitsschritte);
            AppCompatImageButton button = activity.findViewById(R.id.btnArbeitsschrittHinzufuegen);
            button.performClick();
            assertEquals(2, layout.getChildCount());
        });
    }

    // Speichern ohne Titeleingabe ist nicht möglich
    @Test
    public void speichernOhneTitelZeigtValidierungsfehler() {
        scenario.onActivity(activity -> {
            EditText titel = activity.findViewById(R.id.etRezeptTitel);
            Button speichern = activity.findViewById(R.id.btnRezeptSpeichern);
            titel.setText("");
            speichern.performClick();
            assertNotNull(titel.getError());
        });
    }

    // Speichern ohne Portionsangabe ist nicht möglich
    @Test
    public void speichernMitNullPortionenZeigtValidierungsfehler() {
        scenario.onActivity(activity -> {
            EditText titel = activity.findViewById(R.id.etRezeptTitel);
            EditText portionen = activity.findViewById(R.id.etRezeptPortionen);
            Button speichern = activity.findViewById(R.id.btnRezeptSpeichern);
            titel.setText("Testrezept");
            portionen.setText("0");
            speichern.performClick();
            assertNotNull(portionen.getError());
        });
    }

    // Speichern mit ungültiger Portionsangabe ist nicht möglich
    @Test
    public void speichernMitUngueltigerPortionenzahlZeigtValidierungsfehler() {
        scenario.onActivity(activity -> {
            EditText titel = activity.findViewById(R.id.etRezeptTitel);
            EditText portionen = activity.findViewById(R.id.etRezeptPortionen);
            Button speichern = activity.findViewById(R.id.btnRezeptSpeichern);

            titel.setText("Testrezept");
            portionen.setText("abc");

            speichern.performClick();

            assertNotNull(portionen.getError());
        });
    }

    // Speichern mit gültigem Titel und Portionsangabe (unabhängig von der Zutatenvalidierung)
    @Test
    public void speichernBeiGueltigemTitelUndPortionenBleibtZunaechstMoeglich() {
        scenario.onActivity(activity -> {
            EditText titel = activity.findViewById(R.id.etRezeptTitel);
            EditText portionen = activity.findViewById(R.id.etRezeptPortionen);
            Button speichern = activity.findViewById(R.id.btnRezeptSpeichern);
            titel.setText("Testrezept");
            portionen.setText("2");
            speichern.performClick();
            assertNull(titel.getError());
            assertNull(portionen.getError());
        });
    }

    // Hilfsmethode: Ein Eingabefeld aus einer dynamisch erzeugten Zutatenzeile abrufen
    private EditText zutatenEingabefeld(LinearLayout zeile, int position) {
        ViewGroup container = (ViewGroup) zeile.getChildAt(position);
        return (EditText) container.getChildAt(0);
    }

    // Prüft, ob ein vollständig ausgefülltes Rezept erfolgreich gespeichert wird
    @Test
    public void vollstaendigesRezeptWirdErfolgreichGespeichert() {
        scenario.onActivity(activity -> {
            EditText titel = activity.findViewById(R.id.etRezeptTitel);
            EditText portionen = activity.findViewById(R.id.etRezeptPortionen);
            titel.setText("Gemuesesuppe");
            portionen.setText("2");
            LinearLayout layoutZutaten = activity.findViewById(R.id.layoutZutaten);
            LinearLayout zutatenZeile = (LinearLayout) layoutZutaten.getChildAt(0);
            EditText menge = zutatenEingabefeld(zutatenZeile, 0);
            EditText einheit = zutatenEingabefeld(zutatenZeile, 1);
            EditText zutat = zutatenEingabefeld(zutatenZeile, 2);
            menge.setText("2");
            einheit.setText("EL");
            zutat.setText("Karotten");
            Button speichern = activity.findViewById(R.id.btnRezeptSpeichern);
            speichern.performClick();
        });
        long zeitlimit = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < zeitlimit && scenario.getState() != Lifecycle.State.DESTROYED) {
            SystemClock.sleep(50);
        }
        assertEquals("Die Activity wurde nach dem Speichern nicht beendet.", Lifecycle.State.DESTROYED, scenario.getState());
        List<Rezept> gespeicherteRezepte = testDatenbank.getDatenbank().rezeptDAO().alleAbrufen();
        assertEquals(1, gespeicherteRezepte.size());
        assertEquals("Gemuesesuppe", gespeicherteRezepte.get(0).getTitel());
        assertEquals(2, gespeicherteRezepte.get(0).getPortionen());
    }

    // Hilfsmethode: Legt ein Rezept mit Zutaten, Kategorie und Arbeitsschritt für Bearbeitungstests an
    private int testRezeptAnlegen(String bildPfad) {
        Rezept rezept = new Rezept("Testrezept", bildPfad, 2);
        RezeptVermittler vermittler = new RezeptVermittler(testDatenbank.getDatenbank());
        return vermittler.anlegen(rezept,
                Collections.singletonList(new RezeptVermittler.ZutatAngabe("Kartoffeln", 500, "g")),
                Collections.singletonList("Hauptgericht"),
                Collections.singletonList("Kartoffeln kochen."));
    }

    // Hilfsmethode: Startet das Formular im Bearbeitungsmodus
    private void bearbeitungsmodusStarten(int rezeptId) {
        scenario.close();
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), RezeptErstellenActivity.class);
        intent.putExtra(RezeptDetailActivity.EXTRA_REZEPT_ID, rezeptId);
        scenario = ActivityScenario.launch(intent);
        long zeitlimit = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < zeitlimit) {
            AtomicBoolean geladen = new AtomicBoolean(false);
            scenario.onActivity(activity -> {
                EditText titel = activity.findViewById(R.id.etRezeptTitel);
                geladen.set("Testrezept".contentEquals(titel.getText()));
            });
            if (geladen.get()) {
                return;
            }
            SystemClock.sleep(50);
        }
        throw new AssertionError("Die vorhandenen Rezeptdaten wurden nicht geladen.");
    }

    // Hilfsmethode: Erstellt eine gültige JPEG-Datei im Rezeptbildordner
    private File testBildErstellen(String dateiname) throws Exception {
        File ordner = new File(ApplicationProvider.getApplicationContext().getFilesDir(), "rezept_bilder");
        if (!ordner.exists() && !ordner.mkdirs()) {
            throw new AssertionError("Bildordner konnte nicht erstellt werden.");
        }
        File bild = new File(ordner, dateiname);
        Bitmap bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
        try (FileOutputStream ausgabe = new FileOutputStream(bild)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ausgabe);
        } finally {
            bitmap.recycle();
        }
        return bild;
    }

    // Hilfsmethode: Wartet darauf, dass die Activity nach dem Speichern beendet wird
    private void wartenBisActivityBeendet() {
        long zeitlimit = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < zeitlimit) {
            if (scenario.getState() == Lifecycle.State.DESTROYED) {
                return;
            }
            SystemClock.sleep(50);
        }
        throw new AssertionError("Die Activity wurde nach dem Speichern nicht beendet.");
    }

    // Ein vorhandenes Rezept wird mit seinen gespeicherten Angaben geladen
    @Test
    public void vorhandenesRezeptWirdImBearbeitungsmodusGeladen() {
        int rezeptId = testRezeptAnlegen(null);
        bearbeitungsmodusStarten(rezeptId);
        scenario.onActivity(activity -> {
            assertEquals(activity.getString(R.string.rezept_bearbeiten), ((TextView) activity.findViewById(R.id.tvRezeptFormularTitel)).getText().toString());
            assertEquals("Testrezept", ((EditText) activity.findViewById(R.id.etRezeptTitel)).getText().toString());
            assertEquals("2", ((EditText) activity.findViewById(R.id.etRezeptPortionen)).getText().toString());
            assertEquals("Hauptgericht", ((EditText) activity.findViewById(R.id.etRezeptKategorien)).getText().toString());
            LinearLayout zutaten = activity.findViewById(R.id.layoutZutaten);
            assertEquals(1, zutaten.getChildCount());
            EditText zutatenname = zutatenEingabefeld((LinearLayout) zutaten.getChildAt(0), 2);
            assertEquals("Kartoffeln", zutatenname.getText().toString());
            LinearLayout schritte = activity.findViewById(R.id.layoutArbeitsschritte);
            assertEquals(1, schritte.getChildCount());
            ViewGroup schrittContainer = (ViewGroup) schritte.getChildAt(0);
            EditText schritt = (EditText) schrittContainer.getChildAt(0);
            assertEquals("Kartoffeln kochen.", schritt.getText().toString());
        });
    }

    // Änderungen an einem vorhandenen Rezept werden gespeichert
    @Test
    public void rezeptbearbeitungSpeichertAenderungen() {
        int rezeptId = testRezeptAnlegen(null);
        bearbeitungsmodusStarten(rezeptId);
        scenario.onActivity(activity -> {
            EditText titel = activity.findViewById(R.id.etRezeptTitel);
            titel.setText("Cremige Kartoffelsuppe");
            Button speichern = activity.findViewById(R.id.btnRezeptSpeichern);
            speichern.performClick();
        });
        wartenBisActivityBeendet();
        Rezept gespeichertesRezept = testDatenbank.getDatenbank().rezeptDAO().nachIdAbrufen(rezeptId);
        assertNotNull(gespeichertesRezept);
        assertEquals("Cremige Kartoffelsuppe", gespeichertesRezept.getTitel());
    }

    // Ein Abbruch der Bearbeitung übernimmt keine Änderungen
    @Test
    public void rezeptbearbeitungKannAbgebrochenWerden() {
        int rezeptId = testRezeptAnlegen(null);
        bearbeitungsmodusStarten(rezeptId);
        scenario.onActivity(activity -> {
            EditText titel = activity.findViewById(R.id.etRezeptTitel);
            titel.setText("Geänderter Titel");
            activity.findViewById(R.id.btnRezeptAbbrechen).performClick();
        });
        wartenBisActivityBeendet();
        Rezept unveraendertesRezept = testDatenbank.getDatenbank().rezeptDAO().nachIdAbrufen(rezeptId);
        assertNotNull(unveraendertesRezept);
        assertEquals("Testrezept", unveraendertesRezept.getTitel());
    }

    // Das Entfernen eines vorhandenen Bildes wird gespeichert
    @Test
    public void vorhandenesRezeptbildWirdEntfernt() throws Exception {
        File altesBild = testBildErstellen("testbild_entfernen.jpg");
        int rezeptId = testRezeptAnlegen(altesBild.getAbsolutePath());
        bearbeitungsmodusStarten(rezeptId);
        scenario.onActivity(activity -> {
            assertEquals(View.VISIBLE, activity.findViewById(R.id.cardRezeptVorschau).getVisibility());
            activity.findViewById(R.id.btnRezeptBildEntfernen).performClick();
            assertEquals(View.GONE, activity.findViewById(R.id.cardRezeptVorschau).getVisibility());
            activity.findViewById(R.id.btnRezeptSpeichern).performClick();
        });
        wartenBisActivityBeendet();
        Rezept gespeichertesRezept = testDatenbank.getDatenbank().rezeptDAO().nachIdAbrufen(rezeptId);
        assertNotNull(gespeichertesRezept);
        assertNull(gespeichertesRezept.getBildPfad());
        assertFalse(altesBild.exists());
    }

    // Ein vorhandenes Rezeptbild kann durch ein neues Bild ersetzt werden
    @Test
    public void vorhandenesRezeptbildKannErsetztWerden() throws Exception {
        File altesBild = testBildErstellen("testbild_alt.jpg");
        File neuesBild = testBildErstellen("testbild_neu.jpg");
        int rezeptId = testRezeptAnlegen(altesBild.getAbsolutePath());
        bearbeitungsmodusStarten(rezeptId);
        AtomicReference<String> neuerBildPfad = new AtomicReference<>();
        scenario.onActivity(activity -> {
            try {
                Method bildSpeichern = RezeptErstellenActivity.class.getDeclaredMethod("bildSpeichern", Uri.class);
                bildSpeichern.setAccessible(true);
                bildSpeichern.invoke(activity, Uri.fromFile(neuesBild));
                Field bildPfadFeld = RezeptErstellenActivity.class.getDeclaredField("bildPfad");
                bildPfadFeld.setAccessible(true);
                neuerBildPfad.set((String) bildPfadFeld.get(activity));
                assertNotNull(neuerBildPfad.get());
                assertNotEquals(altesBild.getAbsolutePath(), neuerBildPfad.get());
                activity.findViewById(R.id.btnRezeptSpeichern).performClick();
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError(exception);
            }
        });
        wartenBisActivityBeendet();
        Rezept gespeichertesRezept = testDatenbank.getDatenbank().rezeptDAO().nachIdAbrufen(rezeptId);
        assertNotNull(gespeichertesRezept);
        assertEquals(neuerBildPfad.get(), gespeichertesRezept.getBildPfad());
        assertTrue(new File(neuerBildPfad.get()).exists());
        assertFalse(altesBild.exists());
    }

    // Ein Bild dass während der Bearbeitung gewählt und diese dann abgebrochen wird, wird gelöscht
    @Test
    public void bearbeitungAbbrechenLoeschtNeuAusgewaehltesBild() throws Exception {
        Context context = androidx.test.core.app.ApplicationProvider.getApplicationContext();
        File bildOrdner = new File(context.getFilesDir(), "rezept_bilder");
        if (!bildOrdner.exists()) {
            assertTrue(bildOrdner.mkdirs());
        }
        File originalBild = new File(bildOrdner, "original_testbild.jpg");
        Bitmap originalBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
        try (FileOutputStream ausgabe = new FileOutputStream(originalBild)) {
            assertTrue(originalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, ausgabe));
        }
        originalBitmap.recycle();
        File quellBild = new File(context.getCacheDir(), "neues_testbild.jpg");
        Bitmap neuesBitmap = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888);
        try (FileOutputStream ausgabe = new FileOutputStream(quellBild)) {
            assertTrue(neuesBitmap.compress(Bitmap.CompressFormat.JPEG, 100, ausgabe));
        }
        neuesBitmap.recycle();
        RezeptVermittler vermittler = new RezeptVermittler(testDatenbank.getDatenbank());
        Rezept rezept = new Rezept("Originalrezept", originalBild.getAbsolutePath(), 2);
        int rezeptId = vermittler.anlegen(rezept,
                Arrays.asList(new RezeptVermittler.ZutatAngabe("Karotten", 2, "Stück")),
                Arrays.asList("Hauptgericht"),
                Arrays.asList("Karotten schneiden.")
        );
        scenario.close();
        Intent intent = new Intent(context, RezeptErstellenActivity.class);
        intent.putExtra(RezeptDetailActivity.EXTRA_REZEPT_ID, rezeptId);
        scenario = ActivityScenario.launch(intent);
        long zeitlimit = System.currentTimeMillis() + 5000;
        boolean formularGeladen = false;
        while (System.currentTimeMillis() < zeitlimit) {
            java.util.concurrent.atomic.AtomicBoolean geladen = new java.util.concurrent.atomic.AtomicBoolean(false);
            scenario.onActivity(activity -> {
                EditText titel = activity.findViewById(R.id.etRezeptTitel);
                geladen.set(titel.getText().toString().equals("Originalrezept"));
            });
            if (geladen.get()) {
                formularGeladen = true;
                break;
            }
            SystemClock.sleep(50);
        }
        assertTrue("Die vorhandenen Rezeptdaten wurden nicht geladen.", formularGeladen);
        String[] neuerBildPfad = new String[1];
        scenario.onActivity(activity -> {
            try {
                Method methode = RezeptErstellenActivity.class.getDeclaredMethod("bildSpeichern", Uri.class);
                methode.setAccessible(true);
                methode.invoke(activity, Uri.fromFile(quellBild));
                Field feld = RezeptErstellenActivity.class.getDeclaredField("bildPfad");
                feld.setAccessible(true);
                neuerBildPfad[0] = (String) feld.get(activity);
            } catch (Exception exception) {
                throw new AssertionError(exception);
            }
        });
        assertNotNull("Das neue Bild wurde nicht übernommen.", neuerBildPfad[0]);
        assertTrue(new File(neuerBildPfad[0]).exists());
        assertNotEquals(neuerBildPfad[0], originalBild.getAbsolutePath());
        scenario.onActivity(activity ->
                activity.findViewById(R.id.btnRezeptAbbrechen).performClick()
        );
        zeitlimit = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < zeitlimit && scenario.getState() != Lifecycle.State.DESTROYED) {
            SystemClock.sleep(50);
        }
        assertEquals(Lifecycle.State.DESTROYED, scenario.getState());
        Rezept gespeichertesRezept = testDatenbank.getDatenbank().rezeptDAO().nachIdAbrufen(rezeptId);
        assertNotNull(gespeichertesRezept);
        assertEquals(originalBild.getAbsolutePath(), gespeichertesRezept.getBildPfad());
        assertTrue(originalBild.exists());
        assertFalse("Das neu ausgewählte Bild wurde beim Abbruch nicht gelöscht.", new File(neuerBildPfad[0]).exists());
        quellBild.delete();
    }
}
