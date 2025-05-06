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

public class VoziloForm {

    public VoziloForm(Vozilo postojece, Consumer<Vozilo> onSacuvaj) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(postojece == null ? "Novo vozilo" : "Izmena vozila");

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

        TextField nazivPolje = new TextField(postojece != null ? postojece.getNaziv() : "");
        VBox nazivBox = new VBox(5, new Label("Naziv vozila:"), nazivPolje);

        TextField tablicePolje = new TextField(postojece != null ? postojece.getTablice() : "");
        VBox tabliceBox = new VBox(5, new Label("Registarske tablice:"), tablicePolje);

        DatePicker registracijaPicker = new DatePicker(postojece != null ? postojece.getRegistracijaIstice() : null);
        registracijaPicker.setConverter(converter);
        VBox regBox = new VBox(5, new Label("Datum registracije:"), registracijaPicker);

        DatePicker tehnickiPicker = new DatePicker(postojece != null ? postojece.getTehnickiIstice() : null);
        tehnickiPicker.setConverter(converter);
        VBox tehnickiBox = new VBox(5, new Label("Datum tehničkog pregleda:"), tehnickiPicker);

        Button sacuvajBtn = new Button("Sačuvaj");

        sacuvajBtn.setOnAction(e -> {
            try {
                if (nazivPolje.getText().isEmpty() || tablicePolje.getText().isEmpty()
                        || registracijaPicker.getValue() == null || tehnickiPicker.getValue() == null) {
                    throw new IllegalArgumentException("Sva polja moraju biti popunjena.");
                }

                Vozilo novo = new Vozilo(
                        postojece != null ? postojece.getId() : 0,
                        nazivPolje.getText(),
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
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        });

        VBox layout = new VBox(12, nazivBox, tabliceBox, regBox, tehnickiBox, sacuvajBtn);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-font-size: 16px;");

        stage.setScene(new Scene(layout, 450, 400));
        stage.showAndWait();
    }
}
