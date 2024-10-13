package rewrite.pipeline.output.audio;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import rewrite.pipeline.frame.FramePacket;
import rewrite.pipeline.output.FrameOutputSource;

public abstract class DiscordSendHandlerOutput implements FrameOutputSource<FramePacket>, AudioSendHandler {
}
