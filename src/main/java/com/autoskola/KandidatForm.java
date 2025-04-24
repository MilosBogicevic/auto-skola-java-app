package com.autoskola;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.function.Consumer;

public class KandidatForm {

    public KandidatForm(Kandidat postojeći, Consumer<Kandidat> onSacuvaj) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(postojeći == null ? "Novi kandidat" : "Izmena kandidata");

        TextField imePolje = new TextField();
        imePolje.setPromptText("Ime");
        if (postojeći != null) imePolje.setText(postojeći.getIme());

        TextField prezimePolje = new TextField();
        prezimePolje.setPromptText("Prezime");
        if (postojeći != null) prezimePolje.setText(postojeći.getPrezime());

        TextField jmbgPolje = new TextField();
        jmbgPolje.setPromptText("JMBG");
        if (postojeći != null) jmbgPolje.setText(postojeći.getJmbg());

        TextField telefonPolje = new TextField();
        telefonPolje.setPromptText("Telefon");
        if (postojeći != null) telefonPolje.setText(postojeći.getTelefon());

        TextField emailPolje = new TextField();
        emailPolje.setPromptText("Email");
        if (postojeći != null) emailPolje.setText(postojeći.getEmail());

        ComboBox<String> kategorijaBox = new ComboBox<>();
        kategorijaBox.getItems().addAll("A", "A1", "B", "C", "CE", "D");
        if (postojeći != null) kategorijaBox.setValue(postojeći.getKategorija());

        CheckBox polozioTeoriju = new CheckBox("Položio teoriju");
        if (postojeći != null) polozioTeoriju.setSelected(postojeći.isPolozioTeoriju());

        CheckBox polozioVoznju = new CheckBox("Položio vožnju");
        if (postojeći != null) polozioVoznju.setSelected(postojeći.isPolozioVoznju());

        DatePicker datumUpisaPicker = new DatePicker();
        datumUpisaPicker.setPromptText("Datum upisa");
        if (postojeći != null) datumUpisaPicker.setValue(postojeći.getDatumUpisa());
        else datumUpisaPicker.setValue(LocalDate.now());

        TextField cenaTeorijaPolje = new TextField();
        cenaTeorijaPolje.setPromptText("Cena teorije (€)");
        if (postojeći != null) cenaTeorijaPolje.setText(String.valueOf(postojeći.getCenaTeorija()));

        TextField cenaPraksaPolje = new TextField();
        cenaPraksaPolje.setPromptText("Cena prakse (€)");
        if (postojeći != null) cenaPraksaPolje.setText(String.valueOf(postojeći.getCenaPraksa()));

        TextField brojRataPolje = new TextField();
        brojRataPolje.setPromptText("Broj rata");
        if (postojeći != null) brojRataPolje.setText(String.valueOf(postojeći.getBrojRata()));

        TextField iznosPoRatiPolje = new TextField();
        iznosPoRatiPolje.setPromptText("Iznos po rati (€)");
        if (postojeći != null) iznosPoRatiPolje.setText(String.valueOf(postojeći.getIznosPoRati()));

        TextField placenoPolje = new TextField();
        placenoPolje.setPromptText("Plaćeno do sada (€)");
        if (postojeći != null) placenoPolje.setText(String.valueOf(postojeći.getPlaceno()));

        Button sacuvajBtn = new Button("Sačuvaj");

        sacuvajBtn.setOnAction(e -> {
            try {
                Kandidat novi = new Kandidat(
                        postojeći != null ? postojeći.getId() : 0,
                        imePolje.getText(),
                        prezimePolje.getText(),
                        jmbgPolje.getText(),
                        telefonPolje.getText(),
                        emailPolje.getText(),
                        kategorijaBox.getValue(),
                        polozioTeoriju.isSelected(),
                        polozioVoznju.isSelected(),
                        datumUpisaPicker.getValue(),
                        Double.parseDouble(cenaTeorijaPolje.getText()),
                        Double.parseDouble(cenaPraksaPolje.getText()),
                        Integer.parseInt(brojRataPolje.getText()),
                        Double.parseDouble(iznosPoRatiPolje.getText()),
                        Double.parseDouble(placenoPolje.getText())
                );
                onSacuvaj.accept(novi);
                stage.close();
            } catch (Exception ex) {
                prikaziGresku("Greška u unosu", "Proveri da su svi brojevi ispravni.");
            }
        });

        VBox forma = new VBox(10,
                imePolje, prezimePolje, jmbgPolje, telefonPolje, emailPolje,
                kategorijaBox, datumUpisaPicker,
                polozioTeoriju, polozioVoznju,
                cenaTeorijaPolje, cenaPraksaPolje,
                brojRataPolje, iznosPoRatiPolje, placenoPolje,
                sacuvajBtn
        );
        forma.setPadding(new Insets(20));

        stage.setScene(new Scene(forma, 450, 650));
        stage.showAndWait();
    }

    private void prikaziGresku(String naslov, String poruka) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(naslov);
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}
