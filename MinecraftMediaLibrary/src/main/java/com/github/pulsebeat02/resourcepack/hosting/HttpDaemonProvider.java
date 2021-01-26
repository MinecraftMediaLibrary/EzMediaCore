package com.github.pulsebeat02.resourcepack.hosting;

import com.github.pulsebeat02.http.HttpDaemon;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class HttpDaemonProvider extends AbstractHostingProvider {

    private final static String SERVER_IP = Bukkit.getIp();

    private HttpDaemon daemon;
    private final int port;

    public HttpDaemonProvider(@NotNull final String path, final int port) {
        this.port = port;
        try {
            this.daemon = new HttpDaemon(port, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startServer() {
        daemon.start();
    }

    @Override
    public String generateUrl(@NotNull final String file) {
        return "http://" + SERVER_IP + ":" + port + "/" + file;
    }

    @Override
    public String generateUrl(@NotNull final Path path) {
        return "http://" + SERVER_IP + ":" + port + "/" + path.getFileName();
    }

    public HttpDaemon getDaemon() {
        return daemon;
    }

    public int getPort() {
        return port;
    }

}
