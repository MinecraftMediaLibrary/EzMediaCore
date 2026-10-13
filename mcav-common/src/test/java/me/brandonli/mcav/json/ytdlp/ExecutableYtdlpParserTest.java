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
package me.brandonli.mcav.json.ytdlp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import me.brandonli.mcav.capability.Capability;
import me.brandonli.mcav.capability.CapabilityGuard;
import me.brandonli.mcav.capability.installer.ytdlp.YtdlpInstaller;
import me.brandonli.mcav.json.ytdlp.format.Format;
import me.brandonli.mcav.json.ytdlp.format.UrlParseDump;
import me.brandonli.mcav.media.source.uri.UriSource;
import me.brandonli.mcav.utils.runtime.CommandTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Tests {@link ExecutableYtdlpParser} with canned yt-dlp output instead of the real executable.
 */
final class ExecutableYtdlpParserTest {

  private static final String URL = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
  private static final Path EXECUTABLE = Path.of("tools", "yt-dlp");
  private static final Duration TIMEOUT = Duration.ofMinutes(2);

  private static UriSource source() {
    final URI uri = URI.create(URL);
    return UriSource.uri(uri);
  }

  private static String fixture() throws IOException {
    try (final InputStream stream = ExecutableYtdlpParserTest.class.getResourceAsStream("/ytdlp/video.json")) {
      assertNotNull(stream, "the fixture must be on the test classpath");
      final byte[] bytes = stream.readAllBytes();
      final String content = new String(bytes, StandardCharsets.UTF_8);
      return content.strip();
    }
  }

  private static CommandTask task(final int exitCode, final String output, final String errorOutput) throws IOException {
    final CommandTask task = Mockito.mock(CommandTask.class);
    Mockito.when(task.run(ArgumentMatchers.any(Duration.class))).thenReturn(exitCode);
    Mockito.when(task.getOutput()).thenReturn(output);
    Mockito.when(task.getErrorOutput()).thenReturn(errorOutput);
    return task;
  }

  private static ExecutableYtdlpParser parser(final CommandTask task, final AtomicReference<String[]> command) {
    final Function<String[], CommandTask> factory = arguments -> {
      command.set(arguments);
      return task;
    };
    return new ExecutableYtdlpParser(() -> EXECUTABLE, factory);
  }

  private static YtdlpParseException assertParseFails(final String output) throws IOException {
    final CommandTask task = task(0, output, "");
    final AtomicReference<String[]> command = new AtomicReference<>();
    final ExecutableYtdlpParser parser = parser(task, command);
    final UriSource source = source();
    return assertThrows(YtdlpParseException.class, () -> parser.parse(source));
  }

  @Test
  void shouldParseTheFirstDocumentWithTheExpectedCommandLine() throws IOException {
    final String fixture = fixture();
    final String output = fixture + "\n{\"id\": \"second\", \"title\": \"Second Video\"}\n";
    final CommandTask task = task(0, output, "");
    final AtomicReference<String[]> command = new AtomicReference<>();
    final ExecutableYtdlpParser parser = parser(task, command);
    final UriSource source = source();
    final UrlParseDump dump = parser.parse(source, "--cookies-from-browser", "firefox");
    final String executable = EXECUTABLE.toString();
    final String[] expected = {
      executable,
      "--dump-json",
      "--no-cache-dir",
      "--no-playlist",
      "--playlist-items",
      "1",
      "--no-warnings",
      "--cookies-from-browser",
      "firefox",
      "--",
      URL,
    };
    final String[] actual = command.get();
    final List<Format> formats = dump.formats;
    assertArrayEquals(expected, actual);
    assertEquals("dQw4w9WgXcQ", dump.id);
    assertEquals("Example Video", dump.title);
    assertNotNull(formats);
    final int formatsCount = formats.size();
    assertEquals(3, formatsCount);
    final CommandTask verifiedTask = Mockito.verify(task);
    verifiedTask.run(TIMEOUT);
  }

  @Test
  void shouldAcceptMoreExtraArgumentsThanItsFixedOptions() throws IOException {
    final CommandTask task = task(0, "{\"id\":\"many\"}", "");
    final AtomicReference<String[]> command = new AtomicReference<>();
    final ExecutableYtdlpParser parser = parser(task, command);
    final UriSource source = source();
    final String[] options = {
      "--format",
      "best",
      "--socket-timeout",
      "10",
      "--retries",
      "2",
      "--fragment-retries",
      "2",
      "--user-agent",
      "mcav-test",
      "--no-progress",
      "--quiet",
    };
    final UrlParseDump dump = parser.parse(source, options);
    final String[] actual = command.get();
    assertNotNull(actual);
    assertEquals("many", dump.id);
    assertEquals(21, actual.length);
    for (int index = 0; index < options.length; index++) {
      final String option = actual[7 + index];
      assertEquals(options[index], option);
    }
    final String separator = actual[19];
    assertEquals("--", separator);
    final String url = actual[20];
    assertEquals(URL, url);
  }

  @Test
  void shouldRunWithoutExtraArguments() throws IOException {
    final CommandTask task = task(0, "{\"id\": \"plain\"}", "");
    final AtomicReference<String[]> command = new AtomicReference<>();
    final ExecutableYtdlpParser parser = parser(task, command);
    final UriSource source = source();
    final UrlParseDump dump = parser.parse(source);
    final String executable = EXECUTABLE.toString();
    final String[] expected = {
      executable,
      "--dump-json",
      "--no-cache-dir",
      "--no-playlist",
      "--playlist-items",
      "1",
      "--no-warnings",
      "--",
      URL,
    };
    final String[] actual = command.get();
    assertArrayEquals(expected, actual);
    assertEquals("plain", dump.id);
  }

  @Test
  void shouldIgnoreSurroundingWhitespaceAndWindowsLineEndings() throws IOException {
    final CommandTask windowsTask = task(0, "\r\n  {\"id\": \"first\"}  \r\n{\"id\": \"second\"}\r\n", "");
    final CommandTask singleLineTask = task(0, "  {\"id\": \"only\"}  \n\n", "");
    final AtomicReference<String[]> command = new AtomicReference<>();
    final ExecutableYtdlpParser windowsParser = parser(windowsTask, command);
    final ExecutableYtdlpParser singleLineParser = parser(singleLineTask, command);
    final UriSource source = source();
    final UrlParseDump windowsDump = windowsParser.parse(source);
    final UrlParseDump singleLineDump = singleLineParser.parse(source);
    assertEquals("first", windowsDump.id);
    assertEquals("only", singleLineDump.id);
  }

  @Test
  void shouldReportOutputWithoutMetadata() throws IOException {
    final YtdlpParseException empty = assertParseFails("");
    final YtdlpParseException blank = assertParseFails(" \r\n\t\n");
    final YtdlpParseException jsonNull = assertParseFails("null\n");
    final String expected = "URL %s got no metadata from yt-dlp!".formatted(URL);
    final String emptyMessage = empty.getMessage();
    final String blankMessage = blank.getMessage();
    final String jsonNullMessage = jsonNull.getMessage();
    final Throwable jsonNullCause = jsonNull.getCause();
    assertEquals(expected, emptyMessage);
    assertEquals(expected, blankMessage);
    assertEquals(expected, jsonNullMessage);
    assertNull(jsonNullCause);
  }

  @Test
  void shouldReportMalformedMetadata() throws IOException {
    final YtdlpParseException truncated = assertParseFails("{\"id\": \"abc\", \"title\": ");
    final YtdlpParseException array = assertParseFails("[1, 2, 3]");
    final YtdlpParseException wrongType = assertParseFails("{\"formats\": \"not a list\"}");
    final String expected = "URL %s got invalid metadata from yt-dlp!".formatted(URL);
    final String truncatedMessage = truncated.getMessage();
    final String arrayMessage = array.getMessage();
    final String wrongTypeMessage = wrongType.getMessage();
    final Throwable truncatedCause = truncated.getCause();
    final Throwable arrayCause = array.getCause();
    final Throwable wrongTypeCause = wrongType.getCause();
    assertEquals(expected, truncatedMessage);
    assertEquals(expected, arrayMessage);
    assertEquals(expected, wrongTypeMessage);
    assertInstanceOf(JsonSyntaxException.class, truncatedCause);
    assertInstanceOf(JsonSyntaxException.class, arrayCause);
    assertInstanceOf(JsonSyntaxException.class, wrongTypeCause);
  }

  @Test
  void shouldReportMetadataWhoseDecimalFieldHoldsText() throws IOException {
    final YtdlpParseException duration = assertParseFails("{\"id\": \"abc\", \"duration\": \"long\"}");
    final YtdlpParseException quality = assertParseFails("{\"id\": \"abc\", \"formats\": [{\"quality\": \"high\"}]}");
    final String expected = "URL %s got invalid metadata from yt-dlp!".formatted(URL);
    final String durationMessage = duration.getMessage();
    final String qualityMessage = quality.getMessage();
    final Throwable durationCause = duration.getCause();
    final Throwable qualityCause = quality.getCause();
    assertEquals(expected, durationMessage);
    assertEquals(expected, qualityMessage);
    assertInstanceOf(NumberFormatException.class, durationCause);
    assertInstanceOf(NumberFormatException.class, qualityCause);
  }

  @Test
  void shouldRejectTextAfterTheDocumentInsteadOfIgnoringIt() throws IOException {
    final YtdlpParseException trailingText = assertParseFails("{\"id\": \"abc\"} unexpected trailing text");
    final YtdlpParseException secondDocument = assertParseFails("{\"id\": \"abc\"} {\"id\": \"def\"}");
    final String expected = "URL %s got invalid metadata from yt-dlp!".formatted(URL);
    final String trailingTextMessage = trailingText.getMessage();
    final String secondDocumentMessage = secondDocument.getMessage();
    final Throwable trailingTextCause = trailingText.getCause();
    final Throwable secondDocumentCause = secondDocument.getCause();
    assertEquals(expected, trailingTextMessage);
    assertEquals(expected, secondDocumentMessage);
    assertInstanceOf(JsonSyntaxException.class, trailingTextCause);
    assertInstanceOf(JsonSyntaxException.class, secondDocumentCause);
  }

  @Test
  void shouldReportTheErrorOutputWhenYtdlpFails() throws IOException {
    final CommandTask task = task(1, "partial output", "ERROR: [youtube] dQw4w9WgXcQ: Private video\n");
    final AtomicReference<String[]> command = new AtomicReference<>();
    final ExecutableYtdlpParser parser = parser(task, command);
    final UriSource source = source();
    final YtdlpParseException exception = assertThrows(YtdlpParseException.class, () -> parser.parse(source));
    final String message = exception.getMessage();
    final String expected = "yt-dlp failed with \"ERROR: [youtube] dQw4w9WgXcQ: Private video\" while URL %s was being resolved!".formatted(
      URL
    );
    assertEquals(expected, message);
  }

  @Test
  void shouldReportTheStandardOutputWhenTheErrorOutputIsBlank() throws IOException {
    final CommandTask task = task(2, "  Usage: yt-dlp [OPTIONS] URL\n", " \n");
    final AtomicReference<String[]> command = new AtomicReference<>();
    final ExecutableYtdlpParser parser = parser(task, command);
    final UriSource source = source();
    final YtdlpParseException exception = assertThrows(YtdlpParseException.class, () -> parser.parse(source));
    final String message = exception.getMessage();
    final String expected = "yt-dlp failed with \"Usage: yt-dlp [OPTIONS] URL\" while URL %s was being resolved!".formatted(URL);
    assertEquals(expected, message);
  }

  @Test
  void shouldPropagateInstallationFailuresWithoutRunningAnything() {
    final IOException failure = new IOException("no network!");
    final AtomicBoolean created = new AtomicBoolean();
    final ExecutableYtdlpParser parser = new ExecutableYtdlpParser(
      () -> {
        throw failure;
      },
      _ -> {
        created.set(true);
        return Mockito.mock(CommandTask.class);
      }
    );
    final UriSource source = source();
    final IOException thrown = assertThrows(IOException.class, () -> parser.parse(source));
    final boolean taskCreated = created.get();
    assertSame(failure, thrown);
    assertFalse(taskCreated);
  }

  @Test
  void shouldPropagateFailuresToStartTheProcess() throws IOException {
    final IOException failure = new IOException("cannot start!");
    final CommandTask task = Mockito.mock(CommandTask.class);
    Mockito.when(task.run(ArgumentMatchers.any(Duration.class))).thenThrow(failure);
    final AtomicReference<String[]> command = new AtomicReference<>();
    final ExecutableYtdlpParser parser = parser(task, command);
    final UriSource source = source();
    final IOException thrown = assertThrows(IOException.class, () -> parser.parse(source));
    assertSame(failure, thrown);
  }

  @Test
  void shouldRejectNullArgumentsBeforeInstallingAnything() {
    final AtomicBoolean located = new AtomicBoolean();
    final ExecutableYtdlpParser parser = new ExecutableYtdlpParser(
      () -> {
        located.set(true);
        return EXECUTABLE;
      },
      _ -> Mockito.mock(CommandTask.class)
    );
    final UriSource source = source();
    assertThrows(NullPointerException.class, () -> parser.parse(null));
    assertThrows(NullPointerException.class, () -> parser.parse(source, (String[]) null));
    assertThrows(NullPointerException.class, () -> parser.parse(source, "--cookies-from-browser", null));
    final boolean installed = located.get();
    assertFalse(installed);
  }

  @ParameterizedTest
  @ValueSource(strings = { "--cookies=/x", "--exec=calc", "ftp://example.com/video", "file:///etc/passwd", "www.youtube.com/watch?v=x" })
  void shouldRejectInputsThatAreNotAbsoluteWebUrlsBeforeRunningAnything(final String raw) {
    final URI uri = URI.create(raw);
    final UriSource source = Mockito.mock(UriSource.class);
    Mockito.when(source.getUri()).thenReturn(uri);
    final AtomicBoolean located = new AtomicBoolean();
    final AtomicBoolean created = new AtomicBoolean();
    final ExecutableYtdlpParser parser = new ExecutableYtdlpParser(
      () -> {
        located.set(true);
        return EXECUTABLE;
      },
      _ -> {
        created.set(true);
        return Mockito.mock(CommandTask.class);
      }
    );
    final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(source));
    final String message = exception.getMessage();
    final boolean installed = located.get();
    final boolean taskCreated = created.get();
    final boolean namesTheInput = message.contains(raw);
    assertTrue(namesTheInput, message);
    assertFalse(installed);
    assertFalse(taskCreated);
  }

  @Test
  void shouldAcceptWebUrlsWhateverTheCaseOfTheScheme() throws IOException {
    final CommandTask task = task(0, "{\"id\": \"upper\"}", "");
    final AtomicReference<String[]> command = new AtomicReference<>();
    final ExecutableYtdlpParser parser = parser(task, command);
    final URI uri = URI.create("HTTPS://www.youtube.com/watch?v=upper");
    final UriSource source = UriSource.uri(uri);
    final UrlParseDump dump = parser.parse(source);
    final String[] actual = command.get();
    final String lastArgument = actual[actual.length - 1];
    assertEquals("upper", dump.id);
    assertEquals("HTTPS://www.youtube.com/watch?v=upper", lastArgument);
  }

  @Test
  void shouldReturnTheSharedDefaultParserFromSimple() {
    final YtdlpParser first = YtdlpParser.simple();
    final YtdlpParser second = YtdlpParser.simple();
    assertSame(first, second);
    assertSame(ExecutableYtdlpParser.INSTANCE, first);
    assertInstanceOf(ExecutableYtdlpParser.class, first);
  }

  @Test
  void shouldRunTheInstalledExecutableInTheDefaultParser() throws IOException {
    final String javaHome = System.getProperty("java.home");
    final String launcherName = File.separatorChar == '\\' ? "java.exe" : "java";
    final Path javaPath = Path.of(javaHome, "bin", launcherName);
    final YtdlpInstaller installer = Mockito.mock(YtdlpInstaller.class);
    Mockito.when(installer.download(true)).thenReturn(javaPath);
    final UriSource source = source();
    final YtdlpParser parser = YtdlpParser.simple();
    try (final MockedStatic<YtdlpInstaller> installers = Mockito.mockStatic(YtdlpInstaller.class)) {
      installers.when(YtdlpInstaller::shared).thenReturn(installer);
      final YtdlpParseException exception = assertThrows(YtdlpParseException.class, () -> parser.parse(source));
      final String message = exception.getMessage();
      final boolean namesTheUrl = message.endsWith("\" while URL " + URL + " was being resolved!");
      final boolean namesTheOption = message.contains("--dump-json");
      assertTrue(namesTheUrl, message);
      assertTrue(namesTheOption, message);
    }
    final YtdlpInstaller verifiedInstaller = Mockito.verify(installer);
    verifiedInstaller.download(true);
  }

  @Test
  void shouldRefuseInTheDefaultParserWhileTheLibraryPreparesYtdlp() {
    final YtdlpInstaller installer = Mockito.mock(YtdlpInstaller.class);
    final UriSource source = source();
    final YtdlpParser parser = YtdlpParser.simple();
    final CapabilityGuard guard = CapabilityGuard.shared();
    guard.markPreparing(Capability.YT_DLP);
    try (final MockedStatic<YtdlpInstaller> installers = Mockito.mockStatic(YtdlpInstaller.class)) {
      installers.when(YtdlpInstaller::shared).thenReturn(installer);
      final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> parser.parse(source));
      final String message = exception.getMessage();
      assertEquals(
        "yt-dlp is still being prepared in the background; wait for MCAVApi.whenCapabilityReady(Capability.YT_DLP) or try again shortly!",
        message
      );
    } finally {
      guard.forget(Capability.YT_DLP);
    }
    Mockito.verifyNoInteractions(installer);
  }
}
