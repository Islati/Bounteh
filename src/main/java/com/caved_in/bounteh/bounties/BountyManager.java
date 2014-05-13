package com.caved_in.bounteh.bounties;

import com.caved_in.bounteh.Bounteh;
import com.caved_in.bounteh.BountyMessages;
import com.caved_in.bounteh.players.Hunter;
import com.caved_in.bounteh.players.Hunters;
import com.caved_in.bounteh.threads.InitBountyThread;
import com.caved_in.bounteh.threads.UpdateBountyStatusThread;
import com.caved_in.commons.Commons;
import com.caved_in.commons.chat.Chat;
import com.caved_in.commons.player.Players;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BountyManager {
	/* Whether or not the bounties map has changed */
	private static boolean bountiesChanged = false;
	/* Map of targets and their associated bounties */
	private static final Map<UUID, Bounty> playerBounties = new HashMap<>();
	/* Pending Bounties to be either confirmed or denied */
	private static final Map<UUID, Bounty> pendingBounties = new HashMap<>();

	private static final Map<UUID, Bounty> pendingCancellations = new HashMap<>();

	private static List<Bounty> sortedBounties = new ArrayList<>();

	public static void initBounty(final Bounty bounty) {
		Commons.threadManager.runTaskAsynch(new InitBountyThread(bounty));
	}

	public static boolean completeBounty(UUID targetId, UUID hunterId) {
		Bounty bounty = playerBounties.remove(targetId);
		Player target = Players.getPlayer(targetId);
		Player hunting = Players.getPlayer(hunterId);
		if ((target == null) || hunting == null) {
			return false;
		}
		Hunter huntingPlayer = Hunters.getData(targetId);
		Hunter targetPlayer = Hunters.getData(target);
		huntingPlayer.removeBounty(bounty);
		String targetName = target.getName();
		String hunterName = hunting.getName();

		EconomyResponse withdrawResponse = Bounteh.economy.withdrawPlayer(target.getName(), bounty.getDeathPenalty());
		//Debug the withdraw response
		Commons.debug("Withdraw economy response (for death penalty) on bounty for " + targetName, " - Amount: " + withdrawResponse.amount, " - Balance: " + withdrawResponse.balance, " - Response Type: " + withdrawResponse.type.name(), " - Error Message: " + withdrawResponse.errorMessage);
		EconomyResponse depositResponse = Bounteh.economy.depositPlayer(hunting.getName(), bounty.getWorth());
		//Debug the deposit response
		Commons.debug("Deposit economy response (for killing " + targetName + ") given to " + hunterName + ": ", " - Amount: " + depositResponse.amount, " - Balance: " + depositResponse.balance, " - Response Type: " + depositResponse.type.name(), " - Error Message: " + depositResponse.errorMessage);
		Chat.broadcast(BountyMessages.bountyCompleted(hunterName, targetName, bounty.getWorth()));
		//Update the status of the bounty in the database
		Commons.threadManager.runTaskAsynch(new UpdateBountyStatusThread(true, bounty.getBountyId()));
		return true;
	}

	public static void expireBounty(Bounty bounty) {
		if (!bounty.isExpired()) {
			return;
		}

		Stream<UUID> bountyHunterIdStream = bounty.getHunters().stream();
		//Get all the online players and send them a message saying their bounty expired
		Set<Player> onlineHunters = bountyHunterIdStream.filter(Players::isOnline).map(Players::getPlayer).collect(Collectors.toSet());
		Players.messageAll(onlineHunters,String.format("&7[&eBounty&7] &eYour bounty on %s has expired",bounty.getTargetName()));
		//Remove the hunters from the bounty
		bountyHunterIdStream.forEach(id -> bounty.removeHunter(Players.getPlayer(id)));
	}

	public static boolean isPlayerTarget(String name) {
		for (Bounty bounty : getActiveBounties()) {
			if (bounty.getPlayerName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isPlayerTarget(Player player) {
		return isPlayerTarget(player.getUniqueId());
	}

	public static boolean isPlayerTarget(UUID id) {
		return playerBounties.containsKey(id);
	}

	public static Bounty getBounty(UUID playerId) {
		return playerBounties.get(playerId);
	}

	public static Bounty getBountyById(UUID id) {
		for(Bounty b : playerBounties.values()) {
			if (b.getBountyId().equals(id)) {
				return b;
			}
		}
		return null;
	}

	public static Collection<Bounty> getBounties() {
		return playerBounties.values();
	}

	public static Bounty getBounty(Player player) {
		return getBounty(player.getUniqueId());
	}

	public static Bounty getBounty(String playerName) {
		Optional<Bounty> bountyOptional = getBounties().stream().filter(b -> b.getTargetName().equalsIgnoreCase(playerName)).findFirst();
		return bountyOptional.isPresent() ? bountyOptional.get() : null;
	}

	public static Set<Bounty> getBountiesById(Collection<UUID> ids) {
		Set<Bounty> bountySet = new HashSet<>();
		for (UUID id : ids) {
			for (Bounty bounty : playerBounties.values()) {
				if (bounty.getBountyId() == id) {
					bountySet.add(bounty);
					break;
				}
			}
		}
		return bountySet;
	}

	public static boolean removeBounty(UUID id) {
		return playerBounties.remove(id) != null;
	}

	public static synchronized void addBounty(Bounty bounty) {
		playerBounties.put(bounty.getTargetId(), bounty);
		initBounty(bounty);
	}

	/**
	 * Whether or not a player has a pending bounty
	 *
	 * @param player
	 * @return
	 */
	public static boolean hasPendingBounty(Player player) {
		return hasPendingBounty(player.getUniqueId());
	}

	/**
	 * Whether or not a player has a pending bounty
	 *
	 * @param playerId
	 * @return
	 */
	public static boolean hasPendingBounty(UUID playerId) {
		return pendingBounties.containsKey(playerId);
	}

	/**
	 * Add a pending bounty to later either be confirmed or denied
	 *
	 * @param id
	 * @param bounty
	 */
	public static synchronized void addPendingBounty(UUID id, Bounty bounty) {
		pendingBounties.put(id, bounty);
	}

	public static boolean abortPendingBounty(UUID id) {
		return pendingBounties.remove(id) != null;
	}

	public static boolean confirmPendingBounty(UUID id) {
		//If there's no pending bounty with this ID then return false;
		if (!hasPendingBounty(id)) {
			return false;
		}
		//Add the pending bounty to the player bounties list
		addBounty(pendingBounties.remove(id));
		return true;
	}

	public static Bounty getPendingBounty(UUID id) {
		return pendingBounties.get(id);
	}

	public static Collection<Bounty> getActiveBounties() {
		return playerBounties.values();
	}

	public static boolean addPendingCancellation(Player bountyOwner, String targetName) {
		if (!isPlayerTarget(targetName)) {
			return false;
		}

		Bounty bounty = getBounty(targetName);
		if (bounty == null) {
			return false;
		}

		pendingCancellations.put(bountyOwner.getUniqueId(), playerBounties.remove(bounty.getTargetId()));
		return true;
	}

	public static void confirmPendingCancellation(Player player) {
		UUID playerId = player.getUniqueId();
		Bounty bounty = pendingCancellations.remove(playerId);
		String bountyTarget = bounty.getTargetName();
		Set<UUID> bountyHunters = bounty.getHunters();
		double inconvienance = bountyHunters.size() > 0 ? bounty.getInconvienance(bountyHunters.size()) : 0;

		if (Bounteh.getConfiguration().payInconvienane() && inconvienance > 0) {
			bountyHunters.forEach(id -> Bounteh.economy.depositPlayer(Players.getOfflinePlayer(id), inconvienance));
		}

		for (UUID id : bountyHunters) {
			if (!Players.isOnline(id)) {
				continue;
			}

			Player hunter = Players.getPlayer(id);
			Players.sendMessage(hunter, String.format("&aYour bounty on &e%s&a has been cancelled and the contract fee was refunded", bountyTarget));
			if (Bounteh.getConfiguration().payInconvienane() && inconvienance > 0) {
				Players.sendMessage(hunter,"&7You've received &l%s&r&7 for the inconvenience");
			}
		}
	}

	public static void abortPendingCancellation(UUID bountyOwner) {
		Bounty bounty = pendingCancellations.remove(bountyOwner);
		playerBounties.put(bounty.getTargetId(), bounty);
	}

	public static boolean hasPendingCancelation(UUID playerId) {
		return pendingCancellations.containsKey(playerId);
	}

	public static boolean hasPendingCancelation(Player player) {
		return hasPendingBounty(player.getUniqueId());
	}
}
