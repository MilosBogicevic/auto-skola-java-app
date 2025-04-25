package com.autoskola;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class KandidatDetaljiForm {

    public KandidatDetaljiForm(Kandidat kandidat) {
        if (kandidat == null) return; // zaštita od null

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Detalji kandidata - " + safe(kandidat.getIme()) + " " + safe(kandidat.getPrezime()));

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

        layout.getChildren().addAll(
                new Label("ID kandidata: " + kandidat.getIdKandidata()),
                new Label("Ime i prezime: " + safe(kandidat.getIme()) + " " + safe(kandidat.getPrezime())),
                new Label("JMBG: " + safe(kandidat.getJmbg())),
                new Label("Telefon: " + safe(kandidat.getTelefon())),
                new Label("Email: " + (isEmpty(kandidat.getEmail()) ? "nema" : kandidat.getEmail())),
                new Label("Kategorija: " + safe(kandidat.getKategorija())),
                new Label("Datum upisa: " + (kandidat.getDatumUpisa() != null ? kandidat.getDatumUpisa().format(format) : "N/A")),
                new Label("Položio teoriju: " + (kandidat.isPolozioTeoriju() ? "DA" : "NE")),
                new Label("Položio vožnju: " + (kandidat.isPolozioVoznju() ? "DA" : "NE")),
                new Label("Cena teorije: " + formatRSD(kandidat.getCenaTeorija())),
                new Label("Cena prakse: " + formatRSD(kandidat.getCenaPraksa())),
                new Label("Plaćeno: " + formatRSD(kandidat.getPlaceno())),
                new Label("Preostalo: " + formatRSD(kandidat.getPreostalo())),
                new Label(""),
                new Label("Uplate kandidata:")
        );

        List<Uplata> uplate = Database.vratiUplateZaKandidata(kandidat.getId());
        if (uplate == null || uplate.isEmpty()) {
            layout.getChildren().add(new Label("- Nema zabeleženih uplata"));
        } else {
            for (Uplata u : uplate) {
                layout.getChildren().add(
                        new Label("• " + u.getDatum().format(format) + " - " + formatRSD(u.getIznos()))
                );
            }
        }

        ScrollPane scroll = new ScrollPane(layout);
        scroll.setFitToWidth(true);
        scroll.setPrefSize(500, 600);

        stage.setScene(new Scene(scroll));
        stage.showAndWait();
    }

    private String formatRSD(double iznos) {
        return String.format("%,.0f RSD", iznos);
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
