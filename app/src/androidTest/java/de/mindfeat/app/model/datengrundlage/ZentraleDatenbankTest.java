package de.mindfeat.app.model.datengrundlage;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import de.mindfeat.app.model.datengrundlage.entitaeten.Einkaufslistenbeitrag;
import de.mindfeat.app.model.datengrundlage.entitaeten.Einkaufslistenelement;
import de.mindfeat.app.model.datengrundlage.entitaeten.Essensplan;
import de.mindfeat.app.model.datengrundlage.entitaeten.InhaltEssensplan;
import de.mindfeat.app.model.datengrundlage.entitaeten.Kategorie;
import de.mindfeat.app.model.datengrundlage.entitaeten.Rezept;
import de.mindfeat.app.model.datengrundlage.entitaeten.RezeptKategorie;
import de.mindfeat.app.model.datengrundlage.entitaeten.RezeptZutat;
import de.mindfeat.app.model.datengrundlage.entitaeten.Zubereitungsschritt;
import de.mindfeat.app.model.datengrundlage.entitaeten.Zutat;


@RunWith(AndroidJUnit4.class)

public class ZentraleDatenbankTest {

    private TestDatenbank testDatenbank;
    private ZentraleDatenbank datenbank;

    // Testdatenbank pro test erstellen
    @Before
    public void datenbankErstellen() {
        testDatenbank = new TestDatenbank();
        datenbank = testDatenbank.getDatenbank();
    }

    // Datenbank nach dem Test schließen
    @After
    public void datenbankSchliessen() {
        if (testDatenbank != null) {
            testDatenbank.schliessen();
        }
    }

    // Verfügbarkeit der zentralen DB und aller DAOs prüfen
    @Test
    public void datenbankErstellenUndAlleDAOsAbrufen() {
        assertNotNull(datenbank);
        assertNotNull(datenbank.rezeptDAO());
        assertNotNull(datenbank.zutatDAO());
        assertNotNull(datenbank.rezeptZutatDAO());
        assertNotNull(datenbank.kategorieDAO());
        assertNotNull(datenbank.rezeptKategorieDAO());
        assertNotNull(datenbank.zubereitungsschrittDAO());
        assertNotNull(datenbank.essensplanDAO());
        assertNotNull(datenbank.inhaltEssensplanDAO());
        assertNotNull(datenbank.einkaufslistenelementDAO());
        assertNotNull(datenbank.einkaufslistenbeitragDAO());
    }

    // Integrationstests

    // Zusammenspiel der Tabellen und DAOs beim Speichern und Abrufen eines vollständigen Beispielszenarios
    @Test
    public void vollstaendigerDatenflussMitZentralerDatenbank() {
        // Rezept anlegen
        Rezept rezept = new Rezept("Gulasch", null, 2);
        int rezeptId = (int) datenbank.rezeptDAO().einfuegen(rezept);

        // Zutaten anlegen
        Zutat paprika = new Zutat("Paprika");
        int paprikaId = (int) datenbank.zutatDAO().einfuegen(paprika);
        Zutat zwiebel = new Zutat("Zwiebeln");
        int zwiebelId = (int) datenbank.zutatDAO().einfuegen(zwiebel);

        // Mengen und EInheiten festlegen
        RezeptZutat rezeptPaprika = new RezeptZutat(rezeptId, paprikaId, 2.0, "Stück");
        datenbank.rezeptZutatDAO().einfuegen(rezeptPaprika);
        RezeptZutat rezeptZucchini = new RezeptZutat(rezeptId, zwiebelId, 6.0, "Stück");
        datenbank.rezeptZutatDAO().einfuegen(rezeptZucchini);

        // Kategorie anlegen und dem Rezept zuordnen
        Kategorie kategorie = new Kategorie("Vegetarisch");
        int kategorieId = (int) datenbank.kategorieDAO().einfuegen(kategorie);
        RezeptKategorie rezeptKategorie = new RezeptKategorie(rezeptId, kategorieId);
        datenbank.rezeptKategorieDAO().einfuegen(rezeptKategorie);

        // Zubereitungsschritte anlegen
        datenbank.zubereitungsschrittDAO().einfuegen(new Zubereitungsschritt(rezeptId, 1, "Zwiebeln und Paprika schneiden."));
        datenbank.zubereitungsschrittDAO().einfuegen(new Zubereitungsschritt(rezeptId, 2, "Zwiebeln im Topf anbraten."));

        // Essensplan anlegen und Rezept einem Datum zuordnen
        Essensplan essensplan = new Essensplan("Wochenplan", "2026-11-02", "2026-11-08", 1);
        int essensplanId = (int) datenbank.essensplanDAO().einfuegen(essensplan);
        datenbank.inhaltEssensplanDAO().einfuegen(new InhaltEssensplan(essensplanId, rezeptId, "2026-11-04", 2, 2));

        // Einkaufslistenelement anlegen
        Einkaufslistenelement einkaufslistenelement = new Einkaufslistenelement(paprikaId, 2.0, "Stück", false);
        int einkaufslistenelementId = (int)
                datenbank.einkaufslistenelementDAO().einfuegen(einkaufslistenelement);

        // Einkaufslistenbeitrag für den Essensplan anlegen
        Einkaufslistenbeitrag beitrag = new Einkaufslistenbeitrag(einkaufslistenelementId, essensplanId, 2.0);
        datenbank.einkaufslistenbeitragDAO().einfuegen(beitrag);

        // Prüfen, ob der Beitrag gespeichert wurde
        assertNotNull(datenbank.einkaufslistenbeitragDAO());

        // Prüfen, ob das Rezept korrekt gespeichert wurde
        Rezept gespeichertesRezept = datenbank.rezeptDAO().nachIdAbrufen(rezeptId);
        assertNotNull(gespeichertesRezept);
        assertEquals("Gulasch", gespeichertesRezept.getTitel());
        assertEquals(2, gespeichertesRezept.getPortionen());

        // Prüfen, ob beide Zutaten dem Rezept zugeordnet sind
        List<RezeptZutat> rezeptZutaten = datenbank.rezeptZutatDAO().nachRezeptIdAbrufen(rezeptId);
        assertEquals(2, rezeptZutaten.size());

        // Prüfen, ob die Kategoriezuordnung vorhanden ist
        List<RezeptKategorie> rezeptKategorien = datenbank.rezeptKategorieDAO().nachRezeptIdAbrufen(rezeptId);
        assertEquals(1, rezeptKategorien.size());
        assertEquals(kategorieId, rezeptKategorien.get(0).getKategorieId());

        // Prüfen, ob die Zubereitungsschritte in der richtigen Reihenfolge vorliegen
        List<Zubereitungsschritt> schritte = datenbank.zubereitungsschrittDAO().nachRezeptIdAbrufen(rezeptId);
        assertEquals(2, schritte.size());
        assertEquals(1, schritte.get(0).getReihenfolge());
        assertEquals(2, schritte.get(1).getReihenfolge());

        // Prüfen, ob das Rezept im Essensplan eingetragen ist
        List<InhaltEssensplan> planeintraege = datenbank.inhaltEssensplanDAO().nachEssensplanIdAbrufen(essensplanId);
        assertEquals(1, planeintraege.size());
        assertEquals(rezeptId, planeintraege.get(0).getRezeptId());

        // Prüfen, ob das Einkaufslistenelement gespeichert wurde
        List<Einkaufslistenelement> einkaufsliste = datenbank.einkaufslistenelementDAO().nachZutatIdAbrufen(paprikaId);
        assertEquals(1, einkaufsliste.size());
        assertEquals(paprikaId, einkaufsliste.get(0).getZutatId());
        assertEquals(false, einkaufsliste.get(0).isErledigt());
    }

    // Fremdschlüssel und CASCADE-Löschregeln über die zentrale Datenbank prüfen
    @Test
    public void loeschenVonRezeptUndZutatEntferntAbhaengigeEintraege() {

        // Ausgangsdaten anlegen
        Rezept rezept = new Rezept("Nudelsalat", null, 4);
        int rezeptId = (int) datenbank.rezeptDAO().einfuegen(rezept);
        Zutat zutat = new Zutat("Nudeln");
        int zutatId = (int) datenbank.zutatDAO().einfuegen(zutat);
        datenbank.rezeptZutatDAO().einfuegen(new RezeptZutat(rezeptId, zutatId, 300.0, "g"));
        Kategorie kategorie = new Kategorie("Grillen");
        int kategorieId = (int) datenbank.kategorieDAO().einfuegen(kategorie);
        datenbank.rezeptKategorieDAO().einfuegen(new RezeptKategorie(rezeptId, kategorieId));
        datenbank.zubereitungsschrittDAO().einfuegen(new Zubereitungsschritt(rezeptId, 1, "Nudeln kochen."));
        Essensplan essensplan = new Essensplan("Wochenplan", "2026-11-02", "2026-11-08", 1);
        int essensplanId = (int) datenbank.essensplanDAO().einfuegen(essensplan);
        int planeintragId = (int) datenbank.inhaltEssensplanDAO().einfuegen(new InhaltEssensplan(essensplanId, rezeptId, "2026-11-04", 2, 2));
        int einkaufslistenId = (int) datenbank.einkaufslistenelementDAO().einfuegen(new Einkaufslistenelement(zutatId, 300.0, "g", false));

        // Rezept löschen (abhängige Rezept-Zuordnungen und Planeinträge automatisch entfernen)
        Rezept gespeichertesRezept = datenbank.rezeptDAO().nachIdAbrufen(rezeptId);
        datenbank.rezeptDAO().loeschen(gespeichertesRezept);
        assertNull(datenbank.rezeptDAO().nachIdAbrufen(rezeptId));
        assertEquals(0, datenbank.rezeptZutatDAO().nachRezeptIdAbrufen(rezeptId).size());
        assertEquals(0, datenbank.rezeptKategorieDAO().nachRezeptIdAbrufen(rezeptId).size());
        assertEquals(0, datenbank.zubereitungsschrittDAO().nachRezeptIdAbrufen(rezeptId).size());
        assertNull(datenbank.inhaltEssensplanDAO().nachIdAbrufen(planeintragId));

        // Zutat und Einkaufslisteneintrag bleiben bestehen (nicht vom gelöschten Rezept abhängig)
        assertNotNull(datenbank.zutatDAO().nachIdAbrufen(zutatId));
        assertNotNull(datenbank.einkaufslistenelementDAO().nachIdAbrufen(einkaufslistenId));

        // Einkaufslisteneintrag aufgrund des Fremdschlüssels ebenfalls entfernen
        Zutat gespeicherteZutat = datenbank.zutatDAO().nachIdAbrufen(zutatId);
        datenbank.zutatDAO().loeschen(gespeicherteZutat);
        assertNull(datenbank.zutatDAO().nachIdAbrufen(zutatId));
        assertNull(datenbank.einkaufslistenelementDAO().nachIdAbrufen(einkaufslistenId));
    }
}
