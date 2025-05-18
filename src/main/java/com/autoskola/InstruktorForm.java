package com.autoskola;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class InstruktorForm {

    public InstruktorForm(Instruktor postojeći, Consumer<Instruktor> onSacuvaj) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(postojeći == null ? "Novi instruktor" : "Izmena instruktora");

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

        TextField imePolje = new TextField();
        TextField prezimePolje = new TextField();

        if (postojeći != null) {
            String[] delovi = postojeći.getIme().split(" ", 2);
            imePolje.setText(delovi.length > 0 ? delovi[0] : "");
            prezimePolje.setText(delovi.length > 1 ? delovi[1] : "");
        }

        VBox imeBox = new VBox(5, new Label("Ime:"), imePolje);
        VBox prezimeBox = new VBox(5, new Label("Prezime:"), prezimePolje);
        HBox imePrezimeBox = new HBox(10, imeBox, prezimeBox);
        HBox.setHgrow(imeBox, Priority.ALWAYS);
        HBox.setHgrow(prezimeBox, Priority.ALWAYS);

        DatePicker lekarskiPicker = new DatePicker(postojeći != null ? postojeći.getLekarskiIstice() : null);
        lekarskiPicker.setConverter(converter);

        DatePicker vozackaPicker = new DatePicker(postojeći != null ? postojeći.getVozackaIstice() : null);
        vozackaPicker.setConverter(converter);

        DatePicker licencaPicker = new DatePicker(postojeći != null ? postojeći.getLicencaIstice() : null);
        licencaPicker.setConverter(converter);

        Button sacuvajBtn = new Button("Sačuvaj", IkonicaUtil.napravi("save.png"));
        sacuvajBtn.setGraphicTextGap(8);
        sacuvajBtn.setContentDisplay(ContentDisplay.LEFT);
        sacuvajBtn.setStyle("-fx-font-size: 16px;");

        VBox.setMargin(sacuvajBtn, new Insets(10, 0, 10, 0));

        sacuvajBtn.setOnAction(e -> {
            sacuvajBtn.setDisable(true);

            try {
                String ime = imePolje.getText().trim();
                String prezime = prezimePolje.getText().trim();
                LocalDate lekarski = lekarskiPicker.getValue();
                LocalDate vozacka = vozackaPicker.getValue();
                LocalDate licenca = licencaPicker.getValue();

                if (ime.isEmpty() || prezime.isEmpty() || lekarski == null || vozacka == null || licenca == null) {
                    throw new IllegalArgumentException("Sva polja moraju biti popunjena.");
                }

                Instruktor novi = new Instruktor(
                        postojeći != null ? postojeći.getId() : 0,
                        (ime + " " + prezime).trim(),
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
                alert.setContentText("Sva polja moraju biti ispravno popunjena. " + ex.getMessage());
                alert.showAndWait();
                sacuvajBtn.setDisable(false);
            }
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox lekarskiBox = new VBox(5, new Label("Lekarski pregled ističe:"), lekarskiPicker);
        VBox vozackaBox = new VBox(5, new Label("Vozačka dozvola ističe:"), vozackaPicker);
        VBox licencaBox = new VBox(5, new Label("Licenca ističe:"), licencaPicker);

        VBox layout = new VBox(10,
                imePrezimeBox,
                lekarskiBox,
                vozackaBox,
                licencaBox,
                spacer,
                sacuvajBtn
        );

        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-font-size: 16px;");

        Scene scene = new Scene(layout, 450, Region.USE_COMPUTED_SIZE);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.showAndWait();
    }
}
