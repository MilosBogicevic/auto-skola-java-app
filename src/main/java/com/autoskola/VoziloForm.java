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

        TextField nazivPolje = new TextField();
        nazivPolje.setPromptText("Naziv vozila");
        if (postojece != null) nazivPolje.setText(postojece.getNaziv());

        TextField tablicePolje = new TextField();
        tablicePolje.setPromptText("Registarske tablice");
        if (postojece != null) tablicePolje.setText(postojece.getTablice());

        DatePicker registracijaPicker = new DatePicker();
        registracijaPicker.setPromptText("Datum registracije");
        registracijaPicker.setConverter(converter);
        if (postojece != null) registracijaPicker.setValue(postojece.getRegistracijaIstice());

        DatePicker tehnickiPicker = new DatePicker();
        tehnickiPicker.setPromptText("Datum tehničkog");
        tehnickiPicker.setConverter(converter);
        if (postojece != null) tehnickiPicker.setValue(postojece.getTehnickiIstice());

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

        VBox layout = new VBox(10, nazivPolje, tablicePolje, registracijaPicker, tehnickiPicker, sacuvajBtn);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-font-size: 16px;");

        stage.setScene(new Scene(layout, 400, 280));
        stage.showAndWait();
    }
}
