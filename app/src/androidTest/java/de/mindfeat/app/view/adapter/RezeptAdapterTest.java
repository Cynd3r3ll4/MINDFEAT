package de.mindfeat.app.view.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import de.mindfeat.app.R;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;

@RunWith(AndroidJUnit4.class)

public class RezeptAdapterTest {

    private Context context;
    private RezeptAdapter adapter;

    @Before
    public void setUp() {
        Context basisContext = ApplicationProvider.getApplicationContext();
        context = new ContextThemeWrapper(basisContext, R.style.Theme_MINDFEAT);
        adapter = new RezeptAdapter();
    }

    @After
    public void tearDown() {
        adapter.schliessen();
    }

    // Hilfsmethode zum Erstellen eines Testrezepts
    private Rezept rezeptErstellen(String titel, String bildPfad) {
        return new Rezept(titel, bildPfad, 2);
    }

    // Hilfsmethode zum Erstellen eines ViewHolders
    private RezeptAdapter.RezeptViewHolder viewHolderErstellen() {
        FrameLayout parent = new FrameLayout(context);
        return adapter.onCreateViewHolder(parent, 0);
    }

    // Hilfsmethode zum Zugriff auf die Titelansicht
    private TextView titelAnsichtErmitteln(View itemView) {
        return itemView.findViewById(R.id.tvRezeptTitel);
    }

    // Hilfsmethode zum Zugriff auf die Bildansicht
    private ImageView bildAnsichtErmitteln(View itemView) {
        return itemView.findViewById(R.id.ivRezeptBild);
    }

    @Test
    public void getItemCountBeiNeuemAdapterIstNull() {
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void onCreateViewHolderErzeugtRezeptansicht() {
        RezeptAdapter.RezeptViewHolder holder = viewHolderErstellen();
        assertNotNull(holder);
        assertNotNull(titelAnsichtErmitteln(holder.itemView));
        assertNotNull(bildAnsichtErmitteln(holder.itemView));
    }

    @Test
    public void rezepteAktualisierenUebernimmtMehrereRezepte() {
        Rezept rezept1 = rezeptErstellen("Gemüsesuppe", null);
        Rezept rezept2 = rezeptErstellen("Nudelauflauf", null);
        Rezept rezept3 = rezeptErstellen("Obstsalat", null);
        adapter.rezepteAktualisieren(Arrays.asList(rezept1, rezept2, rezept3));
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void rezepteAktualisierenErsetztBisherigeListe() {
        adapter.rezepteAktualisieren(
                Arrays.asList(
                        rezeptErstellen("Altes Rezept 1", null),
                        rezeptErstellen("Altes Rezept 2", null)));
        adapter.rezepteAktualisieren(Collections.singletonList(rezeptErstellen("Neues Rezept", null)));
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void rezepteAktualisierenMitNullLeertListe() {
        adapter.rezepteAktualisieren(Collections.singletonList(rezeptErstellen("Gemüsesuppe", null)));
        adapter.rezepteAktualisieren(null);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void onBindViewHolderSetztRezepttitel() {
        adapter.rezepteAktualisieren(Collections.singletonList(rezeptErstellen("Kartoffelgratin", null)));
        RezeptAdapter.RezeptViewHolder holder = viewHolderErstellen();
        adapter.onBindViewHolder(holder, 0);
        assertEquals("Kartoffelgratin", titelAnsichtErmitteln(holder.itemView).getText().toString());
    }

    @Test
    public void onBindViewHolderSetztPlatzhalterBeiFehlendemBildpfad() {
        adapter.rezepteAktualisieren(Collections.singletonList(rezeptErstellen("Gemüsesuppe", null)));
        RezeptAdapter.RezeptViewHolder holder = viewHolderErstellen();
        adapter.onBindViewHolder(holder, 0);
        ImageView bild = bildAnsichtErmitteln(holder.itemView);
        assertNotNull(bild.getDrawable());
        assertNull(bild.getTag());
    }

    @Test
    public void onBindViewHolderSetztKeinenBildpfadBeiLeeremPfad() {
        adapter.rezepteAktualisieren(Collections.singletonList(rezeptErstellen("Nudelauflauf", "   ")));
        RezeptAdapter.RezeptViewHolder holder = viewHolderErstellen();
        adapter.onBindViewHolder(holder, 0);
        ImageView bild = bildAnsichtErmitteln(holder.itemView);
        assertNotNull(bild.getDrawable());
        assertNull(bild.getTag());
    }

    @Test
    public void onBindViewHolderHinterlegtVorhandenenBildpfadAlsTag() {
        String bildPfad = "/nicht/vorhandenes/testbild.jpg";
        adapter.rezepteAktualisieren(Collections.singletonList(rezeptErstellen("Obstsalat", bildPfad)));
        RezeptAdapter.RezeptViewHolder holder = viewHolderErstellen();
        adapter.onBindViewHolder(holder, 0);
        ImageView bild = bildAnsichtErmitteln(holder.itemView);
        assertEquals(bildPfad, bild.getTag());
        assertNotNull(bild.getDrawable());
    }

    @Test
    public void wiederverwendeterViewHolderEntferntAltenBildpfad() {
        String alterBildPfad = "/nicht/vorhandenes/altes-bild.jpg";
        adapter.rezepteAktualisieren(Collections.singletonList(rezeptErstellen("Rezept mit Bild", alterBildPfad)));
        RezeptAdapter.RezeptViewHolder holder = viewHolderErstellen();
        adapter.onBindViewHolder(holder, 0);
        assertEquals(alterBildPfad, bildAnsichtErmitteln(holder.itemView).getTag());
        adapter.rezepteAktualisieren(Collections.singletonList(rezeptErstellen("Rezept ohne Bild", null)));
        adapter.onBindViewHolder(holder, 0);
        assertEquals("Rezept ohne Bild", titelAnsichtErmitteln(holder.itemView).getText().toString());
        assertNull(bildAnsichtErmitteln(holder.itemView).getTag());
        assertNotNull(bildAnsichtErmitteln(holder.itemView).getDrawable());
    }

    @Test
    public void schliessenKannMehrfachAufgerufenWerden() {
        adapter.schliessen();
        adapter.schliessen();
        assertEquals(0, adapter.getItemCount());
    }
}
