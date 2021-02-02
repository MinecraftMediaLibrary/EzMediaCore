package com.github.pulsebeat02.minecraftmedialibrary.resourcepack;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.exception.InvalidPackFormatException;
import com.github.pulsebeat02.minecraftmedialibrary.exception.InvalidPackIconException;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.ResourcepackUtilities;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcepackWrapper implements AbstractPackHolder, ConfigurationSerializable {

    private final MinecraftMediaLibrary library;
    private final String path;
    private final File audio;
    private final File icon;
    private final String description;
    private final int packFormat;

    public ResourcepackWrapper(@NotNull final MinecraftMediaLibrary library,
                               @NotNull final String path,
                               @NotNull final File audio,
                               final File icon,
                               final String description,
                               final int packFormat) {
        this.library = library;
        this.path = path;
        this.audio = audio;
        this.icon = icon;
        this.description = description;
        this.packFormat = packFormat;
        if (!ResourcepackUtilities.validatePackFormat(packFormat)) {
            throw new InvalidPackFormatException("Invalid Pack Format Exception (" + packFormat + ")");
        }
        if (icon != null && !ResourcepackUtilities.validateResourcepackIcon(icon)) {
            throw new InvalidPackIconException("Invalid Pack Icon! Must be PNG (" + icon.getName() + ")");
        }
        Logger.info("New Resourcepack (" + path + ") was Initialized");
    }

    public static ResourcepackWrapper deserialize(@NotNull final MinecraftMediaLibrary library, @NotNull final Map<String, Object> deserialize) {
        return new ResourcepackWrapper(library,
                String.valueOf(deserialize.get("path")),
                new File(String.valueOf(deserialize.get("audio"))),
                new File(String.valueOf(deserialize.get("icon"))),
                String.valueOf(deserialize.get(deserialize)),
                NumberConversions.toInt(deserialize.get("pack-format")));
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> serialized = new HashMap<>();
        serialized.put("path", path);
        serialized.put("audio", audio.getAbsolutePath());
        serialized.put("icon", icon.getAbsolutePath());
        serialized.put("description", description);
        serialized.put("pack-format", packFormat);
        return serialized;
    }

    @Override
    public void buildResourcePack() {
        onResourcepackBuild();
        Logger.info("Wrapping Resourcepack...");
        try {
            final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(path));
            final byte[] mcmeta = ("{\r\n" + "	\"pack\": {\r\n" + "    \"pack_format\": " + packFormat + ",\r\n"
                    + "    \"description\": \"" + description + "\"\r\n" + "  }\r\n" + "}")
                    .getBytes();
            final ZipEntry config = new ZipEntry("pack.mcmeta");
            out.putNextEntry(config);
            out.write(mcmeta);
            out.closeEntry();
            final byte[] soundJSON = ("{\r\n" + "   \"" + library.getPlugin().getName() + "\":{\r\n" + "      \"sounds\":[\r\n"
                    + "         \"audio\"\r\n" + "      ]\r\n" + "   }\r\n" + "}").getBytes();
            final ZipEntry sound = new ZipEntry("assets/minecraft/sounds.json");
            out.putNextEntry(sound);
            out.write(soundJSON);
            out.closeEntry();
            final ZipEntry soundFile = new ZipEntry("assets/minecraft/sounds/audio.ogg");
            out.putNextEntry(soundFile);
            out.write(Files.readAllBytes(Paths.get(audio.getAbsolutePath())));
            final ZipEntry iconFile = new ZipEntry("pack.png");
            out.putNextEntry(iconFile);
            out.write(Files.readAllBytes(Paths.get(icon.getAbsolutePath())));
            out.closeEntry();
            out.close();
            Logger.info("Finished Wrapping Resourcepack!");
        } catch (final IOException e) {
            Logger.error("There was an error while wrapping the resourcepack...");
            e.printStackTrace();
        }
    }

    @Override
    public void onResourcepackBuild() {
    }

    public MinecraftMediaLibrary getLibrary() {
        return library;
    }

    public String getPath() {
        return path;
    }

    public File getAudio() {
        return audio;
    }

    public File getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public int getPackFormat() {
        return packFormat;
    }

    public static class Builder {

        private File audio;
        private File icon;
        private String description;
        private int packFormat;
        private String path;

        public Builder setAudio(final File audio) {
            this.audio = audio;
            return this;
        }

        public Builder setIcon(final File icon) {
            this.icon = icon;
            return this;
        }

        public Builder setDescription(final String description) {
            this.description = description;
            return this;
        }

        public Builder setPackFormat(final int packFormat) {
            this.packFormat = packFormat;
            return this;
        }

        public Builder setPath(final String path) {
            this.path = path;
            return this;
        }

        public ResourcepackWrapper createResourcepackHostingProvider(final MinecraftMediaLibrary library) {
            return new ResourcepackWrapper(library, path, audio, icon, description, packFormat);
        }

    }

}
