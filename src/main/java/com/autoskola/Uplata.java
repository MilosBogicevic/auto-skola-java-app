package com.autoskola;

import java.time.LocalDate;

public class Uplata {
    private int id;
    private int kandidatId;
    private LocalDate datum;
    private double iznos;
    private String nacinUplate;

    public Uplata(int id, int kandidatId, LocalDate datum, double iznos, String nacinUplate) {
        this.id = id;
        this.kandidatId = kandidatId;
        this.datum = datum;
        this.iznos = iznos;
        this.nacinUplate = nacinUplate;
    }

    // Stari konstruktor (podrazumevano: Gotovina)
    public Uplata(int id, int kandidatId, LocalDate datum, double iznos) {
        this(id, kandidatId, datum, iznos, "Gotovina");
    }

    public int getId() {
        return id;
    }

    public int getKandidatId() {
        return kandidatId;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public double getIznos() {
        return iznos;
    }

    public String getNacinUplate() {
        return nacinUplate;
    }

    public void setNacinUplate(String nacinUplate) {
        this.nacinUplate = nacinUplate;
    }
}
