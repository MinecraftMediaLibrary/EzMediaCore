package com.github.pulsebeat02.minecraftmedialibrary.http;

import java.net.Socket;

public interface AbstractHttpDaemon {

    /**
     * Method used to start the HTTP Daemon.
     */
    void startServer();

    /**
     * Called right before the HTTP Daemon starts
     * running.
     */
    void onServerStart();

    /**
     * Called right before the HTTP Daemon terminates.
     */
    void onServerTerminate();

    /**
     * Called when an incoming user connects to the
     * HTTP Server.
     *
     * @param client for the incoming connection.
     */
    void onClientConnect(final Socket client);

    /**
     * Called when a resourcepack failed to be
     * installed for a user.
     *
     * @param socket for the connection which failed
     *               download.
     */
    void onResourcepackFailedDownload(final Socket socket);

}
