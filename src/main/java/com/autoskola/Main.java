package com.autoskola;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

public class Main extends Application {

    private ObservableList<Kandidat> kandidatiLista = FXCollections.observableArrayList();
    private FilteredList<Kandidat> filtrirano = new FilteredList<>(kandidatiLista, p -> true);
    private Kandidat selektovani = null;

    @Override
    public void start(Stage stage) {
        Database.initialize();
        kandidatiLista.setAll(Database.vratiSve());

        TextField imeField = new TextField();
        TextField prezimeField = new TextField();
        TextField jmbgField = new TextField();
        TextField telefonField = new TextField();
        TextField emailField = new TextField();
        ComboBox<String> kategorijaBox = new ComboBox<>();
        kategorijaBox.getItems().addAll("A", "A1", "B", "C", "CE", "D");

        CheckBox polozioTeorijuCheck = new CheckBox("Položio teoriju");
        CheckBox polozioVoznjuCheck = new CheckBox("Položio vožnju");

        TextField cenaField = new TextField();
        TextField brojRataField = new TextField();
        TextField iznosPoRatiField = new TextField();
        TextField placenoField = new TextField();

        Button sacuvajBtn = new Button("Sačuvaj");
        Button obrisiBtn = new Button("Obriši");
        Button dodajUplatuBtn = new Button("Dodaj uplatu");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));

        form.add(new Label("Ime:"), 0, 0); form.add(imeField, 1, 0);
        form.add(new Label("Prezime:"), 0, 1); form.add(prezimeField, 1, 1);
        form.add(new Label("JMBG:"), 0, 2); form.add(jmbgField, 1, 2);
        form.add(new Label("Telefon:"), 0, 3); form.add(telefonField, 1, 3);
        form.add(new Label("Email:"), 0, 4); form.add(emailField, 1, 4);
        form.add(new Label("Kategorija:"), 0, 5); form.add(kategorijaBox, 1, 5);
        form.add(polozioTeorijuCheck, 0, 6);
        form.add(polozioVoznjuCheck, 1, 6);
        form.add(new Label("Cena (€):"), 0, 7); form.add(cenaField, 1, 7);
        form.add(new Label("Broj rata:"), 0, 8); form.add(brojRataField, 1, 8);
        form.add(new Label("Iznos po rati (€):"), 0, 9); form.add(iznosPoRatiField, 1, 9);
        form.add(new Label("Plaćeno (€):"), 0, 10); form.add(placenoField, 1, 10);
        form.add(sacuvajBtn, 0, 11); form.add(obrisiBtn, 1, 11); form.add(dodajUplatuBtn, 2, 11);

        TextField searchField = new TextField();
        searchField.setPromptText("Pretraga (ime, prezime, jmbg, kategorija)");

        TableView<Kandidat> table = new TableView<>();
        table.setItems(filtrirano);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getColumns().addAll(
                kol("Ime", k -> k.getIme()),
                kol("Prezime", k -> k.getPrezime()),
                kol("JMBG", k -> k.getJmbg()),
                kol("Telefon", k -> k.getTelefon()),
                kol("Email", k -> k.getEmail()),
                kol("Kategorija", k -> k.getKategorija()),
                kol("Položio T", k -> k.isPolozioTeoriju() ? "Da" : "Ne"),
                kol("Položio V", k -> k.isPolozioVoznju() ? "Da" : "Ne"),
                kol("Cena", k -> String.valueOf(k.getCena())),
                kol("Broj rata", k -> String.valueOf(k.getBrojRata())),
                kol("Po rati", k -> String.valueOf(k.getIznosPoRati())),
                kol("Plaćeno", k -> String.valueOf(k.getPlaceno())),
                obojenaKolona("Preostalo", k -> String.format("%.2f", k.getPreostalo()), k -> k.getPreostalo())
        );

        searchField.textProperty().addListener((obs, old, val) -> {
            filtrirano.setPredicate(k ->
                    k.getIme().toLowerCase().contains(val.toLowerCase()) ||
                            k.getPrezime().toLowerCase().contains(val.toLowerCase()) ||
                            k.getJmbg().toLowerCase().contains(val.toLowerCase()) ||
                            k.getKategorija().toLowerCase().contains(val.toLowerCase())
            );
        });

        table.getSelectionModel().selectedItemProperty().addListener((obs, o, k) -> {
            selektovani = k;
            if (k != null) {
                imeField.setText(k.getIme());
                prezimeField.setText(k.getPrezime());
                jmbgField.setText(k.getJmbg());
                telefonField.setText(k.getTelefon());
                emailField.setText(k.getEmail());
                kategorijaBox.setValue(k.getKategorija());
                polozioTeorijuCheck.setSelected(k.isPolozioTeoriju());
                polozioVoznjuCheck.setSelected(k.isPolozioVoznju());
                cenaField.setText(String.valueOf(k.getCena()));
                brojRataField.setText(String.valueOf(k.getBrojRata()));
                iznosPoRatiField.setText(String.valueOf(k.getIznosPoRati()));
                placenoField.setText(String.valueOf(k.getPlaceno()));
            }
        });

        sacuvajBtn.setOnAction(e -> {
            if (imeField.getText().isBlank() || prezimeField.getText().isBlank() ||
                    jmbgField.getText().isBlank() || telefonField.getText().isBlank() ||
                    kategorijaBox.getValue() == null || cenaField.getText().isBlank() ||
                    brojRataField.getText().isBlank() || iznosPoRatiField.getText().isBlank() ||
                    placenoField.getText().isBlank()) {
                prikaziGresku("Greška", "Sva obavezna polja moraju biti popunjena.");
                return;
            }

            try {
                Kandidat novi = new Kandidat(
                        selektovani != null ? selektovani.getId() : 0,
                        imeField.getText(),
                        prezimeField.getText(),
                        jmbgField.getText(),
                        telefonField.getText(),
                        emailField.getText(),
                        kategorijaBox.getValue(),
                        polozioTeorijuCheck.isSelected(),
                        polozioVoznjuCheck.isSelected(),
                        Double.parseDouble(cenaField.getText()),
                        Integer.parseInt(brojRataField.getText()),
                        Double.parseDouble(iznosPoRatiField.getText()),
                        Double.parseDouble(placenoField.getText())
                );

                if (selektovani == null) {
                    Database.sacuvajKandidata(novi);
                } else {
                    Database.izmeniKandidata(novi);
                }

                kandidatiLista.setAll(Database.vratiSve());
                selektovani = null;
                ocistiFormu(imeField, prezimeField, jmbgField, telefonField, emailField,
                        kategorijaBox, polozioTeorijuCheck, polozioVoznjuCheck,
                        cenaField, brojRataField, iznosPoRatiField, placenoField);
            } catch (Exception ex) {
                prikaziGresku("Greška pri unosu", "Unos brojeva mora biti validan.");
            }
        });

        obrisiBtn.setOnAction(e -> {
            if (selektovani != null) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Potvrda brisanja");
                dialog.setHeaderText("Unesite ime i prezime za potvrdu:");
                dialog.setContentText("Unos:");
                dialog.showAndWait().ifPresent(input -> {
                    String expected = selektovani.getIme().trim() + " " + selektovani.getPrezime().trim();
                    if (input.trim().equalsIgnoreCase(expected)) {
                        Database.obrisiKandidata(selektovani.getId());
                        kandidatiLista.setAll(Database.vratiSve());
                        selektovani = null;
                        ocistiFormu(imeField, prezimeField, jmbgField, telefonField, emailField,
                                kategorijaBox, polozioTeorijuCheck, polozioVoznjuCheck,
                                cenaField, brojRataField, iznosPoRatiField, placenoField);
                    } else {
                        prikaziGresku("Greška", "Unos ne odgovara imenu i prezimenu.");
                    }
                });
            }
        });

        dodajUplatuBtn.setOnAction(e -> {
            if (selektovani != null) {
                TextInputDialog uplataDialog = new TextInputDialog();
                uplataDialog.setTitle("Dodaj uplatu");
                uplataDialog.setHeaderText("Unesite iznos uplate:");
                uplataDialog.setContentText("Uplata:");
                uplataDialog.showAndWait().ifPresent(unos -> {
                    try {
                        double dodatno = Double.parseDouble(unos);
                        if (dodatno < 0) throw new NumberFormatException();
                        double novoPlaceno = selektovani.getPlaceno() + dodatno;

                        Kandidat azuriran = new Kandidat(
                                selektovani.getId(), selektovani.getIme(), selektovani.getPrezime(),
                                selektovani.getJmbg(), selektovani.getTelefon(), selektovani.getEmail(),
                                selektovani.getKategorija(), selektovani.isPolozioTeoriju(),
                                selektovani.isPolozioVoznju(), selektovani.getCena(),
                                selektovani.getBrojRata(), selektovani.getIznosPoRati(), novoPlaceno
                        );

                        Database.izmeniKandidata(azuriran);
                        kandidatiLista.setAll(Database.vratiSve());
                    } catch (NumberFormatException ex) {
                        prikaziGresku("Greška", "Unos mora biti pozitivan broj.");
                    }
                });
            } else {
                prikaziGresku("Upozorenje", "Nije selektovan kandidat.");
            }
        });

        VBox root = new VBox(10, form, searchField, table);
        root.setPadding(new Insets(15));
        stage.setTitle("Auto škola – upravljanje kandidatima");
        stage.setScene(new Scene(root, 1000, 800));
        stage.show();
    }

    private void ocistiFormu(TextField ime, TextField prezime, TextField jmbg, TextField tel, TextField email,
                             ComboBox<String> kat, CheckBox pt, CheckBox pv,
                             TextField cena, TextField rata, TextField iznos, TextField placeno) {
        ime.clear(); prezime.clear(); jmbg.clear(); tel.clear(); email.clear();
        kat.setValue(null); pt.setSelected(false); pv.setSelected(false);
        cena.clear(); rata.clear(); iznos.clear(); placeno.clear();
    }

    private void prikaziGresku(String naslov, String poruka) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(naslov);
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }

    private TableColumn<Kandidat, String> kol(String naslov, java.util.function.Function<Kandidat, String> getter) {
        TableColumn<Kandidat, String> kolona = new TableColumn<>(naslov);
        kolona.setCellValueFactory(data -> new SimpleStringProperty(getter.apply(data.getValue())));
        return kolona;
    }

    private TableColumn<Kandidat, String> obojenaKolona(String naslov,
                                                        java.util.function.Function<Kandidat, String> valueFn,
                                                        java.util.function.ToDoubleFunction<Kandidat> dugovanjeFn) {

        TableColumn<Kandidat, String> kolona = new TableColumn<>(naslov);
        kolona.setCellValueFactory(data -> new SimpleStringProperty(valueFn.apply(data.getValue())));
        kolona.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    double dug = dugovanjeFn.applyAsDouble(getTableView().getItems().get(getIndex()));
                    setStyle("-fx-text-fill: " + (dug > 0 ? "red;" : "green;"));
                }
            }
        });
        return kolona;
    }

    public static void main(String[] args) {
        launch();
    }
}
