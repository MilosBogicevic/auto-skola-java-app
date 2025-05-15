package com.autoskola;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class SecurityLock {
    private static final List<String> DOZVOLJENE_MAC_ADRESE = List.of(
            "D4-3D-7E-DA-9C-E8", // Glavni računar
            "88-51-FB-41-AF-13", // Drugi računar
            "E4-E7-49-A1-9E-11"  // Radni za test
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
