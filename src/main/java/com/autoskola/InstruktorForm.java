package com.autoskola;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.function.Consumer;

public class InstruktorForm {

    public InstruktorForm(Instruktor postojeći, Consumer<Instruktor> onSacuvaj) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(postojeći == null ? "Novi instruktor" : "Izmeni instruktora");

        TextField imePolje = new TextField();
        imePolje.setPromptText("Ime i prezime");
        if (postojeći != null) imePolje.setText(postojeći.getIme());

        DatePicker lekarskiPicker = new DatePicker();
        lekarskiPicker.setPromptText("Lekarski ističe");
        if (postojeći != null) lekarskiPicker.setValue(postojeći.getLekarskiIstice());

        DatePicker vozackaPicker = new DatePicker();
        vozackaPicker.setPromptText("Vozačka ističe");
        if (postojeći != null) vozackaPicker.setValue(postojeći.getVozackaIstice());

        DatePicker licencaPicker = new DatePicker();
        licencaPicker.setPromptText("Licenca ističe");
        if (postojeći != null) licencaPicker.setValue(postojeći.getLicencaIstice());

        Button sacuvajBtn = new Button("Sačuvaj");

        sacuvajBtn.setOnAction(e -> {
            try {
                Instruktor novi = new Instruktor(
                        postojeći != null ? postojeći.getId() : 0,
                        imePolje.getText(),
                        lekarskiPicker.getValue(),
                        vozackaPicker.getValue(),
                        licencaPicker.getValue()
                );
                onSacuvaj.accept(novi);
                stage.close();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Greška");
                alert.setHeaderText("Unos nije uspešan");
                alert.setContentText("Proverite da su svi datumi uneti.");
                alert.showAndWait();
            }
        });

        VBox layout = new VBox(10, imePolje, lekarskiPicker, vozackaPicker, licencaPicker, sacuvajBtn);
        layout.setPadding(new Insets(20));

        stage.setScene(new Scene(layout, 400, 300));
        stage.showAndWait();
    }
}
