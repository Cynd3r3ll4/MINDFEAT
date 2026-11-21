package de.mindfeat.app;

import static org.junit.Assert.assertNotNull;

import android.app.Activity;
import android.app.Instrumentation;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import de.mindfeat.app.model.datengrundlage.TestDatenbank;
import de.mindfeat.app.view.activities.EinkaufslisteActivity;
import de.mindfeat.app.view.activities.EssensplanUebersichtActivity;
import de.mindfeat.app.view.activities.RezeptUebersichtActivity;
import de.mindfeat.app.view.activities.StatistikActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private TestDatenbank testDatenbank;

    @Before
    public void setUp() {
        testDatenbank = new TestDatenbank();
    }

    @After
    public void tearDown() {
        if (testDatenbank != null) {
            testDatenbank.schliessen();
        }
    }

    // Prüft, ob die Startseite korrekt angezeigt wird
    @Test
    public void startseiteWirdKorrektAngezeigt() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.tvAppTitel));
                assertNotNull(activity.findViewById(R.id.cardRezepte));
                assertNotNull(activity.findViewById(R.id.cardEssensplan));
                assertNotNull(activity.findViewById(R.id.cardEinkaufsliste));
                assertNotNull(activity.findViewById(R.id.btnStatistik));
            });
        }
    }

    @Test
    public void klickAufRezepteOeffnetRezeptUebersicht() {
        pruefeNavigation(R.id.cardRezepte, RezeptUebersichtActivity.class);
    }

    @Test
    public void klickAufEssensplanOeffnetEssensplanUebersicht() {
        pruefeNavigation(R.id.cardEssensplan, EssensplanUebersichtActivity.class);
    }

    @Test
    public void klickAufEinkaufslisteOeffnetEinkaufsliste() {
        pruefeNavigation(R.id.cardEinkaufsliste, EinkaufslisteActivity.class);
    }

    @Test
    public void klickAufStatistikOeffnetStatistik() {
        pruefeNavigation(R.id.btnStatistik, StatistikActivity.class);
    }

    // Gemeinsame Prüfung der Navigation zu einer Ziel-Activity
    private void pruefeNavigation(int viewId, Class<? extends Activity> zielActivity) {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(zielActivity.getName(), null, false);
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                android.view.View navigationselement = activity.findViewById(viewId);
                assertNotNull(navigationselement);
                navigationselement.performClick();
            });
            Activity gestarteteActivity = instrumentation.waitForMonitorWithTimeout(monitor, 5000);
            assertNotNull("Die Ziel-Activity wurde nicht gestartet: " + zielActivity.getSimpleName(), gestarteteActivity);
        } finally {
            instrumentation.removeMonitor(monitor);
        }
    }
}
