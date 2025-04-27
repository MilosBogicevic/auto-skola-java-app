package com.autoskola;

import javafx.geometry.Insets;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class KandidatDetaljiForm {

    public KandidatDetaljiForm(Kandidat kandidat) {
        if (kandidat == null) return;

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Detalji kandidata - " + kandidat.getIme() + " " + kandidat.getPrezime());

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

        layout.getChildren().addAll(
                new Label("ID broj kandidata: " + kandidat.getIdKandidata()),
                new Label("Ime i prezime: " + kandidat.getIme() + " " + kandidat.getPrezime()),
                new Label("Telefon: " + kandidat.getTelefon()),
                new Label("Email: " + (kandidat.getEmail().isEmpty() ? "nema" : kandidat.getEmail())),
                new Label("Kategorija: " + kandidat.getKategorija()),
                new Label("Datum upisa: " + kandidat.getDatumUpisa().format(format)),
                new Label("Položio teoriju: " + (kandidat.isPolozioTeoriju() ? "DA" : "NE")),
                new Label("Položio vožnju: " + (kandidat.isPolozioVoznju() ? "DA" : "NE")),
                new Label("Cena teorijske obuke: " + String.format("%,.0f RSD", kandidat.getCenaTeorija())),
                new Label("Cena praktične obuke: " + String.format("%,.0f RSD", kandidat.getCenaPraksa())),
                new Label("Plaćeno: " + String.format("%,.0f RSD", kandidat.getPlaceno())),
                new Label("Preostalo: " + String.format("%,.0f RSD", kandidat.getPreostalo())),
                new Label(""),
                new Label("Uplate kandidata:")
        );

        List<Uplata> uplate = Database.vratiUplateZaKandidata(kandidat.getId());
        if (uplate.isEmpty()) {
            layout.getChildren().add(new Label("- Nema zabeleženih uplata."));
        } else {
            for (Uplata u : uplate) {
                layout.getChildren().add(
                        new Label("• " + u.getDatum().format(format) + " - " + String.format("%,.0f RSD", u.getIznos()))
                );
            }
        }

        ScrollPane scroll = new ScrollPane(layout);
        scroll.setFitToWidth(true);
        scroll.setPrefSize(500, 600);

        Button stampajBtn = new Button("Štampaj");
        stampajBtn.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(stage)) {
                boolean uspeh = job.printPage(layout);
                if (uspeh) {
                    job.endJob();
                }
            }
        });

        VBox koren = new VBox(10, scroll, stampajBtn);
        koren.setPadding(new Insets(10));

        stage.setScene(new Scene(koren));
        stage.showAndWait();
    }
}
