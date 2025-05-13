package com.autoskola;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ObavestenjaHelper {

    private static final DateTimeFormatter srpskiFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    public static boolean prikaziObavestenjaInstruktora(VBox box) {
        List<Instruktor> svi = Database.vratiInstruktore();
        LocalDate danas = LocalDate.now();
        boolean ima = false;

        for (Instruktor i : svi) {
            if (dodajUpozorenje(box, "Lekarski", i.getIme(), i.getLekarskiIstice(), danas)) ima = true;
            if (dodajUpozorenje(box, "Vozačka", i.getIme(), i.getVozackaIstice(), danas)) ima = true;
            if (dodajUpozorenje(box, "Licenca", i.getIme(), i.getLicencaIstice(), danas)) ima = true;
        }

        return ima;
    }

    public static boolean prikaziObavestenjaVozila(VBox box) {
        List<Vozilo> vozila = Database.vratiVozila();
        LocalDate danas = LocalDate.now();
        boolean ima = false;

        for (Vozilo v : vozila) {
            LocalDate registracijaIstice = v.getRegistracijaIstice().plusYears(1);
            if (dodajUpozorenje(box, "Registracija", v.getTablice(), registracijaIstice, danas)) ima = true;
            if (dodajTehnickiUpozorenje(box, v.getTablice(), v.getTehnickiIstice(), danas)) ima = true;
        }

        return ima;
    }

    private static boolean dodajUpozorenje(VBox box, String tip, String ime, LocalDate datumIsteka, LocalDate danas) {
        long dana = ChronoUnit.DAYS.between(danas, datumIsteka);

        if (tip.equals("Registracija")) {
            if (dana < 0) {
                Label l = new Label("❌ " + tip + " istekla za " + ime + " (" + datumIsteka.format(srpskiFormat) + ")");
                l.setStyle("-fx-text-fill: red;");
                box.getChildren().add(l);
                return true;
            } else if (dana <= 7) {
                Label l = new Label("⚠ " + tip + " uskoro ističe za " + ime + " (" + datumIsteka.format(srpskiFormat) + ")");
                l.setStyle("-fx-text-fill: #CC7722;");
                box.getChildren().add(l);
                return true;
            }
            return false;
        }

        String glagol = switch (tip) {
            case "Vozačka", "Licenca" -> "istekla";
            default -> "istekao";
        };

        int prag = switch (tip) {
            case "Vozačka", "Licenca" -> 30;
            default -> 7;
        };

        if (dana < 0 || dana == 0) {
            Label l = new Label("❌ " + tip + " " + glagol + " za " + ime + " (" + datumIsteka.format(srpskiFormat) + ")");
            l.setStyle("-fx-text-fill: red;");
            box.getChildren().add(l);
            return true;
        } else if (dana <= prag) {
            Label l = new Label("⚠ " + tip + " uskoro ističe za " + ime + " (" + datumIsteka.format(srpskiFormat) + ")");
            l.setStyle("-fx-text-fill: #CC7722;");
            box.getChildren().add(l);
            return true;
        }

        return false;
    }

    private static boolean dodajTehnickiUpozorenje(VBox box, String tablice, LocalDate datumTehnickog, LocalDate danas) {
        long proslo = ChronoUnit.DAYS.between(datumTehnickog, danas);
        LocalDate datumIsteka = datumTehnickog.plusDays(180);

        if (proslo >= 180) {
            Label l = new Label("❌ Tehnički istekao za " + tablice + " (" + datumIsteka.format(srpskiFormat) + ")");
            l.setStyle("-fx-text-fill: red;");
            box.getChildren().add(l);
            return true;
        } else if (proslo >= 174) {
            Label l = new Label("⚠ Tehnički uskoro ističe za " + tablice + " (" + datumIsteka.format(srpskiFormat) + ")");
            l.setStyle("-fx-text-fill: #CC7722;");
            box.getChildren().add(l);
            return true;
        }

        return false;
    }
}
