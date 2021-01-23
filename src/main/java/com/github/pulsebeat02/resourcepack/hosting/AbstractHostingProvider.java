package com.github.pulsebeat02.resourcepack.hosting;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public abstract class AbstractHostingProvider {

    /**
     * Generates a url for the specific requested
     * file.
     *
     * @param file to generate parent directory of
     *             the HTTP Server for.
     * @return String url for the generated url to
     * access the specific file.
     */
    public abstract String generateUrl(@NotNull final String file);

    /**
     * Generates a url for the specific requested
     * file.
     *
     * @param path to gnerate parent directory of
     *             the HTTP Server for.
     * @return String url for the generated url to
     * access the specific file.
     */
    public abstract String generateUrl(@NotNull final Path path);

}
