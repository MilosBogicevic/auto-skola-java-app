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

    @Override
    public void start(Stage stage) {
        Database.initialize();

        kandidatiLista.setAll(Database.vratiSve());
        instruktoriLista.setAll(Database.vratiInstruktore());
        vozilaLista.setAll(Database.vratiVozila());

        kandidatiTable = new TableView<>(kandidatiLista);
        kandidatiTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        kandidatiTable.getColumns().addAll(
                kol("ID baze", k -> String.valueOf(k.getId())),
                kol("ID kandidata", Kandidat::getIdKandidata),
                kol("Ime", Kandidat::getIme),
                kol("Prezime", Kandidat::getPrezime),
                kol("JMBG", Kandidat::getJmbg),
                kol("Telefon", Kandidat::getTelefon),
                kol("Email", Kandidat::getEmail),
                kol("Kategorija", Kandidat::getKategorija),
                kol("Datum upisa", k -> k.getDatumUpisa().toString()),
                kol("Teorija", k -> k.isPolozioTeoriju() ? "✔️" : "❌"),
                kol("Voznja", k -> k.isPolozioVoznju() ? "✔️" : "❌"),
                kol("Cena (RSD)", k -> String.format("%.0f", k.getUkupnaCena())),
                kol("Plaćeno (RSD)", k -> String.format("%.0f", k.getPlaceno())),
                kolBoja("Preostalo (RSD)", Kandidat::getPreostalo)
        );

        instruktoriTable = new TableView<>(instruktoriLista);
        instruktoriTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        instruktoriTable.getColumns().addAll(
                kol("Ime", Instruktor::getIme),
                kol("Lekarski", i -> i.getLekarskiIstice().toString()),
                kol("Vozačka", i -> i.getVozackaIstice().toString()),
                kol("Licenca", i -> i.getLicencaIstice().toString())
        );

        vozilaTable = new TableView<>(vozilaLista);
        vozilaTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        vozilaTable.getColumns().addAll(
                kol("Naziv", Vozilo::getNaziv),
                kol("Tablice", Vozilo::getTablice),
                kol("Registracija", v -> v.getRegistracijaIstice().toString()),
                kol("Tehnički", v -> v.getTehnickiIstice().toString())
        );

        Button dodajKandidata = new Button("➕ Novi kandidat");
        Button izmeniKandidata = new Button("✏️ Izmeni kandidata");
        Button dodajUplatu = new Button("💵 Dodaj uplatu");
        Button detaljiBtn = new Button("📄 Detalji / Štampa");

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

                    // Preračunaj ukupno
                    double ukupno = Database.vratiUplateZaKandidata(selektovani.getId())
                            .stream()
                            .mapToDouble(Uplata::getIznos)
                            .sum();

                    // Osveži kandidata iz baze
                    Kandidat azuriran = Database.vratiSve().stream()
                            .filter(k -> k.getId() == selektovani.getId())
                            .findFirst()
                            .orElse(null);

                    if (azuriran != null) {
                        azuriran.setPlaceno(ukupno);
                        Database.izmeniKandidata(azuriran);
                    }

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

        Button dodajInstruktora = new Button("➕ Novi instruktor");
        Button izmeniInstruktora = new Button("✏️ Izmeni instruktora");

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

        Button dodajVozilo = new Button("➕ Novo vozilo");
        Button izmeniVozilo = new Button("✏️ Izmeni vozilo");

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

        TextField pretragaField = new TextField();
        pretragaField.setPromptText("🔍 Pretraga po ID kandidata");
        pretragaField.textProperty().addListener((obs, oldVal, newVal) -> {
            kandidatiTable.setItems(kandidatiLista.filtered(k ->
                    k.getIdKandidata() != null && k.getIdKandidata().toLowerCase().contains(newVal.toLowerCase())
            ));
        });

        tabPane = new TabPane(
                new Tab("Kandidati", kandidatiTable),
                new Tab("Instruktori", instruktoriTable),
                new Tab("Vozila", vozilaTable)
        );
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        VBox dugmici = new VBox();
        tabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            switch (newVal.intValue()) {
                case 0 -> dugmici.getChildren().setAll(dugmiciKandidati);
                case 1 -> dugmici.getChildren().setAll(dugmiciInstruktori);
                case 2 -> dugmici.getChildren().setAll(dugmiciVozila);
            }
        });
        dugmici.getChildren().setAll(dugmiciKandidati);

        VBox levaStrana = new VBox(10, pretragaField, dugmici, tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        obavestenjaBox = new VBox(10);
        obavestenjaBox.setPadding(new Insets(10));
        obavestenjaBox.setPrefWidth(420);
        obavestenjaBox.setStyle("-fx-background-color: #fdfdfd; -fx-border-color: #ccc;");
        Label naslov = new Label("📢 Obaveštenja");
        naslov.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        obavestenjaBox.getChildren().add(naslov);
        osveziObavestenja();

        HBox glavni = new HBox(20, levaStrana, obavestenjaBox);
        glavni.setPadding(new Insets(15));
        HBox.setHgrow(levaStrana, Priority.ALWAYS);

        stage.setTitle("Auto škola – Upravljanje");
        stage.setScene(new Scene(glavni, 1350, 700));
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
        for (Kandidat k : kandidatiLista) {
            long dana = ChronoUnit.DAYS.between(k.getDatumUpisa(), danas);
            if (dana >= 30 && k.getPlaceno() < k.getCenaTeorija()) {
                Label l = new Label("❗ " + k.getIme() + " " + k.getPrezime() + " nije platio teoriju (" + dana + " dana)");
                l.setStyle("-fx-text-fill: red;");
                obavestenjaBox.getChildren().add(l);
            }
        }
        ObavestenjaHelper.prikaziObavestenjaInstruktora(obavestenjaBox);
        ObavestenjaHelper.prikaziObavestenjaVozila(obavestenjaBox);

        if (obavestenjaBox.getChildren().size() == 1) {
            Label nema = new Label("✓ Nema aktivnih obaveštenja.");
            nema.setStyle("-fx-text-fill: green;");
            obavestenjaBox.getChildren().add(nema);
        }

        for (var node : obavestenjaBox.getChildren()) {
            if (node instanceof Label l && !l.getText().startsWith("📢") && !l.getText().startsWith("✓")) {
                l.setOnMouseClicked(this::obradiKlikNaObavestenje);
            }
        }
    }

    private void obradiKlikNaObavestenje(MouseEvent event) {
        String text = ((Label) event.getSource()).getText();
        if (text.contains("Lekarski") || text.contains("Vozačka") || text.contains("Licenca")) {
            tabPane.getSelectionModel().select(1);
        } else if (text.contains("Registracija") || text.contains("Tehnički")) {
            tabPane.getSelectionModel().select(2);
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
