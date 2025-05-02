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

    private final java.text.NumberFormat rsdFormat = java.text.NumberFormat.getNumberInstance(new java.util.Locale("sr", "RS"));
    public KandidatDetaljiForm(Kandidat kandidat) {
        if (kandidat == null) return;

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Detalji kandidata - " + kandidat.getIme() + " " + kandidat.getPrezime());

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-font-size: 16px;");

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
                new Label("Cena teorijske obuke: " + rsdFormat.format(kandidat.getCenaTeorija()) + " RSD"),
                new Label("Cena praktične obuke: " + rsdFormat.format(kandidat.getCenaPraksa()) + " RSD"),
                new Label("Plaćeno: " + rsdFormat.format(kandidat.getPlaceno()) + " RSD"),
                new Label("Preostalo: " + rsdFormat.format(kandidat.getPreostalo()) + " RSD"),
                new Label(""),
                new Label("Uplate kandidata:")
        );

        List<Uplata> uplate = Database.vratiUplateZaKandidata(kandidat.getId());
        if (uplate.isEmpty()) {
            layout.getChildren().add(new Label("- Nema zabeleženih uplata."));
        } else {
            for (Uplata u : uplate) {
                layout.getChildren().add(
                        new Label("• " + u.getDatum().format(format) + " - " + rsdFormat.format(u.getIznos()) + " RSD")
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
        koren.setStyle("-fx-font-size: 16px;");

        stage.setScene(new Scene(koren));
        stage.showAndWait();
    }
}
