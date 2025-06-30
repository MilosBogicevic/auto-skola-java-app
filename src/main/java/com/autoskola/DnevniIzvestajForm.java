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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    private final List<String> linijeCSV = new ArrayList<>();
    private final Map<String, String> mapaLinija = new LinkedHashMap<>();

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
        Button dodajVanEvidencijuBtn = new Button("Dodaj uplatu van evidencije", IkonicaUtil.napravi("add.png"));
        Button obrisiBtn = new Button("Obriši uplatu van evidencije", IkonicaUtil.napravi("trash.png"));

        for (Button b : new Button[]{stampajBtn, prikaziBtn, dodajVanEvidencijuBtn, obrisiBtn}) {
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

        dodajVanEvidencijuBtn.setOnAction(e -> new UplataVanEvidencijeForm(this::prikaziIzvestaj).prikazi());

        obrisiBtn.setOnAction(e -> {
            String selektovana = lista.getSelectionModel().getSelectedItem();
            if (selektovana == null || !selektovana.contains("(kandidat van evidencije)")) {
                prikaziGresku("Morate selektovati uplatu van evidencije.");
                return;
            }
            String originalnaLinija = mapaLinija.get(selektovana);
            String[] podaci = originalnaLinija.split(";", -1);
            String datum = podaci[0];
            String iznos = FormatUtil.format(Integer.parseInt(podaci[2]));

            Alert potvrda = new Alert(Alert.AlertType.CONFIRMATION);
            potvrda.setTitle("Potvrda brisanja");
            potvrda.setHeaderText("Obriši izabranu uplatu?");
            potvrda.setContentText("Datum: " + srpskiFormat.format(LocalDate.parse(datum)) +
                    "\nIznos: " + iznos + " RSD");

            potvrda.getButtonTypes().setAll(
                    new ButtonType("Da", ButtonBar.ButtonData.YES),
                    new ButtonType("Ne", ButtonBar.ButtonData.NO)
            );
            potvrda.getDialogPane().setStyle("-fx-font-size: 16px;");

            if (potvrda.showAndWait().orElse(ButtonType.NO).getButtonData() == ButtonBar.ButtonData.YES) {
                linijeCSV.remove(originalnaLinija);
                sacuvajCSV();
                prikaziIzvestaj();
            }
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox datumiBox = new HBox(10, new Label("Od:"), datumOdPicker, new Label("Do:"), datumDoPicker, prikaziBtn);

        HBox desnaDugmad = new HBox(10, dodajVanEvidencijuBtn, obrisiBtn);
        Region razmak = new Region();
        HBox.setHgrow(razmak, Priority.ALWAYS);
        HBox rasporedDugmadi = new HBox(10, stampajBtn, razmak, desnaDugmad);

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
        mapaLinija.clear();
        linijeCSV.clear();
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

            opis.append(u.getSvrha() != null ? u.getSvrha() : "Obuka");
            opis.append(" – ").append(FormatUtil.format(u.getIznos())).append(" RSD");
            if (!u.getNacinUplate().equals("Gotovina")) {
                opis.append(" – ").append(u.getNacinUplate());
            }

            String stavka = k.getIdKandidata() + " – " + k.getIme() + " " + k.getPrezime()
                    + " – " + srpskiFormat.format(u.getDatum())
                    + " – " + opis;

            lista.getItems().add(stavka);
            ukupno += u.getIznos();
        }

        try (BufferedReader reader = Files.newBufferedReader(Paths.get("van_evidencije.csv"))) {
            String linija;
            boolean prva = true;
            while ((linija = reader.readLine()) != null) {
                if (prva) { prva = false; continue; }
                linijeCSV.add(linija);
                String[] podaci = linija.split(";", -1);
                if (podaci.length >= 4) {
                    LocalDate datum = LocalDate.parse(podaci[0]);
                    if ((datum.isEqual(od) || datum.isAfter(od)) && (datum.isEqual(doD) || datum.isBefore(doD))) {
                        String broj = podaci[1];
                        int iznos = Integer.parseInt(podaci[2]);
                        String svrha = podaci[3];
                        String nacin = podaci.length >= 5 ? podaci[4] : "";

                        StringBuilder opis = new StringBuilder();
                        opis.append(svrha != null ? svrha : "Obuka");
                        opis.append(" – ").append(FormatUtil.format(iznos)).append(" RSD");
                        if (!nacin.equals("Gotovina")) {
                            opis.append(" – ").append(nacin);
                        }

                        String stavka = broj + " – " + datum.format(srpskiFormat) + " – " + opis + " (kandidat van evidencije)";
                        lista.getItems().add(stavka);
                        mapaLinija.put(stavka, linija);
                        ukupno += iznos;
                    }
                }
            }
        } catch (Exception ignored) {}

        if (lista.getItems().isEmpty()) {
            lista.getItems().add("Nema uplata za izabrani period.");
        }

        ukupnoLabel.setText("Ukupno: " + FormatUtil.format(ukupno) + " RSD");
    }

    private void sacuvajCSV() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("van_evidencije.csv"))) {
            writer.write("datum;broj;iznos;svrha;nacin");
            writer.newLine();
            for (String linija : linijeCSV) {
                writer.write(linija);
                writer.newLine();
            }
        } catch (Exception e) {
            prikaziGresku("Greška prilikom čuvanja fajla.");
        }
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
