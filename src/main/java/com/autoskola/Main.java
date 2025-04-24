package com.autoskola;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Main extends Application {

    private ObservableList<Kandidat> kandidatiLista = FXCollections.observableArrayList();
    private TableView<Kandidat> table;
    private Kandidat selektovani;
    private VBox obavestenjaBox;

    @Override
    public void start(Stage stage) {
        Database.initialize();
        kandidatiLista.setAll(Database.vratiSve());

        table = new TableView<>(kandidatiLista);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> selektovani = newSel);

        table.getColumns().addAll(
                kol("Ime", k -> k.getIme()),
                kol("Prezime", k -> k.getPrezime()),
                kol("JMBG", k -> k.getJmbg()),
                kol("Telefon", k -> k.getTelefon()),
                kol("Kategorija", k -> k.getKategorija()),
                kol("Datum upisa", k -> String.valueOf(k.getDatumUpisa()))
        );

        Button dodajKandidata = new Button("➕ Novi kandidat");
        Button izmeniKandidata = new Button("✏️ Izmeni kandidata");
        Button dodajInstruktora = new Button("👨‍🏫 Dodaj instruktora");
        Button dodajVozilo = new Button("🚗 Dodaj vozilo");

        dodajKandidata.setOnAction(e -> new KandidatForm(null, k -> {
            Database.sacuvajKandidata(k);
            osveziPodatke();
        }));

        izmeniKandidata.setOnAction(e -> {
            if (selektovani == null) {
                prikaziPoruku("Niste izabrali kandidata.");
                return;
            }
            new KandidatForm(selektovani, k -> {
                Database.izmeniKandidata(k);
                osveziPodatke();
            });
        });

        dodajInstruktora.setOnAction(e -> new InstruktorForm(null, i -> {
            Database.sacuvajInstruktora(i);
            osveziPodatke();
        }));

        dodajVozilo.setOnAction(e -> new VoziloForm(null, v -> {
            Database.sacuvajVozilo(v);
            osveziPodatke();
        }));

        HBox dugmad = new HBox(10, dodajKandidata, izmeniKandidata, dodajInstruktora, dodajVozilo);
        dugmad.setPadding(new Insets(10));

        obavestenjaBox = new VBox(10);
        obavestenjaBox.setPadding(new Insets(10));
        obavestenjaBox.setPrefWidth(420);
        obavestenjaBox.setStyle("-fx-background-color: #fdfdfd; -fx-border-color: #ccc;");
        Label naslovObavestenja = new Label("📢 Obaveštenja");
        naslovObavestenja.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        obavestenjaBox.getChildren().add(naslovObavestenja);

        osveziObavestenja();

        VBox levaKolona = new VBox(10, dugmad, table);
        HBox glavniLayout = new HBox(20, levaKolona, obavestenjaBox);
        glavniLayout.setPadding(new Insets(15));

        stage.setTitle("Auto škola – Upravljanje");
        stage.setScene(new Scene(glavniLayout, 1200, 650));
        stage.show();
    }

    private TableColumn<Kandidat, String> kol(String naslov, java.util.function.Function<Kandidat, String> getter) {
        TableColumn<Kandidat, String> col = new TableColumn<>(naslov);
        col.setCellValueFactory(data -> new SimpleStringProperty(getter.apply(data.getValue())));
        return col;
    }

    private void prikaziPoruku(String poruka) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Obaveštenje");
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }

    private void osveziPodatke() {
        kandidatiLista.setAll(Database.vratiSve());
        osveziObavestenja();
    }

    private void osveziObavestenja() {
        obavestenjaBox.getChildren().removeIf(n -> n instanceof Label && n != obavestenjaBox.getChildren().get(0));

        LocalDate danas = LocalDate.now();

        // Kandidati – teorija neplaćena duže od 30 dana
        for (Kandidat k : kandidatiLista) {
            long dana = ChronoUnit.DAYS.between(k.getDatumUpisa(), danas);
            boolean nijePlatioTeoriju = k.getPlaceno() < k.getCenaTeorija();

            if (dana >= 30 && nijePlatioTeoriju) {
                Label l = new Label("❗ " + k.getIme() + " " + k.getPrezime() + " nije platio teoriju (" + dana + " dana)");
                l.setStyle("-fx-text-fill: red;");
                obavestenjaBox.getChildren().add(l);
            }
        }

        // Instruktori
        ObavestenjaHelper.prikaziObavestenjaInstruktora(obavestenjaBox);

        // Vozila
        ObavestenjaHelper.prikaziObavestenjaVozila(obavestenjaBox);

        if (obavestenjaBox.getChildren().size() == 1) {
            Label nema = new Label("✓ Nema aktivnih obaveštenja.");
            nema.setStyle("-fx-text-fill: green;");
            obavestenjaBox.getChildren().add(nema);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
