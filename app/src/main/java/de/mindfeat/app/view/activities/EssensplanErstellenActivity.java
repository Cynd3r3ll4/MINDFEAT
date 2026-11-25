package de.mindfeat.app.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import de.mindfeat.app.MainActivity;
import de.mindfeat.app.R;

public class EssensplanErstellenActivity extends AppCompatActivity {

    private EditText tvStartDatum;
    private EditText tvEndDatum;
    private TextView tvDauer;
    private LinearLayout layoutTagesEintraege;
    private LocalDate startDatum;
    private LocalDate endDatum;
    private final DateTimeFormatter datumsFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMANY);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_essensplan_erstellen);
        ansichtenInitialisieren();
        datumsEingabeEinrichten(tvStartDatum, true);
        datumsEingabeEinrichten(tvEndDatum, false);
        klickaktionenEinrichten();
        fenstereinschuebeEinrichten();
    }

    // Verknüpfung der UI-Elemente
    private void ansichtenInitialisieren() {
        tvStartDatum = findViewById(R.id.tvEssensplanStartDatum);
        tvEndDatum = findViewById(R.id.tvEssensplanEndDatum);
        tvDauer = findViewById(R.id.tvEssensplanDauer);
        layoutTagesEintraege = findViewById(R.id.layoutEssensplanTage);
        if (layoutTagesEintraege != null) {
            layoutTagesEintraege.setClipChildren(false);
            layoutTagesEintraege.setClipToPadding(false);
        }
    }

    // Klickaktionen der statischen UI-Elemente
    private void klickaktionenEinrichten() {
        findViewById(R.id.btnStartDatumWaehlen).setOnClickListener(v -> datumWaehlen(true));
        findViewById(R.id.btnEndDatumWaehlen).setOnClickListener(v -> datumWaehlen(false));
        findViewById(R.id.btnEssensplanErstellenZurueck).setOnClickListener(v -> finish());
        findViewById(R.id.btnEssensplanErstellenStartseite).setOnClickListener(v -> {
                    Intent intent = new Intent(EssensplanErstellenActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                });
        findViewById(R.id.btnEssensplanAbbrechen).setOnClickListener(v -> finish());
        findViewById(R.id.btnEssensplanSpeichern).setOnClickListener(v ->
                        Toast.makeText(this, "Die Speicherfunktion ergänzen wir im nächsten Schritt.", Toast.LENGTH_SHORT).show());
    }

    // Ermöglicht die Datumseingabe mit automatischen Punkten
    private void datumsEingabeEinrichten(EditText feld, boolean startdatum) {
        feld.addTextChangedListener(new TextWatcher() {
            private boolean bearbeitungLaeuft = false;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                if (bearbeitungLaeuft) {
                    return;
                }
                bearbeitungLaeuft = true;
                String zahlen = editable.toString().replaceAll("[^0-9]", "");
                if (zahlen.length() > 8) {
                    zahlen = zahlen.substring(0, 8);
                }
                StringBuilder formatiert = new StringBuilder();
                for (int i = 0; i < zahlen.length(); i++) {
                    if (i == 2 || i == 4) {
                        formatiert.append(".");
                    }
                    formatiert.append(zahlen.charAt(i));
                }
                String neuerText = formatiert.toString();
                if (!editable.toString().equals(neuerText)) {
                    feld.setText(neuerText);
                    feld.setSelection(neuerText.length());
                }
                bearbeitungLaeuft = false;
                LocalDate eingegebenesDatum = null;
                if (zahlen.length() == 8) {
                    try {
                        eingegebenesDatum = LocalDate.parse(formatiert.toString(), datumsFormat);
                    } catch (Exception e) {
                        eingegebenesDatum = null;
                    }
                }
                if (startdatum) {
                    startDatum = eingegebenesDatum;
                    if (startDatum != null && endDatum != null && startDatum.isAfter(endDatum)) {
                        endDatum = null;
                        tvEndDatum.setText("");
                    }
                } else {
                    endDatum = eingegebenesDatum;
                    if (endDatum != null && startDatum != null && endDatum.isBefore(startDatum)) {
                        endDatum = null;
                        feld.setError("Das Enddatum darf nicht vor dem Startdatum liegen.");
                    } else {
                        feld.setError(null);
                    }
                }
                zeitraumAktualisieren();
            }
        });
    }

    // Öffnet den Datumsauswahldialog für Start- oder Enddatum
    private void datumWaehlen(boolean startdatumWaehlen) {
        LocalDate vorbelegung;
        if (startdatumWaehlen) {
            vorbelegung = startDatum != null ? startDatum : LocalDate.now();
        } else {
            vorbelegung = endDatum != null ? endDatum : (startDatum != null ? startDatum : LocalDate.now());
        }
        long auswahlMillis = vorbelegung.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        if (!startdatumWaehlen && startDatum != null) {
            long minDatumMillis = startDatum.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
            constraintsBuilder.setStart(minDatumMillis);
            constraintsBuilder.setValidator(DateValidatorPointForward.from(minDatumMillis));
        }
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setSelection(auswahlMillis);
        builder.setCalendarConstraints(constraintsBuilder.build());
        builder.setTitleText(startdatumWaehlen ? "Startdatum auswählen" : "Enddatum auswählen");
        builder.setTheme(R.style.ThemeOverlay_Mindfeat_MaterialCalendar);
        MaterialDatePicker<Long> datePicker = builder.build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            LocalDate ausgewaehltesDatum = Instant.ofEpochMilli(selection).atZone(ZoneOffset.UTC).toLocalDate();
            if (startdatumWaehlen) {
                startDatum = ausgewaehltesDatum;
                tvStartDatum.setText(startDatum.format(datumsFormat));
                if (endDatum != null && endDatum.isBefore(startDatum)) {
                    endDatum = null;
                    tvEndDatum.setText("");
                }
            } else {
                if (startDatum != null && ausgewaehltesDatum.isBefore(startDatum)) {
                    Toast.makeText(this, "Das Enddatum darf nicht vor dem Startdatum liegen.", Toast.LENGTH_SHORT).show();
                    return;
                }
                endDatum = ausgewaehltesDatum;
                tvEndDatum.setText(endDatum.format(datumsFormat));
            }
            zeitraumAktualisieren();
        });
        datePicker.show(getSupportFragmentManager(), "MINDFEAT_DATE_PICKER");
    }

    // Aktualisiert Dauer und Tagesliste nach einer Datumsänderung
    private void zeitraumAktualisieren() {
        if (startDatum == null || endDatum == null) {
            tvDauer.setText("");
            layoutTagesEintraege.removeAllViews();
            return;
        }
        long anzahlTage = ChronoUnit.DAYS.between(startDatum, endDatum) + 1;
        tvDauer.setText(getResources().getQuantityString(
                R.plurals.essensplan_dauer_tage,
                (int) anzahlTage,
                anzahlTage));
        tagesEintraegeErstellen();
    }

    // Erzeugt für jeden Tag des Zeitraums eine Zeile
    private void tagesEintraegeErstellen() {
        layoutTagesEintraege.removeAllViews();
        if (startDatum == null || endDatum == null) {
            return;
        }
        LocalDate datum = startDatum;
        while (!datum.isAfter(endDatum)) {
            tagesEintragHinzufuegen(datum);
            datum = datum.plusDays(1);
        }
    }

    // Erstellt eine einzelne Tageszeile
    private void tagesEintragHinzufuegen(LocalDate datum) {
        LinearLayout zeile = new LinearLayout(this);
        zeile.setOrientation(LinearLayout.HORIZONTAL);
        zeile.setGravity(Gravity.CENTER_VERTICAL);
        zeile.setPadding(0, dpZuPixel(6), 0, dpZuPixel(6));
        zeile.setClipChildren(false);
        zeile.setClipToPadding(false);
        LinearLayout.LayoutParams zeilenParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        zeile.setLayoutParams(zeilenParams);
        String kuerzel;
        switch (datum.getDayOfWeek()) {
            case MONDAY: kuerzel = "Mo";
                break;
            case TUESDAY: kuerzel = "Di";
                break;
            case WEDNESDAY: kuerzel = "Mi";
                break;
            case THURSDAY: kuerzel = "Do";
                break;
            case FRIDAY: kuerzel = "Fr";
                break;
            case SATURDAY: kuerzel = "Sa";
                break;
            case SUNDAY: kuerzel = "So";
                break;
            default: kuerzel = "";
                break;
        }
        FrameLayout tagAnsicht = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.dynamische_buttons, zeile, false);
        tagAnsicht.setForegroundGravity(Gravity.CENTER);
        tagAnsicht.setForeground(null);
        tagAnsicht.addView(erstelleTagText(kuerzel));
        LinearLayout.LayoutParams tagParams = new LinearLayout.LayoutParams(dpZuPixel(44), dpZuPixel(44));
        tagParams.setMargins(0, 0, dpZuPixel(12), 0);
        tagAnsicht.setLayoutParams(tagParams);
        EditText rezeptEingabefeld = new EditText(this, null, 0, R.style.Widget_Mindfeat_FeldEingabe);
        rezeptEingabefeld.setText(R.string.rezept_auswaehlen);
        rezeptEingabefeld.setSingleLine(true);
        rezeptEingabefeld.setFocusable(false);
        rezeptEingabefeld.setClickable(true);
        rezeptEingabefeld.setCursorVisible(false);
        MaterialCardView rezeptFeld = new MaterialCardView(this);
        rezeptFeld.setRadius(dpZuPixel(10));
        rezeptFeld.setCardElevation(dpZuPixel(3));
        rezeptFeld.setStrokeWidth(0);
        rezeptFeld.setCardBackgroundColor(getColor(R.color.mindfeat_beige));
        rezeptFeld.addView(rezeptEingabefeld, new MaterialCardView.LayoutParams(MaterialCardView.LayoutParams.MATCH_PARENT, MaterialCardView.LayoutParams.MATCH_PARENT));
        LinearLayout.LayoutParams rezeptParams = new LinearLayout.LayoutParams(0, dpZuPixel(52), 1f);
        rezeptParams.setMargins(dpZuPixel(2), dpZuPixel(4), dpZuPixel(2), dpZuPixel(4));
        rezeptFeld.setLayoutParams(rezeptParams);
        zeile.addView(tagAnsicht);
        zeile.addView(rezeptFeld);
        layoutTagesEintraege.addView(zeile);
    }

    private TextView erstelleTagText(String kuerzel) {
        TextView tagText = new TextView(this);
        tagText.setText(kuerzel);
        tagText.setTextAppearance(R.style.TextAppearance_Mindfeat_DetailNummer);
        tagText.setGravity(Gravity.CENTER);
        tagText.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        return tagText;
    }

    private int dpZuPixel(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    // Berücksichtigung der Systemleisten bei Edge-to-Edge
    private void fenstereinschuebeEinrichten() {
        View root = findViewById(R.id.essensplanErstellen);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
