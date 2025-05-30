package com.example.azrovassimpleeconomy;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import java.util.Collection;
import org.bukkit.entity.Item;

public class JobListener implements Listener {

    private final AzrovasSimpleEconomy plugin;

    public JobListener(AzrovasSimpleEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        JobType job = plugin.getPlayerJob(player);
        if (job == null) return;

        Block block = event.getBlock();
        Material material = block.getType();

        switch (job) {
            case FARMER:
                handleFarmerBlockBreak(player, block, material);
                break;
            case MINER:
                handleMinerBlockBreak(player, block, material);
                break;
            case LUMBERJACK:
                handleLumberjackBlockBreak(player, block, material);
                break;
            default:
                break;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        JobType job = plugin.getPlayerJob(player);

        if (job != JobType.FISHERMAN) {
            return;
        }

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            if (event.getCaught() instanceof Item) {
                Item caughtItemEntity = (Item) event.getCaught();
                ItemStack caughtItemStack = caughtItemEntity.getItemStack();
                Material fishMaterial = caughtItemStack.getType();
                String actionKey = fishMaterial.name();

                plugin.processJobAction(player, JobType.FISHERMAN, actionKey, 0.0);
            }
        }
    }

    private void handleFarmerBlockBreak(Player player, Block block, Material material) {
        if (block.getBlockData() instanceof Ageable) {
            Ageable ageable = (Ageable) block.getBlockData();
            if (ageable.getAge() != ageable.getMaximumAge()) {
                return;
            }
        }

        String actionKey = material.name();
        plugin.processJobAction(player, JobType.FARMER, actionKey, 0.0);
    }

    private void handleMinerBlockBreak(Player player, Block block, Material material) {
        String actionKey = material.name();
        plugin.processJobAction(player, JobType.MINER, actionKey, 0.0);
    }

    private void handleLumberjackBlockBreak(Player player, Block block, Material material) {
        String actionKey = material.name();
        if (actionKey.endsWith("_LOG") || actionKey.endsWith("_STEM") || actionKey.endsWith("_WOOD") || actionKey.endsWith("_HYPHAE")) {
            plugin.processJobAction(player, JobType.LUMBERJACK, actionKey, 0.0);
        }
    }

    // Basic Explorer listener - very simplified. Could be expanded greatly.
    // This example is too simplistic and likely not what's desired for a real explorer job.
    // A proper explorer job might track new chunks entered and pay after X unique chunks.
    // For now, Explorer job is mostly passive or informational via /jobs info.
    /*
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && 
            event.getFrom().getBlockY() == event.getTo().getBlockY() && 
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return; // Player hasn't moved to a new block
        }

        Player player = event.getPlayer();
        JobType job = plugin.getPlayerJob(player);
        if (job != JobType.EXPLORER) return;

        // Extremely basic: pay a tiny amount for moving to a new chunk if it's different from the last one.
        // This needs a way to store last known chunk per player to avoid spamming rewards.
        // For now, this part is commented out as it needs more robust implementation.
        // plugin.processJobAction(player, JobType.EXPLORER, "CHUNK_EXPLORED", 0.01); 
    }
    */
} 