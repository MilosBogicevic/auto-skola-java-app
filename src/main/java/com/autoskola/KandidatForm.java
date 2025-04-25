package com.autoskola;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.function.Consumer;

public class KandidatForm {

    public KandidatForm(Kandidat postojeći, Consumer<Kandidat> onSacuvaj) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(postojeći == null ? "Novi kandidat" : "Izmena kandidata");

        TextField idKandidatPolje = new TextField(postojeći != null ? postojeći.getIdKandidata() : "");
        idKandidatPolje.setPromptText("ID kandidata (šifra iz evidencije)");

        TextField imePolje = new TextField(postojeći != null ? postojeći.getIme() : "");
        imePolje.setPromptText("Ime");

        TextField prezimePolje = new TextField(postojeći != null ? postojeći.getPrezime() : "");
        prezimePolje.setPromptText("Prezime");

        TextField jmbgPolje = new TextField(postojeći != null ? postojeći.getJmbg() : "");
        jmbgPolje.setPromptText("JMBG");

        TextField telefonPolje = new TextField(postojeći != null ? postojeći.getTelefon() : "");
        telefonPolje.setPromptText("Telefon");

        TextField emailPolje = new TextField(postojeći != null ? postojeći.getEmail() : "");
        emailPolje.setPromptText("Email (opciono)");

        ComboBox<String> kategorijaBox = new ComboBox<>();
        kategorijaBox.getItems().addAll("A", "A1", "B", "C", "CE", "D");
        if (postojeći != null) kategorijaBox.setValue(postojeći.getKategorija());

        CheckBox polozioTeoriju = new CheckBox("Položio teoriju");
        if (postojeći != null) polozioTeoriju.setSelected(postojeći.isPolozioTeoriju());

        CheckBox polozioVoznju = new CheckBox("Položio vožnju");
        if (postojeći != null) polozioVoznju.setSelected(postojeći.isPolozioVoznju());

        DatePicker datumUpisaPicker = new DatePicker(postojeći != null ? postojeći.getDatumUpisa() : LocalDate.now());
        datumUpisaPicker.setPromptText("Datum upisa");

        TextField cenaTeorijaPolje = new TextField(postojeći != null ? String.valueOf(postojeći.getCenaTeorija()) : "");
        cenaTeorijaPolje.setPromptText("Cena teorije (RSD)");

        TextField cenaPraksaPolje = new TextField(postojeći != null ? String.valueOf(postojeći.getCenaPraksa()) : "");
        cenaPraksaPolje.setPromptText("Cena prakse (RSD)");

        Button sacuvajBtn = new Button("Sačuvaj");

        sacuvajBtn.setOnAction(e -> {
            try {
                if (idKandidatPolje.getText().isEmpty() || imePolje.getText().isEmpty()
                        || prezimePolje.getText().isEmpty() || jmbgPolje.getText().isEmpty()
                        || telefonPolje.getText().isEmpty() || kategorijaBox.getValue() == null
                        || cenaTeorijaPolje.getText().isEmpty() || cenaPraksaPolje.getText().isEmpty()) {
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
                        jmbgPolje.getText(),
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
                idKandidatPolje, imePolje, prezimePolje, jmbgPolje, telefonPolje, emailPolje,
                kategorijaBox, datumUpisaPicker,
                polozioTeoriju, polozioVoznju,
                cenaTeorijaPolje, cenaPraksaPolje,
                sacuvajBtn
        );
        forma.setPadding(new Insets(20));

        stage.setScene(new Scene(forma, 450, 600));
        stage.showAndWait();
    }

    private void prikaziGresku(String poruka) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška u unosu");
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}
