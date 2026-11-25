package de.mindfeat.app.view.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.color.MaterialColors;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.mindfeat.app.MainActivity;
import de.mindfeat.app.R;
import de.mindfeat.app.controller.RezeptController;
import de.mindfeat.app.model.datengrundlage.ZentraleDatenbank;
import de.mindfeat.app.model.datengrundlage.entitaeten.Kategorie;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;
import de.mindfeat.app.model.datengrundlage.entitaeten.RezeptZutat;
import de.mindfeat.app.model.datengrundlage.entitaeten.Zubereitungsschritt;
import de.mindfeat.app.model.datengrundlage.entitaeten.Zutat;
import de.mindfeat.app.model.vermittler.RezeptVermittler;
import de.mindfeat.app.view.helfer.BildHelfer;

public class RezeptDetailActivity extends AppCompatActivity {

    public static final String EXTRA_REZEPT_ID = "de.mindfeat.app.EXTRA_REZEPT_ID";
    private TextView tvRezeptTitel;
    private ImageView ivRezeptBild;
    private ChipGroup cgRezeptKategorien;
    private TextView tvRezeptPortionen;
    private LinearLayout llRezeptZutaten;
    private LinearLayout llRezeptZubereitung;
    private RezeptController rezeptController;
    private ExecutorService datenbankExecutor;
    private PopupWindow rezeptAktionsPopup;
    private ActivityResultLauncher<Intent> bearbeitenLauncher;
    private String aktuellerBildPfad;
    private boolean rezeptDetailsGeladen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bearbeitenLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), ergebnis -> {
                    if (ergebnis.getResultCode() == RESULT_OK) {rezeptLaden();}});
        setContentView(R.layout.activity_rezept_detail);
        ansichtenInitialisieren();
        navigationEinrichten();
        fenstereinschuebeEinrichten();
        controllerEinrichten();
        rezeptLaden();
        ImageButton btnRezeptAktionen = findViewById(R.id.btnRezeptAktionen);
        btnRezeptAktionen.setOnClickListener(v -> {
            if (rezeptAktionsPopup != null && rezeptAktionsPopup.isShowing()) {
                rezeptAktionsPopup.dismiss();
            } else {
                zeigeRezeptAktionsmenue(btnRezeptAktionen);
            }
        });
    }

    private void ansichtenInitialisieren() {
        tvRezeptTitel = findViewById(R.id.tvRezeptTitelDetail);
        ivRezeptBild = findViewById(R.id.ivRezeptBildDetail);
        cgRezeptKategorien = findViewById(R.id.cgRezeptKategorien);
        tvRezeptPortionen = findViewById(R.id.tvRezeptPortionen);
        llRezeptZutaten = findViewById(R.id.llRezeptZutaten);
        llRezeptZubereitung = findViewById(R.id.llRezeptZubereitung);
        if (llRezeptZubereitung != null) {
            llRezeptZubereitung.setClipChildren(false);
            llRezeptZubereitung.setClipToPadding(false);
        }
    }

    private void navigationEinrichten() {
        ImageButton btnZurueck = findViewById(R.id.btnRezeptDetailZurueck);
        ImageButton btnStartseite = findViewById(R.id.btnRezeptDetailStartseite);
        btnZurueck.setOnClickListener(v -> finish());
        btnStartseite.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void controllerEinrichten() {
        datenbankExecutor = Executors.newSingleThreadExecutor();
        ZentraleDatenbank datenbank = ZentraleDatenbank.getInstance(getApplicationContext());
        RezeptVermittler rezeptVermittler = new RezeptVermittler(datenbank);
        rezeptController = new RezeptController(rezeptVermittler, datenbankExecutor, this::runOnUiThread);
    }

    private void rezeptLaden() {
        int rezeptId = getIntent().getIntExtra(EXTRA_REZEPT_ID, -1);
        if (rezeptId < 1) {
            Toast.makeText(this, R.string.rezept_fehler_laden, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        rezeptController.nachIdMitDetailsAbrufen(rezeptId, new RezeptController.Callback<>() {
                    @Override
                    public void beiErfolg(RezeptVermittler.RezeptDetails details) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        if (details == null || details.getRezept() == null) {
                            Toast.makeText(RezeptDetailActivity.this, R.string.rezept_fehler_nicht_gefunden, Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                        rezeptAnzeigen(details);
                    }
                    @Override
                    public void beiFehler(Exception exception) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        Toast.makeText(RezeptDetailActivity.this, R.string.rezept_fehler_laden, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void rezeptAnzeigen(RezeptVermittler.RezeptDetails details) {
        Rezept rezept = details.getRezept();
        tvRezeptTitel.setText(rezept.getTitel());
        tvRezeptPortionen.setText(getString(R.string.rezept_detail_portionen, rezept.getPortionen()));
        aktuellerBildPfad = rezept.getBildPfad();
        rezeptBildLaden(aktuellerBildPfad);
        rezeptDetailsGeladen = true;
        cgRezeptKategorien.removeAllViews();
        for (Kategorie kategorie : details.getKategorien()) {
            Chip chip = new Chip(this);
            chip.setText(kategorie.getName());
            chip.setTextAppearance(R.style.TextAppearance_Mindfeat_Body);
            int primaerFarbe = MaterialColors.getColor(this, R.attr.colorPrimary, Color.WHITE);
            int textFarbe = MaterialColors.getColor(this, R.attr.colorOnPrimary, Color.BLACK);
            chip.setChipBackgroundColor(ColorStateList.valueOf(primaerFarbe));
            chip.setTextColor(textFarbe);
            chip.setChipStrokeWidth(0f);
            chip.setChipCornerRadius(dp(50));
            chip.setChipMinHeight(dp(36));
            chip.setChipStartPadding(dp(8));
            chip.setChipEndPadding(dp(8));
            chip.setEnsureMinTouchTargetSize(false);
            chip.setClickable(false);
            chip.setCheckable(false);
            cgRezeptKategorien.addView(chip);
        }
        Map<Integer, Zutat> zutatenNachId = new HashMap<>();
        for (Zutat zutat : details.getZutaten()) {
            zutatenNachId.put(zutat.getId(), zutat);
        }
        llRezeptZutaten.removeAllViews();
        DecimalFormat format = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.GERMANY));
        for (RezeptZutat rezeptZutat : details.getRezeptZutaten()) {
            Zutat zutat = zutatenNachId.get(rezeptZutat.getZutatId());
            if (zutat == null) {
                continue;
            }
            String menge = format.format(rezeptZutat.getMenge());
            String einheit = rezeptZutat.getEinheit() == null
                    ? ""
                    : rezeptZutat.getEinheit().trim();
            String mengenText = (menge + " " + einheit).trim();
            llRezeptZutaten.addView(zutatenZeileErstellen(mengenText, zutat.getName()));
        }
        if (llRezeptZutaten.getChildCount() == 0) {
            llRezeptZutaten.addView(textAnsichtErstellen(getString(R.string.rezept_detail_keine_zutaten)));
        }
        llRezeptZubereitung.removeAllViews();
        for (Zubereitungsschritt schritt : details.getZubereitungsschritte()) {
            llRezeptZubereitung.addView(schrittZeileErstellen(schritt.getReihenfolge(), schritt.getBeschreibung()));
        }
        if (llRezeptZubereitung.getChildCount() == 0) {
            llRezeptZubereitung.addView(textAnsichtErstellen(getString(R.string.rezept_detail_keine_schritte)));
        }
    }

    private TextView textAnsichtErstellen(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextAppearance(R.style.TextAppearance_Mindfeat_Body);
        int vertikalerAbstand = dp(8);
        textView.setPadding(0, vertikalerAbstand, 0, vertikalerAbstand);
        return textView;
    }

    private LinearLayout zutatenZeileErstellen(String menge, String zutatenName) {
        LinearLayout zeile = new LinearLayout(this);
        zeile.setOrientation(LinearLayout.HORIZONTAL);
        zeile.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams zeilenParameter = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        zeilenParameter.bottomMargin = dp(4);
        zeile.setLayoutParams(zeilenParameter);
        TextView mengenText = textAnsichtErstellen(menge);
        mengenText.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams mengenParameter = new LinearLayout.LayoutParams(dp(104), LinearLayout.LayoutParams.WRAP_CONTENT);
        mengenText.setLayoutParams(mengenParameter);
        TextView zutatenText = textAnsichtErstellen(zutatenName);
        LinearLayout.LayoutParams zutatenParameter = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        zutatenParameter.leftMargin = dp(12);
        zutatenText.setLayoutParams(zutatenParameter);
        zeile.addView(mengenText);
        zeile.addView(zutatenText);
        return zeile;
    }

    private LinearLayout schrittZeileErstellen(int nummer, String beschreibung) {
        LinearLayout zeile = new LinearLayout(this);
        zeile.setOrientation(LinearLayout.HORIZONTAL);
        zeile.setGravity(Gravity.TOP);
        zeile.setClipChildren(false);
        zeile.setClipToPadding(false);
        LinearLayout.LayoutParams zeilenParameter = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        zeilenParameter.bottomMargin = dp(16);
        zeile.setLayoutParams(zeilenParameter);
        FrameLayout nummerAnsicht = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.dynamische_buttons, zeile, false);
        TextView nummerText = nummerAnsicht.findViewById(R.id.tvSchrittNummer);
        nummerText.setText(String.valueOf(nummer));
        nummerText.getPaint().setFakeBoldText(true);
        LinearLayout.LayoutParams nummerParameter = new LinearLayout.LayoutParams(dp(44), dp(44));
        nummerParameter.setMargins(dp(2), dp(2), dp(2), dp(2));
        nummerAnsicht.setLayoutParams(nummerParameter);
        TextView beschreibungText = textAnsichtErstellen(beschreibung);
        beschreibungText.setGravity(Gravity.TOP);
        LinearLayout.LayoutParams beschreibungParameter = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        beschreibungParameter.leftMargin = dp(10);
        beschreibungText.setLayoutParams(beschreibungParameter);
        zeile.addView(nummerAnsicht);
        zeile.addView(beschreibungText);
        return zeile;
    }

    // Hilfsmethode
    private int dp(float wert) {
        return (int) (wert * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void rezeptBildLaden(String bildPfad) {
        ivRezeptBild.setImageResource(android.R.drawable.ic_menu_gallery);
        if (bildPfad == null || bildPfad.trim().isEmpty()) {
            return;
        }
        int breite = getResources().getDisplayMetrics().widthPixels;
        int hoehe = (int) (220 * getResources().getDisplayMetrics().density);
        datenbankExecutor.execute(() -> {
            Bitmap bitmap = BildHelfer.skaliertLaden(bildPfad, breite, hoehe);
            runOnUiThread(() -> {
                if (!isFinishing() && !isDestroyed() && bitmap != null) {
                    ivRezeptBild.setImageBitmap(bitmap);
                }
            });
        });
    }

    private void fenstereinschuebeEinrichten() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rezeptDetail), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                }
        );
    }

    @Override
    protected void onDestroy() {
        if (datenbankExecutor != null) {
            datenbankExecutor.shutdown();
        }
        super.onDestroy();
    }

    private void zeigeRezeptAktionsmenue(View anker) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_rezept_aktionen, null, false);
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(getDrawable(R.drawable.bg_popup_rezept_aktionen));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);
        float dichte = getResources().getDisplayMetrics().density;
        popupWindow.setElevation(8 * dichte);
        popupWindow.setOnDismissListener(() -> rezeptAktionsPopup = null);
        View bearbeiten = popupView.findViewById(R.id.zeileBearbeiten);
        bearbeiten.setOnClickListener(v -> {
            popupWindow.dismiss();
            int rezeptId = getIntent().getIntExtra(EXTRA_REZEPT_ID, -1);
            if (rezeptId < 1) {
                Toast.makeText(this, R.string.rezept_fehler_nicht_gefunden, Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(RezeptDetailActivity.this, RezeptErstellenActivity.class);
            intent.putExtra(EXTRA_REZEPT_ID, rezeptId);
            bearbeitenLauncher.launch(intent);
        });
        View loeschen = popupView.findViewById(R.id.zeileLoeschen);
        loeschen.setOnClickListener(v -> {
            popupWindow.dismiss();
            loescheRezept();
        });
        rezeptAktionsPopup = popupWindow;
        popupWindow.showAsDropDown(anker, 0, 0, Gravity.END);
    }

    // Rezept löschen
    private void loescheRezept() {
        if (!rezeptDetailsGeladen) {
            Toast.makeText(this, R.string.rezept_fehler_laden, Toast.LENGTH_LONG).show();
            return;
        }
        View loeschDialog = findViewById(R.id.layoutLoeschDialog);
        loeschDialog.setVisibility(View.VISIBLE);
        MaterialButton btnAbbrechen = loeschDialog.findViewById(R.id.btnAbbrechen);
        MaterialButton btnLoeschen = loeschDialog.findViewById(R.id.btnLoeschen);
        btnAbbrechen.setOnClickListener(v -> loeschDialog.setVisibility(View.GONE));
        btnLoeschen.setOnClickListener(v -> {
            loeschDialog.setVisibility(View.GONE);
            rezeptLoeschenAusfuehren();
        });
    }

    private void rezeptLoeschenAusfuehren() {
        int rezeptId = getIntent().getIntExtra(EXTRA_REZEPT_ID, -1);
        if (rezeptId < 1) {
            Toast.makeText(this, R.string.rezept_fehler_nicht_gefunden, Toast.LENGTH_LONG).show();
            return;
        }
        rezeptController.loeschen(rezeptId, new RezeptController.Callback<>() {
                    @Override
                    public void beiErfolg(Boolean erfolgreich) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        if (!Boolean.TRUE.equals(erfolgreich)) {
                            Toast.makeText(RezeptDetailActivity.this, R.string.rezept_fehler_loeschen, Toast.LENGTH_LONG).show();
                            return;
                        }
                        boolean bildEntfernt = rezeptBildEntfernen();
                        if (bildEntfernt) {
                            Toast.makeText(RezeptDetailActivity.this, R.string.rezept_erfolgreich_geloescht, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RezeptDetailActivity.this, R.string.rezept_bild_fehler_loeschen, Toast.LENGTH_LONG).show();
                        }
                        setResult(RESULT_OK);
                        finish();
                    }

            @Override
                    public void beiFehler(Exception exception) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        Toast.makeText(RezeptDetailActivity.this, R.string.rezept_fehler_loeschen, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private boolean rezeptBildEntfernen() {
        if (aktuellerBildPfad == null || aktuellerBildPfad.trim().isEmpty()) {
            return true;
        }
        try {
            File bildDatei = new File(aktuellerBildPfad);
            File rezeptBildOrdner = new File(getFilesDir(), "rezept_bilder");
            File kanonischerBildOrdner = rezeptBildOrdner.getCanonicalFile();
            File kanonischeBildDatei = bildDatei.getCanonicalFile();
            if (!kanonischerBildOrdner.equals(kanonischeBildDatei.getParentFile())) {
                return false;
            }
            if (!kanonischeBildDatei.exists()) {
                return true;
            }
            return kanonischeBildDatei.isFile() && kanonischeBildDatei.delete();
        } catch (java.io.IOException exception) {
            return false;
        }
    }
}
