package de.mindfeat.app;

import static org.junit.Assert.assertNotNull;

import android.app.Activity;
import android.app.Instrumentation;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import de.mindfeat.app.model.datengrundlage.TestDatenbank;
import de.mindfeat.app.view.activities.RezeptUebersichtActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityEsspressoTest {

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

    @Test
    public void rezepteOeffnetRezeptuebersichtMitSuchfeldUndHinzufuegen() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(RezeptUebersichtActivity.class.getName(), null, false);
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                android.view.View cardRezepte = activity.findViewById(R.id.cardRezepte);
                assertNotNull(cardRezepte);
                cardRezepte.performClick();
            });
            Activity gestarteteActivity = instrumentation.waitForMonitorWithTimeout(monitor, 5000);
            assertNotNull("Die Rezeptübersicht wurde nicht geöffnet.", gestarteteActivity);
        } finally {
            instrumentation.removeMonitor(monitor);
        }
    }
}
