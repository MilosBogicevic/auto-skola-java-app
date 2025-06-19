package com.autoskola;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UplataVanEvidencijeForm {

    private final Runnable onSacuvano;
    private final DateTimeFormatter srpskiFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    public UplataVanEvidencijeForm(Runnable onSacuvano) {
        this.onSacuvano = onSacuvano;
    }

    public void prikazi() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Uplata van evidencije");

        StringConverter<LocalDate> converter = new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? srpskiFormat.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, srpskiFormat) : null;
            }
        };

        TextField brojKandidataField = new TextField();
        brojKandidataField.setPromptText("Broj kandidata");

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
                if (datumPicker.getEditor().getText().trim().isEmpty()
                        || iznosField.getText().isEmpty()
                        || brojKandidataField.getText().trim().isEmpty()) {
                    throw new IllegalArgumentException("Popunite sva polja.");
                }

                String pattern = "^\\d{1,3}(\\.\\d{3})*$|^\\d+$";
                if (!iznosField.getText().matches(pattern)) {
                    throw new IllegalArgumentException("Iznos mora biti u formatu 1.000 ili 1000");
                }

                LocalDate datum;
                try {
                    datum = converter.fromString(datumPicker.getEditor().getText().trim());
                    datumPicker.setValue(datum); // osveži picker
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Datum mora biti u formatu: 01.01.2025.");
                }

                double iznos = FormatUtil.parse(iznosField.getText());
                String nacin = ((RadioButton) grupa.getSelectedToggle()).getText();
                String svrha = svrhaBox.getValue();
                String broj = brojKandidataField.getText().trim();

                File fajl = new File("van_evidencije.csv");
                boolean noviFajl = fajl.createNewFile();

                try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fajl, true), java.nio.charset.StandardCharsets.UTF_8))) {
                    if (noviFajl) {
                        out.println("datum;brojKandidata;iznos;svrha;nacin");
                    }

                    String red = String.join(";",
                            datum.toString(),
                            broj,
                            String.valueOf((int) iznos),
                            svrha.replace(";", ","),
                            nacin.replace(";", ",")
                    );

                    out.println(red);
                }

                // Osvežavanje prikaza odmah nakon snimanja
                if (onSacuvano != null) {
                    Platform.runLater(onSacuvano);
                }

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
                brojKandidataField,
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

        stage.setScene(new Scene(layout, 320, 500));
        stage.setOnShown(ev -> iznosField.requestFocus());
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
