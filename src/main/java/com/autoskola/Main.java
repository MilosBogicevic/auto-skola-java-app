package com.autoskola;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Main extends Application {

    private ObservableList<Kandidat> kandidatiLista = FXCollections.observableArrayList();
    private ObservableList<Instruktor> instruktoriLista = FXCollections.observableArrayList();
    private ObservableList<Vozilo> vozilaLista = FXCollections.observableArrayList();

    private TableView<Kandidat> kandidatiTable;
    private TableView<Instruktor> instruktoriTable;
    private TableView<Vozilo> vozilaTable;

    private VBox obavestenjaBox;
    private HBox dugmiciKandidati;
    private HBox dugmiciInstruktori, dugmiciVozila;
    private TabPane tabPane;
    private TextField pretragaField;

    @Override
    public void start(Stage stage) {
        Database.initialize();

        kandidatiLista.setAll(Database.vratiSve());
        instruktoriLista.setAll(Database.vratiInstruktore());
        vozilaLista.setAll(Database.vratiVozila());

        kandidatiTable = new TableView<>(kandidatiLista);
        kandidatiTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        DateTimeFormatter srpskiFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
        kandidatiTable.getColumns().addAll(
                kol("R. br.", k -> String.valueOf(k.getId())),
                kol("ID br.", Kandidat::getIdKandidata),
                kol("Ime", Kandidat::getIme),
                kol("Prezime", Kandidat::getPrezime),
                kol("JMBG", Kandidat::getJmbg),
                kol("Telefon", Kandidat::getTelefon),
                kol("Email", Kandidat::getEmail),
                kol("Kategorija", Kandidat::getKategorija),
                kol("Datum upisa", k -> k.getDatumUpisa().format(srpskiFormat)),
                kol("Položena teorija", k -> k.isPolozioTeoriju() ? "DA" : "NE"),
                kol("Položena vožnja", k -> k.isPolozioVoznju() ? "DA" : "NE"),
                kol("Cena (RSD)", k -> String.format("%.0f", k.getUkupnaCena())),
                kol("Plaćeno", k -> String.format("%.0f", k.getPlaceno())),
                kolBoja("Preostalo", Kandidat::getPreostalo)
        );

        instruktoriTable = new TableView<>(instruktoriLista);
        instruktoriTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        instruktoriTable.getColumns().addAll(
                kol("Ime", Instruktor::getIme),
                kol("Lekarski", i -> i.getLekarskiIstice().format(srpskiFormat)),
                kol("Vozačka", i -> i.getVozackaIstice().format(srpskiFormat)),
                kol("Licenca", i -> i.getLicencaIstice().format(srpskiFormat))
        );

        vozilaTable = new TableView<>(vozilaLista);
        vozilaTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        vozilaTable.getColumns().addAll(
                kol("Naziv", Vozilo::getNaziv),
                kol("Tablice", Vozilo::getTablice),
                kol("Registracija", v -> v.getRegistracijaIstice().format(srpskiFormat)),
                kol("Tehnički", v -> v.getTehnickiIstice().format(srpskiFormat))
        );

        Button dodajKandidata = new Button("Dodaj kandidata");
        Button izmeniKandidata = new Button("Izmeni kandidata");
        Button dodajUplatu = new Button("Dodaj uplatu");
        Button detaljiBtn = new Button("Detalji / Štampa");

        dodajKandidata.setOnAction(e -> new KandidatForm(null, k -> {
            Database.sacuvajKandidata(k);
            kandidatiLista.setAll(Database.vratiSve());
            osveziObavestenja();
        }));

        izmeniKandidata.setOnAction(e -> {
            Kandidat k = kandidatiTable.getSelectionModel().getSelectedItem();
            if (k != null) {
                new KandidatForm(k, izmenjen -> {
                    Database.izmeniKandidata(izmenjen);
                    kandidatiLista.setAll(Database.vratiSve());
                    osveziObavestenja();
                });
            } else {
                prikaziPoruku("Niste selektovali kandidata.");
            }
        });

        dodajUplatu.setOnAction(e -> {
            Kandidat selektovani = kandidatiTable.getSelectionModel().getSelectedItem();
            if (selektovani != null) {
                new UplataForm(selektovani.getId(), u -> {
                    Database.sacuvajUplatu(u);

                    double ukupno = Database.vratiUplateZaKandidata(selektovani.getId())
                            .stream().mapToDouble(Uplata::getIznos).sum();

                    selektovani.setPlaceno(ukupno);
                    Database.izmeniKandidata(selektovani);

                    // Osvežavanje obaveštenja nakon uplate
                    osveziObavestenja();

                    kandidatiLista.setAll(Database.vratiSve());
                });
            } else {
                prikaziPoruku("Niste selektovali kandidata.");
            }
        });

        detaljiBtn.setOnAction(e -> {
            Kandidat k = kandidatiTable.getSelectionModel().getSelectedItem();
            if (k != null) {
                new KandidatDetaljiForm(k);
            } else {
                prikaziPoruku("Niste selektovali kandidata.");
            }
        });

        dugmiciKandidati = new HBox(10, dodajKandidata, izmeniKandidata, dodajUplatu, detaljiBtn);

        pretragaField = new TextField();
        pretragaField.setPromptText("🔍 Pretraga po ID broju kandidata");
        HBox.setHgrow(pretragaField, Priority.ALWAYS);
        pretragaField.setMaxWidth(Double.MAX_VALUE);
        pretragaField.textProperty().addListener((obs, oldVal, newVal) -> {
            kandidatiTable.setItems(kandidatiLista.filtered(k ->
                    k.getIdKandidata() != null && k.getIdKandidata().toLowerCase().contains(newVal.toLowerCase())
            ));
        });

        Button dodajInstruktora = new Button("Dodaj instruktora");
        Button izmeniInstruktora = new Button("Izmeni instruktora");

        dodajInstruktora.setOnAction(e -> new InstruktorForm(null, i -> {
            Database.sacuvajInstruktora(i);
            instruktoriLista.setAll(Database.vratiInstruktore());
            osveziObavestenja();
        }));

        izmeniInstruktora.setOnAction(e -> {
            Instruktor i = instruktoriTable.getSelectionModel().getSelectedItem();
            if (i != null) {
                new InstruktorForm(i, izmenjen -> {
                    Database.izmeniInstruktora(izmenjen);
                    instruktoriLista.setAll(Database.vratiInstruktore());
                    osveziObavestenja();
                });
            } else {
                prikaziPoruku("Niste selektovali instruktora.");
            }
        });

        dugmiciInstruktori = new HBox(10, dodajInstruktora, izmeniInstruktora);

        Button dodajVozilo = new Button("Dodaj vozilo");
        Button izmeniVozilo = new Button("Izmeni vozilo");

        dodajVozilo.setOnAction(e -> new VoziloForm(null, v -> {
            Database.sacuvajVozilo(v);
            vozilaLista.setAll(Database.vratiVozila());
            osveziObavestenja();
        }));

        izmeniVozilo.setOnAction(e -> {
            Vozilo v = vozilaTable.getSelectionModel().getSelectedItem();
            if (v != null) {
                new VoziloForm(v, izmenjeno -> {
                    Database.izmeniVozilo(izmenjeno);
                    vozilaLista.setAll(Database.vratiVozila());
                    osveziObavestenja();
                });
            } else {
                prikaziPoruku("Niste selektovali vozilo.");
            }
        });

        dugmiciVozila = new HBox(10, dodajVozilo, izmeniVozilo);

        tabPane = new TabPane(
                new Tab("Kandidati", kandidatiTable),
                new Tab("Instruktori", instruktoriTable),
                new Tab("Vozila", vozilaTable)
        );
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        HBox dugmici = new HBox(10, dugmiciKandidati, pretragaField);
        tabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            switch (newVal.intValue()) {
                case 0 -> dugmici.getChildren().setAll(dugmiciKandidati, pretragaField);
                case 1 -> dugmici.getChildren().setAll(dugmiciInstruktori);
                case 2 -> dugmici.getChildren().setAll(dugmiciVozila);
            }
        });

        VBox levaStrana = new VBox(10, dugmici, tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        obavestenjaBox = new VBox(10);
        obavestenjaBox.setPadding(new Insets(10));
        obavestenjaBox.setPrefWidth(420);
        obavestenjaBox.setStyle("-fx-background-color: #fdfdfd; -fx-border-color: #ccc;");
        Label naslov = new Label("Obaveštenja");
        naslov.setStyle("-fx-font-weight: bold;");
        obavestenjaBox.getChildren().add(naslov);
        osveziObavestenja();

        HBox glavni = new HBox(20, levaStrana, obavestenjaBox);
        glavni.setPadding(new Insets(15));
        HBox.setHgrow(levaStrana, Priority.ALWAYS);

        stage.setTitle("Auto škola – Upravljanje");
        Scene scene = new Scene(glavni, 1350, 700);
        glavni.setStyle("-fx-font-size: 16px;");
        stage.setScene(scene);
        stage.show();
    }

    private <T> TableColumn<T, String> kol(String naslov, java.util.function.Function<T, String> getter) {
        TableColumn<T, String> col = new TableColumn<>(naslov);
        col.setCellValueFactory(data -> new SimpleStringProperty(getter.apply(data.getValue())));
        return col;
    }

    private TableColumn<Kandidat, String> kolBoja(String naslov, java.util.function.Function<Kandidat, Double> getter) {
        TableColumn<Kandidat, String> col = new TableColumn<>(naslov);
        col.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.0f", getter.apply(data.getValue()))));
        col.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    double iznos = Double.parseDouble(item);
                    setStyle("-fx-alignment: CENTER; -fx-background-color: " +
                            (iznos > 0 ? "#ffcccc" : "#ccffcc") + ";");
                }
            }
        });
        return col;
    }

    private void osveziObavestenja() {
        obavestenjaBox.getChildren().removeIf(n -> n instanceof Label && n != obavestenjaBox.getChildren().get(0));

        LocalDate danas = LocalDate.now();

        boolean imaKandidata = false;
        for (Kandidat k : kandidatiLista) {
            long dana = ChronoUnit.DAYS.between(k.getDatumUpisa(), danas);
            double razlika = k.getCenaTeorija() - k.getPlaceno();

            // Ako kandidat nije platio dovoljno za teoriju, dodaj obaveštenje
            if (dana >= 30 && razlika > 0) {
                Label l = new Label("❗ " + k.getIme() + " " + k.getPrezime() +
                        " duguje " + String.format("%,.0f RSD", razlika) +
                        " za teoriju (" + dana + " dana od upisa)");
                l.setStyle("-fx-text-fill: red;");
                obavestenjaBox.getChildren().add(l);
                imaKandidata = true;
            }
        }

        // Instruktori i vozila
        ObavestenjaHelper.prikaziObavestenjaInstruktora(obavestenjaBox);
        ObavestenjaHelper.prikaziObavestenjaVozila(obavestenjaBox);

        // Ako nema obaveštenja, prikaži poruku
        if (obavestenjaBox.getChildren().size() == 1) {
            Label nema = new Label("✓ Nema aktivnih obaveštenja.");
            nema.setStyle("-fx-text-fill: green;");
            obavestenjaBox.getChildren().add(nema);
        }

        // Dodavanje klikabilnih obaveštenja
        for (var node : obavestenjaBox.getChildren()) {
            if (node instanceof Label l && !l.getText().startsWith("📢") && !l.getText().startsWith("✓")) {
                l.setOnMouseClicked(this::obradiKlikNaObavestenje);
            }
        }
    }


    private void obradiKlikNaObavestenje(MouseEvent event) {
        String text = ((Label) event.getSource()).getText();
        // Ako je obaveštenje o kandidatu
        if (text.contains("nije platio teoriju") || text.contains("duguje")) {
            tabPane.getSelectionModel().select(0);  // Otvori Tab za Kandidate
        }
        // Ako je obaveštenje o instruktoru
        else if (text.contains("Lekarski") || text.contains("Vozačka") || text.contains("Licenca")) {
            tabPane.getSelectionModel().select(1);  // Otvori Tab za Instruktore
        }
        // Ako je obaveštenje o vozilu
        else if (text.contains("Registracija") || text.contains("Tehnički")) {
            tabPane.getSelectionModel().select(2);  // Otvori Tab za Vozila
        }
    }

    private void prikaziPoruku(String tekst) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Obaveštenje");
        alert.setHeaderText(null);
        alert.setContentText(tekst);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}
