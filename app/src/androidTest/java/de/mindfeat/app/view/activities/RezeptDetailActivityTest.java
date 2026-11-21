package de.mindfeat.app.view.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import android.content.Intent;
import android.os.SystemClock;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.FrameLayout;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

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
}
