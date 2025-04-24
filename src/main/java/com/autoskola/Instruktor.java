package com.autoskola;

import java.time.LocalDate;

public class Instruktor {
    private int id;
    private String ime;
    private LocalDate lekarskiIstice;
    private LocalDate vozackaIstice;
    private LocalDate licencaIstice;

    public Instruktor(int id, String ime, LocalDate lekarskiIstice, LocalDate vozackaIstice, LocalDate licencaIstice) {
        this.id = id;
        this.ime = ime;
        this.lekarskiIstice = lekarskiIstice;
        this.vozackaIstice = vozackaIstice;
        this.licencaIstice = licencaIstice;
    }

    public int getId() { return id; }
    public String getIme() { return ime; }
    public LocalDate getLekarskiIstice() { return lekarskiIstice; }
    public LocalDate getVozackaIstice() { return vozackaIstice; }
    public LocalDate getLicencaIstice() { return licencaIstice; }
}
