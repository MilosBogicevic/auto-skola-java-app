package com.autoskola;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
                if (runs == null) continue;

                StringBuilder punTekst = new StringBuilder();
                for (XWPFRun run : runs) {
                    String tekst = run.getText(0);
                    if (tekst != null) {
                        punTekst.append(tekst);
                    }
                }

                String paragrafTekst = punTekst.toString();
                boolean trebaZamena = false;

                for (String kljuc : zamene.keySet()) {
                    if (paragrafTekst.contains(kljuc)) {
                        trebaZamena = true;
                        break;
                    }
                }

                if (trebaZamena) {
                    // Zameni tekst
                    String zamenjeniTekst = paragrafTekst;
                    for (Map.Entry<String, String> entry : zamene.entrySet()) {
                        zamenjeniTekst = zamenjeniTekst.replace(entry.getKey(), entry.getValue());
                    }

                    // Obriši sve run-ove
                    for (int i = runs.size() - 1; i >= 0; i--) {
                        para.removeRun(i);
                    }

                    // Napravi jedan novi run sa istim stilom kao prvi originalni run
                    XWPFRun noviRun = para.createRun();
                    noviRun.setText(zamenjeniTekst);

                    // Zadrži font iz originalnog teksta ako želiš
                    if (!runs.isEmpty()) {
                        XWPFRun original = runs.get(0);
                        noviRun.setFontFamily("Times New Roman");
                        noviRun.setFontSize(original.getFontSize());
                        noviRun.setBold(original.isBold());
                        noviRun.setItalic(original.isItalic());
                    }

                    // Ili možeš da nateraš da bude 18pt samo ako NIJE datum
                    if (!zamenjeniTekst.contains(k.getDatumUpisa().format(dtf))) {
                        noviRun.setFontSize(18);
                    }
                }
            }

            try (FileOutputStream fos = new FileOutputStream(izlazPath)) {
                doc.write(fos);
            }
        }
    }
}
