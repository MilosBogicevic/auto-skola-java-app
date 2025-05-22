package com.autoskola;

import javafx.geometry.Insets;
import javafx.print.PrinterJob;
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

    public KandidatDetaljiForm(Kandidat kandidat) {
        if (kandidat == null) return;

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Detalji kandidata - " + kandidat.getIme() + " " + kandidat.getPrezime());

        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

        Label naslov = new Label("Detalji kandidata:");
        naslov.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox detaljiBox = new VBox(6);
        detaljiBox.getChildren().addAll(
                new Label("ID broj kandidata: " + kandidat.getIdKandidata()),
                new Label("Ime i prezime: " + kandidat.getIme() + " " + kandidat.getPrezime()),
                new Label("Telefon: " + kandidat.getTelefon()),
                new Label("Email: " + (kandidat.getEmail().isEmpty() ? "nema" : kandidat.getEmail())),
                new Label("Kategorija: " + kandidat.getKategorija()),
                new Label("Datum upisa: " + kandidat.getDatumUpisa().format(format)),
                new Label("Položio teoriju: " + (kandidat.isPolozioTeoriju() ? "da" : "ne")),
                new Label("Položio vožnju: " + (kandidat.isPolozioVoznju() ? "da" : "ne")),
                new Label("Cena teorijske obuke: " + FormatUtil.format(kandidat.getCenaTeorija()) + " RSD"),
                new Label("Cena praktične obuke: " + FormatUtil.format(kandidat.getCenaPraksa()) + " RSD"),
                new Label("Plaćeno: " + FormatUtil.format(kandidat.getPlaceno()) + " RSD"),
                new Label("Preostalo: " + FormatUtil.format(kandidat.getPreostalo()) + " RSD")
        );

        Label uplateNaslov = new Label("Uplate kandidata:");
        uplateNaslov.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        VBox listaUplataBox = new VBox(5);
        List<Uplata> uplate = Database.vratiUplateZaKandidata(kandidat.getId());

        if (uplate.isEmpty()) {
            listaUplataBox.getChildren().add(new Label("- Nema zabeleženih uplata."));
        } else {
            for (Uplata u : uplate) {
                StringBuilder opis = new StringBuilder();
                opis.append(u.getSvrha() != null ? u.getSvrha() : "Obuka");
                opis.append(" – ").append(FormatUtil.format(u.getIznos())).append(" RSD");
                if (!u.getNacinUplate().equals("Gotovina")) {
                    opis.append(" – ").append(u.getNacinUplate());
                }

                Label stavka = new Label("• " + u.getDatum().format(format) + " – " + opis);
                listaUplataBox.getChildren().add(stavka);
            }
        }

        VBox sadrzaj = new VBox(12,
                naslov,
                detaljiBox,
                new Separator(),
                uplateNaslov,
                listaUplataBox
        );
        sadrzaj.setPadding(new Insets(30));
        sadrzaj.setStyle("-fx-font-size: 18px;");

        ScrollPane scroll = new ScrollPane(sadrzaj);
        scroll.setFitToWidth(true);
        scroll.setPrefSize(650, 600);

        Button stampajBtn = new Button("Štampaj detalje", IkonicaUtil.napravi("print.png"));
        Button ugovorBtn = new Button("Otvori ugovor", IkonicaUtil.napravi("contract.png"));

        stampajBtn.setContentDisplay(ContentDisplay.LEFT);
        ugovorBtn.setContentDisplay(ContentDisplay.LEFT);
        stampajBtn.setGraphicTextGap(8);
        ugovorBtn.setGraphicTextGap(8);

        stampajBtn.setContentDisplay(ContentDisplay.LEFT);
        stampajBtn.setGraphicTextGap(8);

        stampajBtn.setOnAction(e -> {
            UgovorGenerator.generisiDetaljeOKandidatu(kandidat);
        });

        ugovorBtn.setOnAction(e -> {
            try {
                String sablon = "sabloni/ugovor_" + kandidat.getKategorija() + ".docx";
                String izlaz = "ugovori/ugovor-" + kandidat.getIme() + "-" + kandidat.getPrezime() + "-" + kandidat.getIdb() + ".docx";

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
