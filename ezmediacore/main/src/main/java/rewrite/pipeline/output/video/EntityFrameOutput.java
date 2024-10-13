package rewrite.pipeline.output.video;

import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.nms.PacketHandler;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import rewrite.dimension.Resolution;
import rewrite.pipeline.output.DelayConfiguration;
import rewrite.pipeline.output.NamedStringCharacter;
import rewrite.pipeline.output.Viewers;
import rewrite.dimension.Dimension;
import rewrite.pipeline.frame.FramePacket;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public final class EntityFrameOutput<T extends Entity> extends MinecraftFrameOutput<FramePacket> {

  private final Class<T> type;
  private final Consumer<T> consumer;
  private final Location location;
  private final NamedStringCharacter character;
  private final Entity[] entities;
  private final AtomicBoolean started;
  private volatile long lastUpdated;

  public EntityFrameOutput(final EzMediaCore core,
                           final Viewers viewers,
                           final DelayConfiguration configuration,
                           final Dimension resolution,
                           final Location location,
                           final NamedStringCharacter character,
                           final Class<T> type,
                           final Consumer<T> consumer) {
    super(core, viewers, resolution, configuration);
    final int height = resolution.getHeight();
    this.location = location;
    this.character = character;
    this.type = type;
    this.consumer = consumer == null ? ignored -> {
    } : consumer;
    this.entities = new Entity[height];
    this.started = new AtomicBoolean(false);
  }

  public static <T extends Entity> EntityFrameOutputBuilder<T> builder() {
    return new EntityFrameOutputBuilder<>();
  }

  @Override
  public void output(final FramePacket input) {
    if (!this.started.get()) {
      this.spawnEntities();
      this.started.set(true);
    }
    final long time = System.currentTimeMillis();
    final Dimension resolution = this.getResolution();
    final int width = resolution.getWidth();
    final String name = this.character.getCharacter();
    final DelayConfiguration configuration = this.getDelayConfiguration();
    final long delay = configuration.getDelay();
    if (time - this.lastUpdated > delay) {
      final EzMediaCore core = this.getCore();
      final PacketHandler handler = core.getHandler();
      final Viewers viewers = this.getViewers();
      final UUID[] watchers = viewers.getViewers();
      final int height = this.entities.length;
      final int[] data = input.getRGBSamples();
      handler.displayEntities(watchers, this.entities, data, name, width, height);
      this.lastUpdated = time;
    }
  }

  @Override
  public void release() {
    for (final Entity entity : this.entities) {
      if (entity != null) {
        entity.remove();
      }
    }
  }

  private void spawnEntities() {
    final Dimension resolution = this.getResolution();
    final int height = resolution.getHeight();
    final int width = resolution.getWidth();
    final String character = this.character.getCharacter();
    final World world = requireNonNull(this.location.getWorld());
    final Location clone = this.location.clone();
    for (int i = height - 1; i >= 0; i--) {
      final String repeated = character.repeat(width);
      final Entity entity = world.spawn(this.location, this.type, this.consumer);
      entity.setCustomName(repeated);
      entity.setCustomNameVisible(true);
      this.entities[i] = entity;
      clone.add(0.0, 0.225, 0.0);
    }
  }

  public static class EntityFrameOutputBuilder<T extends Entity> {

    private Viewers viewers = Viewers.onlinePlayers();
    private DelayConfiguration configuration = DelayConfiguration.DELAY_20_MS;
    private Dimension resolution = Resolution.X360_640;
    private NamedStringCharacter character = NamedStringCharacter.TINY_SQUARE;
    private Class<T> type = (Class<T>) ArmorStand.class;
    private Consumer<T> consumer = ignored -> {};

    public EntityFrameOutputBuilder<T> viewers(final Viewers viewers) {
      this.viewers = viewers;
      return this;
    }

    public EntityFrameOutputBuilder<T> delay(final DelayConfiguration configuration) {
      this.configuration = configuration;
      return this;
    }

    public EntityFrameOutputBuilder<T> resolution(final Dimension resolution) {
      this.resolution = resolution;
      return this;
    }

    public EntityFrameOutputBuilder<T> character(final NamedStringCharacter character) {
      this.character = character;
      return this;
    }

    public EntityFrameOutputBuilder<T> type(final Class<T> type) {
      this.type = type;
      return this;
    }

    public EntityFrameOutputBuilder<T> consumer(final Consumer<T> consumer) {
      this.consumer = consumer;
      return this;
    }

    public EntityFrameOutput<T> build(final EzMediaCore core, final Location location) {
      return new EntityFrameOutput<>(core, this.viewers, this.configuration, this.resolution, location, this.character, this.type, this.consumer);
    }
  }
}
