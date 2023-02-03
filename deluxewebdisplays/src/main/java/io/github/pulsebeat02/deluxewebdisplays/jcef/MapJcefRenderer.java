package io.github.pulsebeat02.deluxewebdisplays.jcef;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefDragData;
import org.cef.handler.CefRenderHandler;

import java.awt.*;
import java.nio.ByteBuffer;

public abstract class MapJcefRenderer implements CefRenderHandler {

  private CefBrowser browser;

  @Override
  public void onPaint(
          final CefBrowser browser,
          final boolean popup,
          final Rectangle[] dirtyRects,
          final ByteBuffer buffer,
          final int width,
          final int height) {

  }

  @Override
  public boolean onCursorChange(final CefBrowser browser, final int cursorType) {
    return false;
  }

  @Override
  public boolean startDragging(final CefBrowser browser, final CefDragData dragData, final int mask, final int x, final int y) {
    return false;
  }

  @Override
  public void updateDragCursor(final CefBrowser browser, final int operation) {}
}
