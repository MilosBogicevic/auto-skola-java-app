package com.autoskola;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DnevniIzvestajForm {

    private final DateTimeFormatter srpskiFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
    private final StringConverter<LocalDate> converter = new StringConverter<>() {
        @Override
        public String toString(LocalDate date) {
            return date != null ? srpskiFormat.format(date) : "";
        }

        @Override
        public LocalDate fromString(String string) {
            return (string != null && !string.isEmpty()) ? LocalDate.parse(string, srpskiFormat) : null;
        }
    };

    private final ListView<String> lista = new ListView<>();
    private final Label ukupnoLabel = new Label("Ukupno: 0 RSD");
    private final Label naslov = new Label("Uplate u periodu: –");
    private final DatePicker datumOdPicker = new DatePicker(LocalDate.now());
    private final DatePicker datumDoPicker = new DatePicker(LocalDate.now());

    public DnevniIzvestajForm() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Dnevni izveštaj uplata");

        naslov.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        datumOdPicker.setConverter(converter);
        datumOdPicker.setPromptText("dd.MM.yyyy.");
        datumDoPicker.setConverter(converter);
        datumDoPicker.setPromptText("dd.MM.yyyy.");

        Button stampajBtn = new Button("Štampaj", IkonicaUtil.napravi("print.png"));
        Button prikaziBtn = new Button("Prikaži izveštaj", IkonicaUtil.napravi("report.png"));

        for (Button b : new Button[]{stampajBtn, prikaziBtn}) {
            b.setGraphicTextGap(8);
            b.setContentDisplay(ContentDisplay.LEFT);
            b.setStyle("-fx-font-size: 16px;");
        }

        prikaziBtn.setOnAction(e -> prikaziIzvestaj());

        stampajBtn.setOnAction(e -> {
            try {
                LocalDate od = datumOdPicker.getValue();
                LocalDate doD = datumDoPicker.getValue();
                if (od != null && doD != null && !doD.isBefore(od)) {
                    UgovorGenerator.generisiDnevniIzvestaj(od, doD);
                } else {
                    prikaziGresku("Proverite datume – 'DO' ne može biti pre 'OD'.");
                }
            } catch (Exception ex) {
                prikaziGresku("Greška prilikom štampe izveštaja.");
            }
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label odLabel = new Label("Od:");
        Label doLabel = new Label("Do:");

        HBox datumiBox = new HBox(10, odLabel, datumOdPicker, doLabel, datumDoPicker, prikaziBtn);
        datumiBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Region razmak = new Region();
        HBox.setHgrow(razmak, Priority.ALWAYS);
        HBox rasporedDugmadi = new HBox(10, stampajBtn, razmak);

        VBox box = new VBox(10);
        box.getChildren().addAll(
                naslov,
                datumiBox,
                new Label("Uplate:"),
                lista,
                ukupnoLabel,
                spacer,
                rasporedDugmadi
        );
        box.setPadding(new Insets(20));
        box.setStyle("-fx-font-size: 18px;");

        prikaziIzvestaj();

        stage.setScene(new Scene(box, 960, 700));
        stage.showAndWait();
    }

    private void prikaziIzvestaj() {
        lista.getItems().clear();
        double ukupno = 0;

        LocalDate od = datumOdPicker.getValue();
        LocalDate doD = datumDoPicker.getValue();

        if (od == null || doD == null || doD.isBefore(od)) {
            prikaziGresku("Neispravan opseg datuma.");
            return;
        }

        naslov.setText("Uplate u periodu: " + od.format(srpskiFormat) + " – " + doD.format(srpskiFormat));

        List<Uplata> uplate = Database.vratiUplateZaPeriod(od, doD);
        for (Uplata u : uplate) {
            Kandidat k = Database.vratiKandidataPoId(u.getKandidatId());
            StringBuilder opis = new StringBuilder();

            // Svrha + iznos
            opis.append(u.getSvrha() != null ? u.getSvrha() : "Obuka");
            opis.append(" – ").append(FormatUtil.format(u.getIznos())).append(" RSD");

            // Npr. Uplata karticom
            if (u.getNacinUplate() != null && !u.getNacinUplate().equalsIgnoreCase("Gotovina")) {
                opis.append(" – ").append(u.getNacinUplate());
            }

            // Npr. Uplata 00234
            if (u.getBrojUplate() != null && !u.getBrojUplate().isBlank()) {
                opis.append(" – Broj uplatnice: ").append(u.getBrojUplate());
            }

            String stavka = k.getIdKandidata() + " – " + k.getIme() + " " + k.getPrezime()
                    + " – " + srpskiFormat.format(u.getDatum())
                    + " – " + opis;

            lista.getItems().add(stavka);
            ukupno += u.getIznos();
        }

        if (lista.getItems().isEmpty()) {
            lista.getItems().add("Nema uplata za izabrani period.");
        }

        ukupnoLabel.setText("Ukupno: " + FormatUtil.format(ukupno) + " RSD");
    }

    private void prikaziGresku(String poruka) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška");
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.getDialogPane().setStyle("-fx-font-size: 16px;");
        alert.showAndWait();
    }
}
