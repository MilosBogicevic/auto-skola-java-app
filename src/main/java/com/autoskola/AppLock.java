package com.autoskola;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.Optional;

public class AppLock {
    private static FileChannel channel;
    private static FileLock lock;
    private static final Path LOCK_FILE = Paths.get(Database.getDatabasePath()).getParent().resolve("app.lock");

    public static boolean zakljucaj() {
        try {
            // Kreiraj fajl ako ne postoji
            if (!Files.exists(LOCK_FILE)) {
                Files.createFile(LOCK_FILE);
            }

            channel = FileChannel.open(LOCK_FILE, StandardOpenOption.WRITE);
            lock = channel.tryLock();

            // Ako je lock neuspešan, aplikacija je već pokrenuta
            if (lock == null) {
                channel.close();
                return false;
            }

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void otkljucaj() {
        try {
            if (lock != null) lock.release();
            if (channel != null) channel.close();
            Files.deleteIfExists(LOCK_FILE);
        } catch (IOException ignored) {
        }
    }
}
