package com.github.pulsebeat02.test;

import com.github.pulsebeat02.MinecraftMediaLibrary;
import com.github.pulsebeat02.concurrent.AsyncVideoExtraction;
import com.github.pulsebeat02.extractor.YoutubeExtraction;
import com.github.pulsebeat02.image.MapImage;
import com.github.pulsebeat02.resourcepack.ResourcepackWrapper;
import com.github.pulsebeat02.resourcepack.hosting.HttpDaemonProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class YoutubeResourcepackTest extends JavaPlugin {

    private MinecraftMediaLibrary library;

    @Override
    public void onEnable() {
        library = new MinecraftMediaLibrary(this, getDataFolder().getPath(), true);
    }

    public String getResourcepackUrlYoutube(@NotNull final String youtubeUrl, @NotNull final String directory, final int port) {

        final YoutubeExtraction extraction = new YoutubeExtraction(youtubeUrl, directory) {
            @Override
            public void onVideoDownload() {
                System.out.println("Video is Downloading!");
            }

            @Override
            public void onAudioExtraction() {
                System.out.println("Audio is being extracted from Video!");
            }
        };
        final ExecutorService executor = Executors.newCachedThreadPool();
        CompletableFuture.runAsync(() -> new AsyncVideoExtraction(extraction).extractAudio(), executor);
        CompletableFuture.runAsync(() -> new AsyncVideoExtraction(extraction).downloadVideo(), executor);

        final ResourcepackWrapper wrapper = new ResourcepackWrapper.Builder()
                .setAudio(extraction.getAudio())
                .setDescription("Youtube Video: " + extraction.getVideoTitle())
                .setPath(directory)
                .setPackFormat(6)
                .createResourcepackHostingProvider(library);
        wrapper.buildResourcePack();

        final HttpDaemonProvider hosting = new HttpDaemonProvider(directory, port);
        hosting.startServer();

        return hosting.generateUrl(Paths.get(directory));

    }

    public void displayImage(final int map, @NotNull final File image) throws IOException {

        final BufferedImage bi = ImageIO.read(image);

        final MapImage imageMap = new MapImage.Builder()
                .setMap(map)
                .setWidth(bi.getWidth())
                .setHeight(bi.getHeight())
                .createImageMap(library);

        imageMap.drawImage();

    }

}
