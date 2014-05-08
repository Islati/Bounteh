package com.caved_in.bounteh.players;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Hunters {
	private static final Map<UUID, Hunter> players = new HashMap<>();

	public static void addData(Player player) {
		players.put(player.getUniqueId(), new Hunter(player));
	}

	public static void removeData(UUID id) {
		players.remove(id);
	}

	public static void removeData(Player player) {
		removeData(player.getUniqueId());
	}

	public static boolean hasData(Player player) {
		return hasData(player.getUniqueId());
	}

	public static boolean hasData(UUID id) {
		return players.containsKey(id);
	}

	public static Hunter getData(Player player) {
		return getData(player.getUniqueId());
	}

	public static Hunter getData(UUID id) {
		return players.get(id);
	}
}
