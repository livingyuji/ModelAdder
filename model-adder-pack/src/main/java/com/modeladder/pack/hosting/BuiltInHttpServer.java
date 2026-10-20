package com.modeladder.pack.hosting;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Embedded lightweight HTTP server dedicated to serving the published resource pack securely.
 */
public final class BuiltInHttpServer {

    private final String bindAddress;
    private final int port;
    private final File publishedPackFile;
    private final Logger logger;
    private HttpServer server;
    private ExecutorService executor;

    public BuiltInHttpServer(
        @NotNull String bindAddress,
        int port,
        @NotNull File publishedPackFile,
        @NotNull Logger logger
    ) {
        this.bindAddress = bindAddress;
        this.port = port;
        this.publishedPackFile = publishedPackFile;
        this.logger = logger;
    }

    /**
     * Starts the HTTP server on the configured bind address and port.
     */
    public synchronized boolean start() {
        if (server != null) {
            return true;
        }

        try {
            InetSocketAddress address = new InetSocketAddress(bindAddress, port);
            server = HttpServer.create(address, 50);
            executor = Executors.newFixedThreadPool(4, r -> {
                Thread t = new Thread(r, "ModelAdder-HTTP-Worker");
                t.setDaemon(true);
                return t;
            });
            server.setExecutor(executor);

            // Handler strictly serving only the published resource pack file
            server.createContext("/pack.zip", new PackHttpHandler());
            server.createContext("/", exchange -> {
                if (!"/pack.zip".equals(exchange.getRequestURI().getPath())) {
                    exchange.sendResponseHeaders(404, -1);
                    exchange.close();
                }
            });

            server.start();
            logger.info("[ModelAdder] Built-in HTTP server listening on " + bindAddress + ":" + port);
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "[ModelAdder] Could not start built-in HTTP server on " + bindAddress + ":" + port + ": " + e.getMessage(), e);
            stop();
            return false;
        }
    }

    /**
     * Gracefully stops the HTTP server.
     */
    public synchronized void stop() {
        if (server != null) {
            server.stop(1);
            server = null;
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    public boolean isRunning() {
        return server != null;
    }

    public int getPort() {
        return port;
    }

    private class PackHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
                return;
            }

            if (!publishedPackFile.exists() || !publishedPackFile.isFile()) {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }

            long fileLength = publishedPackFile.length();
            exchange.getResponseHeaders().set("Content-Type", "application/zip");
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"ModelAdder-Pack.zip\"");
            exchange.getResponseHeaders().set("Cache-Control", "no-cache, must-revalidate");

            if ("HEAD".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(200, -1);
                exchange.close();
                return;
            }

            exchange.sendResponseHeaders(200, fileLength);

            try (InputStream in = Files.newInputStream(publishedPackFile.toPath());
                 OutputStream out = exchange.getResponseBody()) {
                byte[] buffer = new byte[16384];
                int count;
                while ((count = in.read(buffer)) > 0) {
                    out.write(buffer, 0, count);
                }
            } finally {
                exchange.close();
            }
        }
    }
}
