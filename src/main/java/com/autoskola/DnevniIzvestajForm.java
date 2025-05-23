package com.autoskola;

import javafx.geometry.Insets;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DnevniIzvestajForm {

    public DnevniIzvestajForm() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Dnevni izveštaj uplata");

        DateTimeFormatter srpskiFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
        Label naslov = new Label("Uplate na dan: –");
        naslov.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label datumLabel = new Label("Izaberite datum:");
        DatePicker datumPicker = new DatePicker(LocalDate.now());
        datumPicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? date.format(srpskiFormat) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, srpskiFormat) : null;
            }
        });

        Button stampajBtn = new Button("Štampaj", IkonicaUtil.napravi("print.png"));
        Button prikaziBtn = new Button("Prikaži izveštaj", IkonicaUtil.napravi("report.png"));

        for (Button b : new Button[]{stampajBtn, prikaziBtn}) {
            b.setGraphicTextGap(8);
            b.setContentDisplay(ContentDisplay.LEFT);
            b.setStyle("-fx-font-size: 16px;");
        }

        ListView<String> lista = new ListView<>();
        Label ukupnoLabel = new Label("Ukupno: 0 RSD");

        prikaziBtn.setOnAction(e -> {
            lista.getItems().clear();
            LocalDate datum = datumPicker.getValue();
            if (datum == null) return;

            naslov.setText("Uplate na dan: " + datum.format(srpskiFormat));

            List<Uplata> uplate = Database.vratiUplateZaDatum(datum);
            double ukupno = 0;

            for (Uplata u : uplate) {
                Kandidat k = Database.vratiKandidataPoId(u.getKandidatId());
                StringBuilder opis = new StringBuilder();

                opis.append(u.getSvrha() != null ? u.getSvrha() : "Obuka");
                opis.append(" – ").append(FormatUtil.format(u.getIznos())).append(" RSD");
                if (!u.getNacinUplate().equals("Gotovina")) {
                    opis.append(" – ").append(u.getNacinUplate());
                }

                String stavka = k.getIdKandidata() + " – " + k.getIme() + " " + k.getPrezime()
                        + " – " + srpskiFormat.format(u.getDatum())
                        + " – " + opis;

                lista.getItems().add(stavka);
                ukupno += u.getIznos(); // Sabira SVE, uključujući i ispite i dopunske
            }

            if (uplate.isEmpty()) {
                lista.getItems().add("Nema uplata za izabrani datum.");
            }

            ukupnoLabel.setText("Ukupno: " + FormatUtil.format(ukupno) + " RSD");
        });

        stampajBtn.setContentDisplay(ContentDisplay.LEFT);
        stampajBtn.setGraphicTextGap(8);
        stampajBtn.setStyle("-fx-font-size: 16px;");

        stampajBtn.setOnAction(e -> {
            LocalDate datum = datumPicker.getValue();
            if (datum != null) {
                UgovorGenerator.generisiDnevniIzvestaj(datum);
            }
        });

        VBox box = new VBox(10,
                naslov,
                datumLabel, datumPicker,
                prikaziBtn,
                new Label("Uplate:"),
                lista,
                ukupnoLabel,
                stampajBtn
        );
        box.setPadding(new Insets(20));
        box.setStyle("-fx-font-size: 18px;");

        stage.setScene(new Scene(box, 960, 700));
        stage.showAndWait();
    }
}
