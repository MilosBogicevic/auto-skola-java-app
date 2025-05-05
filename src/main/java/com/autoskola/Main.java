package com.autoskola;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import static com.autoskola.Database.connect;

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
        Locale.setDefault(new Locale("sr", "RS"));

        if (!SecurityLock.jeDozvoljenoPokretanje()) {
            prikaziZatvarajuciAlert("Greška", "Ovaj računar nema dozvolu za korišćenje ove aplikacije.", Alert.AlertType.ERROR);
            return;
        }

        if (!AppLock.zakljucaj()) {
            prikaziZatvarajuciAlert("Greška", "Aplikacija je već pokrenuta na drugom računaru.", Alert.AlertType.ERROR);
            return;
        }

        Database.initialize();
        napraviBackupAkoNijeDanasnji();

        // Uklanjanje kandidata kojima je proslo 30 dana od potpune isplate
        LocalDate danas = LocalDate.now();
        for (Kandidat k : Database.vratiSve()) {
            if (k.getPreostalo() == 0 && k.getDatumIsplate() != null) {
                long proslo = ChronoUnit.DAYS.between(k.getDatumIsplate(), danas);
                if (proslo >= 30) {
                    Database.obrisiKandidata(k.getId());
                }
            }
        }

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
                kol("Telefon", Kandidat::getTelefon),
//                kol("Email", Kandidat::getEmail),
                kol("Kategorija", Kandidat::getKategorija),
                kol("Datum upisa", k -> k.getDatumUpisa().format(srpskiFormat)),
                kol("Položena teorija", k -> k.isPolozioTeoriju() ? "da" : "ne"),
                kol("Položena vožnja", k -> k.isPolozioVoznju() ? "da" : "ne"),
                kol("Cena (RSD)", k -> FormatUtil.format(k.getUkupnaCena())),
                kol("Plaćeno", k -> FormatUtil.format(k.getPlaceno())),
                kolBoja("Preostalo", Kandidat::getPreostalo)
        );

        kandidatiTable.setRowFactory(tv -> {
            TableRow<Kandidat> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (row.isEmpty()) {
                    kandidatiTable.getSelectionModel().clearSelection();
                }
            });
            return row;
        });


        instruktoriTable = new TableView<>(instruktoriLista);
        instruktoriTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        instruktoriTable.getColumns().addAll(
                kol("Ime", Instruktor::getIme),
                kol("Lekarski ističe", i -> i.getLekarskiIstice().format(srpskiFormat)),
                kol("Vozačka ističe", i -> i.getVozackaIstice().format(srpskiFormat)),
                kol("Licenca ističe", i -> i.getLicencaIstice().format(srpskiFormat))
        );

        instruktoriTable.setRowFactory(tv -> {
            TableRow<Instruktor> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (row.isEmpty()) {
                    instruktoriTable.getSelectionModel().clearSelection();
                }
            });
            return row;
        });

        vozilaTable = new TableView<>(vozilaLista);
        vozilaTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        vozilaTable.getColumns().addAll(
                kol("Naziv", Vozilo::getNaziv),
                kol("Tablice", Vozilo::getTablice),
                kol("Datum registracije", v -> v.getRegistracijaIstice().format(srpskiFormat)),
                kol("Datum tehničkog", v -> v.getTehnickiIstice().format(srpskiFormat))
        );

        vozilaTable.setRowFactory(tv -> {
            TableRow<Vozilo> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (row.isEmpty()) {
                    vozilaTable.getSelectionModel().clearSelection();
                }
            });
            return row;
        });

        Button dodajKandidata = new Button("Dodaj kandidata");
        Button izmeniKandidata = new Button("Izmeni kandidata");
        Button dodajUplatu = new Button("Dodaj uplatu");
        Button detaljiBtn = new Button("Detalji / Štampa");
        Button dnevniIzvestajBtn = new Button("Dnevni izveštaj");

        dugmiciKandidati = new HBox(10, dnevniIzvestajBtn);

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

                    if (selektovani.getPreostalo() == 0 && selektovani.getDatumIsplate() == null) {
                        LocalDate poslednjaUplata = Database.vratiUplateZaKandidata(selektovani.getId())
                                .stream()
                                .map(Uplata::getDatum)
                                .max(LocalDate::compareTo)
                                .orElse(LocalDate.now()); // fallback ako nema uplata

                        selektovani.setDatumIsplate(poslednjaUplata);
                    }

                    Database.izmeniKandidata(selektovani);

                    // Osvežavanje obaveštenja nakon uplate
                    osveziObavestenja();

                    kandidatiLista.setAll(Database.vratiSve());

                    // ✅ Proveri da li nekome treba obrisati zapis jer je prošlo 30 dana od isplate
                    for (Kandidat k : kandidatiLista) {
                        if (k.getDatumIsplate() != null && k.getPreostalo() == 0) {
                            long proslo = ChronoUnit.DAYS.between(k.getDatumIsplate(), danas);
                            if (proslo >= 30) {
                                Database.obrisiKandidata(k.getId());
                            }
                        }
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

        dnevniIzvestajBtn.setOnAction(e -> {
            new DnevniIzvestajForm();
        });

        dugmiciKandidati = new HBox(10, dodajKandidata, izmeniKandidata, dodajUplatu, detaljiBtn, dnevniIzvestajBtn);

        pretragaField = new TextField();
        pretragaField.setPromptText("🔍 Pretraga po ID broju, imenu ili prezimenu");
        HBox.setHgrow(pretragaField, Priority.ALWAYS);
        pretragaField.setMaxWidth(Double.MAX_VALUE);
        pretragaField.textProperty().addListener((obs, oldVal, newVal) -> {
            kandidatiTable.setItems(kandidatiLista.filtered(k ->
                    (k.getIdKandidata() != null && k.getIdKandidata().toLowerCase().contains(newVal.toLowerCase())) ||
                            k.getIme().toLowerCase().contains(newVal.toLowerCase()) ||
                            k.getPrezime().toLowerCase().contains(newVal.toLowerCase())
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

        Label footerLabel = new Label("Autor aplikacije: Miloš Bogićević. Sajt: ");
        Hyperlink footerLink = new Hyperlink("www.milosb.rs");
        footerLink.setOnAction(e -> getHostServices().showDocument("https://www.milosb.rs"));

        HBox footer = new HBox(footerLabel, footerLink);
        footer.setPadding(new Insets(2, 10, 2, 10));
        footer.setStyle("-fx-background-color: #f0f0f0; -fx-alignment: center; -fx-font-size: 12px;");

        VBox levaStranaSaFooterom = new VBox(10, levaStrana, footer);
        VBox.setVgrow(levaStrana, Priority.ALWAYS);

        HBox glavni = new HBox(20, levaStranaSaFooterom, obavestenjaBox);
        glavni.setPadding(new Insets(15));
        HBox.setHgrow(levaStranaSaFooterom, Priority.ALWAYS);

        stage.setMaximized(true);
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
        col.setCellValueFactory(data -> {
            double iznos = getter.apply(data.getValue());
            return new SimpleStringProperty(FormatUtil.format(iznos));
        });
        col.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    // Moramo parsirati nazad broj za proveru boje
                    try {
                        double iznos = FormatUtil.parse(item);
                        setStyle("-fx-alignment: CENTER; -fx-background-color: " +
                                (iznos > 0 ? "#ffcccc" : "#ccffcc") + ";" + "-fx-text-fill: black;");
                    } catch (Exception e) {
                        setStyle(""); // fallback u slučaju greške u parsiranju
                    }
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
                        " duguje " + FormatUtil.format(razlika) +
                        " za teorijsku obuku");
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

        if (text.contains("duguje")) {
            tabPane.getSelectionModel().select(0);  // Kandidati
            String[] delovi = text.split(" ");
            if (delovi.length >= 3) {
                String ime = delovi[1];
                String prezime = delovi[2];
                kandidatiTable.getSelectionModel().clearSelection();
                for (Kandidat k : kandidatiTable.getItems()) {
                    if (k.getIme().equals(ime) && k.getPrezime().equals(prezime)) {
                        kandidatiTable.getSelectionModel().select(k);
                        kandidatiTable.scrollTo(k);
                        kandidatiTable.requestFocus();
                        break;
                    }
                }
            }
        } else if (text.contains("Lekarski") || text.contains("Vozačka") || text.contains("Licenca")) {
            tabPane.getSelectionModel().select(1);  // Instruktori
            int indeksZa = text.lastIndexOf("za ");
            int indeksOtvorenaZagrada = text.indexOf("(", indeksZa);
            String ime = text.substring(indeksZa + 3, indeksOtvorenaZagrada).trim();

            instruktoriTable.getSelectionModel().clearSelection();
            for (Instruktor i : instruktoriTable.getItems()) {
                if (i.getIme().equals(ime)) {
                    instruktoriTable.getSelectionModel().select(i);
                    instruktoriTable.scrollTo(i);
                    instruktoriTable.requestFocus();
                    break;
                }
            }
        } else if (text.contains("Registracija") || text.contains("Tehnički")) {
            tabPane.getSelectionModel().select(2);  // Vozila
            String tablice = text.substring(text.indexOf("za") + 3, text.indexOf("(")).trim();
            vozilaTable.getSelectionModel().clearSelection();
            for (Vozilo v : vozilaTable.getItems()) {
                if (v.getTablice().equals(tablice)) {
                    vozilaTable.getSelectionModel().select(v);
                    vozilaTable.scrollTo(v);
                    vozilaTable.requestFocus();
                    break;
                }
            }
        }
    }


    private void prikaziPoruku(String tekst) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Obaveštenje");
        alert.setHeaderText(null);
        alert.setContentText(tekst);
        alert.getDialogPane().setStyle("-fx-font-size: 16px;");
        alert.showAndWait();
    }

    private void prikaziZatvarajuciAlert(String naslov, String poruka, Alert.AlertType tip) {
        Alert alert = new Alert(tip);
        alert.setTitle(naslov);
        alert.setHeaderText(null);
        alert.setContentText(poruka);

        DialogPane pane = alert.getDialogPane();
        pane.setStyle("-fx-font-size: 16px; -fx-background-color: white;");
        alert.showAndWait();
        Platform.exit();
    }

    private void napraviBackupAkoNijeDanasnji() {
        try {
            String danas = LocalDate.now().toString();
            Path izvor = Paths.get(Database.getDatabasePath());
            Path backupFolder = Paths.get("backup");
            Path odrediste = backupFolder.resolve("kandidati_" + danas + ".db");

            Files.createDirectories(backupFolder);

            if (!Files.exists(odrediste)) {
                Files.copy(izvor, odrediste, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Backup napravljen: " + odrediste);
            } else {
                System.out.println("Backup već postoji za danas: " + odrediste);
            }

            obrisiStareBackupFajlove();

        } catch (IOException e) {
            System.err.println("Greška prilikom backup-a: " + e.getMessage());
        }
    }

    private void obrisiStareBackupFajlove() {
        Path backupFolder = Paths.get("backup");
        LocalDate danas = LocalDate.now();

        try {
            if (!Files.exists(backupFolder)) return;

            Files.list(backupFolder)
                    .filter(path -> path.getFileName().toString().startsWith("kandidati_") && path.toString().endsWith(".db"))
                    .forEach(path -> {
                        try {
                            String ime = path.getFileName().toString(); // npr. kandidati_2025-04-01.db
                            String datumString = ime.substring("kandidati_".length(), ime.length() - 3); // 2025-04-01
                            LocalDate datum = LocalDate.parse(datumString);

                            long dana = ChronoUnit.DAYS.between(datum, danas);
                            if (dana > 30) {
                                Files.delete(path);
                                System.out.println("Obrisan stari backup: " + path.getFileName());
                            }
                        } catch (Exception e) {
                            System.err.println("Ne mogu da obradim fajl: " + path + " → " + e.getMessage());
                        }
                    });

        } catch (IOException e) {
            System.err.println("Greška prilikom listanja backup foldera: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        launch();
    }
}
