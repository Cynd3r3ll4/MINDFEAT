package de.mindfeat.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityEsspressoTest {

    @Test
    public void rezepteOeffnetRezeptuebersichtMitSuchfeldUndHinzufuegen() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            // Von der Startseite zur Rezeptübersicht navigieren
            onView(withId(R.id.cardRezepte)).perform(click());
            // Überschrift der Rezeptübersicht wird angezeigt
            onView(withId(R.id.tvRezeptUeberschrift)).check(matches(isDisplayed()));
            // Suchfeld wird angezeigt
            onView(withId(R.id.etRezeptSuche)).check(matches(isDisplayed()));
            // Schaltfläche zum Hinzufügen eines Rezepts wird angezeigt
            onView(withId(R.id.btnRezeptHinzufuegen)).check(matches(isDisplayed()));
            // Rezeptliste ist vorhanden
            onView(withId(R.id.rvRezepte)).check(matches(isDisplayed()));
        }
    }
}
