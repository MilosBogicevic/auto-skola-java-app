package com.autoskola;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ObavestenjaHelper {

    private static final DateTimeFormatter srpskiFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

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
            LocalDate registracijaIstice = v.getRegistracijaIstice().plusYears(1);
            dodajUpozorenje(box, "Registracija", v.getTablice(), registracijaIstice, danas);

            dodajTehnickiUpozorenje(box, v.getTablice(), v.getTehnickiIstice(), danas);
        }
    }

    private static void dodajUpozorenje(VBox box, String tip, String ime, LocalDate datumIsteka, LocalDate danas) {
        long dana = ChronoUnit.DAYS.between(danas, datumIsteka);

        if (tip.equals("Registracija")) {
            if (dana < 0) {
                Label l = new Label("❌ " + tip + " istekla za " + ime + " (" + datumIsteka.format(srpskiFormat) + ")");
                l.setStyle("-fx-text-fill: red;");
                box.getChildren().add(l);
            } else if (dana <= 7) {
                Label l = new Label("⚠ " + tip + " uskoro ističe za " + ime + " (" + datumIsteka.format(srpskiFormat) + ")");
                l.setStyle("-fx-text-fill: orange;");
                box.getChildren().add(l);
            }
            return;
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
        } else if (dana <= prag) {
            Label l = new Label("⚠ " + tip + " uskoro ističe za " + ime + " (" + datumIsteka.format(srpskiFormat) + ")");
            l.setStyle("-fx-text-fill: orange;");
            box.getChildren().add(l);
        }
    }


    private static void dodajTehnickiUpozorenje(VBox box, String tablice, LocalDate datumTehnickog, LocalDate danas) {
        long proslo = ChronoUnit.DAYS.between(datumTehnickog, danas);
        LocalDate datumIsteka = datumTehnickog.plusDays(180);

        if (proslo >= 181) {
            Label l = new Label("❌ Tehnički istekao za " + tablice + " (istekao: " + datumIsteka.format(srpskiFormat) + ")");
            l.setStyle("-fx-text-fill: red;");
            box.getChildren().add(l);
        } else if (proslo >= 171) {
            Label l = new Label("⚠ Tehnički uskoro ističe za " + tablice + " (ističe: " + datumIsteka.format(srpskiFormat) + ")");
            l.setStyle("-fx-text-fill: orange;");
            box.getChildren().add(l);
        }
    }
}
