package io.github.pulsebeat02.ezmediacore.dither.algorithm.random;

import java.io.Serial;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public final class Xoroshiro128PlusRandom extends Random {

  private static final double DOUBLE_UNIT = 0x1.0p-53;
  private static final float FLOAT_UNIT = 0x1.0p-24f;

  @Serial private static final long serialVersionUID = -7653551394619089245L;

  private long s0, s1;

  public Xoroshiro128PlusRandom() {
    this(createSeed() ^ System.nanoTime());
  }

  public Xoroshiro128PlusRandom(final long seed) {
    super(0);
    this.s0 = MurmurHash3.hash(seed);
    this.s1 = MurmurHash3.hash(this.s0);
    if (this.s0 == 0 && this.s1 == 0) {
      this.s0 = MurmurHash3.hash(0xdeadbeefL);
      this.s1 = MurmurHash3.hash(this.s0);
    }
  }

  private static final AtomicLong UNIQUE = new AtomicLong(8682522807148012L);

  private static long createSeed() {
    for (; ; ) {
      final long current = UNIQUE.get();
      final long next = current * 1181783497276652981L;
      if (UNIQUE.compareAndSet(current, next)) {
        return next;
      }
    }
  }

  @Override
  public boolean nextBoolean() {
    return this.nextLong() >= 0;
  }

  @Override
  public void nextBytes(final byte[] bytes) {
    for (int i = 0, len = bytes.length; i < len; ) {
      long rnd = this.nextInt();
      for (int n = Math.min(len - i, 8); n-- > 0; rnd >>>= 8) {
        bytes[i++] = (byte) rnd;
      }
    }
  }

  @Override
  public double nextDouble() {
    return (this.nextLong() >>> 11) * DOUBLE_UNIT;
  }

  @Override
  public float nextFloat() {
    return (this.nextInt() >>> 8) * FLOAT_UNIT;
  }

  @Override
  public int nextInt() {
    return (int) this.nextLong();
  }

  @Override
  public int nextInt(final int n) {
    return super.nextInt(n);
  }

  @Override
  public double nextGaussian() {
    return super.nextGaussian();
  }

  @Override
  public long nextLong() {
    final long s0 = this.s0;
    long s1 = this.s1;
    final long result = s0 + s1;
    s1 ^= s0;
    this.s0 = Long.rotateLeft(s0, 55) ^ s1 ^ s1 << 14;
    this.s1 = Long.rotateLeft(s1, 36);
    return result;
  }

  @Override
  protected int next(final int bits) {
    return ((int) this.nextLong()) >>> (32 - bits);
  }
}
