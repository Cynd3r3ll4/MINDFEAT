package de.mindfeat.app.view.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import de.mindfeat.app.MainActivity;
import de.mindfeat.app.R;

@RunWith(AndroidJUnit4.class)
public class RezeptNavigationEsspressoTest {

    @Test
    public void rezeptHinzufuegenOeffnetFormularMitEingabefeldern() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            // Startseite öffnen: Rezeptübersicht
            onView(withId(R.id.cardRezepte)).perform(click());
            // Rezeptformular öffnen
            onView(withId(R.id.btnRezeptHinzufuegen)).perform(click());
            // Grundlegende Rezeptfelder werden angezeigt
            onView(withId(R.id.etRezeptTitel)).check(matches(isDisplayed()));
            onView(withId(R.id.etRezeptKategorien)).check(matches(isDisplayed()));
            onView(withId(R.id.etRezeptPortionen)).check(matches(isDisplayed()));
            // Dynamisch erzeugte Zutatenfelder werden angezeigt
            onView(withHint("Menge")).check(matches(isDisplayed()));
            onView(withHint("Einheit")).check(matches(isDisplayed()));
            onView(withHint("Zutat")).check(matches(isDisplayed()));
            // Dynamisch erzeugtes Arbeitsschrittfeld wird angezeigt
            onView(withHint("Arbeitsschritt beschreiben")).check(matches(isDisplayed()));
            // Zum Speichern-Button im ScrollView scrollen
            onView(withId(R.id.btnRezeptSpeichern)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).check(matches(isEnabled()));
        }
    }

    @Test
    public void zurueckSchaltflaecheFuehrtZurRezeptuebersicht() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            // Rezeptübersicht öffnen
            onView(withId(R.id.cardRezepte)).perform(click());
            // Rezeptformular öffnen
            onView(withId(R.id.btnRezeptHinzufuegen)).perform(click());
            // Zurück-Schaltfläche im Formular betätigen
            onView(withId(R.id.btnRezeptErstellenZurueck)).perform(click());
            // Rezeptübersicht wird wieder angezeigt
            onView(withId(R.id.tvRezeptUeberschrift)).check(matches(isDisplayed()));
            onView(withId(R.id.etRezeptSuche)).check(matches(isDisplayed()));
            onView(withId(R.id.btnRezeptHinzufuegen)).check(matches(isDisplayed()));
        }
    }
}
