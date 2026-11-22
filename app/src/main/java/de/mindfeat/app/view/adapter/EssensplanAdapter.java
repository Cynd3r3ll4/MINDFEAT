package de.mindfeat.app.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.mindfeat.app.R;
import de.mindfeat.app.model.datengrundlage.entitaeten.Essensplan;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EssensplanAdapter extends RecyclerView.Adapter<EssensplanAdapter.EssensplanViewHolder> {

    private final List<Essensplan> essensplaene = new ArrayList<>();

    private static final DateTimeFormatter DATUM_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMANY);

    // Einzelnen Listeneintrag erzeugen
    @NonNull
    @Override
    public EssensplanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View ansicht = LayoutInflater.from(parent.getContext()).inflate(R.layout.essensplaneintrag, parent, false);
        return new EssensplanViewHolder(ansicht);
    }

    // Daten eines Essensplans in den Listeneintrag einsetzen
    @Override
    public void onBindViewHolder(@NonNull EssensplanViewHolder holder, int position) {
        Essensplan essensplan = essensplaene.get(position);
        holder.ivEssensplanBild.setImageResource(R.drawable.ic_essensplan_kalender);
        holder.tvEssensplanZeitraum.setText(zeitraumFormatieren(essensplan));
    }

    // Anzahl der anzuzeigenden Essenspläne
    @Override
    public int getItemCount() {
        return essensplaene.size();
    }

    // Neue Essensplanliste übernehmen
    public void essensplaeneAktualisieren(List<Essensplan> neueEssensplaene) {
        essensplaene.clear();
        if (neueEssensplaene != null) {
            essensplaene.addAll(neueEssensplaene);
        }
        notifyDataSetChanged();
    }

    // Zeitraum für die Anzeige formatieren
    public static String zeitraumFormatieren(Essensplan essensplan) {
        LocalDate startDatum = LocalDate.parse(essensplan.getStartDatum());
        LocalDate endDatum = LocalDate.parse(essensplan.getEndDatum());
        String start = startDatum.format(DATUM_FORMAT);
        String ende = endDatum.format(DATUM_FORMAT);
        if (startDatum.equals(endDatum)) {
            return start;
        }
        return start + " – " + ende;
    }

    // ViewHolder hält die Ansichten eines Listeneintrags
    static class EssensplanViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivEssensplanBild;
        private final TextView tvEssensplanZeitraum;
        EssensplanViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEssensplanBild = itemView.findViewById(R.id.ivEssensplanBild);
            tvEssensplanZeitraum = itemView.findViewById(R.id.tvEssensplanZeitraum);
        }
    }
}
