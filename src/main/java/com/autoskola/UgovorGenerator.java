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
                if (runs == null || runs.isEmpty()) continue;

                // Prvo pokušaj jednostavnu zamenu u svakom run-u
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

                if (pronadjenJednostavanToken) continue; // Ako smo sve zamenili jednostavno, nastavi dalje

                // Ako nismo uspeli, pokušaj da spojiš sve run-ove i obradiš kao jedan tekst
                StringBuilder sb = new StringBuilder();
                for (XWPFRun run : runs) {
                    String text = run.getText(0);
                    if (text != null) sb.append(text);
                }
                String ceoTekst = sb.toString();

                boolean sadrziToken = false;
                for (String kljuc : zamene.keySet()) {
                    if (ceoTekst.contains(kljuc)) {
                        sadrziToken = true;
                        break;
                    }
                }

                if (sadrziToken) {
                    String zamenjeni = ceoTekst;
                    for (Map.Entry<String, String> entry : zamene.entrySet()) {
                        zamenjeni = zamenjeni.replace(entry.getKey(), entry.getValue());
                    }

                    // Sačuvaj stil iz prvog run-a
                    XWPFRun prvi = runs.get(0);
                    String font = prvi.getFontFamily() != null ? prvi.getFontFamily() : "Times New Roman";
                    int velicina = prvi.getFontSize() > 0 ? prvi.getFontSize() : 14;
                    boolean bold = prvi.isBold();
                    boolean italic = prvi.isItalic();

                    // Obriši sve stare run-ove
                    for (int i = runs.size() - 1; i >= 0; i--) para.removeRun(i);

                    // Napravi novi run sa zamenjenim tekstom i stilom
                    XWPFRun novi = para.createRun();
                    novi.setText(zamenjeni);
                    novi.setFontFamily(font);
                    novi.setFontSize(velicina);
                    novi.setBold(bold);
                    novi.setItalic(italic);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(izlazPath)) {
                doc.write(fos);
            }
        }
    }
}
