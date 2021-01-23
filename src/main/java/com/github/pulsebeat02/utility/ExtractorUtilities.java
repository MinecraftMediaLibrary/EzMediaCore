package com.github.pulsebeat02.utility;

import com.github.pulsebeat02.exception.InvalidYoutubeURLException;
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
        Pattern compiledPattern = Pattern.compile("(?<=youtu.be/|watch\\?v=|/videos/|embed)[^#]*");
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        throw new InvalidYoutubeURLException("Cannot extract Video ID (" + url + ")");
    }

    public static byte[] createHashSHA(@NotNull final File file) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        InputStream fis = new FileInputStream(file);
        int n = 0;
        byte[] buffer = new byte[8192];
        while (n != -1) {
            n = fis.read(buffer);
            if (n > 0) {
                digest.update(buffer, 0, n);
            }
        }
        return digest.digest();
    }

}
