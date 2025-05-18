package com.autoskola;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
                dodajLabel(box, "Registracija istekla za " + ime + " (" + datumIsteka.format(srpskiFormat) + ")", "red", "error.png");
                return true;
            } else if (dana <= 7) {
                dodajLabel(box, "Registracija uskoro ističe za " + ime + " (" + datumIsteka.format(srpskiFormat) + ")", "#CC7722", "warning.png");
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
            dodajLabel(box, tip + " " + glagol + " za " + ime + " (" + datumIsteka.format(srpskiFormat) + ")", "red", "error.png");
            return true;
        } else if (dana <= prag) {
            dodajLabel(box, tip + " uskoro ističe za " + ime + " (" + datumIsteka.format(srpskiFormat) + ")", "#CC7722", "warning.png");
            return true;
        }

        return false;
    }

    private static boolean dodajTehnickiUpozorenje(VBox box, String tablice, LocalDate datumTehnickog, LocalDate danas) {
        long proslo = ChronoUnit.DAYS.between(datumTehnickog, danas);
        LocalDate datumIsteka = datumTehnickog.plusDays(180);

        if (proslo >= 180) {
            dodajLabel(box, "Tehnički istekao za " + tablice + " (" + datumIsteka.format(srpskiFormat) + ")", "red", "error.png");
            return true;
        } else if (proslo >= 174) {
            dodajLabel(box, "Tehnički uskoro ističe za " + tablice + " (" + datumIsteka.format(srpskiFormat) + ")", "#CC7722", "warning.png");
            return true;
        }

        return false;
    }

    private static void dodajLabel(VBox box, String tekst, String boja, String ikonica) {
        Label label = new Label(tekst);
        label.setStyle("-fx-text-fill: " + boja);

        ImageView icon = new ImageView(new Image(ObavestenjaHelper.class.getResourceAsStream("/icons/" + ikonica)));
        icon.setFitWidth(20);
        icon.setFitHeight(20);
        label.setGraphic(icon);
        label.setGraphicTextGap(8);

        box.getChildren().add(label);
    }
}
