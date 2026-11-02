package de.mindfeat.app.datengrundlage;

import android.content.Context;

import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import de.mindfeat.app.datengrundlage.entitaeten.Rezept;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)

public class ZentraleDatenbankTest {

    private ZentraleDatenbank datenbank;

    // Eine In-Memory-Datenbank für den Test erstellen unabhängig von Produktivdatenbank
    @Before
    public void setUp() {Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        datenbank = Room.inMemoryDatabaseBuilder(context, ZentraleDatenbank.class).allowMainThreadQueries().build();
    }

    // Die Datenbank nach jedem Test schließen
    @After
    public void tearDown() {
        if (datenbank != null) {
            datenbank.close();
        }
    }

    // Die zentrale Datenbank und das Rezept-DAO sind verfügbar
    @Test
    public void datenbankErstellenUndDAOAbrufen() {
        assertNotNull(datenbank);
        assertNotNull(datenbank.rezeptDAO());
    }

    // Ein Rezept kann über die zentrale Datenbank gespeichert und abgerufen werden
    @Test
    public void rezeptUeberZentraleDatenbankSpeichernUndAbrufen() {
        Rezept rezept = new Rezept("Graupeneintopf", null, 4);
        long id = datenbank.rezeptDAO().einfuegen(rezept);
        Rezept gespeichertesRezept = datenbank.rezeptDAO().nachIdAbrufen((int) id);
        assertNotNull(gespeichertesRezept);
        assertEquals("Graupeneintopf", gespeichertesRezept.getTitel());
        assertEquals(4, gespeichertesRezept.getPortionen());
    }
}
