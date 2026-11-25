package de.mindfeat.app.view.activities;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.mindfeat.app.R;
import de.mindfeat.app.controller.EssensplanController;
import de.mindfeat.app.model.datengrundlage.ZentraleDatenbank;
import de.mindfeat.app.model.datengrundlage.entitaeten.Essensplan;
import de.mindfeat.app.model.vermittler.EssensplanVermittler;
import de.mindfeat.app.view.adapter.EssensplanAdapter;

public class EssensplanUebersichtActivity extends AppCompatActivity {

    private RecyclerView rvEssensplaene;
    private EditText etEssensplanSuche;
    private EssensplanAdapter essensplanAdapter;
    private EssensplanController essensplanController;
    private ExecutorService datenbankExecutor;
    private final List<Essensplan> alleEssensplaene = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_essensplan_uebersicht);
        ansichtenInitialisieren();
        listeEinrichten();
        datenverbindungEinrichten();
        sucheEinrichten();
        navigationEinrichten();
        fenstereinschuebeEinrichten();
    }

    // Verknüpfung der UI-Elemente
    private void ansichtenInitialisieren() {
        rvEssensplaene = findViewById(R.id.rvEssensplaene);
        etEssensplanSuche = findViewById(R.id.etEssensplanSuche);
    }

    // Einrichtung der Essensplanliste
    private void listeEinrichten() {
        rvEssensplaene.setLayoutManager(new LinearLayoutManager(this));
        essensplanAdapter = new EssensplanAdapter();
        rvEssensplaene.setAdapter(essensplanAdapter);
        rvEssensplaene.setHasFixedSize(false);
    }

    // Verbindung zwischen Activity, Controller und Datenbank herstellen
    private void datenverbindungEinrichten() {
        datenbankExecutor = Executors.newSingleThreadExecutor();
        ZentraleDatenbank datenbank = ZentraleDatenbank.getInstance(getApplicationContext());
        EssensplanVermittler essensplanVermittler = new EssensplanVermittler(datenbank);
        essensplanController = new EssensplanController(essensplanVermittler, datenbankExecutor, this::runOnUiThread);
    }

    // Suchfeld mit der Essensplansuche verbinden
    private void sucheEinrichten() {
        etEssensplanSuche.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Keine Aktion erforderlich
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                essensplaeneFiltern(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {
                // Keine Aktion erforderlich
            }
        });
    }

    // Alle Essenspläne aus der Datenbank laden
    private void essensplaeneLaden() {
        essensplanController.alleAbrufen(
                new EssensplanController.Callback<>() {
                    @Override
                    public void beiErfolg(List<Essensplan> essensplaene) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        alleEssensplaene.clear();
                        if (essensplaene != null) {
                            alleEssensplaene.addAll(essensplaene);
                        }
                        essensplaeneFiltern(etEssensplanSuche.getText().toString());
                    }
                    @Override
                    public void beiFehler(Exception exception) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        Toast.makeText(EssensplanUebersichtActivity.this, "Die Essenspläne konnten nicht geladen werden.", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    // Essenspläne anhand ihres Zeitraums filtern
    private void essensplaeneFiltern(String suchtext) {
        String suchbegriff = suchtext.trim().toLowerCase(Locale.GERMANY);
        List<Essensplan> gefilterteEssensplaene = new ArrayList<>();
        for (Essensplan essensplan : alleEssensplaene) {
            String startDatum = essensplan.getStartDatum();
            String endDatum = essensplan.getEndDatum();
            String formatierterZeitraum = EssensplanAdapter.zeitraumFormatieren(essensplan);
            String suchgrundlage = (startDatum + " " + endDatum + " " + formatierterZeitraum).toLowerCase(Locale.GERMANY);
            if (suchgrundlage.contains(suchbegriff)) {
                gefilterteEssensplaene.add(essensplan);
            }
        }
        essensplanAdapter.essensplaeneAktualisieren(gefilterteEssensplaene);
    }

    // Navigation zurück zur Startseite
    private void navigationEinrichten() {
        findViewById(R.id.btnEssensplanStartseite).setOnClickListener(v -> finish());
        findViewById(R.id.btnEssensplanUebersichtZurueck).setOnClickListener(v -> finish());
        findViewById(R.id.btnEssensplanHinzufuegen).setOnClickListener(v -> {
                    Intent intent = new Intent(EssensplanUebersichtActivity.this, EssensplanErstellenActivity.class);startActivity(intent);});
    }

    // Berücksichtigung der Systemleisten bei Edge-to-Edge
    private void fenstereinschuebeEinrichten() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.essensplanUebersicht), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (essensplanController != null) {
            essensplaeneLaden();
        }
    }

    // Ressourcen beim Zerstören der Activity freigeben
    @Override
    protected void onDestroy() {
        if (datenbankExecutor != null) {
            datenbankExecutor.shutdown();
        }
        super.onDestroy();
    }
}
