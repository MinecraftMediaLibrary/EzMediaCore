package io.github.pulsebeat02.ezmediacore.jcef;

import java.io.File;
import java.io.IOException;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import me.friwi.jcefmaven.impl.progress.ConsoleProgressHandler;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefBrowserFactory;

public final class CefAppTest {

  public static void main(final String[] args)
      throws UnsupportedPlatformException, CefInitializationException, IOException, InterruptedException {

    final CefAppBuilder builder = new CefAppBuilder();
    builder.setInstallDir(new File("jcef-bundle"));
    builder.setProgressHandler(new ConsoleProgressHandler());
    builder.addJcefArgs("--disable-gpu");
    builder.getCefSettings().windowless_rendering_enabled = true;

    final CefApp app = builder.build();
    final CefClient client = app.createClient();
    final CefBrowser browser = client.createBrowser("https://www.google.com", true, true);


  }

}
