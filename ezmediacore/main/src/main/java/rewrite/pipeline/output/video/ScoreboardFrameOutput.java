package rewrite.pipeline.output.video;

import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.nms.PacketHandler;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.*;
import rewrite.dimension.Resolution;
import rewrite.pipeline.output.DelayConfiguration;
import rewrite.pipeline.output.Viewers;
import rewrite.pipeline.output.NamedStringCharacter;
import rewrite.dimension.Dimension;
import rewrite.pipeline.frame.FramePacket;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;

public final class ScoreboardFrameOutput extends MinecraftFrameOutput<FramePacket> {

  private final Scoreboard scoreboard;
  private final String name;
  private final NamedStringCharacter character;
  private final AtomicBoolean started;
  private volatile long lastUpdated;

  public ScoreboardFrameOutput(
          final EzMediaCore core,
          final Viewers viewers,
          final DelayConfiguration configuration,
          final String name,
          final Dimension resolution,
          final NamedStringCharacter character) {
    super(core, viewers, resolution, configuration);
    this.name = name;
    this.scoreboard = this.createScoreboard();
    this.started = new AtomicBoolean(false);
    this.character = character;
  }

  public static ScoreboardFrameOutputBuilder builder() {
    return new ScoreboardFrameOutputBuilder();
  }

  public Scoreboard createScoreboard() {
    final Server server = Bukkit.getServer();
    final ScoreboardManager manager = requireNonNull(server.getScoreboardManager());
    return manager.getNewScoreboard();
  }

  @Override
  public void output(final FramePacket input) {
    if (!this.started.get()) {
      this.registerScreen();
      this.started.set(true);
    }
    final EzMediaCore core = this.getCore();
    final Plugin plugin = core.getPlugin();
    final BukkitScheduler scheduler = Bukkit.getScheduler();
    final int[] data = input.getRGBSamples();
    scheduler.callSyncMethod(plugin, this.processRunnable(data));
  }

  private <T> Callable<T> processRunnable(final int[] data) {
    return () -> {
      final long time = System.currentTimeMillis();
      final DelayConfiguration configuration = this.getDelayConfiguration();
      final long delay = configuration.getDelay();
      if (time - this.lastUpdated > delay) {
        final EzMediaCore core = this.getCore();
        final PacketHandler handler = core.getHandler();
        final Viewers viewers = this.getViewers();
        final UUID[] watchers = viewers.getViewers();
        final Dimension resolution = this.getResolution();
        final int width = resolution.getWidth();
        final int height = resolution.getHeight();
        final String character = this.character.getCharacter();
        this.setViewerScoreboards();
        handler.displayScoreboard(watchers, this.scoreboard, data, character, width, height);
        this.lastUpdated = time;
      }
      return null;
    };
  }

  private Objective getObjective() {
    final UUID uuid = UUID.randomUUID();
    final String name = uuid.toString();
    final Objective objective = this.scoreboard.registerNewObjective(name, Criteria.DUMMY, this.name);
    objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    return objective;
  }

  private void registerScreen() {
    final Objective objective = this.getObjective();
    final Dimension resolution = this.getResolution();
    final int height = resolution.getHeight();
    for (int i = 0; i < height; i++) {
      final Team team = this.scoreboard.registerNewTeam("SLOT_" + i);
      final String entry = "COLOR_" + i;
      team.addEntry(entry);
      final Score score = objective.getScore(entry);
      objective.getScore(entry).setScore(height - i);
    }
  }

  private void setViewerScoreboards() {
    final Viewers viewers = this.getViewers();
    final Set<Player> players = viewers.getPlayers();
    for (final Player player : players) {
      player.setScoreboard(this.scoreboard);
    }
  }

  public static class ScoreboardFrameOutputBuilder {

    private Viewers viewers = Viewers.onlinePlayers();
    private DelayConfiguration configuration = DelayConfiguration.DELAY_20_MS;
    private String name = "Scoreboard Video Player";
    private Dimension resolution = Resolution.X360_640;
    private NamedStringCharacter character = NamedStringCharacter.TINY_SQUARE;

    public ScoreboardFrameOutputBuilder viewers(final Viewers viewers) {
      this.viewers = viewers;
      return this;
    }

    public ScoreboardFrameOutputBuilder delay(final DelayConfiguration configuration) {
      this.configuration = configuration;
      return this;
    }

    public ScoreboardFrameOutputBuilder name(final String name) {
      this.name = name;
      return this;
    }

    public ScoreboardFrameOutputBuilder resolution(final Dimension resolution) {
      this.resolution = resolution;
      return this;
    }

    public ScoreboardFrameOutputBuilder character(final NamedStringCharacter character) {
      this.character = character;
      return this;
    }

    public ScoreboardFrameOutput build(final EzMediaCore core) {
      return new ScoreboardFrameOutput(core, this.viewers, this.configuration, this.name, this.resolution, this.character);
    }
  }
}
