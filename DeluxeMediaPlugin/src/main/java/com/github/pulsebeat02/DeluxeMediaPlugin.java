package com.github.pulsebeat02;

import com.github.pulsebeat02.video.dither.FilterLiteDither;
import com.github.pulsebeat02.video.itemframe.ItemFrameCallback;
import com.github.pulsebeat02.video.vlcj.VLCJIntegratedPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class DeluxeMediaPlugin extends JavaPlugin {

    private MinecraftMediaLibrary library;

    private VLCJIntegratedPlayer player;
    private ItemFrameCallback callback;

    @Override
    public void onEnable() {
        library = new MinecraftMediaLibrary(this, getDataFolder().getPath(), true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        int height;
        int width;
        String[] dims;
        switch (args.length) {
            case 1:
                if (player == null) {
                    return false;
                }
                if (args[0].equalsIgnoreCase("stop")) {
                    player.stop();
                    sender.sendMessage("Stopped the Video");
                } else if (args[0].equalsIgnoreCase("start")) {
                    sender.sendMessage("Playing the Video");
                    player.start();
                } else if (args[0].equalsIgnoreCase("release")) {
                    player = null;
                    callback = null;
                }
                break;
            case 2:
                if (callback == null) {
                    return true;
                }
                if (args[0].equalsIgnoreCase("setmap")) {
                    int mapId = Integer.parseInt(args[1]);
                    sender.sendMessage("Set map to " + mapId);
                } else if (args[0].equalsIgnoreCase("setdim")) {
                    dims = args[1].split(":");
                    width = Integer.parseInt(dims[0]);
                    height = Integer.parseInt(dims[1]);
                    callback.setWidth(width);
                    callback.setHeight(height);
                    sender.sendMessage(String.format("Set map dimensions to %dx%d", width, height));
                }
                break;
            case 3:
                if (args[0].equalsIgnoreCase("load")) {
                    String url = args[1];
                    dims = args[2].split(":");
                    width = Integer.parseInt(dims[0]);
                    height = Integer.parseInt(dims[1]);
                    if (player != null) {
                        callback = null;
                    }
                    callback = new ItemFrameCallback(library, null, 0, 5, 5, width, 0,
                            new FilterLiteDither());
                    player = new VLCJIntegratedPlayer(url, width, height, callback::send);
                    player.start();
                }
        }
        return true;
    }

}
