package de.mindfeat.app.view.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.FrameLayout;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import de.mindfeat.app.R;
import de.mindfeat.app.model.datengrundlage.TestDatenbank;
import de.mindfeat.app.model.vermittler.RezeptVermittler;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;

@RunWith(AndroidJUnit4.class)
public class RezeptDetailActivityTest {

    private TestDatenbank testDatenbank;
    private RezeptVermittler rezeptVermittler;
    private ActivityScenario<RezeptDetailActivity> scenario;

    @Before
    public void setUp() {
        testDatenbank = new TestDatenbank();
        rezeptVermittler = new RezeptVermittler(testDatenbank.getDatenbank());
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

    // Eine Rezeptdetailansicht mit der übergebenen Rezept-ID öffnen
    private void detailansichtStarten(int rezeptId) {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                RezeptDetailActivity.class
        );
        intent.putExtra(RezeptDetailActivity.EXTRA_REZEPT_ID, rezeptId);
        scenario = ActivityScenario.launch(intent);
    }

    // Auf das asynchrone Laden der Rezeptdaten warten
    private void wartenBis(Bedingung bedingung) {
        long zeitlimit = System.currentTimeMillis() + 5000;
        do {
            AtomicBoolean erfuellt = new AtomicBoolean(false);
            scenario.onActivity(activity -> erfuellt.set(bedingung.istErfuellt(activity)));
            if (erfuellt.get()) {
                return;
            }
            SystemClock.sleep(50);
        } while (System.currentTimeMillis() < zeitlimit);
        throw new AssertionError("Die erwarteten Rezeptdetails wurden nicht rechtzeitig geladen.");
    }

    // Funktionale Bedingung für die Wartehilfe
    private interface Bedingung {
        boolean istErfuellt(RezeptDetailActivity activity);
    }

    // Vollständige Rezeptdetails werden angezeigt
    @Test
    public void rezeptdetailsWerdenVollstaendigAngezeigt() {
        Rezept rezept = new Rezept("Kartoffelsuppe", null, 3);
        int rezeptId = rezeptVermittler.anlegen(rezept, Arrays.asList(
                        new RezeptVermittler.ZutatAngabe("Kartoffeln", 250, "g"),
                        new RezeptVermittler.ZutatAngabe("Olivenöl", 1.5, "EL")),
                Arrays.asList("Hauptgericht", "Herbstlich"),
                Arrays.asList("Kartoffeln schälen und würfeln.", "Kartoffeln kochen und anschließend pürieren."));
        detailansichtStarten(rezeptId);
        wartenBis(activity ->
                ((LinearLayout) activity.findViewById(R.id.llRezeptZutaten)).getChildCount() == 2
                        && ((LinearLayout) activity.findViewById(R.id.llRezeptZubereitung)).getChildCount() == 2);
        scenario.onActivity(activity -> {
            TextView titel = activity.findViewById(R.id.tvRezeptTitelDetail);
            TextView portionen = activity.findViewById(R.id.tvRezeptPortionen);
            assertEquals("Kartoffelsuppe", titel.getText().toString());
            assertEquals("Für 3 Portionen", portionen.getText().toString());
            ChipGroup kategorien = activity.findViewById(R.id.cgRezeptKategorien);
            assertEquals(2, kategorien.getChildCount());
            assertEquals("Hauptgericht", ((Chip) kategorien.getChildAt(0)).getText().toString());
            assertEquals("Herbstlich", ((Chip) kategorien.getChildAt(1)).getText().toString());
            LinearLayout zutaten = activity.findViewById(R.id.llRezeptZutaten);
            assertEquals(2, zutaten.getChildCount());
            LinearLayout ersteZutat = (LinearLayout) zutaten.getChildAt(0);
            assertEquals("250 g", ((TextView) ersteZutat.getChildAt(0)).getText().toString());
            assertEquals("Kartoffeln", ((TextView) ersteZutat.getChildAt(1)).getText().toString());
            LinearLayout zweiteZutat = (LinearLayout) zutaten.getChildAt(1);
            assertEquals("1,5 EL", ((TextView) zweiteZutat.getChildAt(0)).getText().toString());
            assertEquals("Olivenöl", ((TextView) zweiteZutat.getChildAt(1)).getText().toString());
            LinearLayout schritte = activity.findViewById(R.id.llRezeptZubereitung);
            assertEquals(2, schritte.getChildCount());
            LinearLayout ersterSchritt = (LinearLayout) schritte.getChildAt(0);
            FrameLayout ersteNummer = (FrameLayout) ersterSchritt.getChildAt(0);
            assertEquals("1", ((TextView) ersteNummer.findViewById(R.id.tvSchrittNummer)).getText().toString());
            assertEquals("Kartoffeln schälen und würfeln.", ((TextView) ersterSchritt.getChildAt(1)).getText().toString());
            LinearLayout zweiterSchritt = (LinearLayout) schritte.getChildAt(1);
            FrameLayout zweiteNummer = (FrameLayout) zweiterSchritt.getChildAt(0);
            assertEquals("2", ((TextView) zweiteNummer.findViewById(R.id.tvSchrittNummer)).getText().toString());
            assertEquals("Kartoffeln kochen und anschließend pürieren.", ((TextView) zweiterSchritt.getChildAt(1)).getText().toString());
        });
    }

    // Bei fehlenden Zutaten und Arbeitsschritten erscheinen Platzhaltertexte
    @Test
    public void leereZutatenUndSchritteWerdenMitHinweisAngezeigt() {
        Rezept rezept = new Rezept("Einfaches Rezept", null, 2);
        int rezeptId = rezeptVermittler.anlegen(rezept, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        detailansichtStarten(rezeptId);
        wartenBis(activity ->
                ((LinearLayout) activity.findViewById(R.id.llRezeptZutaten)).getChildCount() == 1
                        && ((LinearLayout) activity.findViewById(R.id.llRezeptZubereitung)).getChildCount() == 1);
        scenario.onActivity(activity -> {
            LinearLayout zutaten = activity.findViewById(R.id.llRezeptZutaten);
            LinearLayout schritte = activity.findViewById(R.id.llRezeptZubereitung);
            assertEquals(1, zutaten.getChildCount());
            assertEquals(1, schritte.getChildCount());
            assertEquals(activity.getString(R.string.rezept_detail_keine_zutaten), ((TextView) zutaten.getChildAt(0)).getText().toString());
            assertEquals(activity.getString(R.string.rezept_detail_keine_schritte), ((TextView) schritte.getChildAt(0)).getText().toString());
            ChipGroup kategorien = activity.findViewById(R.id.cgRezeptKategorien);
            assertEquals(0, kategorien.getChildCount());
        });
    }

    // Bei einer ungültigen Rezept-ID wird die Detailansicht beendet
    @Test
    public void ungueltigeRezeptIdBeendetDetailansicht() {
        detailansichtStarten(-1);
        wartenBisActivityBeendet();
        assertEquals(Lifecycle.State.DESTROYED, scenario.getState());
    }

    // Bei einer nicht vorhandenen Rezept-ID wird die Detailansicht beendet
    @Test
    public void nichtVorhandenesRezeptBeendetDetailansicht() {
        detailansichtStarten(9999);
        wartenBisActivityBeendet();
        assertEquals(Lifecycle.State.DESTROYED, scenario.getState());
    }

    //Hilfsmethode: Lifecycle überprüfen
    private void wartenBisActivityBeendet() {
        long ende = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < ende) {
            if (scenario.getState() == Lifecycle.State.DESTROYED) {
                return;
            }
            SystemClock.sleep(50);
        }
        fail("Die Activity wurde nicht innerhalb von 5 Sekunden beendet.");
    }

    // Öffnet das Aktionsmenü und liefert das angezeigte Popup zurück
    private PopupWindow aktionsmenueOeffnen() {
        AtomicReference<PopupWindow> popupReferenz = new AtomicReference<>();
        scenario.onActivity(activity -> {
            activity.findViewById(R.id.btnRezeptAktionen).performClick();
            try {
                Field feld = RezeptDetailActivity.class.getDeclaredField("rezeptAktionsPopup");
                feld.setAccessible(true);
                PopupWindow popup = (PopupWindow) feld.get(activity);
                assertNotNull(popup);
                assertTrue(popup.isShowing());
                popupReferenz.set(popup);
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError(exception);
            }
        });
        return popupReferenz.get();
    }

    // Das Aktionsmenü lässt sich öffnen und über den Aktionsbutton schließen
    @Test
    public void aktionsmenueKannGeoeffnetUndGeschlossenWerden() {
        Rezept rezept = new Rezept("Testrezept", null, 2);
        int rezeptId = rezeptVermittler.anlegen(rezept, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        detailansichtStarten(rezeptId);
        wartenBis(activity -> "Testrezept".equals(((TextView) activity.findViewById(R.id.tvRezeptTitelDetail)).getText().toString()));
        PopupWindow popup = aktionsmenueOeffnen();
        scenario.onActivity(activity -> activity.findViewById(R.id.btnRezeptAktionen).performClick());
        assertFalse(popup.isShowing());
    }

    // Die Menüaktion Bearbeiten öffnet das Formular mit der richtigen Rezept-ID.
    @Test
    public void menueaktionBearbeitenOeffnetBearbeitungsformular() {
        Rezept rezept = new Rezept("Testrezept", null, 2);
        int rezeptId = rezeptVermittler.anlegen(rezept, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        detailansichtStarten(rezeptId);
        wartenBis(activity -> "Testrezept".equals(((TextView) activity.findViewById(R.id.tvRezeptTitelDetail)).getText().toString()));
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(RezeptErstellenActivity.class.getName(), null, false);
        PopupWindow popup = aktionsmenueOeffnen();
        scenario.onActivity(activity -> popup.getContentView().findViewById(R.id.zeileBearbeiten).performClick());
        android.app.Activity bearbeitungsActivity = monitor.waitForActivityWithTimeout(5000);
        instrumentation.removeMonitor(monitor);
        assertNotNull("Das Bearbeitungsformular wurde nicht geöffnet.", bearbeitungsActivity);
        assertEquals(rezeptId, bearbeitungsActivity.getIntent().getIntExtra(RezeptDetailActivity.EXTRA_REZEPT_ID, -1));
        bearbeitungsActivity.finish();
    }

    // Der eigene Löschdialog wird über die Menüaktion eingeblendet.
    @Test
    public void menueaktionLoeschenZeigtEigenenLoeschdialog() {
        Rezept rezept = new Rezept("Testrezept", null, 2);
        int rezeptId = rezeptVermittler.anlegen(rezept, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        detailansichtStarten(rezeptId);
        wartenBis(activity -> "Testrezept".equals(((TextView) activity.findViewById(R.id.tvRezeptTitelDetail)).getText().toString()));
        PopupWindow popup = aktionsmenueOeffnen();
        scenario.onActivity(activity -> popup.getContentView().findViewById(R.id.zeileLoeschen).performClick());
        wartenBis(activity -> activity.findViewById(R.id.layoutLoeschDialog).getVisibility() == View.VISIBLE);
        scenario.onActivity(activity -> {
            assertEquals(View.VISIBLE, activity.findViewById(R.id.layoutLoeschDialog).getVisibility());
            assertNotNull(activity.findViewById(R.id.btnAbbrechen));
            assertNotNull(activity.findViewById(R.id.btnLoeschen));
        });
    }

    // Abbrechen schließt den Löschdialog, ohne das Rezept zu löschen.
    @Test
    public void loeschdialogKannAbgebrochenWerden() {
        Rezept rezept = new Rezept("Testrezept", null, 2);
        int rezeptId = rezeptVermittler.anlegen(rezept, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        detailansichtStarten(rezeptId);
        wartenBis(activity -> "Testrezept".equals(((TextView) activity.findViewById(R.id.tvRezeptTitelDetail)).getText().toString()));
        PopupWindow popup = aktionsmenueOeffnen();
        scenario.onActivity(activity -> popup.getContentView().findViewById(R.id.zeileLoeschen).performClick());
        wartenBis(activity -> activity.findViewById(R.id.layoutLoeschDialog).getVisibility() == View.VISIBLE);
        scenario.onActivity(activity -> activity.findViewById(R.id.btnAbbrechen).performClick());
        scenario.onActivity(activity -> assertEquals(View.GONE, activity.findViewById(R.id.layoutLoeschDialog).getVisibility()));
        assertNotNull(testDatenbank.getDatenbank().rezeptDAO().nachIdAbrufen(rezeptId));
    }

    // Bestätigen löscht das Rezept und beendet die Detailansicht.
    @Test
    public void loeschdialogBestaetigungLoeschtRezept() {
        Rezept rezept = new Rezept("Testrezept", null, 2);
        int rezeptId = rezeptVermittler.anlegen(rezept, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        detailansichtStarten(rezeptId);
        wartenBis(activity -> "Testrezept".equals(((TextView) activity.findViewById(R.id.tvRezeptTitelDetail)).getText().toString()));
        PopupWindow popup = aktionsmenueOeffnen();
        scenario.onActivity(activity -> popup.getContentView().findViewById(R.id.zeileLoeschen).performClick());
        wartenBis(activity -> activity.findViewById(R.id.layoutLoeschDialog).getVisibility() == View.VISIBLE);
        scenario.onActivity(activity -> activity.findViewById(R.id.btnLoeschen).performClick());
        wartenBisActivityBeendet();
        assertNull(testDatenbank.getDatenbank().rezeptDAO().nachIdAbrufen(rezeptId));
    }

    // Beim Löschen eines Rezepts wird das Bild samt bilddatei gelöscht
    @Test
    public void rezeptMitBildLoeschenEntferntAuchBilddatei() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        File bildOrdner = new File(context.getFilesDir(), "rezept_bilder");
        if (!bildOrdner.exists()) {
            assertTrue(bildOrdner.mkdirs());
        }
        File bildDatei = new File(bildOrdner, "test_rezept_loeschen.jpg");
        Bitmap bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
        try (FileOutputStream ausgabe = new FileOutputStream(bildDatei)) {
            assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ausgabe));
        }
        bitmap.recycle();
        assertTrue(bildDatei.exists());
        Rezept rezept = new Rezept("Rezept mit Bild", bildDatei.getAbsolutePath(), 2);
        int rezeptId = rezeptVermittler.anlegen(rezept, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        detailansichtStarten(rezeptId);
        wartenBis(activity -> ((TextView) activity.findViewById(R.id.tvRezeptTitelDetail)).getText().toString().equals("Rezept mit Bild"));
        scenario.onActivity(activity -> {
            ImageButton btnAktionen = activity.findViewById(R.id.btnRezeptAktionen);btnAktionen.performClick();
        });
        scenario.onActivity(activity -> {
            try {
                Field feld = RezeptDetailActivity.class.getDeclaredField("rezeptAktionsPopup");
                feld.setAccessible(true);
                PopupWindow popup = (PopupWindow) feld.get(activity);
                assertNotNull(popup);
                assertTrue(popup.isShowing());
                View loeschen = popup.getContentView().findViewById(R.id.zeileLoeschen);
                loeschen.performClick();

            } catch (Exception exception) {
                throw new AssertionError(exception);
            }
        });
        scenario.onActivity(activity -> {
            View dialog = activity.findViewById(R.id.layoutLoeschDialog);
            assertEquals(View.VISIBLE, dialog.getVisibility());
            MaterialButton btnLoeschen = dialog.findViewById(R.id.btnLoeschen);
            btnLoeschen.performClick();
        });
        wartenBisActivityBeendet();
        assertNull(rezeptVermittler.nachIdMitDetailsAbrufen(rezeptId));
        assertFalse("Die Rezeptbilddatei wurde nicht gelöscht.", bildDatei.exists());
    }

    // Nach einer Bearbeitung wird die Detailansicht aktualisiert
    @Test
    public void detailansichtWirdNachBearbeitungAktualisiert() throws Exception {
        Rezept rezept = new Rezept("Originaltitel", null, 2);
        int rezeptId = rezeptVermittler.anlegen(rezept,
                Arrays.asList(new RezeptVermittler.ZutatAngabe("Karotten", 2, "Stück")),
                Arrays.asList("Hauptgericht"),
                Arrays.asList("Karotten schneiden.")
        );
        detailansichtStarten(rezeptId);
        wartenBis(activity -> ((TextView) activity.findViewById(R.id.tvRezeptTitelDetail)).getText().toString().equals("Originaltitel"));
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(RezeptErstellenActivity.class.getName(), null, false);
        try {
            scenario.onActivity(activity -> {
                activity.findViewById(R.id.btnRezeptAktionen).performClick();
                try {
                    java.lang.reflect.Field feld = RezeptDetailActivity.class.getDeclaredField("rezeptAktionsPopup");
                    feld.setAccessible(true);
                    PopupWindow popup = (PopupWindow) feld.get(activity);
                    assertNotNull(popup);
                    assertTrue(popup.isShowing());
                    View bearbeiten = popup.getContentView().findViewById(R.id.zeileBearbeiten);
                    bearbeiten.performClick();
                } catch (Exception exception) {
                    throw new AssertionError(exception);
                }
            });
            Activity gestarteteActivity = monitor.waitForActivityWithTimeout(5000);
            assertNotNull("Die Bearbeitungsansicht wurde nicht geöffnet.", gestarteteActivity);
            RezeptErstellenActivity bearbeitenActivity = (RezeptErstellenActivity) gestarteteActivity;
            long zeitlimit = System.currentTimeMillis() + 5000;
            boolean formularGeladen = false;
            while (System.currentTimeMillis() < zeitlimit) {
                java.util.concurrent.atomic.AtomicBoolean geladen = new java.util.concurrent.atomic.AtomicBoolean(false);
                instrumentation.runOnMainSync(() -> {
                    EditText titel = bearbeitenActivity.findViewById(R.id.etRezeptTitel);
                    geladen.set(titel.getText().toString().equals("Originaltitel"));
                });
                if (geladen.get()) {
                    formularGeladen = true;
                    break;
                }
                SystemClock.sleep(50);
            }
            assertTrue("Die Rezeptdaten wurden im Bearbeitungsformular nicht geladen.", formularGeladen);
            instrumentation.runOnMainSync(() -> {
                EditText titel = bearbeitenActivity.findViewById(R.id.etRezeptTitel);
                EditText portionen = bearbeitenActivity.findViewById(R.id.etRezeptPortionen);
                titel.setText("Aktualisierter Titel");
                portionen.setText("4");
                Button speichern = bearbeitenActivity.findViewById(R.id.btnRezeptSpeichern);
                speichern.performClick();
            });
            wartenBis(activity ->
                    ((TextView) activity.findViewById(R.id.tvRezeptTitelDetail)).getText().toString().equals("Aktualisierter Titel"));
            scenario.onActivity(activity -> {
                TextView titel = activity.findViewById(R.id.tvRezeptTitelDetail);
                TextView portionen = activity.findViewById(R.id.tvRezeptPortionen);
                assertEquals("Aktualisierter Titel", titel.getText().toString());
                assertEquals("Für 4 Portionen", portionen.getText().toString());
            });
        } finally {
            instrumentation.removeMonitor(monitor);
        }
    }
}
