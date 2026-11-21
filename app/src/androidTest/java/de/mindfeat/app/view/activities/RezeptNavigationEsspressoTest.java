package de.mindfeat.app.view.activities;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.app.Instrumentation;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.mindfeat.app.R;
import de.mindfeat.app.model.datengrundlage.TestDatenbank;

@RunWith(AndroidJUnit4.class)
public class RezeptNavigationEsspressoTest {

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
    public void rezeptHinzufuegenOeffnetFormularMitEingabefeldern() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(RezeptErstellenActivity.class.getName(), null, false);
        try (ActivityScenario<RezeptUebersichtActivity> scenario = ActivityScenario.launch(RezeptUebersichtActivity.class)) {
            scenario.onActivity(activity -> {
                AppCompatImageButton button = activity.findViewById(R.id.btnRezeptHinzufuegen);
                assertNotNull(button);
                button.performClick();
            });
            Activity gestarteteActivity = instrumentation.waitForMonitorWithTimeout(monitor, 5000);
            assertNotNull("Das Rezeptformular wurde nicht geöffnet.", gestarteteActivity);
        } finally {
            instrumentation.removeMonitor(monitor);
        }
    }

    @Test
    public void zurueckSchaltflaecheFuehrtZurRezeptuebersicht() {
        try (ActivityScenario<RezeptErstellenActivity> scenario = ActivityScenario.launch(RezeptErstellenActivity.class)) {
            scenario.onActivity(activity -> {
                AppCompatImageButton button = activity.findViewById(R.id.btnRezeptErstellenZurueck);
                assertNotNull(button);
                button.performClick();
                assertTrue(activity.isFinishing());
            });
        }
    }
}
