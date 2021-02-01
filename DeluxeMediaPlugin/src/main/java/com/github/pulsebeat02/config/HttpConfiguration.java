package com.github.pulsebeat02.config;

import com.github.pulsebeat02.DeluxeMediaPlugin;
import com.github.pulsebeat02.http.HttpDaemon;
import com.github.pulsebeat02.resourcepack.hosting.HttpDaemonProvider;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class HttpConfiguration extends AbstractConfiguration {

    private HttpDaemonProvider daemon;
    private boolean enabled;

    public HttpConfiguration(@NotNull final DeluxeMediaPlugin plugin) {
        super(plugin, "httpserver.yml");
    }

    @Override
    public void deserialize() {
        final FileConfiguration configuration = getFileConfiguration();
        configuration.set("enabled", enabled);
        configuration.set("port", daemon.getPort());
        final HttpDaemon http = daemon.getDaemon();
        configuration.set("directory", http.getDirectory().getAbsolutePath());
        configuration.set("header", http.getHeader() == HttpDaemon.ZipHeader.ZIP ? "ZIP" : "OCTET_STREAM");
        configuration.set("verbose", http.isVerbose());
        saveConfig();
    }

    @Override
    public void serialize() {
        final FileConfiguration configuration = getFileConfiguration();
        final boolean enabled = configuration.getBoolean("enabled");
        final int port = configuration.getInt("port");
        final String directory = configuration.getString("directory");
        final String header = configuration.getString("header");
        final boolean verbose = configuration.getBoolean("verbose");
        if (enabled) {
            assert directory != null;
            daemon = new HttpDaemonProvider(directory, port);
            final HttpDaemon http = daemon.getDaemon();
            assert header != null;
            http.setZipHeader(header.equals("ZIP") ? HttpDaemon.ZipHeader.ZIP : HttpDaemon.ZipHeader.OCTET_STREAM);
            http.setVerbose(verbose);
        }
        this.enabled = enabled;
    }

}
