package io.github.pulsebeat02.ezmediacore.pipeline.output.audio;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import io.github.pulsebeat02.ezmediacore.pipeline.frame.FramePacket;
import io.github.pulsebeat02.ezmediacore.pipeline.output.FrameOutputSource;

public abstract class DiscordSendHandlerOutput implements FrameOutputSource<FramePacket>, AudioSendHandler {
}
