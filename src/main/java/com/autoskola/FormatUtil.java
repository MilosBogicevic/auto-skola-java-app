package com.autoskola;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class FormatUtil {

    private static final DecimalFormatSymbols symbols;
    private static final DecimalFormat format;

    static {
        symbols = new DecimalFormatSymbols(new Locale("sr", "RS"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        format = new DecimalFormat("#,##0", symbols);
    }

    public static String format(double iznos) {
        return format.format(iznos);
    }

    public static double parse(String tekst) throws Exception {
        return format.parse(tekst).doubleValue();
    }
}
