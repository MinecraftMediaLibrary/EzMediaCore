/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/11/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.resourcepack;

public enum PackFormatVersioning {
    /**
     * Ver 1 15 pack format versioning.
     */
    VER_1_15(5),
    /** Ver 1 15 1 pack format versioning. */
    VER_1_15_1(5),
    /** Ver 1 15 2 pack format versioning. */
    VER_1_15_2(5),
    /** Ver 1 16 1 pack format versioning. */
    VER_1_16_1(5),
    /** Ver 1 16 2 pack format versioning. */
    VER_1_16_2(6),
    /** Ver 1 16 3 pack format versioning. */
    VER_1_16_3(6),
    /** Ver 1 16 4 pack format versioning. */
    VER_1_16_4(6),
    /**
     * Ver 1 16 5 pack format versioning.
     */
    VER_1_16_5(6);

    private final int packFormat;

    PackFormatVersioning(final int id) {
        this.packFormat = id;
    }

    /**
     * Gets pack format id.
     *
     * @return the pack format id
     */
    public int getPackFormatID() {
        return packFormat;
    }
}
