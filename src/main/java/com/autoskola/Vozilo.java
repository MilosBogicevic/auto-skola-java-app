package com.autoskola;

import java.time.LocalDate;

public class Vozilo {
    private int id;
    private String naziv;
    private String tablice;
    private LocalDate registracijaIstice;
    private LocalDate tehnickiIstice;

    public Vozilo(int id, String naziv, String tablice, LocalDate registracijaIstice, LocalDate tehnickiIstice) {
        this.id = id;
        this.naziv = naziv;
        this.tablice = tablice;
        this.registracijaIstice = registracijaIstice;
        this.tehnickiIstice = tehnickiIstice;
    }

    public int getId() { return id; }
    public String getNaziv() { return naziv; }
    public String getTablice() { return tablice; }
    public LocalDate getRegistracijaIstice() { return registracijaIstice; }
    public LocalDate getTehnickiIstice() { return tehnickiIstice; }
}
