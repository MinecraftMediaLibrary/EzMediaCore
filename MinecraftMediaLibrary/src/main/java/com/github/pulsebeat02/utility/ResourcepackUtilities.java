package com.github.pulsebeat02.utility;

import com.github.pulsebeat02.logger.Logger;
import com.github.pulsebeat02.resourcepack.PackFormatVersioning;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ResourcepackUtilities {

    public static boolean validatePackFormat(final int format) {
        for (final PackFormatVersioning version : PackFormatVersioning.values()) {
            if (format == version.getPackFormatID()) {
                Logger.info("Pack Format Supported! (" + format + ")");
                return true;
            }
        }
        Logger.warn("Pack Format Not Supported! (" + format + ")");
        return false;
    }

    public static boolean validateResourcepackIcon(@NotNull final File icon) {
        final boolean valid = icon.getName().endsWith(".png");
        if (valid) {
            Logger.info("Resourcepack Icon Accepted! (" + icon.getAbsolutePath() + ")");
        } else {
            Logger.warn("Resourcepack Icon Not Supported! (" + icon.getAbsolutePath() + ")");
        }
        return icon.getName().endsWith(".png");
    }

}
