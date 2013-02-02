package com.zmanww.bukkit.SnowControl;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author zwollner
 * 
 */
public class PlayerListener implements Listener {

	private static SnowControl plugin;

	public PlayerListener(SnowControl instance) {
		plugin = instance;
	}

	@EventHandler()
	public void blockBreak(BlockBreakEvent event) {
		final Block block = event.getBlock();
		if (block.getType() == Material.ICE) {
			if (event.getPlayer().getItemInHand().getType() == Material.STONE_PICKAXE
					|| event.getPlayer().getItemInHand().getType() == Material.WOOD_PICKAXE
					|| event.getPlayer().getItemInHand().getType() == Material.IRON_PICKAXE
					|| event.getPlayer().getItemInHand().getType() == Material.GOLD_PICKAXE
					|| event.getPlayer().getItemInHand().getType() == Material.DIAMOND_PICKAXE) {
				block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.ICE, 1));
			}
		}
		final Location loc = event.getBlock().getLocation();
		if (block.getRelative(BlockFace.UP).getType() == Material.SNOW) {
			/*
			 * If block above broken is snow, then remove the next replaceable block under it so that the falling snow
			 * lands properly.
			 */
			SnowManager.removeReplaceableUnder(block);
			final byte above = block.getRelative(BlockFace.UP).getData();// get level of snow about to fall
			final byte under = SnowManager.getSnowLevelUnder(block);// get level of snow where it will land
			final byte newUnder = (byte) (above + under + 1 > 7 ? 7 : above + under + 1);
			loc.getWorld().spawnFallingBlock(loc, Material.SNOW, newUnder);
		}
	}

	@EventHandler()
	public void onBlockDamage(BlockDamageEvent event) {
		if (Config.getInstance().debugEnabled()) {
			Block block = event.getBlock();
			if (event.getItemInHand().getType() == Material.STICK) {
				int y = event.getBlock().getWorld().getHighestBlockYAt(block.getX(), block.getZ());
				Block highBlk = SnowManager.getHighestNonAirBlock(block.getWorld().getBlockAt(block.getX(), y,
						block.getZ()));
				event.getPlayer().sendMessage("Highest: " + highBlk.getType().name());
				event.getPlayer().sendMessage(
						event.getBlock().getType().name() + ":" + event.getBlock().getData() + " Light="
								+ event.getBlock().getLightFromSky());

				List<Block> snowBlocks = SnowManager.getSnowBlocksUnder(event.getBlock());
				if ((event.getBlock().getType() == Material.SNOW || event.getBlock().getType() == Material.SNOW_BLOCK)) {
					snowBlocks.add(event.getBlock());
				}
				for (Block blk : snowBlocks) {
					event.getPlayer().sendMessage(
							"under >" + blk.getType().name() + ":" + blk.getData() + " Light="
									+ event.getBlock().getLightFromSky());
				}
				event.getPlayer().sendMessage("**");
				event.setCancelled(true);
			} else if (event.getItemInHand().getType() == Material.SNOW_BALL) {
				event.getPlayer().sendMessage("CurrentDepth=" + SnowManager.getSnowDepth(block));
				event.getPlayer().sendMessage("MinSurrounding=" + SnowManager.getMinSurrounding(block, (byte) -1));
				event.getPlayer().sendMessage("MaxSurrounding=" + SnowManager.getMaxSurrounding(block, (byte) -1));
				event.getPlayer().sendMessage("canSnowBeAdded=" + SnowManager.canSnowBeAdded(block));
				event.getPlayer().sendMessage(
						"canSnowBeAddedAbove=" + SnowManager.canSnowBeAdded(block.getRelative(BlockFace.UP)));

				List<Block> snowBlocks = SnowManager.getBlocksToIncreaseUnder(event.getBlock());
				for (Block blk : snowBlocks) {
					event.getPlayer().sendMessage("under>" + blk.getType().name() + ":" + blk.getData());
				}
				event.getPlayer().sendMessage("**");
				event.setCancelled(true);
			} else if (event.getItemInHand().getType() == Material.SNOW_BLOCK) {
				event.getPlayer().sendMessage("Increasing Snow Level");

				boolean canIncrease = false;
				if (SnowManager.canSnowBeAdded(block)) {
					canIncrease = true;
				} else if (SnowManager.canSnowBeAdded(block.getRelative(BlockFace.UP))) {
					block = block.getRelative(BlockFace.UP);
					canIncrease = true;
				}
				if (canIncrease) {
					SnowManager.increaseSnowLevel(new Location(block.getWorld(), block.getX(), block.getY(), block
							.getZ()));
					for (Block blk : SnowManager.getBlocksToIncreaseUnder(block)) {
						SnowManager
								.increaseSnowLevel(new Location(block.getWorld(), blk.getX(), blk.getY(), blk.getZ()));
					}

				}
				event.getPlayer().sendMessage("**");
				event.setCancelled(true);
			} else if (event.getItemInHand().getType() == Material.BLAZE_ROD) {
				event.getPlayer().sendMessage("Decreasing Snow Level");

				List<Block> snowBlocks = SnowManager.getSnowBlocksUnder(block);
				if ((block.getType() == Material.SNOW || block.getType() == Material.SNOW_BLOCK)) {
					snowBlocks.add(block);
				}
				for (Block blk : snowBlocks) {
					if (blk.getType() == Material.SNOW_BLOCK) {
						blk.setType(Material.SNOW);
						blk.setData((byte) 7);
					}
					if (blk.getLightFromSky() >= 12) {
						// Melt it down
						SnowManager.decreaseSnowLevel(new Location(event.getBlock().getWorld(), blk.getX(), blk.getY(),
								blk.getZ()));
					}
				}
				event.getPlayer().sendMessage("**");
				event.setCancelled(true);
			}
		}
	}
}
