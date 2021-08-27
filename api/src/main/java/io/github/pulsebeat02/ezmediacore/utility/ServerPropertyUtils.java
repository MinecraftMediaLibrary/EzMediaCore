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
package io.github.pulsebeat02.ezmediacore.utility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;

public final class ServerPropertyUtils {

  private static final Class<?> serverClass;
  private static final Class<?> propertyManagerClass;
  private static final Method getServerMethod;
  private static final Method getPropertyManagerMethod;
  private static final Method setPropertyMethod;
  private static final Method savePropertiesFileMethod;

  static {
    try {
      final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
      serverClass = Class.forName("net.minecraft.server." + version + ".MinecraftServer");
      propertyManagerClass = Class.forName("net.minecraft.server." + version + ".PropertyManager");
      getServerMethod = serverClass.getDeclaredMethod("getServer");
      getPropertyManagerMethod = serverClass.getDeclaredMethod("getPropertyManager");
      setPropertyMethod =
          propertyManagerClass.getDeclaredMethod("setProperty", String.class, Object.class);
      savePropertiesFileMethod = propertyManagerClass.getDeclaredMethod("savePropertiesFile");
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private ServerPropertyUtils() {
  }

  public static void savePropertiesFile() {
    try {
      savePropertiesFileMethod.invoke(getPropertyManager());
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  public static void setServerProperty(final ServerProperty property, final Object value)
      throws Exception {
    setPropertyMethod.invoke(getPropertyManager(), property.getPropertyName(), value);
  }

  public static Object getPropertyManager() {
    try {
      return getPropertyManagerMethod.invoke(getServerMethod.invoke(null));
    } catch (final IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * An enum storing the possible server properties you can change.
   */
  public enum ServerProperty {
    SPAWN_PROTECTION("spawn-protection"),
    SERVER_NAME("server-name"),
    FORCE_GAMEMODE("force-gamemode"),
    NETHER("allow-nether"),
    DEFAULT_GAMEMODE("gamemode"),
    QUERY("enable-query"),
    PLAYER_IDLE_TIMEOUT("player-idle-timeout"),
    DIFFICULTY("difficulty"),
    SPAWN_MONSTERS("spawn-monsters"),
    OP_PERMISSION_LEVEL("op-permission-level"),
    RESOURCE_PACK_HASH("resource-pack-hash"),
    RESOURCE_PACK("resource-pack"),
    ANNOUNCE_PLAYER_ACHIEVEMENTS("announce-player-achievements"),
    PVP("pvp"),
    SNOOPER("snooper-enabled"),
    LEVEL_NAME("level-name"),
    LEVEL_TYPE("level-type"),
    LEVEL_SEED("level-seed"),
    HARDCORE("hardcore"),
    COMMAND_BLOCKS("enable-command-blocks"),
    MAX_PLAYERS("max-players"),
    PACKET_COMPRESSION_LIMIT("network-compression-threshold"),
    MAX_WORLD_SIZE("max-world-size"),
    IP("server-ip"),
    PORT("server-port"),
    DEBUG_MODE("debug"),
    SPAWN_NPCS("spawn-npcs"),
    SPAWN_ANIMALS("spawn-animals"),
    FLIGHT("allow-flight"),
    VIEW_DISTANCE("view-distance"),
    WHITE_LIST("white-list"),
    GENERATE_STRUCTURES("generate-structures"),
    MAX_BUILD_HEIGHT("max-build-height"),
    MOTD("motd"),
    REMOTE_CONTROL("enable-rcon");

    private final String name;

    ServerProperty(final String name) {
      this.name = name;
    }

    public String getPropertyName() {
      return this.name;
    }
  }
}
