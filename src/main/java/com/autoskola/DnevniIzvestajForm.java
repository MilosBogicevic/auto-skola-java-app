package com.autoskola;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.print.PrinterJob;
import javafx.geometry.Insets;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DnevniIzvestajForm {

    private static final DateTimeFormatter srpskiFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    public DnevniIzvestajForm() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Dnevni izveštaj");

        DateTimeFormatter srpskiFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
        StringConverter<LocalDate> converter = new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? date.format(srpskiFormat) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, srpskiFormat) : null;
            }
        };

        DatePicker datumPicker = new DatePicker(LocalDate.now());
        datumPicker.setPromptText("Izaberite datum");
        datumPicker.setConverter(converter);

        Button stampaBtn = new Button("Štampaj");

        VBox prikazIzvestajaBox = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(prikazIzvestajaBox);
        scrollPane.setPadding(new Insets(10));
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(500, 500);

        datumPicker.setOnAction(e -> {
            LocalDate datum = datumPicker.getValue();
            List<Uplata> uplate = Database.vratiUplateZaDatum(datum);
            prikaziUplate(uplate, prikazIzvestajaBox, datum);
        });

        List<Uplata> uplateDanas = Database.vratiUplateZaDatum(LocalDate.now());
        prikaziUplate(uplateDanas, prikazIzvestajaBox, LocalDate.now());

        stampaBtn.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(stage)) {
                boolean uspeh = job.printPage(prikazIzvestajaBox);
                if (uspeh) {
                    job.endJob();
                }
            }
        });

        VBox layout = new VBox(10, datumPicker, scrollPane, stampaBtn);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-font-size: 16px;");

        stage.setScene(new Scene(layout, 600, 600));
        stage.showAndWait();
    }

    private void prikaziUplate(List<Uplata> uplate, VBox prikazIzvestajaBox, LocalDate datum) {
        prikazIzvestajaBox.getChildren().clear();
        Label naslovLabel = new Label("Uplate na dan: " + datum.format(srpskiFormat));
        naslovLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        prikazIzvestajaBox.getChildren().add(naslovLabel);

        if (uplate.isEmpty()) {
            prikazIzvestajaBox.getChildren().add(new Label("Nema uplata za odabrani datum."));
        } else {
            for (Uplata u : uplate) {
                Kandidat kandidat = Database.vratiKandidataPoId(u.getKandidatId());
                prikazIzvestajaBox.getChildren().add(new Label(
                        "ID broj kandidata: " + kandidat.getIdKandidata() + ". Kandidat: " + kandidat.getIme() + " " + kandidat.getPrezime() + ". Datum uplate: "
                                + u.getDatum().format(srpskiFormat) + ". Iznos: " + u.getIznos() + " RSD"
                ));
            }
        }
    }
}
