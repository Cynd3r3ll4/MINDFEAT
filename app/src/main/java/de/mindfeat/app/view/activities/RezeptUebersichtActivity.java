package de.mindfeat.app.view.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.mindfeat.app.MainActivity;
import de.mindfeat.app.R;

public class RezeptUebersichtActivity extends AppCompatActivity {

    private RecyclerView rvRezepte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rezept_uebersicht);
        ansichtenInitialisieren();
        listeEinrichten();
        navigationEinrichten();
        fenstereinschuebeEinrichten();
    }

    // Verknüpfung der UI-Elemente
    private void ansichtenInitialisieren() {
        rvRezepte = findViewById(R.id.rvRezepte);
    }

    // Einrichtung der scrollbaren Rezeptliste
    private void listeEinrichten() {
        rvRezepte.setLayoutManager(new LinearLayoutManager(this));
        rvRezepte.setHasFixedSize(false);
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
}
