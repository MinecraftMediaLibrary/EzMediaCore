package com.github.pulsebeat02.http;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public interface AbstractRequestHandler {

    /**
     * Creates a header for the HTTP request.
     * Useful for certain connections.
     *
     * @param file to create header for
     * @return Header of the specified file
     */
    String buildHeader(@NotNull final File file);

    /**
     * Handles the incoming request accordingly.
     * Warning: Overriding this requires a rewrite
     * of the incoming connection
     */
    void handleRequest();

}