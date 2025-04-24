package com.autoskola;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.function.Consumer;

public class UplataForm {

    public UplataForm(int kandidatId, Consumer<Uplata> onSacuvaj) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Nova uplata");

        DatePicker datumPicker = new DatePicker(LocalDate.now());
        datumPicker.setPromptText("Datum uplate");

        TextField iznosField = new TextField();
        iznosField.setPromptText("Iznos uplate (€)");

        Button sacuvajBtn = new Button("Sačuvaj");

        sacuvajBtn.setOnAction(e -> {
            try {
                LocalDate datum = datumPicker.getValue();
                double iznos = Double.parseDouble(iznosField.getText());

                Uplata uplata = new Uplata(0, kandidatId, datum, iznos);
                onSacuvaj.accept(uplata);
                stage.close();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Greška");
                alert.setHeaderText("Neispravan unos");
                alert.setContentText("Proverite da li ste uneli validan iznos.");
                alert.showAndWait();
            }
        });

        VBox layout = new VBox(10, datumPicker, iznosField, sacuvajBtn);
        layout.setPadding(new Insets(20));

        stage.setScene(new Scene(layout, 300, 180));
        stage.showAndWait();
    }
}
