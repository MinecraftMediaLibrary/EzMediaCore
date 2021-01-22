package com.github.pulsebeat02.utility;

import com.github.pulsebeat02.resourcepack.PackFormatVersioning;

import java.io.File;

public class ResourcepackUtilities {

    public static boolean validatePackFormat(final int format) {
        for (PackFormatVersioning version : PackFormatVersioning.values()) {
            if (format == version.getPackFormatID()) {
                return true;
            }
        }
        return false;
    }

    public static boolean validateResourcepackIcon(final File icon) {
        return icon.getName().endsWith(".png");
    }

}
