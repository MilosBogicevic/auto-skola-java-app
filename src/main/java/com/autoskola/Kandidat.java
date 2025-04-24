package com.autoskola;

public class Kandidat {
    private int id;
    private String ime;
    private String prezime;
    private String jmbg;
    private String telefon;
    private String email;
    private String kategorija;
    private boolean polozioTeoriju;
    private boolean polozioVoznju;
    private double cena;
    private int brojRata;
    private double iznosPoRati;
    private double placeno;

    public Kandidat(int id, String ime, String prezime, String jmbg, String telefon, String email,
                    String kategorija, boolean polozioTeoriju, boolean polozioVoznju,
                    double cena, int brojRata, double iznosPoRati, double placeno) {
        this.id = id;
        this.ime = ime;
        this.prezime = prezime;
        this.jmbg = jmbg;
        this.telefon = telefon;
        this.email = email;
        this.kategorija = kategorija;
        this.polozioTeoriju = polozioTeoriju;
        this.polozioVoznju = polozioVoznju;
        this.cena = cena;
        this.brojRata = brojRata;
        this.iznosPoRati = iznosPoRati;
        this.placeno = placeno;
    }

    public int getId() { return id; }
    public String getIme() { return ime; }
    public String getPrezime() { return prezime; }
    public String getJmbg() { return jmbg; }
    public String getTelefon() { return telefon; }
    public String getEmail() { return email; }
    public String getKategorija() { return kategorija; }
    public boolean isPolozioTeoriju() { return polozioTeoriju; }
    public boolean isPolozioVoznju() { return polozioVoznju; }
    public double getCena() { return cena; }
    public int getBrojRata() { return brojRata; }
    public double getIznosPoRati() { return iznosPoRati; }
    public double getPlaceno() { return placeno; }

    public double getPreostalo() {
        return Math.max(0, cena - placeno);
    }
}
