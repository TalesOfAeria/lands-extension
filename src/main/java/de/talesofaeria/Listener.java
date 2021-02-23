package de.talesofaeria;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.angeschossen.lands.api.events.ChunkDeleteEvent;
import me.angeschossen.lands.api.events.ChunkPreClaimEvent;
import me.angeschossen.lands.api.events.LandCreateEvent;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Listener implements org.bukkit.event.Listener {

    @EventHandler
    public void onAreaClaim(ChunkPreClaimEvent e) {

        World world = e.getWorld().getWorld();
        int chunkX = e.getX();
        int chunkZ = e.getZ();

        File areaSchematic = new File(LandsRestorer.plugin.getDataFolder().getAbsolutePath() + "/regionsaves/" + chunkX + "-" + chunkZ + ".schem");

        CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(world), BlockVector3.at(chunkX * 16, 0, chunkZ * 16), BlockVector3.at(chunkX * 16 + 15, world.getMaxHeight(), chunkZ * 16 + 15));
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(world), -1)) {
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    editSession, region, clipboard, region.getMinimumPoint()
            );
            Operations.complete(forwardExtentCopy);
        } catch (WorldEditException worldEditException) {
            worldEditException.printStackTrace();
        }

        try(ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(areaSchematic))) {
            writer.write(clipboard);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @EventHandler
    public void onAreaUnclaim(ChunkDeleteEvent e) {
        World world = e.getWorld();
        int chunkX = e.getX();
        int chunkZ = e.getZ();
        File areaSchematic = new File(LandsRestorer.plugin.getDataFolder().getAbsolutePath() + "/regionsaves/" + chunkX + "-" + chunkZ + ".schem");

        Bukkit.getScheduler().runTaskAsynchronously(LandsRestorer.plugin, () -> {
            Clipboard clipboard = null;
            ClipboardFormat format = ClipboardFormats.findByFile(areaSchematic);
            try (ClipboardReader reader = format.getReader(new FileInputStream(areaSchematic))) {
                clipboard = reader.read();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(world), -1)) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(chunkX * 16, 0, chunkZ * 16))
                        .build();
                Operations.complete(operation);
            } catch (WorldEditException worldEditException) {
                worldEditException.printStackTrace();
            }
        });


    }


}
