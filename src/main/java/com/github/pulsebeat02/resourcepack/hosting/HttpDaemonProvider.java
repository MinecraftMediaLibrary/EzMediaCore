package com.github.pulsebeat02.resourcepack.hosting;

import com.github.pulsebeat02.http.HttpDaemon;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;

public class HttpDaemonProvider extends AbstractHostingProvider {

    private final static String SERVER_IP = Bukkit.getIp();

    private final HttpDaemon daemon;
    private final File resourcepack;
    private final int port;

    public HttpDaemonProvider(final File resourcepack, final String path, final int port) throws IOException {
        this.resourcepack = resourcepack;
        this.port = port;
        this.daemon = new HttpDaemon(port, path);
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

    public File getResourcepack() {
        return resourcepack;
    }

    public int getPort() {
        return port;
    }

}
