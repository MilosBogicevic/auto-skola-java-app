package com.autoskola;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.function.Consumer;

public class VoziloForm {

    public VoziloForm(Vozilo postojece, Consumer<Vozilo> onSacuvaj) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(postojece == null ? "Novo vozilo" : "Izmena vozila");

        TextField tablicePolje = new TextField();
        tablicePolje.setPromptText("Registarske tablice");
        if (postojece != null) tablicePolje.setText(postojece.getTablice());

        DatePicker registracijaPicker = new DatePicker();
        registracijaPicker.setPromptText("Registracija ističe");
        if (postojece != null) registracijaPicker.setValue(postojece.getRegistracijaIstice());

        DatePicker tehnickiPicker = new DatePicker();
        tehnickiPicker.setPromptText("Tehnički pregled ističe");
        if (postojece != null) tehnickiPicker.setValue(postojece.getTehnickiIstice());

        Button sacuvajBtn = new Button("Sačuvaj");

        sacuvajBtn.setOnAction(e -> {
            try {
                Vozilo novo = new Vozilo(
                        postojece != null ? postojece.getId() : 0,
                        tablicePolje.getText(),
                        registracijaPicker.getValue(),
                        tehnickiPicker.getValue()
                );
                onSacuvaj.accept(novo);
                stage.close();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Greška");
                alert.setHeaderText("Unos nije uspešan");
                alert.setContentText("Proverite da su svi datumi uneti.");
                alert.showAndWait();
            }
        });

        VBox layout = new VBox(10, tablicePolje, registracijaPicker, tehnickiPicker, sacuvajBtn);
        layout.setPadding(new Insets(20));

        stage.setScene(new Scene(layout, 400, 250));
        stage.showAndWait();
    }
}
