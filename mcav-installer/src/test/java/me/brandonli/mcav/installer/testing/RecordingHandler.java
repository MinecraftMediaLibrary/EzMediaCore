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
package me.brandonli.mcav.installer.testing;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public final class RecordingHandler extends Handler {

  private final List<LogRecord> records;

  public RecordingHandler() {
    this.records = new CopyOnWriteArrayList<>();
  }

  @Override
  public void publish(final LogRecord logRecord) {
    this.records.add(logRecord);
  }

  @Override
  public void flush() {}

  @Override
  public void close() {}

  public List<LogRecord> getRecords() {
    return this.records;
  }
}
