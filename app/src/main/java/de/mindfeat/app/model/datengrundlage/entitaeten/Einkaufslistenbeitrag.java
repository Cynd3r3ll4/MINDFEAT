package de.mindfeat.app.model.datengrundlage.entitaeten;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "einkaufslistenbeitraege",
        foreignKeys = {
                @ForeignKey(
                        entity = Einkaufslistenelement.class,
                        parentColumns = "id",
                        childColumns = "einkaufslistenelementId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Essensplan.class,
                        parentColumns = "id",
                        childColumns = "essensplanId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = "einkaufslistenelementId"),
                @Index(value = "essensplanId"),
                @Index(
                        value = {"einkaufslistenelementId", "essensplanId"},
                        unique = true
                )
        }
)

public class Einkaufslistenbeitrag {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int einkaufslistenelementId;
    private Integer essensplanId; // wenn null = Beitrag manuell hinzugefügt, integer damit null möglich ist
    private Double menge;

    // Vollständiger Konstruktor für einen neuen Einkaufslistenbeitrag
    public Einkaufslistenbeitrag(int einkaufslistenelementId, Integer essensplanId, Double menge) {
        setEinkaufslistenelementId(einkaufslistenelementId);
        setEssensplanId(essensplanId);
        setMenge(menge);
    }

    // Getter und Setter

    // ID
    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("Die ID darf nicht negativ sein.");
        }
        this.id = id;
    }

    // Einkaufslistenelement-ID
    public int getEinkaufslistenelementId() {
        return einkaufslistenelementId;
    }

    public void setEinkaufslistenelementId(int einkaufslistenelementId) {
        if (einkaufslistenelementId < 1) {
            throw new IllegalArgumentException("Es muss ein Einkaufslistenelement zugeordnet sein.");
        }
        this.einkaufslistenelementId = einkaufslistenelementId;
    }

    // Essensplan-ID (null bei manuellen Beiträgen)
    public Integer getEssensplanId() {
        return essensplanId;
    }

    public void setEssensplanId(Integer essensplanId) {
        if (essensplanId != null && essensplanId < 1) {
            throw new IllegalArgumentException("Die Essensplan-ID muss positiv sein oder null betragen.");
        }
        this.essensplanId = essensplanId;
    }

    // Mengenanteil
    public Double getMenge() {
        return menge;
    }

    public void setMenge(Double menge) {
        if (menge != null
                && (!Double.isFinite(menge) || menge <= 0)) {
            throw new IllegalArgumentException("Bitte trage eine Menge ein.");
        }
        this.menge = menge;
    }
}
