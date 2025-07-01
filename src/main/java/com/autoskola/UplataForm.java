package com.autoskola;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class UplataForm {

    public UplataForm(int kandidatId, Consumer<Uplata> onSacuvaj) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Nova uplata");

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

        TextField brojUplateField = new TextField();
        brojUplateField.setPromptText("Broj uplatnice");

        DatePicker datumPicker = new DatePicker(LocalDate.now());
        datumPicker.setPromptText("dd.MM.yyyy.");
        datumPicker.setConverter(converter);

        TextField iznosField = new TextField();
        iznosField.setPromptText("Iznos uplate (RSD)");

        Label nacinLabel = new Label("Način uplate:");
        ToggleGroup grupa = new ToggleGroup();

        RadioButton gotovina = new RadioButton("Gotovina");
        gotovina.setToggleGroup(grupa);
        gotovina.setSelected(true);

        RadioButton racun = new RadioButton("Uplata na račun");
        racun.setToggleGroup(grupa);

        RadioButton zabrana = new RadioButton("Administrativna zabrana");
        zabrana.setToggleGroup(grupa);

        RadioButton cekovi = new RadioButton("Čekovi");
        cekovi.setToggleGroup(grupa);

        VBox radioBox = new VBox(5, gotovina, racun, zabrana, cekovi);
        radioBox.setPadding(new Insets(0, 0, 0, 10));

        Label svrhaLabel = new Label("Svrha uplate:");
        ComboBox<String> svrhaBox = new ComboBox<>();
        svrhaBox.getItems().addAll("Obuka", "Teorijski ispit", "Praktični ispit", "Dopunski čas");
        svrhaBox.setValue("Obuka");

        Button sacuvajBtn = new Button("Sačuvaj", IkonicaUtil.napravi("save.png"));
        sacuvajBtn.setGraphicTextGap(8);
        sacuvajBtn.setContentDisplay(ContentDisplay.LEFT);
        sacuvajBtn.setStyle("-fx-font-size: 16px;");

        sacuvajBtn.setOnAction(e -> {
            try {
                if (datumPicker.getEditor().getText().trim().isEmpty() || iznosField.getText().isEmpty()) {
                    throw new IllegalArgumentException("Morate uneti datum i iznos.");
                }

                String pattern = "^\\d{1,3}(\\.\\d{3})*$|^\\d+$";
                if (!iznosField.getText().matches(pattern)) {
                    throw new IllegalArgumentException("Iznos mora biti u formatu 1.000 ili 1000");
                }

                LocalDate datum;
                try {
                    datum = converter.fromString(datumPicker.getEditor().getText().trim());
                    datumPicker.setValue(datum);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Datum mora biti u formatu: 01.01.2025.");
                }

                double iznos = FormatUtil.parse(iznosField.getText());
                String nacin = ((RadioButton) grupa.getSelectedToggle()).getText();
                String svrha = svrhaBox.getValue();
                String brojUplate = brojUplateField.getText().trim();

                Uplata uplata = new Uplata(0, kandidatId, datum, iznos, nacin, svrha, brojUplate.isEmpty() ? null : brojUplate);
                onSacuvaj.accept(uplata);
                stage.close();
            } catch (ParseException ex) {
                prikaziGresku("Proverite da li je iznos ispravno unet.");
            } catch (Exception ex) {
                prikaziGresku(ex.getMessage());
            }
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox layout = new VBox(10,
                brojUplateField,
                datumPicker,
                iznosField,
                nacinLabel,
                radioBox,
                svrhaLabel,
                svrhaBox,
                spacer,
                sacuvajBtn
        );
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-font-size: 16px;");

        stage.setScene(new Scene(layout, 350, Region.USE_COMPUTED_SIZE));
        stage.setOnShown(ev -> gotovina.requestFocus());
        stage.showAndWait();
    }

    private void prikaziGresku(String poruka) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška u unosu");
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.getDialogPane().setStyle("-fx-font-size: 16px;");
        alert.showAndWait();
    }
}
