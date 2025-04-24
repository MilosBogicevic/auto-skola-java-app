package com.autoskola;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String URL = "jdbc:sqlite:kandidati.db";

    public static void initialize() {
        try (Connection conn = connect()) {
            conn.createStatement().execute("DROP TABLE IF EXISTS kandidati");

            String sql = """
                CREATE TABLE kandidati (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    ime TEXT,
                    prezime TEXT,
                    jmbg TEXT,
                    telefon TEXT,
                    email TEXT,
                    kategorija TEXT,
                    polozio_teoriju INTEGER,
                    polozio_voznju INTEGER,
                    cena REAL,
                    broj_rata INTEGER,
                    iznos_po_rati REAL,
                    placeno REAL
                );
                """;
            conn.createStatement().execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void sacuvajKandidata(Kandidat k) {
        String sql = """
            INSERT INTO kandidati (
                ime, prezime, jmbg, telefon, email, kategorija,
                polozio_teoriju, polozio_voznju,
                cena, broj_rata, iznos_po_rati, placeno
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, k.getIme());
            stmt.setString(2, k.getPrezime());
            stmt.setString(3, k.getJmbg());
            stmt.setString(4, k.getTelefon());
            stmt.setString(5, k.getEmail());
            stmt.setString(6, k.getKategorija());
            stmt.setBoolean(7, k.isPolozioTeoriju());
            stmt.setBoolean(8, k.isPolozioVoznju());
            stmt.setDouble(9, k.getCena());
            stmt.setInt(10, k.getBrojRata());
            stmt.setDouble(11, k.getIznosPoRati());
            stmt.setDouble(12, k.getPlaceno());
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
                Kandidat k = new Kandidat(
                        rs.getInt("id"),
                        rs.getString("ime"),
                        rs.getString("prezime"),
                        rs.getString("jmbg"),
                        rs.getString("telefon"),
                        rs.getString("email"),
                        rs.getString("kategorija"),
                        rs.getBoolean("polozio_teoriju"),
                        rs.getBoolean("polozio_voznju"),
                        rs.getDouble("cena"),
                        rs.getInt("broj_rata"),
                        rs.getDouble("iznos_po_rati"),
                        rs.getDouble("placeno")
                );
                lista.add(k);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    public static void obrisiKandidata(int id) {
        String sql = "DELETE FROM kandidati WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void izmeniKandidata(Kandidat k) {
        String sql = """
            UPDATE kandidati SET
                ime = ?, prezime = ?, jmbg = ?, telefon = ?, email = ?, kategorija = ?,
                polozio_teoriju = ?, polozio_voznju = ?,
                cena = ?, broj_rata = ?, iznos_po_rati = ?, placeno = ?
            WHERE id = ?
            """;

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, k.getIme());
            stmt.setString(2, k.getPrezime());
            stmt.setString(3, k.getJmbg());
            stmt.setString(4, k.getTelefon());
            stmt.setString(5, k.getEmail());
            stmt.setString(6, k.getKategorija());
            stmt.setBoolean(7, k.isPolozioTeoriju());
            stmt.setBoolean(8, k.isPolozioVoznju());
            stmt.setDouble(9, k.getCena());
            stmt.setInt(10, k.getBrojRata());
            stmt.setDouble(11, k.getIznosPoRati());
            stmt.setDouble(12, k.getPlaceno());
            stmt.setInt(13, k.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
