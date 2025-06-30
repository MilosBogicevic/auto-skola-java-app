package com.autoskola;

import javafx.scene.control.Alert;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.awt.Desktop;

public class UgovorGenerator {

    public static void generisiUgovor(Kandidat k, String sablonPath, String izlazPath) throws Exception {
        try (FileInputStream fis = new FileInputStream(sablonPath);
             XWPFDocument doc = new XWPFDocument(fis)) {

            Map<String, String> zamene = new HashMap<>();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

            zamene.put("{{IME}}", k.getIme());
            zamene.put("{{PREZIME}}", k.getPrezime());
            zamene.put("{{IDKANDIDATA}}", k.getIdKandidata());
            zamene.put("{{IDB}}", k.getIdb());
            zamene.put("{{JMBG}}", k.getJmbg());
            zamene.put("{{LICNA_KARTA}}", k.getBrojLicneKarte());
            zamene.put("{{TELEFON}}", k.getTelefon());
            zamene.put("{{EMAIL}}", k.getEmail());
            zamene.put("{{ADRESA}}", k.getAdresa());
            zamene.put("{{GRAD}}", k.getGrad());
            zamene.put("{{KATEGORIJA}}", k.getKategorija());
            zamene.put("{{DATUM_UPISA}}", k.getDatumUpisa().format(dtf));

            for (XWPFParagraph para : doc.getParagraphs()) {
                List<XWPFRun> runs = para.getRuns();
                if (runs == null || runs.isEmpty()) continue;

                boolean pronadjenJednostavanToken = false;
                for (XWPFRun run : runs) {
                    String text = run.getText(0);
                    if (text == null) continue;

                    boolean promenjeno = false;
                    for (Map.Entry<String, String> entry : zamene.entrySet()) {
                        if (text.contains(entry.getKey())) {
                            text = text.replace(entry.getKey(), entry.getValue());
                            promenjeno = true;
                        }
                    }

                    if (promenjeno) {
                        run.setText(text, 0);
                        pronadjenJednostavanToken = true;
                    }
                }

                if (pronadjenJednostavanToken) continue;

                StringBuilder sb = new StringBuilder();
                for (XWPFRun run : runs) {
                    String text = run.getText(0);
                    if (text != null) sb.append(text);
                }
                String ceoTekst = sb.toString();

                boolean sadrziToken = zamene.keySet().stream().anyMatch(ceoTekst::contains);
                if (sadrziToken) {
                    String zamenjeni = ceoTekst;
                    for (Map.Entry<String, String> entry : zamene.entrySet()) {
                        zamenjeni = zamenjeni.replace(entry.getKey(), entry.getValue());
                    }

                    XWPFRun prvi = runs.get(0);
                    String font = prvi.getFontFamily() != null ? prvi.getFontFamily() : "Times New Roman";
                    int velicina = prvi.getFontSize() > 0 ? prvi.getFontSize() : 14;
                    boolean bold = prvi.isBold();
                    boolean italic = prvi.isItalic();

                    for (int i = runs.size() - 1; i >= 0; i--) para.removeRun(i);

                    XWPFRun novi = para.createRun();
                    novi.setText(zamenjeni);
                    novi.setFontFamily(font);
                    novi.setFontSize(velicina);
                    novi.setBold(bold);
                    novi.setItalic(italic);
                }
            }

            File fajl = new File(izlazPath);
            File parent = fajl.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(fajl)) {
                doc.write(fos);
                Desktop.getDesktop().open(fajl);
            }
        }
    }

    public static void generisiDetaljeOKandidatu(Kandidat k) {
        try (XWPFDocument doc = new XWPFDocument(new FileInputStream("sabloni/kandidat_detalji.docx"))) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

            dodajParagraf(doc, "ID broj kandidata: " + k.getIdKandidata());
            dodajParagraf(doc, "Ime i prezime: " + k.getIme() + " " + k.getPrezime());
            dodajParagraf(doc, "Kategorija: " + k.getKategorija());
            dodajParagraf(doc, "Datum upisa: " + k.getDatumUpisa().format(dtf));
            dodajParagraf(doc, "Položena teorija: " + (k.isPolozioTeoriju() ? "da" : "ne"));
            dodajParagraf(doc, "Položena vožnja: " + (k.isPolozioVoznju() ? "da" : "ne"));
            dodajParagraf(doc, "Cena teorijske obuke: " + FormatUtil.format(k.getCenaTeorija()) + " RSD");
            dodajParagraf(doc, "Cena praktične obuke: " + FormatUtil.format(k.getCenaPraksa()) + " RSD");
            dodajParagraf(doc, "Plaćeno za obuku: " + FormatUtil.format(k.getPlaceno()) + " RSD");
            dodajParagraf(doc, "Preostalo: " + FormatUtil.format(k.getPreostalo()) + " RSD");

            doc.createParagraph().createRun().addBreak();
            dodajParagraf(doc, "Uplate:");

            List<Uplata> uplate = Database.vratiUplateZaKandidata(k.getId());
            if (uplate.isEmpty()) {
                dodajParagraf(doc, "- Nema zabeleženih uplata.");
            } else {
                for (Uplata u : uplate) {
                    String tekst = u.getDatum().format(dtf) + " – "
                            + (u.getSvrha() != null ? u.getSvrha() : "Obuka") + " – "
                            + FormatUtil.format(u.getIznos()) + " RSD"
                            + (u.getNacinUplate().equals("Gotovina") ? "" : " – " + u.getNacinUplate());
                    dodajParagraf(doc, "• " + tekst);
                }
            }

            String naziv = "izvestaji/detalji-kandidata-" + k.getIme() + "-" + k.getPrezime() + "-" + k.getIdb() + ".docx";
            File fajl = new File(naziv);
            File parent = fajl.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (FileOutputStream out = new FileOutputStream(fajl)) {
                doc.write(out);
                Desktop.getDesktop().open(fajl);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Greška");
            alert.setHeaderText("Neuspešno otvaranje Word dokumenta");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public static void generisiDnevniIzvestaj(LocalDate datumOd, LocalDate datumDo) {
        try (XWPFDocument doc = new XWPFDocument(new FileInputStream("sabloni/dnevni_izvestaj.docx"))) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

            for (LocalDate datum = datumOd; !datum.isAfter(datumDo); datum = datum.plusDays(1)) {
                XWPFParagraph naslov = doc.createParagraph();
                XWPFRun run = naslov.createRun();
                run.setText("Dnevni izveštaj za: " + datum.format(dtf));
                run.setFontSize(14);
                run.setBold(true);
                naslov.setSpacingAfter(300);

                double ukupno = 0;
                boolean imaUplata = false;

                List<Uplata> uplate = Database.vratiUplateZaDatum(datum);
                for (Uplata u : uplate) {
                    Kandidat k = Database.vratiKandidataPoId(u.getKandidatId());
                    String opis = (u.getSvrha() != null ? u.getSvrha() : "Obuka") + " – "
                            + FormatUtil.format(u.getIznos()) + " RSD"
                            + (u.getNacinUplate().equals("Gotovina") ? "" : " – " + u.getNacinUplate());
                    String stavka = k.getIdKandidata() + " – " + k.getIme() + " " + k.getPrezime()
                            + " – " + dtf.format(u.getDatum()) + " – " + opis;
                    dodajParagraf(doc, stavka);
                    ukupno += u.getIznos();
                    imaUplata = true;
                }

                if (!imaUplata) {
                    dodajParagraf(doc, "- Nema uplata za ovaj dan.");
                } else {
                    doc.createParagraph().createRun().addBreak();
                    dodajParagraf(doc, "Ukupno: " + FormatUtil.format(ukupno) + " RSD");
                }

                doc.createParagraph().createRun().addBreak();
            }

            String naziv = "izvestaji/dnevni-izvestaj-" + datumOd.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    + "_do_" + datumDo.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ".docx";
            File fajl = new File(naziv);
            File parent = fajl.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (FileOutputStream out = new FileOutputStream(fajl)) {
                doc.write(out);
                Desktop.getDesktop().open(fajl);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Greška");
            alert.setHeaderText("Neuspešno otvaranje Word dokumenta");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private static void dodajParagraf(XWPFDocument doc, String tekst) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingAfter(100);
        XWPFRun run = p.createRun();
        run.setText(tekst);
        run.setFontSize(12);
    }
}
