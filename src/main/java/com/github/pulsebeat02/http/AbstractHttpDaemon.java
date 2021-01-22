package com.github.pulsebeat02.http;

import java.net.Socket;

public interface AbstractHttpDaemon {

    void onServerStart();

    void onServerTerminate();

    void onClientConnect(final Socket client);

    void onResourcepackFailedDownload();

}
