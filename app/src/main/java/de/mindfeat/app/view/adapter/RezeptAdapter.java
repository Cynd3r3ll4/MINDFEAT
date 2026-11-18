package de.mindfeat.app.view.adapter;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.mindfeat.app.R;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;
import de.mindfeat.app.view.helfer.BildHelfer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RezeptAdapter extends RecyclerView.Adapter<RezeptAdapter.RezeptViewHolder> {

    private final List<Rezept> rezepte = new ArrayList<>();

    // Bilder im Hintergrund laden
    private final ExecutorService bildExecutor = Executors.newSingleThreadExecutor();

    // Ergebnisse auf dem UI-Thread anzeigen
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Einzelnen Listeneintrag erzeugen
    @NonNull
    @Override
    public RezeptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View ansicht = LayoutInflater.from(parent.getContext()).inflate(R.layout.rezepteintrag, parent, false);
        return new RezeptViewHolder(ansicht);
    }

    // Daten eines Rezepts in den Listeneintrag einsetzen
    @Override
    public void onBindViewHolder(@NonNull RezeptViewHolder holder, int position) {
        Rezept rezept = rezepte.get(position);
        holder.tvRezeptTitel.setText(rezept.getTitel());
        holder.ivRezeptBild.setImageResource(android.R.drawable.ic_menu_gallery);
        String bildPfad = rezept.getBildPfad();
        if (bildPfad == null || bildPfad.trim().isEmpty()) {
            holder.ivRezeptBild.setTag(null);
            return;
        }
        holder.ivRezeptBild.setTag(bildPfad);
        int zielBreite = holder.ivRezeptBild.getWidth();
        int zielHoehe = holder.ivRezeptBild.getHeight();
        if (zielBreite <= 0) {
            zielBreite = (int) (88 * holder.itemView.getResources().getDisplayMetrics().density);
        }
        if (zielHoehe <= 0) {
            zielHoehe = (int) (88 * holder.itemView.getResources().getDisplayMetrics().density);
        }
        final int breite = zielBreite;
        final int hoehe = zielHoehe;
        bildExecutor.execute(() -> {
            Bitmap bitmap = BildHelfer.skaliertLaden(bildPfad, breite, hoehe);
            mainHandler.post(() -> {
                if (Objects.equals(holder.ivRezeptBild.getTag(), bildPfad)) {
                    if (bitmap != null) {
                        holder.ivRezeptBild.setImageBitmap(bitmap);
                    }
                }
            });
        });
    }

    // Anzahl der anzuzeigenden Rezepte
    @Override
    public int getItemCount() {
        return rezepte.size();
    }

    // Neue Rezeptliste übernehmen
    public void rezepteAktualisieren(List<Rezept> neueRezepte) {
        rezepte.clear();
        if (neueRezepte != null) {
            rezepte.addAll(neueRezepte);
        }
        notifyDataSetChanged();
    }

    // Ressourcen freigeben
    public void schliessen() {
        bildExecutor.shutdownNow();
    }

    // ViewHolder hält die Ansichten eines Listeneintrags
    static class RezeptViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivRezeptBild;
        private final TextView tvRezeptTitel;

        RezeptViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRezeptBild = itemView.findViewById(R.id.ivRezeptBild);
            tvRezeptTitel = itemView.findViewById(R.id.tvRezeptTitel);
        }
    }
}
