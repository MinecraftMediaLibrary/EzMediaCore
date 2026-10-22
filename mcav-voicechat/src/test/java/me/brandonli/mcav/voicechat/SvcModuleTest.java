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
package me.brandonli.mcav.voicechat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.maxhenkel.voicechat.api.VoicechatServerApi;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests {@link SvcModule}.
 */
final class SvcModuleTest {

  private SvcModule module;

  @BeforeEach
  void setUp() {
    this.module = new SvcModule();
  }

  @AfterEach
  void clearTheInjectedApi() {
    this.module.stop();
  }

  @Test
  void shouldBeNamedSvc() {
    final String name = this.module.getModuleName();
    assertEquals("svc", name);
  }

  @Test
  void shouldHaveNoApiUntilOneIsInjected() {
    this.module.start();
    final Optional<VoicechatServerApi> api = SvcModule.getVoiceChatApi();
    assertEquals(Optional.empty(), api);

    final IllegalStateException exception = assertThrows(IllegalStateException.class, SvcModule::requireVoiceChatApi);
    final String message = exception.getMessage();
    assertEquals("The Simple Voice Chat API was not injected; call SvcModule#inject from your voice chat plugin!", message);
  }

  @Test
  void shouldKeepTheInjectedApiUntilStopped() {
    final VoicechatServerApi api = Mockito.mock(VoicechatServerApi.class);
    this.module.inject(api);
    final Optional<VoicechatServerApi> injected = SvcModule.getVoiceChatApi();
    final VoicechatServerApi required = SvcModule.requireVoiceChatApi();
    final Optional<VoicechatServerApi> expected = Optional.of(api);
    assertEquals(expected, injected);
    assertSame(api, required);

    this.module.stop();
    final Optional<VoicechatServerApi> afterStop = SvcModule.getVoiceChatApi();
    assertEquals(Optional.empty(), afterStop);
    assertThrows(IllegalStateException.class, SvcModule::requireVoiceChatApi);
  }

  @Test
  void shouldReplaceAPreviouslyInjectedApi() {
    final VoicechatServerApi first = Mockito.mock(VoicechatServerApi.class);
    final VoicechatServerApi second = Mockito.mock(VoicechatServerApi.class);
    this.module.inject(first);
    this.module.inject(second);
    final Optional<VoicechatServerApi> injected = SvcModule.getVoiceChatApi();
    final Optional<VoicechatServerApi> expected = Optional.of(second);
    assertEquals(expected, injected);
  }

  @Test
  void shouldRejectANullApiAndKeepThePreviousOne() {
    final VoicechatServerApi api = Mockito.mock(VoicechatServerApi.class);
    this.module.inject(api);
    assertThrows(NullPointerException.class, () -> this.module.inject(null));

    final Optional<VoicechatServerApi> injected = SvcModule.getVoiceChatApi();
    final Optional<VoicechatServerApi> expected = Optional.of(api);
    assertEquals(expected, injected);
  }
}
