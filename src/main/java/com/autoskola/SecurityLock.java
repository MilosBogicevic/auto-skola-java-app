package com.autoskola;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class SecurityLock {
    private static final List<String> DOZVOLJENE_MAC_ADRESE = List.of(
            "00-11-22-33-44-55", // Prvi računar
            "E4-E7-49-A1-9E-11",
            "66-77-88-99-AA-BB"  // Drugi računar
    );

    public static boolean jeDozvoljenoPokretanje() {
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                byte[] mac = ni.getHardwareAddress();
                if (mac == null) continue;

                StringBuilder sb = new StringBuilder();
                for (byte b : mac) {
                    sb.append(String.format("%02X-", b));
                }
                if (sb.length() > 0) sb.setLength(sb.length() - 1);
                String macAdresa = sb.toString();

                if (DOZVOLJENE_MAC_ADRESE.contains(macAdresa)) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Greška prilikom čitanja MAC adrese: " + e.getMessage());
        }
        return false;
    }
}
