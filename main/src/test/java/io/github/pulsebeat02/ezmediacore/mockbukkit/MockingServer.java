package io.github.pulsebeat02.ezmediacore.mockbukkit;

import be.seeseemelk.mockbukkit.ServerMock;

public final class MockingServer extends ServerMock {

  MockingServer() {}

  @Override
  public boolean getOnlineMode() {
    return true;
  }
}
