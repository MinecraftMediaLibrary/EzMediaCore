package com.github.pulsebeat02.minecraftmedialibrary.resourcepack.hosting;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

public class MCPackProvider extends AbstractHostingProvider {

    public void uploadFile(@NotNull final File file) {
        try {
            final CloseableHttpClient httpClient = HttpClients.createDefault();
            final HttpPost uploadFile = new HttpPost("...");
            final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("field1", "yes", ContentType.TEXT_PLAIN);
            builder.addBinaryBody(
                    "file",
                    new FileInputStream(file),
                    ContentType.APPLICATION_OCTET_STREAM,
                    file.getName()
            );
            final HttpEntity multipart = builder.build();
            uploadFile.setEntity(multipart);
            final CloseableHttpResponse response = httpClient.execute(uploadFile);
            final HttpEntity responseEntity = response.getEntity();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String generateUrl(@NotNull final String file) {
        return null;
    }

    @Override
    public String generateUrl(@NotNull final Path path) {
        return null;
    }

}
