package com.autoskola;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String URL;
    static {
        String path;
        try {
            path = new File(System.getProperty("user.dir")).getAbsolutePath();
        } catch (Exception e) {
            path = new File("").getAbsolutePath(); // fallback
        }
        URL = "jdbc:sqlite:" + path + File.separator + "kandidati.db";
    }

    public static void initialize() {
        try (Connection conn = connect()) {
            Statement st = conn.createStatement();

            st.execute("""
                CREATE TABLE IF NOT EXISTS kandidati (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_kandidata TEXT,
                    ime TEXT,
                    prezime TEXT,
                    idb TEXT,
                    jmbg TEXT,
                    broj_licne_karte TEXT,
                    adresa TEXT,
                    grad TEXT,
                    telefon TEXT,
                    email TEXT,
                    kategorija TEXT,
                    polozio_teoriju INTEGER,
                    polozio_voznju INTEGER,
                    datum_upisa TEXT,
                    cena_teorija REAL,
                    cena_praksa REAL,
                    placeno REAL,
                    datum_isplate TEXT
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS instruktori (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    ime TEXT,
                    lekarski TEXT,
                    vozacka TEXT,
                    licenca TEXT
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS vozila (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    naziv TEXT,
                    tablice TEXT,
                    registracija TEXT,
                    tehnicki TEXT
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS uplate (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    kandidat_id INTEGER,
                    datum TEXT,
                    iznos REAL
                );
            """);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static String getDatabasePath() {
        return URL.replace("jdbc:sqlite:", "");
    }

    // === KANDIDATI ===

    public static void sacuvajKandidata(Kandidat k) {
        String sql = """
            INSERT INTO kandidati (
                id_kandidata, ime, prezime, idb, jmbg, broj_licne_karte,
                adresa, grad, telefon, email, kategorija,
                polozio_teoriju, polozio_voznju, datum_upisa,
                cena_teorija, cena_praksa, placeno, datum_isplate
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, k.getIdKandidata());
            stmt.setString(2, k.getIme());
            stmt.setString(3, k.getPrezime());
            stmt.setString(4, k.getIdb());
            stmt.setString(5, k.getJmbg());
            stmt.setString(6, k.getBrojLicneKarte());
            stmt.setString(7, k.getAdresa());
            stmt.setString(8, k.getGrad());
            stmt.setString(9, k.getTelefon());
            stmt.setString(10, k.getEmail());
            stmt.setString(11, k.getKategorija());
            stmt.setBoolean(12, k.isPolozioTeoriju());
            stmt.setBoolean(13, k.isPolozioVoznju());
            stmt.setString(14, k.getDatumUpisa().toString());
            stmt.setDouble(15, k.getCenaTeorija());
            stmt.setDouble(16, k.getCenaPraksa());
            stmt.setDouble(17, k.getPlaceno());
            stmt.setString(18, k.getDatumIsplate() != null ? k.getDatumIsplate().toString() : null);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void izmeniKandidata(Kandidat k) {
        String sql = """
            UPDATE kandidati SET
                id_kandidata = ?, ime = ?, prezime = ?, idb = ?, jmbg = ?, broj_licne_karte = ?,
                adresa = ?, grad = ?, telefon = ?, email = ?, kategorija = ?,
                polozio_teoriju = ?, polozio_voznju = ?, datum_upisa = ?,
                cena_teorija = ?, cena_praksa = ?, placeno = ?, datum_isplate = ?
            WHERE id = ?
        """;

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, k.getIdKandidata());
            stmt.setString(2, k.getIme());
            stmt.setString(3, k.getPrezime());
            stmt.setString(4, k.getIdb());
            stmt.setString(5, k.getJmbg());
            stmt.setString(6, k.getBrojLicneKarte());
            stmt.setString(7, k.getAdresa());
            stmt.setString(8, k.getGrad());
            stmt.setString(9, k.getTelefon());
            stmt.setString(10, k.getEmail());
            stmt.setString(11, k.getKategorija());
            stmt.setBoolean(12, k.isPolozioTeoriju());
            stmt.setBoolean(13, k.isPolozioVoznju());
            stmt.setString(14, k.getDatumUpisa().toString());
            stmt.setDouble(15, k.getCenaTeorija());
            stmt.setDouble(16, k.getCenaPraksa());
            stmt.setDouble(17, k.getPlaceno());
            stmt.setString(18, k.getDatumIsplate() != null ? k.getDatumIsplate().toString() : null);
            stmt.setInt(19, k.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Kandidat> vratiSve() {
        List<Kandidat> lista = new ArrayList<>();
        String sql = "SELECT * FROM kandidati";

        try (Connection conn = connect(); ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                LocalDate datumIsplate = rs.getString("datum_isplate") != null
                        ? LocalDate.parse(rs.getString("datum_isplate")) : null;

                Kandidat k = new Kandidat(
                        rs.getInt("id"),
                        rs.getString("id_kandidata"),
                        rs.getString("ime"),
                        rs.getString("prezime"),
                        rs.getString("idb"),
                        rs.getString("jmbg"),
                        rs.getString("broj_licne_karte"),
                        rs.getString("adresa"),
                        rs.getString("grad"),
                        rs.getString("telefon"),
                        rs.getString("email"),
                        rs.getString("kategorija"),
                        rs.getBoolean("polozio_teoriju"),
                        rs.getBoolean("polozio_voznju"),
                        LocalDate.parse(rs.getString("datum_upisa")),
                        rs.getDouble("cena_teorija"),
                        rs.getDouble("cena_praksa"),
                        rs.getDouble("placeno"),
                        datumIsplate
                );
                lista.add(k);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    public static Kandidat vratiKandidataPoId(int kandidatId) {
        Kandidat kandidat = null;
        String sql = "SELECT * FROM kandidati WHERE id = ?";

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, kandidatId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                LocalDate datumIsplate = rs.getString("datum_isplate") != null
                        ? LocalDate.parse(rs.getString("datum_isplate")) : null;

                kandidat = new Kandidat(
                        rs.getInt("id"),
                        rs.getString("id_kandidata"),
                        rs.getString("ime"),
                        rs.getString("prezime"),
                        rs.getString("idb"),
                        rs.getString("jmbg"),
                        rs.getString("broj_licne_karte"),
                        rs.getString("adresa"),
                        rs.getString("grad"),
                        rs.getString("telefon"),
                        rs.getString("email"),
                        rs.getString("kategorija"),
                        rs.getBoolean("polozio_teoriju"),
                        rs.getBoolean("polozio_voznju"),
                        LocalDate.parse(rs.getString("datum_upisa")),
                        rs.getDouble("cena_teorija"),
                        rs.getDouble("cena_praksa"),
                        rs.getDouble("placeno"),
                        datumIsplate
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return kandidat;
    }

    public static void obrisiKandidata(int kandidatId) {
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement("DELETE FROM kandidati WHERE id = ?")) {
            stmt.setInt(1, kandidatId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // === INSTRUKTORI ===

    public static void sacuvajInstruktora(Instruktor i) {
        String sql = "INSERT INTO instruktori (ime, lekarski, vozacka, licenca) VALUES (?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, i.getIme());
            stmt.setString(2, i.getLekarskiIstice().toString());
            stmt.setString(3, i.getVozackaIstice().toString());
            stmt.setString(4, i.getLicencaIstice().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void izmeniInstruktora(Instruktor i) {
        String sql = "UPDATE instruktori SET ime = ?, lekarski = ?, vozacka = ?, licenca = ? WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, i.getIme());
            stmt.setString(2, i.getLekarskiIstice().toString());
            stmt.setString(3, i.getVozackaIstice().toString());
            stmt.setString(4, i.getLicencaIstice().toString());
            stmt.setInt(5, i.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void obrisiInstruktora(int id) {
        String sql = "DELETE FROM instruktori WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Instruktor> vratiInstruktore() {
        List<Instruktor> lista = new ArrayList<>();
        try (Connection conn = connect(); ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM instruktori")) {
            while (rs.next()) {
                lista.add(new Instruktor(
                        rs.getInt("id"),
                        rs.getString("ime"),
                        LocalDate.parse(rs.getString("lekarski")),
                        LocalDate.parse(rs.getString("vozacka")),
                        LocalDate.parse(rs.getString("licenca"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // === VOZILA ===

    public static void sacuvajVozilo(Vozilo v) {
        String sql = "INSERT INTO vozila (naziv, tablice, registracija, tehnicki) VALUES (?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, v.getNaziv());
            stmt.setString(2, v.getTablice());
            stmt.setString(3, v.getRegistracijaIstice().toString());
            stmt.setString(4, v.getTehnickiIstice().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void izmeniVozilo(Vozilo v) {
        String sql = "UPDATE vozila SET naziv = ?, tablice = ?, registracija = ?, tehnicki = ? WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, v.getNaziv());
            stmt.setString(2, v.getTablice());
            stmt.setString(3, v.getRegistracijaIstice().toString());
            stmt.setString(4, v.getTehnickiIstice().toString());
            stmt.setInt(5, v.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void obrisiVozilo(int id) {
        String sql = "DELETE FROM vozila WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Vozilo> vratiVozila() {
        List<Vozilo> lista = new ArrayList<>();
        try (Connection conn = connect(); ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM vozila")) {
            while (rs.next()) {
                lista.add(new Vozilo(
                        rs.getInt("id"),
                        rs.getString("naziv"),
                        rs.getString("tablice"),
                        LocalDate.parse(rs.getString("registracija")),
                        LocalDate.parse(rs.getString("tehnicki"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // === UPLATE ===

    public static void sacuvajUplatu(Uplata u) {
        String sql = "INSERT INTO uplate (kandidat_id, datum, iznos) VALUES (?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, u.getKandidatId());
            stmt.setString(2, u.getDatum().toString());
            stmt.setDouble(3, u.getIznos());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Uplata> vratiUplateZaKandidata(int kandidatId) {
        List<Uplata> lista = new ArrayList<>();
        String sql = "SELECT * FROM uplate WHERE kandidat_id = ? ORDER BY datum ASC";

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, kandidatId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(new Uplata(
                        rs.getInt("id"),
                        rs.getInt("kandidat_id"),
                        LocalDate.parse(rs.getString("datum")),
                        rs.getDouble("iznos")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    public static List<Uplata> vratiUplateZaDatum(LocalDate datum) {
        List<Uplata> lista = new ArrayList<>();
        String sql = "SELECT * FROM uplate WHERE datum = ?";

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, datum.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(new Uplata(
                        rs.getInt("id"),
                        rs.getInt("kandidat_id"),
                        LocalDate.parse(rs.getString("datum")),
                        rs.getDouble("iznos")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }
}
