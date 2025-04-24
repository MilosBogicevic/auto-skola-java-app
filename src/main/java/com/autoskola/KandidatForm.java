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

        TextField imePolje = new TextField(postojeći != null ? postojeći.getIme() : "");
        imePolje.setPromptText("Ime");

        TextField prezimePolje = new TextField(postojeći != null ? postojeći.getPrezime() : "");
        prezimePolje.setPromptText("Prezime");

        TextField jmbgPolje = new TextField(postojeći != null ? postojeći.getJmbg() : "");
        jmbgPolje.setPromptText("JMBG");

        TextField telefonPolje = new TextField(postojeći != null ? postojeći.getTelefon() : "");
        telefonPolje.setPromptText("Telefon");

        TextField emailPolje = new TextField(postojeći != null ? postojeći.getEmail() : "");
        emailPolje.setPromptText("Email");

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

        TextField placenoPolje = new TextField(postojeći != null ? String.valueOf(postojeći.getPlaceno()) : "");
        placenoPolje.setPromptText("Plaćeno do sada (RSD)");

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
                        Double.parseDouble(placenoPolje.getText())
                );
                onSacuvaj.accept(novi);
                stage.close();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Greška u unosu");
                alert.setHeaderText(null);
                alert.setContentText("Proverite da su svi brojevi ispravno uneti.");
                alert.showAndWait();
            }
        });

        VBox forma = new VBox(10,
                imePolje, prezimePolje, jmbgPolje, telefonPolje, emailPolje,
                kategorijaBox, datumUpisaPicker,
                polozioTeoriju, polozioVoznju,
                cenaTeorijaPolje, cenaPraksaPolje,
                placenoPolje, sacuvajBtn
        );
        forma.setPadding(new Insets(20));

        stage.setScene(new Scene(forma, 450, 600));
        stage.showAndWait();
    }
}
