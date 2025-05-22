package com.autoskola;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class KandidatDetaljiForm {

    private final Label stanjeLabel = new Label();
    private final ListView<Uplata> uplateList = new ListView<>();

    public KandidatDetaljiForm(Kandidat kandidat, Runnable onOsvezi) {
        if (kandidat == null) return;

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Detalji kandidata - " + kandidat.getIme() + " " + kandidat.getPrezime());

        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

        Label naslov = new Label("Detalji kandidata:");
        naslov.setStyle("-fx-size: 18px; -fx-font-weight: bold;");

        VBox detaljiBox = new VBox(6);
        detaljiBox.getChildren().addAll(
                new Label("ID broj kandidata: " + kandidat.getIdKandidata()),
                new Label("Ime i prezime: " + kandidat.getIme() + " " + kandidat.getPrezime()),
                new Label("Telefon: " + kandidat.getTelefon()),
                new Label("Kategorija: " + kandidat.getKategorija()),
                new Label("Datum upisa: " + kandidat.getDatumUpisa().format(format)),
                new Label("Položio teoriju: " + (kandidat.isPolozioTeoriju() ? "da" : "ne")),
                new Label("Položio vožnju: " + (kandidat.isPolozioVoznju() ? "da" : "ne")),
                new Label("Cena teorijske obuke: " + FormatUtil.format(kandidat.getCenaTeorija()) + " RSD"),
                new Label("Cena praktične obuke: " + FormatUtil.format(kandidat.getCenaPraksa()) + " RSD")
        );

        Label uplateNaslov = new Label("Uplate kandidata:");
        uplateNaslov.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        uplateList.setPrefHeight(250);
        uplateList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Uplata u, boolean empty) {
                super.updateItem(u, empty);
                if (empty || u == null) {
                    setText(null);
                } else {
                    String opis = u.getDatum().format(format) + " – " +
                            FormatUtil.format(u.getIznos()) + " RSD – " +
                            (u.getSvrha() != null ? u.getSvrha() : "Obuka");

                    if (u.getNacinUplate() != null && !u.getNacinUplate().equalsIgnoreCase("Gotovina")) {
                        opis += " – " + u.getNacinUplate();
                    }
                    setText(opis);
                }
            }
        });

        stanjeLabel.setStyle("-fx-font-size: 18px; -fx-padding: 10 0 0 0;");

        Button obrisiUplatuBtn = new Button("Obriši uplatu", IkonicaUtil.napravi("trash.png"));
        obrisiUplatuBtn.setContentDisplay(ContentDisplay.LEFT);
        obrisiUplatuBtn.setGraphicTextGap(8);

        obrisiUplatuBtn.setOnAction(e -> {
            Uplata selektovana = uplateList.getSelectionModel().getSelectedItem();
            if (selektovana == null) {
                Alert poruka = new Alert(Alert.AlertType.INFORMATION);
                poruka.setTitle("Obaveštenje");
                poruka.setHeaderText(null);
                poruka.setContentText("Niste selektovali uplatu.");
                poruka.getDialogPane().setStyle("-fx-font-size: 16px;");
                poruka.getButtonTypes().setAll(new ButtonType("U redu", ButtonBar.ButtonData.OK_DONE));
                poruka.showAndWait();
                return;
            }

            Alert potvrda = new Alert(Alert.AlertType.CONFIRMATION);
            potvrda.setTitle("Potvrda brisanja");
            potvrda.setHeaderText("Obriši izabranu uplatu?");
            potvrda.setContentText("Datum: " + selektovana.getDatum().format(format) +
                    "\nIznos: " + FormatUtil.format(selektovana.getIznos()) + " RSD");

            potvrda.getButtonTypes().setAll(
                    new ButtonType("Da", ButtonBar.ButtonData.YES),
                    new ButtonType("Ne", ButtonBar.ButtonData.NO)
            );
            potvrda.getDialogPane().setStyle("-fx-font-size: 16px;");

            if (potvrda.showAndWait().orElse(ButtonType.NO).getButtonData() == ButtonBar.ButtonData.YES) {
                Database.obrisiUplatu(selektovana.getId());

                double novoPlaceno = Database.vratiUplateZaKandidata(kandidat.getId()).stream()
                        .filter(u -> u.getSvrha() == null || u.getSvrha().equalsIgnoreCase("Obuka"))
                        .mapToDouble(Uplata::getIznos).sum();

                kandidat.setPlaceno(novoPlaceno);
                Database.izmeniKandidata(kandidat);

                osveziStanjeIKandidata(kandidat);
                onOsvezi.run();
            }
        });

        Button stampajBtn = new Button("Štampaj detalje", IkonicaUtil.napravi("print.png"));
        Button ugovorBtn = new Button("Otvori ugovor", IkonicaUtil.napravi("contract.png"));

        for (Button b : new Button[]{stampajBtn, ugovorBtn}) {
            b.setContentDisplay(ContentDisplay.LEFT);
            b.setGraphicTextGap(8);
        }

        stampajBtn.setOnAction(e -> UgovorGenerator.generisiDetaljeOKandidatu(kandidat));

        ugovorBtn.setOnAction(e -> {
            try {
                String sablon = "sabloni/ugovor_" + kandidat.getKategorija() + ".docx";
                String izlaz = "ugovori/ugovor-" + kandidat.getIme() + "-" + kandidat.getPrezime() + "-" + kandidat.getIdb() + ".docx";
                File izlazFajl = new File(izlaz);
                if (!izlazFajl.getParentFile().exists()) izlazFajl.getParentFile().mkdirs();
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

        HBox dugmici = new HBox(10, obrisiUplatuBtn, stampajBtn, ugovorBtn);
        dugmici.setPadding(new Insets(10, 0, 0, 0));
        dugmici.setStyle("-fx-font-size: 16px;");

        VBox sadrzaj = new VBox(12,
                naslov,
                detaljiBox,
                new Separator(),
                uplateNaslov,
                uplateList,
                stanjeLabel,
                dugmici
        );
        sadrzaj.setPadding(new Insets(30));
        sadrzaj.setStyle("-fx-font-size: 18px;");

        ScrollPane scroll = new ScrollPane(sadrzaj);
        scroll.setFitToWidth(true);
        scroll.setPrefSize(700, 700);

        VBox koren = new VBox(scroll);
        koren.setPadding(new Insets(10));
        koren.setStyle("-fx-font-size: 18px;");

        osveziStanjeIKandidata(kandidat);

        stage.setScene(new Scene(koren));
        stage.showAndWait();
    }

    private void osveziStanjeIKandidata(Kandidat kandidat) {
        uplateList.getItems().setAll(Database.vratiUplateZaKandidata(kandidat.getId()));
        stanjeLabel.setText("Plaćeno: " + FormatUtil.format(kandidat.getPlaceno()) +
                " RSD / Preostalo: " + FormatUtil.format(kandidat.getPreostalo()) + " RSD");
    }

    public KandidatDetaljiForm(Kandidat kandidat) {
        this(kandidat, () -> {});
    }
}
