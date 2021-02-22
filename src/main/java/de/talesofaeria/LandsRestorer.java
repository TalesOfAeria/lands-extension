package de.talesofaeria;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class LandsRestorer extends JavaPlugin {

    public static LandsRestorer plugin;

    @Override
    public void onEnable() {
        plugin = this;
        getServer().getPluginManager().registerEvents(new Listener(), this);

        File baseFolder = new File(plugin.getDataFolder() + "/");
        if(!baseFolder.exists())
            baseFolder.mkdir();

        File schemFolder = new File(plugin.getDataFolder() + "/regionsaves/");
        if(!schemFolder.exists())
            schemFolder.mkdir();


    }
}
