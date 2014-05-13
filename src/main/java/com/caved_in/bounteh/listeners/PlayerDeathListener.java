package com.caved_in.bounteh.listeners;

import com.caved_in.bounteh.bounties.Bounty;
import com.caved_in.bounteh.bounties.BountyManager;
import com.caved_in.commons.item.Items;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerDeathListener implements Listener {
	@EventHandler
	public void onEvent(PlayerDeathEvent event) {
		Player player = event.getEntity();
		UUID targetId = player.getUniqueId();
		//If the player doesn't have a bounty, then cancel execution
		if (!BountyManager.isPlayerTarget(targetId)) {
			return;
		}

		Player killer = player.getKiller();
		if (killer == null) {
			return;
		}

		Bounty bounty = BountyManager.getBounty(targetId);
		if (!bounty.isHunter(killer)) {
			return;
		}

//		String playerName = player.getName();
//		//Make the players head to be added to the drops
//		ItemStack playerHead = Items.getSkull(playerName);
//		event.getDrops().add(playerHead);
		//Complete the bounty, assign rewards, etc
		BountyManager.completeBounty(targetId,player.getKiller().getUniqueId());
	}
}
