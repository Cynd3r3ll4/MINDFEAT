package de.mindfeat.app.view.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.mindfeat.app.R;
import de.mindfeat.app.controller.RezeptController;
import de.mindfeat.app.model.datengrundlage.ZentraleDatenbank;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;
import de.mindfeat.app.model.vermittler.RezeptVermittler;
import de.mindfeat.app.view.adapter.RezeptAdapter;

public class RezeptUebersichtActivity extends AppCompatActivity {
    private RecyclerView rvRezepte;
    private EditText etRezeptSuche;
    private RezeptAdapter rezeptAdapter;
    private RezeptController rezeptController;
    private ExecutorService datenbankExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rezept_uebersicht);
        ansichtenInitialisieren();
        listeEinrichten();
        datenverbindungEinrichten();
        sucheEinrichten();
        navigationEinrichten();
        fenstereinschuebeEinrichten();
    }


    // Verknüpfung der UI-Elemente
    private void ansichtenInitialisieren() {
        rvRezepte = findViewById(R.id.rvRezepte);
        etRezeptSuche = findViewById(R.id.etRezeptSuche);
    }


    // Einrichtung der Rezeptliste
    private void listeEinrichten() {
        rvRezepte.setLayoutManager(new LinearLayoutManager(this));
        rezeptAdapter = new RezeptAdapter();
        rvRezepte.setAdapter(rezeptAdapter);
        rvRezepte.setHasFixedSize(false);
    }


    // Verbindung zwischen Activity, Controller und Datenbank herstellen
    private void datenverbindungEinrichten() {
        datenbankExecutor = Executors.newSingleThreadExecutor();
        ZentraleDatenbank datenbank = ZentraleDatenbank.getInstance(getApplicationContext());
        RezeptVermittler rezeptVermittler = new RezeptVermittler(datenbank);
        rezeptController = new RezeptController(rezeptVermittler, datenbankExecutor, this::runOnUiThread);
        sucheAusfuehren(""); // Beim Öffnen zunächst alle Rezepte anzeigen
    }

    // Suchfeld mit der Rezeptsuche verbinden
    private void sucheEinrichten() {
        etRezeptSuche.addTextChangedListener(new TextWatcher() { // Textwatcher reagiert sofort auf Texteingabe
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Keine Aktion erforderlich
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sucheAusfuehren(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {
                // Keine Aktion erforderlich
            }
        });
    }


    // Suchanfrage über den Controller ausführen
    private void sucheAusfuehren(String suchtext) {
        rezeptController.suchAbrufen(suchtext, new RezeptController.Callback<>() {
                    @Override
                    public void beiErfolg(List<Rezept> rezepte) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        rezeptAdapter.rezepteAktualisieren(rezepte);
                    }

            @Override
                    public void beiFehler(Exception exception) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        Toast.makeText(RezeptUebersichtActivity.this, "Die Rezeptsuche war erfolglos.", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }


    // Navigation zurück zur Startseite
    private void navigationEinrichten() {
        findViewById(R.id.btnRezeptStartseite).setOnClickListener(v -> finish());
    }


    // Berücksichtigung der Systemleisten bei Edge-to-Edge
    private void fenstereinschuebeEinrichten() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rezeptUebersicht), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                }
        );
    }


    // Ressourcen beim Zerstören der Activity freigeben
    @Override
    protected void onDestroy() {
        if (rezeptAdapter != null) {
            rezeptAdapter.schliessen();
        }
        if (datenbankExecutor != null) {
            datenbankExecutor.shutdown();
        }
        super.onDestroy();
    }
}
