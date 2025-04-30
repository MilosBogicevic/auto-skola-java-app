package com.autoskola;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.print.PrinterJob;
import javafx.geometry.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DnevniIzvestajForm {

    // Koristimo srpski format za datum
    private static final DateTimeFormatter srpskiFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    public DnevniIzvestajForm() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Dnevni izveštaj");

        // Kreiranje DatePicker-a za odabir datuma
        DatePicker datumPicker = new DatePicker(LocalDate.now());
        datumPicker.setPromptText("Izaberite datum");

        // Dugme za štampanje izveštaja
        Button stampaBtn = new Button("Štampaj");

        // Polje za prikaz uplatnih podataka u formi
        VBox prikazIzvestajaBox = new VBox(10);

        // Korišćenje ScrollPane-a za automatski skrol
        ScrollPane scrollPane = new ScrollPane(prikazIzvestajaBox);
        scrollPane.setPadding(new Insets(10));
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(500, 500);

        // Automatski prikaz uplata kada se izabere datum
        datumPicker.setOnAction(e -> {
            LocalDate datum = datumPicker.getValue();
            List<Uplata> uplate = Database.vratiUplateZaDatum(datum);
            prikaziUplate(uplate, prikazIzvestajaBox, datum);
        });

        // Inicijalno prikazivanje uplata za današnji datum
        List<Uplata> uplateDanas = Database.vratiUplateZaDatum(LocalDate.now());
        prikaziUplate(uplateDanas, prikazIzvestajaBox, LocalDate.now());

        // Akcija na klik dugmeta "Štampaj"
        stampaBtn.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(stage)) {
                boolean uspeh = job.printPage(prikazIzvestajaBox);
                if (uspeh) {
                    job.endJob();
                }
            }
        });

        // Dodavanje svih elemenata u layout
        VBox layout = new VBox(10, datumPicker, scrollPane, stampaBtn);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-font-size: 16px;");

        // Postavljanje scene i prikazivanje prozora
        stage.setScene(new Scene(layout, 800, 600));
        stage.showAndWait();
    }

    // Metoda koja prikazuje uplate u prozoru
    private void prikaziUplate(List<Uplata> uplate, VBox prikazIzvestajaBox, LocalDate datum) {
        prikazIzvestajaBox.getChildren().clear(); // Čisti prethodne uplate

        // Dodavanje naslova sa datumom u izveštaju
        Label naslovLabel = new Label("Uplate na dan: " + datum.format(srpskiFormat));
        naslovLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        prikazIzvestajaBox.getChildren().add(naslovLabel);

        if (uplate.isEmpty()) {
            prikazIzvestajaBox.getChildren().add(new Label("Nema uplata za odabrani datum."));
        } else {
            for (Uplata u : uplate) {
                // Dohvatanje kandidata za prikazivanje imena i prezimena
                Kandidat kandidat = Database.vratiKandidataPoId(u.getKandidatId());

                // Dodavanje linije u izveštaj koja prikazuje ID kandidata, ime, prezime, datum i iznos uplate
                prikazIzvestajaBox.getChildren().add(new Label(
                        "ID broj kandidata: " + kandidat.getIdKandidata() + ". Kandidat: " + kandidat.getIme() + " " + kandidat.getPrezime() + ". Datum uplate: "
                                + u.getDatum().format(srpskiFormat) + ". Iznos: " + u.getIznos() + " RSD"
                ));
            }
        }
    }
}
