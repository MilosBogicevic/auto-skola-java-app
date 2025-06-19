package com.autoskola;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class KandidatForm {

    public KandidatForm(Kandidat postojeći, Consumer<Kandidat> onSacuvaj) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(postojeći == null ? "Novi kandidat" : "Izmena kandidata");

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

        TextField idKandidatPolje = new TextField(postojeći != null ? postojeći.getIdKandidata() : "");
        TextField idbPolje = new TextField(postojeći != null ? postojeći.getIdb() : "");

        TextField imePolje = new TextField(postojeći != null ? postojeći.getIme() : "");
        TextField prezimePolje = new TextField(postojeći != null ? postojeći.getPrezime() : "");

        TextField jmbgPolje = new TextField(postojeći != null ? postojeći.getJmbg() : "");
        TextField licnaPolje = new TextField(postojeći != null ? postojeći.getBrojLicneKarte() : "");

        TextField adresaPolje = new TextField(postojeći != null ? postojeći.getAdresa() : "");
        TextField gradPolje = new TextField(postojeći != null ? postojeći.getGrad() : "");

        TextField telefonPolje = new TextField(postojeći != null ? postojeći.getTelefon() : "");
        TextField emailPolje = new TextField(postojeći != null ? postojeći.getEmail() : "");

        ComboBox<String> kategorijaBox = new ComboBox<>();
        kategorijaBox.getItems().addAll("A1", "A2", "A", "B", "C", "CE", "F");
        if (postojeći != null) kategorijaBox.setValue(postojeći.getKategorija());

        DatePicker datumUpisaPicker = new DatePicker(postojeći != null ? postojeći.getDatumUpisa() : LocalDate.now());
        datumUpisaPicker.setConverter(converter);

        CheckBox polozioTeoriju = new CheckBox("Položena teorija");
        if (postojeći != null) polozioTeoriju.setSelected(postojeći.isPolozioTeoriju());

        CheckBox polozioVoznju = new CheckBox("Položena vožnja");
        if (postojeći != null) polozioVoznju.setSelected(postojeći.isPolozioVoznju());

        TextField cenaTeorijaPolje = new TextField(postojeći != null ? FormatUtil.format(postojeći.getCenaTeorija()) : "");
        TextField cenaPraksaPolje = new TextField(postojeći != null ? FormatUtil.format(postojeći.getCenaPraksa()) : "");

        VBox idBox = new VBox(2, new Label("ID broj kandidata:"), idKandidatPolje);
        VBox idbBox = new VBox(2, new Label("IDB (eUprava):"), idbPolje);
        HBox idHBox = new HBox(10, idbBox, idBox);

        VBox imeBox = new VBox(2, new Label("Ime:"), imePolje);
        VBox prezimeBox = new VBox(2, new Label("Prezime:"), prezimePolje);
        HBox imePrezimeBox = new HBox(10, imeBox, prezimeBox);

        VBox jmbgBox = new VBox(2, new Label("JMBG:"), jmbgPolje);
        VBox licnaBox = new VBox(2, new Label("Broj lične karte:"), licnaPolje);
        HBox jmbgLicnaBox = new HBox(10, jmbgBox, licnaBox);

        VBox adresaBox = new VBox(2, new Label("Adresa stanovanja:"), adresaPolje);
        VBox gradBox = new VBox(2, new Label("Grad:"), gradPolje);
        HBox adresaGradBox = new HBox(10, gradBox, adresaBox);

        VBox telefonBox = new VBox(2, new Label("Telefon:"), telefonPolje);
        VBox emailBox = new VBox(2, new Label("Email (opciono):"), emailPolje);
        HBox telefonEmailBox = new HBox(10, telefonBox, emailBox);

        VBox teorijskaBox = new VBox(2, new Label("Cena teorijske obuke (RSD):"), cenaTeorijaPolje);
        VBox prakticnaBox = new VBox(2, new Label("Cena praktične obuke (RSD):"), cenaPraksaPolje);
        HBox cenaBox = new HBox(10, teorijskaBox, prakticnaBox);

        Label kategorijaLabel = new Label("Kategorija:");
        HBox kategorijaBoxHBox = new HBox(10, kategorijaLabel, kategorijaBox);
        kategorijaBoxHBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label datumLabel = new Label("Datum upisa:");
        HBox datumBox = new HBox(10, datumLabel, datumUpisaPicker);
        datumBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        for (HBox hbox : new HBox[]{idHBox, imePrezimeBox, jmbgLicnaBox, adresaGradBox, telefonEmailBox, cenaBox}) {
            for (javafx.scene.Node node : hbox.getChildren()) {
                if (node instanceof VBox) {
                    HBox.setHgrow(node, Priority.ALWAYS);
                }
            }
        }

        Button sacuvajBtn = new Button("Sačuvaj", IkonicaUtil.napravi("save.png"));
        sacuvajBtn.setGraphicTextGap(8);
        sacuvajBtn.setContentDisplay(ContentDisplay.LEFT);
        sacuvajBtn.setStyle("-fx-font-size: 16px;");
        VBox.setMargin(sacuvajBtn, new Insets(10, 0, 10, 0));

        sacuvajBtn.setOnAction(e -> {
            try {
                if (idKandidatPolje.getText().isEmpty() || idbPolje.getText().isEmpty() ||
                        imePolje.getText().isEmpty() || prezimePolje.getText().isEmpty() ||
                        jmbgPolje.getText().isEmpty() || telefonPolje.getText().isEmpty() ||
                        kategorijaBox.getValue() == null || cenaTeorijaPolje.getText().isEmpty() ||
                        cenaPraksaPolje.getText().isEmpty()) {
                    throw new IllegalArgumentException("Sva obavezna polja moraju biti popunjena.");
                }

                String pattern = "^\\d{1,3}(\\.\\d{3})*$|^\\d+$";
                if (!cenaTeorijaPolje.getText().matches(pattern)) {
                    throw new IllegalArgumentException("Iznos mora biti u formatu 1.000 ili 1000.");
                }
                if (!cenaPraksaPolje.getText().matches(pattern)) {
                    throw new IllegalArgumentException("Iznos mora biti u formatu 1.000 ili 1000.");
                }

                LocalDate datumUpisa;
                try {
                    datumUpisa = converter.fromString(datumUpisaPicker.getEditor().getText().trim());
                    datumUpisaPicker.setValue(datumUpisa);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Datum upisa mora biti u formatu: 01.01.2025.");
                }

                double cenaTeorija = FormatUtil.parse(cenaTeorijaPolje.getText());
                double cenaPraksa = FormatUtil.parse(cenaPraksaPolje.getText());

                double placeno = 0;
                if (postojeći != null) {
                    placeno = Database.vratiUplateZaKandidata(postojeći.getId())
                            .stream()
                            .mapToDouble(Uplata::getIznos)
                            .sum();
                }

                Kandidat novi = new Kandidat(
                        postojeći != null ? postojeći.getId() : 0,
                        idKandidatPolje.getText().trim(),
                        imePolje.getText().trim(),
                        prezimePolje.getText().trim(),
                        idbPolje.getText().trim(),
                        jmbgPolje.getText().trim(),
                        licnaPolje.getText().trim(),
                        adresaPolje.getText().trim(),
                        gradPolje.getText().trim(),
                        telefonPolje.getText().trim(),
                        emailPolje.getText().trim(),
                        kategorijaBox.getValue(),
                        polozioTeoriju.isSelected(),
                        polozioVoznju.isSelected(),
                        datumUpisa,
                        cenaTeorija,
                        cenaPraksa,
                        placeno,
                        postojeći != null ? postojeći.getDatumIsplate() : null
                );
                onSacuvaj.accept(novi);
                stage.close();
            } catch (ParseException ex) {
                prikaziGresku("Proverite da li su cene ispravno unete.");
            } catch (Exception ex) {
                prikaziGresku(ex.getMessage());
            }
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox forma = new VBox(10,
                idHBox,
                imePrezimeBox,
                jmbgLicnaBox,
                adresaGradBox,
                telefonEmailBox,
                kategorijaBoxHBox,
                datumBox,
                polozioTeoriju,
                polozioVoznju,
                cenaBox,
                spacer,
                sacuvajBtn
        );
        forma.setPadding(new Insets(20));
        forma.setStyle("-fx-font-size: 16px;");

        Scene scene = new Scene(forma);
        forma.setPrefWidth(500);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.showAndWait();
    }

    private void prikaziGresku(String poruka) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška u unosu");
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.getDialogPane().setStyle("-fx-font-size: 16px;");
        alert.showAndWait();
    }
}
