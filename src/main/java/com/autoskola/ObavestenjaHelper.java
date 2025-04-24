package com.autoskola;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ObavestenjaHelper {

    public static void prikaziObavestenjaInstruktora(VBox box) {
        List<Instruktor> svi = Database.vratiInstruktore();
        LocalDate danas = LocalDate.now();

        for (Instruktor i : svi) {
            dodajUpozorenje(box, "Lekarski", i.getIme(), i.getLekarskiIstice(), danas);
            dodajUpozorenje(box, "Vozačka", i.getIme(), i.getVozackaIstice(), danas);
            dodajUpozorenje(box, "Licenca", i.getIme(), i.getLicencaIstice(), danas);
        }
    }

    public static void prikaziObavestenjaVozila(VBox box) {
        List<Vozilo> vozila = Database.vratiVozila();
        LocalDate danas = LocalDate.now();

        for (Vozilo v : vozila) {
            dodajUpozorenje(box, "Registracija", v.getTablice(), v.getRegistracijaIstice(), danas);
            dodajUpozorenje(box, "Tehnički", v.getTablice(), v.getTehnickiIstice(), danas);
        }
    }

    private static void dodajUpozorenje(VBox box, String tip, String ime, LocalDate datumIsteka, LocalDate danas) {
        long dana = ChronoUnit.DAYS.between(danas, datumIsteka);

        if (dana < 0) {
            Label l = new Label("❌ " + tip + " istekla za " + ime + " (" + datumIsteka + ")");
            l.setStyle("-fx-text-fill: red;");
            box.getChildren().add(l);
        } else if (dana <= 7) {
            Label l = new Label("⚠ " + tip + " uskoro ističe za " + ime + " (" + datumIsteka + ")");
            l.setStyle("-fx-text-fill: orange;");
            box.getChildren().add(l);
        }
    }
}
