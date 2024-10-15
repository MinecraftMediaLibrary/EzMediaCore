package rewrite.pipeline.grabbers;

public final class GrabberAudioFormat {

  private final String format;
  private final String samplingFormat;
  private final int sampleRate;
  private final int sampleSizeInBits;
  private final int channels;
  private final boolean signed;
  private final boolean bigEndian;

  public GrabberAudioFormat(final String format, final String samplingFormat, final int sampleRate, final int sampleSizeInBits, final int channels, final boolean signed, final boolean bigEndian) {
    this.format = format;
    this.samplingFormat = samplingFormat;
    this.sampleRate = sampleRate;
    this.sampleSizeInBits = sampleSizeInBits;
    this.channels = channels;
    this.signed = signed;
    this.bigEndian = bigEndian;
  }

  public String getFormat() {
    return this.format;
  }

  public int getSampleRate() {
    return this.sampleRate;
  }

  public int getSampleSizeInBits() {
    return this.sampleSizeInBits;
  }

  public int getChannels() {
    return this.channels;
  }

  public boolean isSigned() {
    return this.signed;
  }

  public boolean isBigEndian() {
    return this.bigEndian;
  }

  public String getSamplingFormat() {
    return this.samplingFormat;
  }
}
