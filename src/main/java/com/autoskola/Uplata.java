package com.autoskola;

import java.time.LocalDate;

public class Uplata {
    private int id;
    private int kandidatId;
    private LocalDate datum;
    private double iznos;

    public Uplata(int id, int kandidatId, LocalDate datum, double iznos) {
        this.id = id;
        this.kandidatId = kandidatId;
        this.datum = datum;
        this.iznos = iznos;
    }

    public int getId() { return id; }
    public int getKandidatId() { return kandidatId; }
    public LocalDate getDatum() { return datum; }
    public double getIznos() { return iznos; }
}
