package com.github.pulsebeat02.utility;

import com.github.pulsebeat02.exception.InvalidYoutubeURLException;
import com.github.pulsebeat02.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractorUtilities {

    public static String getVideoID(@NotNull final String url) {
        final Pattern compiledPattern = Pattern.compile("(?<=youtu.be/|watch\\?v=|/videos/|embed)[^#]*");
        final Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            final String id = matcher.group();
            Logger.info("Found Video ID for " + url + "(" + id + ")");
            return id;
        }
        throw new InvalidYoutubeURLException("Cannot extract Video ID (" + url + ")");
    }

    public static byte[] createHashSHA(@NotNull final File file) throws NoSuchAlgorithmException, IOException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-1");
        final InputStream fis = new FileInputStream(file);
        int n = 0;
        final byte[] buffer = new byte[8192];
        while (n != -1) {
            n = fis.read(buffer);
            if (n > 0) {
                digest.update(buffer, 0, n);
            }
        }
        final byte[] hash = digest.digest();
        Logger.info("Generated Hash for File " + file.getAbsolutePath() + " (" + new String(hash) + ")");
        return hash;
    }

}
