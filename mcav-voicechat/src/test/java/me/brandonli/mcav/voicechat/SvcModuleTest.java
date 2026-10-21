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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.maxhenkel.voicechat.api.VoicechatServerApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests {@link SvcModule}.
 */
final class SvcModuleTest {

  private final SvcModule module = new SvcModule();

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
    final VoicechatServerApi api = SvcModule.getVoiceChatApi();
    assertNull(api);
    final IllegalStateException exception = assertThrows(IllegalStateException.class, SvcModule::requireVoiceChatApi);
    final String message = exception.getMessage();
    assertEquals("The Simple Voice Chat API was not injected; call SvcModule#inject from your voice chat plugin!", message);
  }

  @Test
  void shouldKeepTheInjectedApiUntilStopped() {
    final VoicechatServerApi api = Mockito.mock(VoicechatServerApi.class);
    this.module.inject(api);
    final VoicechatServerApi injected = SvcModule.getVoiceChatApi();
    final VoicechatServerApi required = SvcModule.requireVoiceChatApi();
    assertSame(api, injected);
    assertSame(api, required);
    this.module.stop();
    final VoicechatServerApi afterStop = SvcModule.getVoiceChatApi();
    assertNull(afterStop);
    assertThrows(IllegalStateException.class, SvcModule::requireVoiceChatApi);
  }

  @Test
  void shouldReplaceAPreviouslyInjectedApi() {
    final VoicechatServerApi first = Mockito.mock(VoicechatServerApi.class);
    final VoicechatServerApi second = Mockito.mock(VoicechatServerApi.class);
    this.module.inject(first);
    this.module.inject(second);
    final VoicechatServerApi injected = SvcModule.getVoiceChatApi();
    assertSame(second, injected);
  }

  @Test
  void shouldRejectANullApiAndKeepThePreviousOne() {
    final VoicechatServerApi api = Mockito.mock(VoicechatServerApi.class);
    this.module.inject(api);
    assertThrows(NullPointerException.class, () -> this.module.inject(null));
    final VoicechatServerApi injected = SvcModule.getVoiceChatApi();
    assertSame(api, injected);
  }
}
