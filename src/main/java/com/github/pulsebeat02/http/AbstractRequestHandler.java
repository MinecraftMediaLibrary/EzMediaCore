package com.github.pulsebeat02.http;

import java.io.File;

public interface AbstractRequestHandler {

    String buildHeader(final File file);

    void handleRequest();

}