package com.autoskola;

import javafx.geometry.Insets;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
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
                new Label("Cena teorijske obuke: " + FormatUtil.format(kandidat.getCenaTeorija()) + " RSD"),
                new Label("Cena praktične obuke: " + FormatUtil.format(kandidat.getCenaPraksa()) + " RSD"),
                new Label("Plaćeno: " + FormatUtil.format(kandidat.getPlaceno()) + " RSD"),
                new Label("Preostalo: " + FormatUtil.format(kandidat.getPreostalo()) + " RSD"),
                new Label(""),
                new Label("Uplate kandidata:")
        );

        List<Uplata> uplate = Database.vratiUplateZaKandidata(kandidat.getId());
        if (uplate.isEmpty()) {
            layout.getChildren().add(new Label("- Nema zabeleženih uplata."));
        } else {
            for (Uplata u : uplate) {
                layout.getChildren().add(
                        new Label("• " + u.getDatum().format(format) + " - " + FormatUtil.format(u.getIznos()) + " RSD")
                );
            }
        }

        ScrollPane scroll = new ScrollPane(layout);
        scroll.setFitToWidth(true);
        scroll.setPrefSize(500, 600);

        Button stampajBtn = new Button("Štampaj detalje");
        stampajBtn.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(stage)) {
                boolean uspeh = job.printPage(layout);
                if (uspeh) {
                    job.endJob();
                }
            }
        });

        Button ugovorBtn = new Button("Otvori ugovor");
        ugovorBtn.setOnAction(e -> {
            try {
                String sablon = "sabloni/ugovor_" + kandidat.getKategorija() + ".docx";
                String izlaz = "ugovori/ugovor-" + kandidat.getIdKandidata() + ".docx";

                // Kreiraj folder ako ne postoji
                File izlazFajl = new File(izlaz);
                File parentDir = izlazFajl.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }

                UgovorGenerator.generisiUgovor(kandidat, sablon, izlaz);
                Desktop.getDesktop().open(izlazFajl);

            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Greška");
                alert.setHeaderText("Nije moguće otvoriti ugovor");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        });

        HBox dugmici = new HBox(10, stampajBtn, ugovorBtn);
        dugmici.setPadding(new Insets(10, 0, 0, 0));

        VBox koren = new VBox(10, scroll, dugmici);
        koren.setPadding(new Insets(10));
        koren.setStyle("-fx-font-size: 16px;");

        stage.setScene(new Scene(koren));
        stage.showAndWait();
    }
}
