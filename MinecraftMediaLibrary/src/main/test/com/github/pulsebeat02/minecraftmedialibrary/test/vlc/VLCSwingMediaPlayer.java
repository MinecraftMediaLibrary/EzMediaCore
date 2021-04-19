package com.github.pulsebeat02.minecraftmedialibrary.test.vlc;

import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

public class VLCSwingMediaPlayer extends JFrame {

  private static final long serialVersionUID = -1962402166242950337L;

  private final EmbeddedMediaPlayerComponent mediaPlayerComponent;

  public VLCSwingMediaPlayer(final String title) {
    super(title);
    mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
    initialize();
  }

  public void initialize() {

    setBounds(100, 100, 600, 400);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(final WindowEvent e) {
            mediaPlayerComponent.release();
            System.exit(0);
          }
        });

    final JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(mediaPlayerComponent, BorderLayout.CENTER);

    final JPanel controlsPane = new JPanel();
    final JButton playButton = new JButton("Play");
    controlsPane.add(playButton);
    contentPane.add(controlsPane, BorderLayout.SOUTH);
    playButton.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().play());
    setContentPane(contentPane);
    setVisible(true);

  }

  public void loadVideo(final String path) {
    mediaPlayerComponent.mediaPlayer().media().startPaused(path);
  }

  public static void main(final String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (final Exception e) {
      e.printStackTrace();
    }
    final NativeDiscovery discovery = new NativeDiscovery();
    discovery.discover();
    System.out.println(discovery.discoveredPath());
    final Map<String, String> env = System.getenv();
    for (final String envName : env.keySet()) {
      System.out.format("%s=%s%n", envName, env.get(envName));
    }
    new VLCSwingMediaPlayer("VLC Media Player").loadVideo("C:\\test.mp4");
  }
}
