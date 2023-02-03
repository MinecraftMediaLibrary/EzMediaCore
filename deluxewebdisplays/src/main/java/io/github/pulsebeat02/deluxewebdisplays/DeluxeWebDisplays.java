package io.github.pulsebeat02.deluxewebdisplays;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class DeluxeWebDisplays extends JavaPlugin {

  private final BukkitAudiences audiences;
  private final Audience console;

  public DeluxeWebDisplays() {
    this.audiences = BukkitAudiences.create(this);
    this.console = this.audiences.console();
  }

  private void startMetrics() {
    new Metrics(this, 10229);
  }

  public @NotNull BukkitAudiences getAudiences() {
    return this.audiences;
  }

  public @NotNull Audience getConsole() {
    return this.console;
  }
}
