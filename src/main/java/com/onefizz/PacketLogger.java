package com.onefizz;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * PacketLogger — асинхронная запись логов в файл.
 * Логи пишутся в onefizz-logs/packets-{дата}.log
 */
public final class PacketLogger {

    private static final Path LOG_DIR = FabricLoader.getInstance().getGameDir().resolve("onefizz-logs");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final DateTimeFormatter FILE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");

    private static final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
    private static volatile boolean running = false;
    private static Thread writerThread;
    private static Path currentFile;

    public static void start() {
        if (running) return;
        try { Files.createDirectories(LOG_DIR); } catch (IOException ignored) {}
        currentFile = LOG_DIR.resolve("packets-" + LocalDateTime.now().format(FILE_FMT) + ".log");
        running = true;
        writerThread = new Thread(PacketLogger::writerLoop, "OneFizz-PacketLogger");
        writerThread.setDaemon(true);
        writerThread.start();
    }

    public static void stop() {
        running = false;
        if (writerThread != null) writerThread.interrupt();
    }

    public static void log(String category, String message) {
        if (!running) return;
        String ts = LocalDateTime.now().format(TIME_FMT);
        queue.add("[" + ts + "] [" + category + "] " + message);
    }

    /** Логирует с дополнительным контекстом (позиция игрока и т.д.) */
    public static void logWithContext(String category, String message, double x, double y, double z) {
        if (!running) return;
        String ts = LocalDateTime.now().format(TIME_FMT);
        queue.add(String.format("[%s] [%s] %s | pos=(%.2f, %.2f, %.2f)", ts, category, message, x, y, z));
    }

    public static Path getCurrentFile() { return currentFile; }

    private static void writerLoop() {
        try (BufferedWriter w = Files.newBufferedWriter(currentFile,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write("=== OneFizz Packet Inspector Log ===\n");
            w.write("=== Started: " + LocalDateTime.now() + " ===\n\n");
            w.flush();

            while (running || !queue.isEmpty()) {
                String line = queue.poll();
                if (line != null) {
                    w.write(line);
                    w.newLine();
                    w.flush();
                } else {
                    Thread.sleep(50);
                }
            }
        } catch (InterruptedException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
