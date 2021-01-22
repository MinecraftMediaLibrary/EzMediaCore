package com.github.pulsebeat02.resourcepack.hosting;

import com.github.pulsebeat02.http.HttpDaemon;
import org.bukkit.Bukkit;

import java.io.IOException;

public class HttpDaemonProvider extends AbstractHostingProvider {

    private final static String SERVER_IP = Bukkit.getIp();

    private HttpDaemon daemon;
    private final int port;

    public HttpDaemonProvider(final String path, final int port) {
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
    public String generateUrl(final String file) {
        return "http://" + SERVER_IP + ":" + port + "/" + file;
    }

    public HttpDaemon getDaemon() {
        return daemon;
    }

    public int getPort() {
        return port;
    }

}
