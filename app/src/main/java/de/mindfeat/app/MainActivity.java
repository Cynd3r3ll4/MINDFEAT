package de.mindfeat.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import de.mindfeat.app.view.activities.EinkaufslisteActivity;
import de.mindfeat.app.view.activities.EssensplanUebersichtActivity;
import de.mindfeat.app.view.activities.RezeptUebersichtActivity;
import de.mindfeat.app.view.activities.StatistikActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        navigationEinrichten();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void navigationEinrichten() {
        findViewById(R.id.cardRezepte).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RezeptUebersichtActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.cardEssensplan).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EssensplanUebersichtActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.cardEinkaufsliste).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EinkaufslisteActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.btnStatistik).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StatistikActivity.class);
            startActivity(intent);
        });
    }
}