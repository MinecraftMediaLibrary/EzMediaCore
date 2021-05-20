/*............................................................................................
 . Copyright © 2021 Brandon Li                                                               .
 .                                                                                           .
 . Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
 . software and associated documentation files (the “Software”), to deal in the Software     .
 . without restriction, including without limitation the rights to use, copy, modify, merge, .
 . publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
 . persons to whom the Software is furnished to do so, subject to the following conditions:  .
 .                                                                                           .
 . The above copyright notice and this permission notice shall be included in all copies     .
 . or substantial portions of the Software.                                                  .
 .                                                                                           .
 . THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
 .  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
 .   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
 .   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
 .   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
 .   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
 .   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
 .   SOFTWARE.                                                                               .
 ............................................................................................*/

package com.github.pulsebeat02.minecraftmedialibrary.test.vlc;

import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class VLCSwingMediaPlayer extends JFrame {

  private static final long serialVersionUID = -1962402166242950337L;

  private final EmbeddedMediaPlayerComponent mediaPlayerComponent;

  public VLCSwingMediaPlayer(final String title) {
    super(title);
    mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
  }

  public static void main(final String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (final Exception e) {
      e.printStackTrace();
    }
    System.out.println(new NativeDiscovery().discover());
    final VLCSwingMediaPlayer player = new VLCSwingMediaPlayer("VLC Media Player");
    player.initialize();
    player.loadVideo("/Users/bli24/Desktop/test.mp4");
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
    playButton.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().start());

    final JButton pauseButton = new JButton("Pause");
    controlsPane.add(pauseButton);
    contentPane.add(controlsPane, BorderLayout.SOUTH);
    playButton.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().pause());

    final JButton resumeButton = new JButton("Resume");
    controlsPane.add(resumeButton);
    contentPane.add(controlsPane, BorderLayout.SOUTH);
    playButton.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().play());

    setContentPane(contentPane);
    setVisible(true);
  }

  public void loadVideo(final String path) {
    mediaPlayerComponent.mediaPlayer().media().startPaused(path);
  }
}
