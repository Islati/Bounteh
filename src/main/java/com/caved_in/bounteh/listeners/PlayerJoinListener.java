package com.caved_in.bounteh.listeners;

import com.caved_in.bounteh.players.Hunters;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Hunters.addData(event.getPlayer());
	}
}
