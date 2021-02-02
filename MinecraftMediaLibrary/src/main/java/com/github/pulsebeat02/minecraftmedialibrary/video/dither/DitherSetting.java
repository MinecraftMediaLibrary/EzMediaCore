package com.github.pulsebeat02.minecraftmedialibrary.video.dither;

import org.jetbrains.annotations.NotNull;

public enum DitherSetting {

    STANDARD_MINECRAFT_DITHER(new StandardDithering()),
    SIERRA_FILTER_LITE_DITHER(new FilterLiteDither()),
    BAYER_ORDERED_2_DIMENSIONAL(new OrderedDithering(OrderedDithering.DitherType.ModeTwo)),
    BAYER_ORDERED_4_DIMENSIONAL(new OrderedDithering(OrderedDithering.DitherType.ModeFour)),
    BAYER_ORDERED_8_DIMENSIONAL(new OrderedDithering(OrderedDithering.DitherType.ModeEight)),
    FLOYD_STEINBERG_DITHER(new FloydImageDither());

    private final AbstractDitherHolder holder;

    DitherSetting(@NotNull final AbstractDitherHolder holder) {
        this.holder = holder;
    }

    public AbstractDitherHolder getHolder() {
        return holder;
    }

}
