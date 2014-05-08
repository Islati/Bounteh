package com.caved_in.bounteh.listeners;

import com.caved_in.bounteh.bounties.BountyManager;
import com.caved_in.bounteh.players.Hunter;
import com.caved_in.bounteh.players.Hunters;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerQuitListener implements Listener {

	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		UUID id = player.getUniqueId();
		BountyManager.abortPendingBounty(id);
		Hunters.removeData(id);
	}
}
