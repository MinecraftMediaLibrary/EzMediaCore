package io.github.pulsebeat02.ezmediacore;

import io.github.pulsebeat02.ezmediacore.player.output.vlc.VLCFrameOutput;
import io.github.pulsebeat02.ezmediacore.player.output.vlc.VLCStandardOutput;
import io.github.pulsebeat02.ezmediacore.player.output.vlc.sout.VLCTranscoderOutput;
import javax.swing.JFrame;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.log.LogLevel;
import uk.co.caprica.vlcj.log.NativeLog;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

public class VLCTranscodingTest {

  public static void main(final String[] args) {

    final VLCTranscoderOutput transcoder = VLCTranscoderOutput.ofOutput();
    transcoder.setProperty(VLCTranscoderOutput.SCODEC, "none");
    transcoder.setProperty(VLCTranscoderOutput.VCODEC, "none");
    transcoder.setProperty(VLCTranscoderOutput.ACODEC, "vorbis");

    final VLCStandardOutput standard = VLCStandardOutput.ofSection("http");
    standard.setProperty(VLCStandardOutput.DST, "localhost:8554/audio.ogg");

    final VLCFrameOutput output = new VLCFrameOutput();
    output.setTranscoder(transcoder);
    output.setStandard(standard);

    System.out.println(output);

    final MediaPlayerFactory factory = new MediaPlayerFactory("-vvv");
    final NativeLog log = factory.application().newLog();
    log.setLevel(LogLevel.DEBUG);
    log.addLogListener(
        (level, module, file, line, name, header, id, message) ->
            System.out.printf("[%-20s] (%-20s) %7s: %s%s%n", module, name, level, message, System.lineSeparator()));

    final EmbeddedMediaPlayerComponent component = new EmbeddedMediaPlayerComponent();
    final JFrame frame = new JFrame("Video Player");
    frame.setSize(512, 1024);
    frame.add(component);
    frame.setVisible(true);

    component.mediaPlayer().media().play("C:\\Users\\Brandon Li\\test.mkv", output.toString());
  }
}
