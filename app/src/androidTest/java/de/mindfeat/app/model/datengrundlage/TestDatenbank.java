package de.mindfeat.app.model.datengrundlage;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

public class TestDatenbank {
    private final ZentraleDatenbank datenbank;
    public TestDatenbank() {
        Context context = ApplicationProvider.getApplicationContext();
        datenbank = Room.inMemoryDatabaseBuilder(context, ZentraleDatenbank.class).allowMainThreadQueries().build();
    }

    public ZentraleDatenbank getDatenbank() {
        return datenbank;
    }

    public void schliessen() {
        datenbank.close();
    }
}
