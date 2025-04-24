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
        stage.setTitle(postojeći == null ? "Novi instruktor" : "Izmena instruktora");

        TextField imePolje = new TextField(postojeći != null ? postojeći.getIme() : "");
        imePolje.setPromptText("Ime i prezime");

        DatePicker lekarskiPicker = new DatePicker(postojeći != null ? postojeći.getLekarskiIstice() : null);
        lekarskiPicker.setPromptText("Lekarski ističe");

        DatePicker vozackaPicker = new DatePicker(postojeći != null ? postojeći.getVozackaIstice() : null);
        vozackaPicker.setPromptText("Vozačka ističe");

        DatePicker licencaPicker = new DatePicker(postojeći != null ? postojeći.getLicencaIstice() : null);
        licencaPicker.setPromptText("Licenca ističe");

        Button sacuvajBtn = new Button("Sačuvaj");

        sacuvajBtn.setOnAction(e -> {
            // Disable button to prevent multiple clicks
            sacuvajBtn.setDisable(true);

            try {
                String ime = imePolje.getText().trim();
                LocalDate lekarski = lekarskiPicker.getValue();
                LocalDate vozacka = vozackaPicker.getValue();
                LocalDate licenca = licencaPicker.getValue();

                if (ime.isEmpty() || lekarski == null || vozacka == null || licenca == null) {
                    throw new IllegalArgumentException("Sva polja moraju biti popunjena.");
                }

                Instruktor novi = new Instruktor(
                        postojeći != null ? postojeći.getId() : 0,
                        ime,
                        lekarski,
                        vozacka,
                        licenca
                );

                onSacuvaj.accept(novi);
                stage.close();

            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Greška");
                alert.setHeaderText("Neispravan unos");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
                sacuvajBtn.setDisable(false); // Re-enable button if failed
            }
        });

        VBox layout = new VBox(10, imePolje, lekarskiPicker, vozackaPicker, licencaPicker, sacuvajBtn);
        layout.setPadding(new Insets(20));

        stage.setScene(new Scene(layout, 400, 300));
        stage.showAndWait();
    }
}
