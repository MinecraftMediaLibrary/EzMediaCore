package com.github.pulsebeat02.resourcepack;

public enum PackFormatVersioning {

    VER_1_15(5),
    VER_1_15_1(5),
    VER_1_15_2(5),
    VER_1_16_1(5),
    VER_1_16_2(6),
    VER_1_16_3(6),
    VER_1_16_4(6),
    VER_1_16_5(6);

    private final int formatID;

    PackFormatVersioning(final int id) {
        this.formatID = id;
    }

    public int getPackFormatID() {
        return formatID;
    }

}
