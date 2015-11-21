package com.caved_in.bounteh.players;

import com.caved_in.commons.game.players.UserManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Hunters extends UserManager<Hunter> {
	public Hunters() {
		super(Hunter.class);
	}
}
