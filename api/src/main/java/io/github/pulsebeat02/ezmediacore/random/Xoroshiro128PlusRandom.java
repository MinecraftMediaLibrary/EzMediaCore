/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.pulsebeat02.ezmediacore.random;

import java.io.Serial;
import java.util.Random;

/**
 * Implements Xoroshiro128PlusRandom. Not synchronized (anywhere).
 *
 * @see "http://xoroshiro.di.unimi.it/"
 */
public class Xoroshiro128PlusRandom extends Random {

  private static final double DOUBLE_UNIT = 0x1.0p-53; // 1.0 / (1L << 53);
  private static final float FLOAT_UNIT = 0x1.0p-24f; // 1.0 / (1L << 24);
  @Serial private static final long serialVersionUID = -2223015498326800080L;

  private long s0, s1;

  public Xoroshiro128PlusRandom(final long seed) {
    // Must be here, the only Random constructor. Has side-effects on setSeed, see below.
    super(0);

    this.s0 = MurmurHash3.hash(seed);
    this.s1 = MurmurHash3.hash(this.s0);

    if (this.s0 == 0 && this.s1 == 0) {
      this.s0 = MurmurHash3.hash(0xdeadbeefL);
      this.s1 = MurmurHash3.hash(this.s0);
    }
  }

  @Override
  public void setSeed(final long seed) {
    // Called from super constructor and observing uninitialized state?
    if (this.s0 == 0 && this.s1 == 0) {
      return;
    }

    throw AssertingRandom.noSetSeed();
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
    // Leave superclass's implementation.
    return super.nextInt(n);
  }

  @Override
  public double nextGaussian() {
    // Leave superclass's implementation.
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
