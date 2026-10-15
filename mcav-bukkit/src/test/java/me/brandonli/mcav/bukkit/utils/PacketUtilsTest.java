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
package me.brandonli.mcav.bukkit.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.epoll.EpollChannelOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import me.brandonli.mcav.bukkit.media.map.MapPacketFactory;
import me.brandonli.mcav.bukkit.media.map.MapTilePatch;
import me.brandonli.mcav.bukkit.testing.FakeServer;
import me.brandonli.mcav.bukkit.testing.UtilityClassAssertions;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Tests {@link PacketUtils}.
 */
final class PacketUtilsTest {

  private static final UUID FIRST = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private static final UUID SECOND = UUID.fromString("00000000-0000-0000-0000-000000000002");

  private static final UUID LATE = UUID.fromString("00000000-0000-0000-0000-000000000003");

  private FakeServer server;

  @BeforeEach
  void startServer() {
    this.server = FakeServer.start();
  }

  @AfterEach
  void stopServer() {
    this.server.close();
  }

  private Listener captureListener(final Class<? extends Event> eventType, final EventPriority priority, final int registrations) {
    final PluginManager pluginManager = this.server.getPluginManager();
    final Plugin plugin = this.server.getPlugin();
    final ArgumentCaptor<Listener> captor = ArgumentCaptor.forClass(Listener.class);
    verify(pluginManager, times(registrations)).registerEvent(
      eq(eventType),
      captor.capture(),
      eq(priority),
      any(EventExecutor.class),
      eq(plugin)
    );
    return captor.getValue();
  }

  private Listener captureListener(final int registrations) {
    return this.captureListener(PlayerJoinEvent.class, EventPriority.LOWEST, registrations);
  }

  private EventExecutor captureExecutor(final Class<? extends Event> eventType, final EventPriority priority) {
    final PluginManager pluginManager = this.server.getPluginManager();
    final Plugin plugin = this.server.getPlugin();
    final ArgumentCaptor<EventExecutor> captor = ArgumentCaptor.forClass(EventExecutor.class);
    verify(pluginManager).registerEvent(eq(eventType), any(Listener.class), eq(priority), captor.capture(), eq(plugin));
    return captor.getValue();
  }

  @Test
  void shouldDistinguishReconnectsWhileKeepingAnUnchangedConnectionsIdentity() throws ReflectiveOperationException, EventException {
    final CraftPlayer first = this.server.addPlayer(FIRST);
    this.server.addPlayer(SECOND);
    this.server.injectModule();
    final Object original = Objects.requireNonNull(PacketUtils.connectionIdentity(FIRST));
    assertNotSame(original, PacketUtils.connectionIdentity(SECOND));
    assertNull(PacketUtils.connectionIdentity(LATE));
    final Listener listener = this.captureListener(1);
    final EventExecutor join = this.captureExecutor(PlayerJoinEvent.class, EventPriority.LOWEST);
    final PlayerJoinEvent joined = mock(PlayerJoinEvent.class);
    when(joined.getPlayer()).thenReturn(first);
    join.execute(listener, joined);
    assertSame(original, PacketUtils.connectionIdentity(FIRST), "a repeated event cannot invalidate a live baseline");
    PacketUtils.init();
    assertSame(original, PacketUtils.connectionIdentity(FIRST), "refreshing the cache keeps a live connection's identity");

    this.server.removePlayer(FIRST);
    final CraftPlayer replacement = this.server.addPlayer(FIRST);
    when(joined.getPlayer()).thenReturn(replacement);
    join.execute(listener, joined);
    final Object reconnected = Objects.requireNonNull(PacketUtils.connectionIdentity(FIRST));
    assertNotSame(original, reconnected, "a new connection cannot inherit the previous client's map state");
    PacketUtils.shutdown();
    assertNull(PacketUtils.connectionIdentity(FIRST));
    assertEquals(
      "Player must not be null",
      assertThrows(NullPointerException.class, () -> PacketUtils.connectionIdentity(null)).getMessage()
    );
  }

  @Test
  void shouldCacheTheConnectionsOfPlayersThatAreAlreadyOnline() throws ReflectiveOperationException {
    this.server.addPlayer(FIRST);
    this.server.addPlayer(SECOND);
    this.server.injectModule();
    final boolean firstConnected = PacketUtils.isConnected(FIRST);
    final boolean secondConnected = PacketUtils.isConnected(SECOND);
    final boolean lateConnected = PacketUtils.isConnected(LATE);
    assertTrue(firstConnected);
    assertTrue(secondConnected);
    assertFalse(lateConnected, "players that are not online cannot receive packets");
  }

  @Test
  void shouldKeepTheCacheInSyncWithJoinsAndQuits() throws ReflectiveOperationException, EventException {
    this.server.injectModule();
    final Listener listener = this.captureListener(1);
    final EventExecutor joinExecutor = this.captureExecutor(PlayerJoinEvent.class, EventPriority.LOWEST);
    final EventExecutor quitExecutor = this.captureExecutor(PlayerQuitEvent.class, EventPriority.MONITOR);
    final CraftPlayer late = this.server.addPlayer(LATE);
    final boolean beforeJoin = PacketUtils.isConnected(LATE);
    final PlayerJoinEvent joinEvent = mock(PlayerJoinEvent.class);
    when(joinEvent.getPlayer()).thenReturn(late);
    final PlayerQuitEvent quitEvent = mock(PlayerQuitEvent.class);
    when(quitEvent.getPlayer()).thenReturn(late);

    joinExecutor.execute(listener, joinEvent);
    final boolean connectedAfterJoin = PacketUtils.isConnected(LATE);
    quitExecutor.execute(listener, quitEvent);
    final boolean connectedAfterQuit = PacketUtils.isConnected(LATE);

    assertFalse(beforeJoin);
    assertTrue(connectedAfterJoin);
    assertFalse(connectedAfterQuit);
  }

  @Test
  void shouldIgnoreEventsOfOtherTypes() throws ReflectiveOperationException, EventException {
    final CraftPlayer first = this.server.addPlayer(FIRST);
    this.server.injectModule();
    final Listener listener = this.captureListener(1);
    final EventExecutor joinExecutor = this.captureExecutor(PlayerJoinEvent.class, EventPriority.LOWEST);
    final EventExecutor quitExecutor = this.captureExecutor(PlayerQuitEvent.class, EventPriority.MONITOR);
    final CraftPlayer late = this.server.addPlayer(LATE);
    final PlayerJoinEvent joinEvent = mock(PlayerJoinEvent.class);
    when(joinEvent.getPlayer()).thenReturn(late);
    final PlayerQuitEvent quitEvent = mock(PlayerQuitEvent.class);
    when(quitEvent.getPlayer()).thenReturn(first);

    joinExecutor.execute(listener, quitEvent);
    quitExecutor.execute(listener, joinEvent);
    final boolean lateConnected = PacketUtils.isConnected(LATE);
    final boolean firstConnected = PacketUtils.isConnected(FIRST);

    assertFalse(lateConnected, "a quit is not handled as a join");
    assertTrue(firstConnected, "a join is not handled as a quit");
  }

  @Test
  void shouldHandleJoinsFirstAndQuitsLastWithOneListener() throws ReflectiveOperationException {
    this.server.injectModule();
    final Listener joinListener = this.captureListener(PlayerJoinEvent.class, EventPriority.LOWEST, 1);
    final Listener quitListener = this.captureListener(PlayerQuitEvent.class, EventPriority.MONITOR, 1);
    assertSame(joinListener, quitListener, "one listener owns both handlers, so both are unregistered together");
  }

  @Test
  void shouldSendEveryPacketInOrderToConnectedViewersOnly() throws ReflectiveOperationException {
    this.server.addPlayer(FIRST);
    this.server.injectModule();
    final Component firstText = Component.literal("first");
    final Component secondText = Component.literal("second");
    final ClientboundSystemChatPacket first = new ClientboundSystemChatPacket(firstText, false);
    final ClientboundSystemChatPacket second = new ClientboundSystemChatPacket(secondText, true);
    final List<UUID> viewers = List.of(SECOND, FIRST);
    PacketUtils.sendPackets(viewers, first, second);
    final List<Packet<?>> packets = this.server.getSentPackets(FIRST);
    final List<Packet<?>> expected = List.of(first, second);
    assertEquals(expected, packets);
  }

  /**
   * Gives the first player's packet listener a network connection of its own, without a channel.
   */
  private static Connection useConnection() {
    final Connection connection = FakeServer.connection();
    final CraftPlayer player = (CraftPlayer) Objects.requireNonNull(Bukkit.getPlayer(FIRST));
    FakeServer.connect(player.getHandle().connection, connection);
    return connection;
  }

  @Test
  void shouldCapTheUnsentBytesOfAConnectionThatTakesTheOption() throws ReflectiveOperationException {
    this.server.addPlayer(FIRST);
    this.server.injectModule();
    assertFalse(PacketUtils.limitUnsent(SECOND, 1024));
    assertFalse(PacketUtils.limitUnsent(FIRST, 1024));
    final Connection connection = useConnection();
    assertFalse(PacketUtils.limitUnsent(FIRST, 1024));
    connection.channel = new EmbeddedChannel();
    assertFalse(PacketUtils.limitUnsent(FIRST, 1024));
    final Channel epoll = mock(Channel.class);
    final ChannelConfig config = mock(ChannelConfig.class);
    when(epoll.config()).thenReturn(config);
    when(config.setOption(EpollChannelOption.TCP_NOTSENT_LOWAT, 1024L)).thenReturn(true);
    connection.channel = epoll;
    assertTrue(PacketUtils.limitUnsent(FIRST, 1024));
    verify(config).setOption(EpollChannelOption.TCP_NOTSENT_LOWAT, 1024L);
    assertThrows(NullPointerException.class, () -> PacketUtils.limitUnsent(null, 1));
    assertThrows(IllegalArgumentException.class, () -> PacketUtils.limitUnsent(FIRST, 0));
  }

  @Test
  void shouldWriteAPacketWithAListenerToTheConnectionAtOnce() throws ReflectiveOperationException {
    this.server.addPlayer(FIRST);
    this.server.injectModule();
    final Connection connection = useConnection();
    final EmbeddedChannel channel = new EmbeddedChannel();
    connection.channel = channel;
    final MapTilePatch page = new MapTilePatch(100, 0, 0, 128, 1, new byte[128]);
    final ClientboundBundlePacket bundle = MapPacketFactory.bundles(List.of(page)).getFirst().packet();
    final List<ChannelFuture> writes = new ArrayList<>();
    assertTrue(PacketUtils.sendPacket(FIRST, bundle, writes::add));
    // Paper holds a map packet that a plugin sends from another thread until the server's next tick, which put two
    // frames of a 30 fps MCV2 video into one tick about every other tick
    assertSame(bundle, channel.readOutbound(), "the packet is written to the connection and flushed at once");
    assertEquals(1, writes.size());
    assertTrue(writes.getFirst().isSuccess(), "the listener runs once the write is done");
    assertTrue(channel.close().isSuccess());
    assertTrue(PacketUtils.sendPacket(FIRST, bundle, writes::add));
    assertEquals(2, writes.size());
    assertFalse(writes.getLast().isSuccess(), "a write that fails runs the listener too");
    connection.channel = null;
    assertFalse(PacketUtils.sendPacket(FIRST, bundle, writes::add), "a connection that is not open yet takes nothing");
    assertFalse(PacketUtils.sendPacket(SECOND, bundle, writes::add), "a player who is not online takes nothing");
    assertEquals(2, writes.size(), "the listener of a packet that was not sent never runs");
  }

  @Test
  void shouldReplaceThePreviousListenerWhenInitializingAgain() throws ReflectiveOperationException {
    this.server.injectModule();
    final Listener firstListener = this.captureListener(1);
    try (final MockedStatic<HandlerList> handlerLists = Mockito.mockStatic(HandlerList.class)) {
      PacketUtils.init();
      handlerLists.verify(() -> HandlerList.unregisterAll(firstListener));
    }
    final Listener secondListener = this.captureListener(2);
    assertNotSame(firstListener, secondListener);
  }

  @Test
  void shouldUnregisterTheListenerAndForgetEveryConnectionWhenShuttingDown() throws ReflectiveOperationException {
    this.server.addPlayer(FIRST);
    this.server.injectModule();
    final Listener listener = this.captureListener(1);
    try (final MockedStatic<HandlerList> handlerLists = Mockito.mockStatic(HandlerList.class)) {
      PacketUtils.shutdown();
      PacketUtils.shutdown();
      handlerLists.verify(() -> HandlerList.unregisterAll(listener), times(1));
      handlerLists.verify(() -> HandlerList.unregisterAll(any(Listener.class)), times(1));
    }
    final boolean connected = PacketUtils.isConnected(FIRST);
    assertFalse(connected);
  }

  @Test
  void shouldNeedAnInjectedPlugin() {
    assertThrows(IllegalStateException.class, PacketUtils::init);
  }

  @Test
  void shouldRejectMissingArguments() {
    final List<UUID> viewers = List.of(FIRST);
    assertThrows(NullPointerException.class, () -> PacketUtils.isConnected(null));
    assertThrows(NullPointerException.class, () -> PacketUtils.sendPackets(null));
    assertThrows(NullPointerException.class, () -> PacketUtils.sendPackets(viewers, (Packet<?>[]) null));
  }

  @Test
  void shouldNotBeInstantiable() {
    UtilityClassAssertions.assertNotInstantiable(PacketUtils.class);
  }
}
