package io.github.pulsebeat02.ezmediacore;

import io.github.pulsebeat02.ezmediacore.jlibdl.JLibDL;
import java.io.IOException;

public class JLibDLTest {

  public static void main(final String[] args) throws IOException, InterruptedException {
    final JLibDL jLibDL = new JLibDL();
    System.out.println(
        jLibDL.request("https://www.twitch.tv/shroud").getInfo().getFormats().get(0).getUrl());
    System.out.println(
        jLibDL
            .request("https://www.youtube.com/watch?v=5qap5aO4i9A")
            .getInfo()
            .getFormats()
            .get(0)
            .getUrl());
  }
}
