package de.mindfeat.app.view.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import de.mindfeat.app.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RezeptErstellenActivityTest {

    private ActivityScenario<RezeptErstellenActivity> scenario;

    @Before
    public void setUp() {
        scenario = ActivityScenario.launch(RezeptErstellenActivity.class);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
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
            assertTrue(titel.getError() == null);
            assertTrue(portionen.getError() == null);
        });
    }
}
