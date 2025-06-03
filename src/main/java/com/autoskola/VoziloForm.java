package com.autoskola;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class VoziloForm {

    public VoziloForm(Vozilo postojece, ObservableList<Vozilo> postojecaLista, Consumer<Vozilo> onSacuvaj) {
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

        Button sacuvajBtn = new Button("Sačuvaj", IkonicaUtil.napravi("save.png"));
        sacuvajBtn.setGraphicTextGap(8);
        sacuvajBtn.setContentDisplay(ContentDisplay.LEFT);
        sacuvajBtn.setStyle("-fx-font-size: 16px;");
        VBox.setMargin(sacuvajBtn, new Insets(10, 0, 10, 0));

        sacuvajBtn.setOnAction(e -> {
            try {
                if (nazivPolje.getText().isEmpty() || tablicePolje.getText().isEmpty()) {
                    throw new IllegalArgumentException("Sva polja moraju biti popunjena.");
                }

                try {
                    LocalDate datumRegistracije = converter.fromString(registracijaPicker.getEditor().getText().trim());
                    LocalDate datumTehnickog = converter.fromString(tehnickiPicker.getEditor().getText().trim());
                    registracijaPicker.setValue(datumRegistracije);
                    tehnickiPicker.setValue(datumTehnickog);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Datum mora biti u formatu: 01.01.2025.");
                }

                String uneseneTablice = tablicePolje.getText().trim();

                // Ako se dodaje novo vozilo, proveri da li već postoji
                if (postojece == null && postojecaLista.stream()
                        .anyMatch(v -> v.getTablice().equalsIgnoreCase(uneseneTablice))) {
                    throw new IllegalArgumentException("Vozilo sa tim tablicama već postoji.");
                }

                Vozilo novo = new Vozilo(
                        postojece != null ? postojece.getId() : 0,
                        nazivPolje.getText(),
                        uneseneTablice,
                        registracijaPicker.getValue(),
                        tehnickiPicker.getValue()
                );
                onSacuvaj.accept(novo);
                stage.close();
            } catch (IllegalArgumentException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Greška");
                alert.setHeaderText(null);
                alert.setContentText(ex.getMessage()); // ← prikazuje stvarnu poruku iz throw new IllegalArgumentException(...)
                alert.getDialogPane().setStyle("-fx-font-size: 16px;");
                alert.getButtonTypes().setAll(new ButtonType("U redu", ButtonBar.ButtonData.OK_DONE));
                alert.showAndWait();
            }
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox layout = new VBox(12, nazivBox, tabliceBox, regBox, tehnickiBox, spacer, sacuvajBtn);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-font-size: 16px;");

        Scene scene = new Scene(layout, 450, Region.USE_COMPUTED_SIZE);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.showAndWait();
    }
}
