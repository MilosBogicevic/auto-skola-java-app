package com.autoskola;

import java.time.LocalDate;

public class Kandidat {
    private int id;
    private String idKandidata;
    private String ime;
    private String prezime;
    private String jmbg;
    private String telefon;
    private String email;
    private String kategorija;
    private boolean polozioTeoriju;
    private boolean polozioVoznju;

    private LocalDate datumUpisa;
    private double cenaTeorija;
    private double cenaPraksa;
    private double placeno;

    public Kandidat(int id, String idKandidata, String ime, String prezime, String jmbg, String telefon, String email,
                    String kategorija, boolean polozioTeoriju, boolean polozioVoznju,
                    LocalDate datumUpisa, double cenaTeorija, double cenaPraksa,
                    double placeno) {

        this.id = id;
        this.idKandidata = idKandidata;
        this.ime = ime;
        this.prezime = prezime;
        this.jmbg = jmbg;
        this.telefon = telefon;
        this.email = email;
        this.kategorija = kategorija;
        this.polozioTeoriju = polozioTeoriju;
        this.polozioVoznju = polozioVoznju;
        this.datumUpisa = datumUpisa;
        this.cenaTeorija = cenaTeorija;
        this.cenaPraksa = cenaPraksa;
        this.placeno = placeno;
    }

    public int getId() { return id; }
    public String getIdKandidata() { return idKandidata; }
    public String getIme() { return ime; }
    public String getPrezime() { return prezime; }
    public String getJmbg() { return jmbg; }
    public String getTelefon() { return telefon; }
    public String getEmail() { return email; }
    public String getKategorija() { return kategorija; }
    public boolean isPolozioTeoriju() { return polozioTeoriju; }
    public boolean isPolozioVoznju() { return polozioVoznju; }

    public LocalDate getDatumUpisa() { return datumUpisa; }
    public double getCenaTeorija() { return cenaTeorija; }
    public double getCenaPraksa() { return cenaPraksa; }
    public double getPlaceno() { return placeno; }

    public void setPlaceno(double placeno) {
        this.placeno = placeno;
    }

    public double getUkupnaCena() {
        return cenaTeorija + cenaPraksa;
    }

    public double getPreostalo() {
        return Math.max(0, getUkupnaCena() - placeno);
    }
}
