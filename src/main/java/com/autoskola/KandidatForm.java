package com.autoskola;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

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
        idKandidatPolje.setPromptText("ID kandidata (šifra iz evidencije)");

        TextField imePolje = new TextField(postojeći != null ? postojeći.getIme() : "");
        imePolje.setPromptText("Ime");

        TextField prezimePolje = new TextField(postojeći != null ? postojeći.getPrezime() : "");
        prezimePolje.setPromptText("Prezime");

        TextField telefonPolje = new TextField(postojeći != null ? postojeći.getTelefon() : "");
        telefonPolje.setPromptText("Telefon");

        TextField emailPolje = new TextField(postojeći != null ? postojeći.getEmail() : "");
        emailPolje.setPromptText("Email (opciono)");

        ComboBox<String> kategorijaBox = new ComboBox<>();
        kategorijaBox.getItems().addAll("A1", "A2", "A", "B", "C", "CE");
        if (postojeći != null) kategorijaBox.setValue(postojeći.getKategorija());

        CheckBox polozioTeoriju = new CheckBox("Položio teoriju");
        if (postojeći != null) polozioTeoriju.setSelected(postojeći.isPolozioTeoriju());

        CheckBox polozioVoznju = new CheckBox("Položio vožnju");
        if (postojeći != null) polozioVoznju.setSelected(postojeći.isPolozioVoznju());

        DatePicker datumUpisaPicker = new DatePicker(postojeći != null ? postojeći.getDatumUpisa() : LocalDate.now());
        datumUpisaPicker.setPromptText("Datum upisa");
        datumUpisaPicker.setConverter(converter);

        TextField cenaTeorijaPolje = new TextField(postojeći != null ? String.valueOf(postojeći.getCenaTeorija()) : "");
        cenaTeorijaPolje.setPromptText("Cena teorijske obuke (RSD)");

        TextField cenaPraksaPolje = new TextField(postojeći != null ? String.valueOf(postojeći.getCenaPraksa()) : "");
        cenaPraksaPolje.setPromptText("Cena praktične obuke (RSD)");

        Button sacuvajBtn = new Button("Sačuvaj");

        sacuvajBtn.setOnAction(e -> {
            try {
                if (idKandidatPolje.getText().isEmpty() || imePolje.getText().isEmpty()
                        || prezimePolje.getText().isEmpty() || telefonPolje.getText().isEmpty()
                        || kategorijaBox.getValue() == null || cenaTeorijaPolje.getText().isEmpty()
                        || cenaPraksaPolje.getText().isEmpty()) {
                    throw new IllegalArgumentException("Sva obavezna polja moraju biti popunjena.");
                }

                double cenaTeorija = Double.parseDouble(cenaTeorijaPolje.getText());
                double cenaPraksa = Double.parseDouble(cenaPraksaPolje.getText());

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
                        imePolje.getText(),
                        prezimePolje.getText(),
                        telefonPolje.getText(),
                        emailPolje.getText().trim(),
                        kategorijaBox.getValue(),
                        polozioTeoriju.isSelected(),
                        polozioVoznju.isSelected(),
                        datumUpisaPicker.getValue(),
                        cenaTeorija,
                        cenaPraksa,
                        placeno
                );
                onSacuvaj.accept(novi);
                stage.close();
            } catch (NumberFormatException ex) {
                prikaziGresku("Proverite da su cene ispravno unete.");
            } catch (Exception ex) {
                prikaziGresku(ex.getMessage());
            }
        });

        VBox forma = new VBox(10,
                idKandidatPolje, imePolje, prezimePolje, telefonPolje, emailPolje,
                kategorijaBox, datumUpisaPicker,
                polozioTeoriju, polozioVoznju,
                cenaTeorijaPolje, cenaPraksaPolje,
                sacuvajBtn
        );
        forma.setPadding(new Insets(20));
        forma.setStyle("-fx-font-size: 16px;");

        stage.setScene(new Scene(forma, 450, 600));
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
