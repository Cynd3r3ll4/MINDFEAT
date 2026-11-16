package de.mindfeat.app.view.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.mindfeat.app.MainActivity;
import de.mindfeat.app.R;
import de.mindfeat.app.controller.RezeptController;
import de.mindfeat.app.model.datengrundlage.ZentraleDatenbank;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;
import de.mindfeat.app.model.vermittler.RezeptVermittler;

public class RezeptErstellenActivity extends AppCompatActivity {

    private EditText etTitel;
    private EditText etKategorien;
    private EditText etPortionen;
    private ImageView ivRezeptBild;
    private LinearLayout layoutZutaten;
    private LinearLayout layoutArbeitsschritte;
    private Button btnSpeichern;
    private final List<EditText> mengenFelder = new ArrayList<>();
    private final List<EditText> einheitenFelder = new ArrayList<>();
    private final List<EditText> zutatenFelder = new ArrayList<>();
    private final List<EditText> schrittFelder = new ArrayList<>();
    private RezeptController rezeptController;
    private ExecutorService datenbankExecutor;
    private Uri kameraUri;
    private String bildPfad;
    private ActivityResultLauncher<String> galerieLauncher;
    private ActivityResultLauncher<Uri> kameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rezept_erstellen);
        ansichtenInitialisieren();
        controllerEinrichten();
        bildLauncherEinrichten();
        eingabefelderEinrichten();
        klickaktionenEinrichten();
        fenstereinschuebeEinrichten();
    }

    private void ansichtenInitialisieren() {
        etTitel = findViewById(R.id.etRezeptTitel);
        etKategorien = findViewById(R.id.etRezeptKategorien);
        etPortionen = findViewById(R.id.etRezeptPortionen);
        ivRezeptBild = findViewById(R.id.ivRezeptVorschau);
        layoutZutaten = findViewById(R.id.layoutZutaten);
        layoutArbeitsschritte = findViewById(R.id.layoutArbeitsschritte);
        layoutZutaten.setClipChildren(false);
        layoutZutaten.setClipToPadding(false);
        layoutArbeitsschritte.setClipChildren(false);
        layoutArbeitsschritte.setClipToPadding(false);
        btnSpeichern = findViewById(R.id.btnRezeptSpeichern);
    }

    private void controllerEinrichten() {
        datenbankExecutor = Executors.newSingleThreadExecutor();
        ZentraleDatenbank datenbank = ZentraleDatenbank.getInstance(getApplicationContext());
        RezeptVermittler vermittler = new RezeptVermittler(datenbank);
        rezeptController = new RezeptController(vermittler, datenbankExecutor, this::runOnUiThread);
    }

    private void bildLauncherEinrichten() {
        galerieLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        bildSpeichern(uri);
                    }
                }
        );

        kameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), erfolgreich -> {
                    if (Boolean.TRUE.equals(erfolgreich) && kameraUri != null) {
                        bildSpeichern(kameraUri);
                    }
                }
        );
    }

    private void eingabefelderEinrichten() {
        zutatHinzufuegen();
        schrittHinzufuegen();
    }

    private void klickaktionenEinrichten() {
        findViewById(R.id.btnRezeptAbbrechen).setOnClickListener(v -> finish());
        findViewById(R.id.btnBildGalerie).setOnClickListener(v -> galerieLauncher.launch("image/*"));
        findViewById(R.id.btnBildKamera).setOnClickListener(v -> kameraOeffnen());
        findViewById(R.id.btnZutatHinzufuegen).setOnClickListener(v -> zutatHinzufuegen());
        findViewById(R.id.btnArbeitsschrittHinzufuegen).setOnClickListener(v -> schrittHinzufuegen());
        findViewById(R.id.btnRezeptErstellenZurueck).setOnClickListener(v -> finish());
        findViewById(R.id.btnRezeptErstellenStartseite).setOnClickListener(v -> {
            Intent intent = new Intent(RezeptErstellenActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();});
        btnSpeichern.setOnClickListener(v -> rezeptSpeichern());
    }

    // Eine neue Eingabezeile für eine Zutat erzeugen
    private void zutatHinzufuegen() {
        LinearLayout zeile = new LinearLayout(this);
        zeile.setOrientation(LinearLayout.HORIZONTAL);
        zeile.setGravity(Gravity.CENTER_VERTICAL);
        zeile.setClipChildren(false);
        zeile.setClipToPadding(false);

        EditText menge = neuesEingabefeld("Menge", android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText einheit = neuesEingabefeld("Einheit", android.text.InputType.TYPE_CLASS_TEXT);
        EditText zutat = neuesEingabefeld("Zutat", android.text.InputType.TYPE_CLASS_TEXT);

        LinearLayout.LayoutParams pMenge = new LinearLayout.LayoutParams(0, dp(48), 1.5f);
        pMenge.rightMargin = dp(8);
        LinearLayout.LayoutParams pEinheit = new LinearLayout.LayoutParams(0, dp(48), 1.5f);
        pEinheit.rightMargin = dp(8);
        LinearLayout.LayoutParams pZutat = new LinearLayout.LayoutParams(0, dp(48), 2.5f);

        zeile.addView(eingabefeldContainer(menge), pMenge);
        zeile.addView(eingabefeldContainer(einheit), pEinheit);
        zeile.addView(eingabefeldContainer(zutat), pZutat);

        LinearLayout.LayoutParams zeilenParameter = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        zeilenParameter.bottomMargin = dp(8);
        layoutZutaten.addView(zeile, zeilenParameter);
        mengenFelder.add(menge);
        einheitenFelder.add(einheit);
        zutatenFelder.add(zutat);
    }

    // Eine neue Eingabezeile für einen Arbeitsschritt erzeugen
    private void schrittHinzufuegen() {
        EditText schritt = neuesEingabefeld("Arbeitsschritt beschreiben", android.text.InputType.TYPE_CLASS_TEXT);
        MaterialCardView container = eingabefeldContainer(schritt, false);
        LinearLayout.LayoutParams parameter = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        parameter.topMargin = dp(8);
        parameter.bottomMargin = dp(8);
        layoutArbeitsschritte.addView(container, parameter);
        schrittFelder.add(schritt);
    }

    // Hilfsmethode: Eingabefeld errichten
    private EditText neuesEingabefeld(String hinweis, int inputType) {
        EditText feld = new EditText(this, null, 0, R.style.Widget_Mindfeat_FeldEingabe);
        feld.setHint(hinweis);
        feld.setInputType(inputType);
        feld.setSingleLine((inputType & android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE) == 0);
        return feld;
    }

    // Hilfsmethode: Eingabecontainer erstellen
    private MaterialCardView eingabefeldContainer(EditText feld) {
        return eingabefeldContainer(feld, false);
    }

    private MaterialCardView eingabefeldContainer(EditText feld, boolean wrapContent) {
        MaterialCardView container = new MaterialCardView(this);
        container.setRadius(dp(10));
        container.setCardElevation(dp(3));
        container.setStrokeWidth(0);
        int oberflaechenfarbe = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, Color.WHITE);
        container.setCardBackgroundColor(oberflaechenfarbe);
        int height = wrapContent ? MaterialCardView.LayoutParams.WRAP_CONTENT : MaterialCardView.LayoutParams.MATCH_PARENT;
        container.addView(feld, new MaterialCardView.LayoutParams(MaterialCardView.LayoutParams.MATCH_PARENT, height));
        return container;
    }

    private int dp(int wert) {
        return Math.round(wert * getResources().getDisplayMetrics().density);
    }

    // Kameraaufnahme in eine temporäre Datei schreiben
    private void kameraOeffnen() {
        try {
            File kameraDatei = File.createTempFile(
                    "mindfeat_kamera_",
                    ".jpg",
                    getExternalCacheDir()
            );
            kameraUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    kameraDatei
            );
            kameraLauncher.launch(kameraUri);
        } catch (Exception exception) {
            Toast.makeText(this, R.string.rezept_fehler_kamera, Toast.LENGTH_LONG).show();
        }
    }

    // Bild aus Galerie oder Kamera in den privaten App-Speicher kopieren
    private void bildSpeichern(Uri quelle) {
        File bildOrdner = new File(getFilesDir(), "rezept_bilder");
        if (!bildOrdner.exists() && !bildOrdner.mkdirs()) {
            Toast.makeText(this, R.string.rezept_fehler_bild, Toast.LENGTH_LONG).show();
            return;
        }
        File zielDatei = new File(bildOrdner, "rezept_" + System.currentTimeMillis() + ".jpg");
        try (InputStream eingabe = getContentResolver().openInputStream(quelle);
             OutputStream ausgabe = new FileOutputStream(zielDatei)) {
            if (eingabe == null) {
                throw new IllegalStateException("Bildquelle konnte nicht geöffnet werden.");
            }
            byte[] puffer = new byte[8192];
            int anzahl;
            while ((anzahl = eingabe.read(puffer)) != -1) {
                ausgabe.write(puffer, 0, anzahl);
            }
            bildPfad = zielDatei.getAbsolutePath();
            Bitmap bitmap = BitmapFactory.decodeFile(bildPfad);
            if (bitmap != null) {
                ivRezeptBild.setImageBitmap(bitmap);
                ivRezeptBild.setVisibility(View.VISIBLE);
            }
        } catch (Exception exception) {
            if (zielDatei.exists()) {
                // Eine unvollständige Datei nicht behalten
                //noinspection ResultOfMethodCallIgnored
                zielDatei.delete();
            }
            Toast.makeText(this, R.string.rezept_fehler_bild, Toast.LENGTH_LONG).show();
        }
    }

    // Alle Eingaben prüfen und an den Controller übergeben
    private void rezeptSpeichern() {
        String titel = etTitel.getText().toString().trim();
        if (titel.isEmpty()) {
            etTitel.setError(getString(R.string.rezept_fehler_titel));
            etTitel.requestFocus();
            return;
        }
        int portionen;
        try {
            portionen = Integer.parseInt(etPortionen.getText().toString().trim());
            if (portionen < 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            etPortionen.setError(getString(R.string.rezept_fehler_portionen));
            etPortionen.requestFocus();
            return;
        }
        List<RezeptVermittler.ZutatAngabe> zutaten = zutatenZusammenstellen();
        if (zutaten == null) {
            return;
        }
        List<String> kategorien = kategorienZusammenstellen();
        List<String> schritte = schritteZusammenstellen();
        Rezept rezept;
        try {
            rezept = new Rezept(titel, bildPfad, portionen);
        } catch (IllegalArgumentException exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        btnSpeichern.setEnabled(false);
        rezeptController.anlegen(rezept, zutaten, kategorien, schritte, new RezeptController.Callback<Integer>() {
                    @Override
                    public void beiErfolg(Integer rezeptId) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        Toast.makeText(RezeptErstellenActivity.this, R.string.rezept_erfolgreich_gespeichert, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                    @Override
                    public void beiFehler(Exception exception) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        btnSpeichern.setEnabled(true);
                        Toast.makeText(RezeptErstellenActivity.this, getString(R.string.rezept_fehler_speichern) + " " + exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private List<RezeptVermittler.ZutatAngabe> zutatenZusammenstellen() {
        List<RezeptVermittler.ZutatAngabe> zutaten = new ArrayList<>();
        for (int i = 0; i < zutatenFelder.size(); i++) {
            String mengeText = mengenFelder.get(i).getText().toString().trim();
            String einheit = einheitenFelder.get(i).getText().toString().trim();
            String name = zutatenFelder.get(i).getText().toString().trim();
            if (mengeText.isEmpty() && einheit.isEmpty() && name.isEmpty()) { // Vollständig leere Zeilen ignorieren
                continue;
            }
            if (mengeText.isEmpty() || einheit.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, R.string.rezept_fehler_zutat_unvollstaendig, Toast.LENGTH_LONG).show();
                return null;
            }
            try {
                double menge = Double.parseDouble(mengeText.replace(',', '.'));
                zutaten.add(new RezeptVermittler.ZutatAngabe(name, menge, einheit));
            } catch (IllegalArgumentException exception) {
                Toast.makeText(this, R.string.rezept_fehler_zutat_menge, Toast.LENGTH_LONG).show();
                return null;
            }
        }
        return zutaten;
    }

    private List<String> kategorienZusammenstellen() {
        List<String> kategorien = new ArrayList<>();
        String eingabe = etKategorien.getText().toString().trim();
        if (eingabe.isEmpty()) {
            return kategorien;
        }
        for (String name : eingabe.split(",")) {
            String bereinigt = name.trim();
            if (!bereinigt.isEmpty() && !kategorien.contains(bereinigt)) {
                kategorien.add(bereinigt);
            }
        }
        return kategorien;
    }

    private List<String> schritteZusammenstellen() {
        List<String> schritte = new ArrayList<>();
        for (EditText feld : schrittFelder) {
            String text = feld.getText().toString().trim();
            if (!text.isEmpty()) {
                schritte.add(text);
            }
        }
        return schritte;
    }

    private void fenstereinschuebeEinrichten() {
        View wurzel = findViewById(R.id.rezeptErstellen);
        ViewCompat.setOnApplyWindowInsetsListener(wurzel, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onDestroy() {
        if (datenbankExecutor != null) {
            datenbankExecutor.shutdown();
        }
        super.onDestroy();
    }
}
