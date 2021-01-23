package com.github.pulsebeat02.http;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpDaemon extends Thread implements AbstractHttpDaemon {

    private boolean running;

    private final int port;
    private final ServerSocket socket;
    private final File directory;
    private ZipHeader header;
    private boolean verbose;

    public HttpDaemon(final int port, @NotNull final File directory) throws IOException {
        this.running = true;
        this.port = port;
        this.socket = new ServerSocket(port);
        this.socket.setReuseAddress(true);
        this.directory = directory;
        this.header = ZipHeader.ZIP;
        this.verbose = true;
    }

    public HttpDaemon(final int port, @NotNull final String path) throws IOException {
        this.running = true;
        this.port = port;
        this.socket = new ServerSocket(port);
        this.socket.setReuseAddress(true);
        this.directory = new File(path);
        this.header = ZipHeader.ZIP;
        this.verbose = true;
    }

    @Override
    public void run() {
        onServerStart();
        startServer();
    }

    @Override
    public void startServer() {
        while (running) {
            try {
                new Thread(new RequestHandler(this, header, socket.accept())).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void terminate() {
        onServerTerminate();
        running = false;
        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onServerStart() {
    }

    @Override
    public void onServerTerminate() {
    }

    @Override
    public void onClientConnect(final Socket client) {
    }

    @Override
    public void onResourcepackFailedDownload(final Socket client) {
    }

    public void setZipHeader(final ZipHeader header) {
        this.header = header;
    }

    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    public ZipHeader getZipHeader() {
        return header;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public File getParentDirectory() {
        return directory;
    }

    public int getPort() {
        return port;
    }

    public boolean isRunning() {
        return running;
    }

    public ServerSocket getSocket() {
        return socket;
    }

    public File getDirectory() {
        return directory;
    }

    public ZipHeader getHeader() {
        return header;
    }

    public enum ZipHeader {

        ZIP("application/zip"), OCTET_STREAM("application/octet-stream");

        private final String header;

        ZipHeader(final String header) {
            this.header = header;
        }

        public String getHeader() {
            return header;
        }

    }

}
