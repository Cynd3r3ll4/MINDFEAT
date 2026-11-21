package de.mindfeat.app.view.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.SystemClock;
import android.widget.EditText;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import de.mindfeat.app.MainActivity;
import de.mindfeat.app.R;
import de.mindfeat.app.model.datengrundlage.TestDatenbank;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RezeptUebersichtActivityTest {

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

    // Prüft, ob die Rezeptübersicht korrekt gestartet wird
    @Test
    public void rezeptUebersichtWirdKorrektGestartet() {
        try (ActivityScenario<RezeptUebersichtActivity> scenario = ActivityScenario.launch(RezeptUebersichtActivity.class)) {
            scenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.tvRezeptUeberschrift));
                assertNotNull(activity.findViewById(R.id.etRezeptSuche));
                assertNotNull(activity.findViewById(R.id.rvRezepte));
                assertNotNull(activity.findViewById(R.id.btnRezeptStartseite));
                assertNotNull(activity.findViewById(R.id.btnRezeptHinzufuegen));
            });
        }
    }

    // Prüft, ob der RecyclerView mit Adapter und LayoutManager eingerichtet ist
    @Test
    public void rezeptlisteWirdMitAdapterUndLayoutManagerEingerichtet() {
        try (ActivityScenario<RezeptUebersichtActivity> scenario = ActivityScenario.launch(RezeptUebersichtActivity.class)) {
            scenario.onActivity(activity -> {
                RecyclerView liste = activity.findViewById(R.id.rvRezepte);
                assertNotNull(liste.getAdapter());
                assertNotNull(liste.getLayoutManager());
            });
        }
    }

    // Prüft, ob das Suchfeld Texteingaben annimmt
    @Test
    public void suchfeldNimmtTexteingabeEntgegen() {
        try (ActivityScenario<RezeptUebersichtActivity> scenario = ActivityScenario.launch(RezeptUebersichtActivity.class)) {
            scenario.onActivity(activity -> {
                EditText suchfeld = activity.findViewById(R.id.etRezeptSuche);
                suchfeld.setText("Kartoffelsuppe");
                assertTrue(suchfeld.getText().toString().equals("Kartoffelsuppe"));
            });
        }
    }

    // Prüft, ob die Hinzufügen-Schaltfläche das Rezeptformular öffnet
    @Test
    public void rezeptHinzufuegenOeffnetRezeptErstellenActivity() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(RezeptErstellenActivity.class.getName(), null, false);
        try (ActivityScenario<RezeptUebersichtActivity> scenario = ActivityScenario.launch(RezeptUebersichtActivity.class)) {
            scenario.onActivity(activity -> {
                AppCompatImageButton button = activity.findViewById(R.id.btnRezeptHinzufuegen);
                button.performClick();
            });
            Activity gestarteteActivity = instrumentation.waitForMonitorWithTimeout(monitor, 5000);
            assertNotNull("Das Rezeptformular wurde nicht geöffnet.", gestarteteActivity);
        } finally {
            instrumentation.removeMonitor(monitor);
        }
    }

    // Prüft, ob die Home-Schaltfläche die Rezeptübersicht beendet
    @Test
    public void startseitenButtonBeendetRezeptUebersicht() {
        try (ActivityScenario<RezeptUebersichtActivity> scenario = ActivityScenario.launch(RezeptUebersichtActivity.class)) {
            scenario.onActivity(activity -> {
                AppCompatImageButton button = activity.findViewById(R.id.btnRezeptStartseite);
                button.performClick();
                assertTrue(activity.isFinishing());
            });
        }
    }

    // Prüft, ob die Rezeptliste anhand des Suchbegriffs gefiltert wird
    @Test
    public void rezeptsucheFiltertDieAngezeigtenRezepte() {
        testDatenbank.getDatenbank().rezeptDAO().einfuegen(new Rezept("Kartoffelsuppe", null, 2));
        testDatenbank.getDatenbank().rezeptDAO().einfuegen(new Rezept("Tomatensuppe", null, 2));
        try (ActivityScenario<RezeptUebersichtActivity> scenario = ActivityScenario.launch(RezeptUebersichtActivity.class)) {
            scenario.onActivity(activity -> {
                EditText suchfeld = activity.findViewById(R.id.etRezeptSuche);
                suchfeld.setText("Kartoffel");
            });
            int[] anzahlRezepte = {0};
            long zeitlimit = System.currentTimeMillis() + 5000;
            while (System.currentTimeMillis() < zeitlimit) {
                scenario.onActivity(activity -> {
                    RecyclerView liste = activity.findViewById(R.id.rvRezepte);
                    anzahlRezepte[0] = liste.getAdapter().getItemCount();
                });
                if (anzahlRezepte[0] == 1) {
                    break;
                }
                SystemClock.sleep(50);
            }
            assertEquals("Die Rezeptliste wurde nicht korrekt gefiltert.", 1, anzahlRezepte[0]);
        }
    }
}
