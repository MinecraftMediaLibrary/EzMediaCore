/*
 * This file is part of mcav, a media playback library for Java
 * Copyright (C) Brandon Li <https://brandonli.me/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.brandonli.mcav.bukkit.media.mcv2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;

/**
 * Whatever the video, the presets and however fast the budget encodes: the pacer never stays long on a rung whose frames
 * take more than their time, a step up it takes under a steady load holds, and it climbs back to the top once the budget
 * frees.
 */
final class Mcv2PacerPropertyTest {

  private static final String SEED = "20260926";

  private static final long SECOND = 1_000_000_000L;

  private static final List<int[]> SIZES = List.of(new int[] { 1920, 1080 }, new int[] { 1280, 720 }, new int[] { 960, 540 });

  private static final double FULL_WIDTH = 1920.0;
  private static final double FULL_HEIGHT = 1080.0;

  private static final int MILLISECONDS_PER_SECOND = 1000;

  /** The presets a screen steps through, when it has more than one. */
  private static final List<Mcv2Pacer.Preset> PRESETS = List.of(
    new Mcv2Pacer.Preset("live", 1),
    new Mcv2Pacer.Preset("live-fast", 0.6),
    new Mcv2Pacer.Preset("fastest", 0.35)
  );

  /** A rung the pacer was on, from when to when, whether a step up brought it there and a step down ended it. */
  private record Stretch(Mcv2Pacer.Rung rung, long from, long to, boolean climbed, boolean leftDown) {}

  /**
   * A screen's encoder under a budget: frames arrive at the video's rate; an encode takes the time of a 1080p frame at
   * the budget's current speed, scaled by the rung's pixels, its preset's cost and a little noise; while the encoder
   * works, the newest frame it is asked for waits, as in the screen.
   */
  private static final class Simulation {

    private final Mcv2Pacer pacer;

    private final long interval;

    private final double noise;

    private final Random random;

    private final List<Stretch> stretches = new ArrayList<>();

    private long now;

    private long started;

    private boolean climbed;

    private long busyUntil = Long.MIN_VALUE;

    private double cost;

    private boolean encoding;

    private boolean waiting;

    Simulation(final int sizes, final int presets, final double framesPerSecond, final double noise, final long seed) {
      this.pacer = new Mcv2Pacer(
        SIZES.subList(0, sizes),
        presets == 1 ? List.of(Mcv2Pacer.Preset.ONLY) : PRESETS.subList(0, presets),
        true
      );
      this.interval = Math.round(SECOND / framesPerSecond);
      this.noise = noise;
      this.random = new Random(seed);
    }

    private double costOf(final Mcv2Pacer.Rung rung, final double fullMilliseconds) {
      final double scale = 1 + this.noise * (2 * this.random.nextDouble() - 1);
      return (fullMilliseconds * scale * rung.width() * rung.height() * rung.preset().cost()) / (FULL_WIDTH * FULL_HEIGHT);
    }

    private void record(final Mcv2Pacer.Change change, final long at) {
      this.stretches.add(new Stretch(change.from(), this.started, at, this.climbed, change.down()));
      this.started = at;
      this.climbed = !change.down() && !change.from().isDithered();
    }

    /** Plays the video for a while with 1080p frames taking {@code fullMilliseconds}. */
    void play(final double seconds, final double fullMilliseconds) {
      final long end = this.now + (long) (seconds * SECOND);
      while (this.now < end) {
        final long arrival = this.now + this.interval;
        while (this.encoding && this.busyUntil <= arrival) {
          this.encoding = false;
          final Mcv2Pacer.Change change = this.pacer.encoded(this.cost, false, this.busyUntil);
          if (change != null) {
            this.record(change, this.busyUntil);
            this.waiting = false;
          }
          if (this.waiting) {
            this.waiting = false;
            this.start(this.busyUntil, fullMilliseconds);
          }
        }
        this.now = arrival;
        final Mcv2Pacer.Change tried = this.pacer.arrive(arrival);
        if (tried != null) {
          this.record(tried, arrival);
        }
        if (this.pacer.isEncoded()) {
          if (this.encoding) {
            this.waiting = true;
          } else {
            this.start(arrival, fullMilliseconds);
          }
        }
      }
    }

    private void start(final long at, final double fullMilliseconds) {
      this.cost = this.costOf(this.pacer.getRung(), fullMilliseconds);
      this.busyUntil = at + (long) (this.cost * 1e6);
      this.encoding = true;
    }

    List<Stretch> finish() {
      final List<Stretch> all = new ArrayList<>(this.stretches);
      all.add(new Stretch(this.pacer.getRung(), this.started, this.now, this.climbed, false));
      return all;
    }
  }

  @Provide
  Arbitrary<Double> frameRates() {
    return Arbitraries.of(24.0, 25.0, 30.0, 50.0, 60.0);
  }

  private static double frameMilliseconds(final Mcv2Pacer.Rung rung, final double framesPerSecond) {
    return (1000.0 * rung.divisor()) / framesPerSecond;
  }

  /**
   * Under a steady load, a rung whose frames take more than their time is left within the time the pacer waits plus
   * two encodes, since it only learns how long a frame takes once the frame is done; and no step up has to be undone.
   */
  @Property(seed = SEED, tries = 300)
  boolean shouldNeverStayLongOverItsTime(
    @ForAll("frameRates") final double framesPerSecond,
    @ForAll @IntRange(min = 1, max = 3) final int sizes,
    @ForAll @IntRange(min = 1, max = 3) final int presets,
    @ForAll @DoubleRange(min = 1, max = 3000) final double fullMilliseconds,
    @ForAll @DoubleRange(min = 0, max = 0.1) final double noise,
    @ForAll final long seed
  ) {
    final Simulation simulation = new Simulation(sizes, presets, framesPerSecond, noise, seed);
    simulation.play(180, fullMilliseconds);
    final List<Stretch> stretches = simulation.finish();
    for (int stretchIndex = 0; stretchIndex < stretches.size(); stretchIndex++) {
      final Stretch stretch = stretches.get(stretchIndex);
      if (stretch.rung().isDithered()) {
        continue;
      }
      final double rungMilliseconds =
        (fullMilliseconds * stretch.rung().width() * stretch.rung().height() * stretch.rung().preset().cost()) / (FULL_WIDTH * FULL_HEIGHT);
      final double seconds = (stretch.to() - stretch.from()) / (double) SECOND;
      final boolean over = rungMilliseconds * (1 - noise) > frameMilliseconds(stretch.rung(), framesPerSecond);
      // it waits for the rung's first samples (and, on the first rung, for the encoder to warm up), then a second
      final double frame =
        Math.max(rungMilliseconds * (1 + noise), frameMilliseconds(stretch.rung(), framesPerSecond)) / MILLISECONDS_PER_SECOND;
      final double allowed =
        Mcv2Pacer.DOWN_SECONDS + (Mcv2Pacer.MINIMUM_SAMPLES + 2) * frame + 1 + (stretchIndex == 0 ? Mcv2Pacer.STARTUP_SECONDS : 0);
      if (over && seconds > allowed) {
        return false;
      }
      // a step up under a steady load is never undone
      if (stretch.climbed() && stretch.leftDown()) {
        return false;
      }
    }
    return true;
  }

  /** After a heavy load, once the budget frees up, the pacer climbs back to the top rung. */
  @Property(seed = SEED, tries = 300)
  boolean shouldClimbBackWhenTheLoadDrops(
    @ForAll("frameRates") final double framesPerSecond,
    @ForAll @IntRange(min = 1, max = 3) final int sizes,
    @ForAll @IntRange(min = 1, max = 3) final int presets,
    @ForAll @DoubleRange(min = 20, max = 3000) final double heavyMilliseconds,
    @ForAll @DoubleRange(min = 0.05, max = 0.55) final double lightShare,
    @ForAll @DoubleRange(min = 0, max = 0.1) final double noise,
    @ForAll final long seed
  ) {
    final Simulation simulation = new Simulation(sizes, presets, framesPerSecond, noise, seed);
    simulation.play(20, heavyMilliseconds);
    // the top rung fits in lightShare of its frame time, with room to spare
    simulation.play(120, (lightShare * MILLISECONDS_PER_SECOND) / framesPerSecond);
    return simulation.pacer.getRung().equals(simulation.pacer.getLadder().getFirst());
  }
}
