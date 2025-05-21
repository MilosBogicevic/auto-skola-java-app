package com.autoskola;

import java.time.LocalDate;

public class Uplata {
    private int id;
    private int kandidatId;
    private LocalDate datum;
    private double iznos;
    private String nacinUplate;
    private String svrha;

    public Uplata(int id, int kandidatId, LocalDate datum, double iznos, String nacinUplate, String svrha) {
        this.id = id;
        this.kandidatId = kandidatId;
        this.datum = datum;
        this.iznos = iznos;
        this.nacinUplate = nacinUplate;
        this.svrha = svrha;
    }

    public Uplata(int id, int kandidatId, LocalDate datum, double iznos, String nacinUplate) {
        this(id, kandidatId, datum, iznos, nacinUplate, "Obuka");
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

    public String getSvrha() {
        return svrha;
    }

    public void setNacinUplate(String nacinUplate) {
        this.nacinUplate = nacinUplate;
    }

    public void setSvrha(String svrha) {
        this.svrha = svrha;
    }
}
