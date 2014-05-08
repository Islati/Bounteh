package com.caved_in.bounteh.bounties;

import com.caved_in.bounteh.Bounteh;
import com.caved_in.bounteh.BountyMessages;
import com.caved_in.commons.Commons;
import com.caved_in.commons.chat.Chat;
import com.caved_in.commons.player.Players;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

import java.util.*;

public class BountyManager {
	/* Whether or not the bounties map has changed */
	private static boolean bountiesChanged = false;
	/* Map of targets and their associated bounties */
	private static final Map<UUID, Bounty> playerBounties = new HashMap<>();
	/* Pending Bounties to be either confirmed or denied */
	private static final Map<UUID, Bounty> pendingBounties = new HashMap<>();
	private static List<Bounty> sortedBounties = new ArrayList<>();

	public static boolean completeBounty(UUID targetId, UUID hunterId) {
		Bounty bounty = playerBounties.get(targetId);
		Player targetPlayer = Players.getPlayer(targetId);
		Player huntingPlayer = Players.getPlayer(hunterId);
		if ((targetPlayer == null) || huntingPlayer == null) {
			return false;
		}

		String targetName  = targetPlayer.getName();
		String hunterName = huntingPlayer.getName();

		EconomyResponse withdrawResponse = Bounteh.economy.withdrawPlayer(targetPlayer.getName(),bounty.getDeathPenalty());
		//Debug the withdraw response
		Commons.debug("Withdraw economy response:"," - Amount: " + withdrawResponse.amount, " - Balance: " + withdrawResponse.balance, " - Response Type: " + withdrawResponse.type.name(), " - Error Message: " + withdrawResponse.errorMessage);
		EconomyResponse depositResponse = Bounteh.economy.depositPlayer(huntingPlayer.getName(),bounty.getWorth());
		//Debug the deposit response
		Commons.debug("Deposit economy response:"," - Amount: " + depositResponse.amount, " - Balance: " + depositResponse.balance, " - Response Type: " + depositResponse.type.name(), " - Error Message: " + depositResponse.errorMessage);
		Chat.broadcast(BountyMessages.bountyCompleted(hunterName,targetName,bounty.getWorth()));
		return true;
	}

	public static void expireBounty(Bounty bounty) {

	}

	public static boolean playerHasActiveBounty(Player player) {
		return playerHasActiveBounty(player.getUniqueId());
	}

	public static boolean playerHasActiveBounty(UUID id) {
		return playerBounties.containsKey(id);
	}

	public static Bounty getPlayerBounty(UUID id) {
		return playerBounties.get(id);
	}

	public static Collection<Bounty> getBounties() {
		return playerBounties.values();
	}

	public static Bounty getPlayerBounty(Player player) {
		return getPlayerBounty(player.getUniqueId());
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

	public static boolean removePlayerBounty(UUID id) {
		return playerBounties.remove(id) != null;
	}

	public static void addPlayerBounty(Bounty bounty) {
		playerBounties.put(bounty.getTargetId(), bounty);
		//TODO Load in
	}

	public static boolean hasPendingBounty(Player player) {
		return hasPendingBounty(player.getUniqueId());
	}

	public static boolean hasPendingBounty(UUID id) {
		return pendingBounties.containsKey(id);
	}

	public static void addPendingBounty(UUID id, Bounty bounty) {
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
		addPlayerBounty(playerBounties.remove(id));
		return true;
	}

	public static Bounty getPendingBounty(UUID id) {
		return pendingBounties.get(id);
	}

	public static Collection<Bounty> getPlayerBounties() {
		return playerBounties.values();
	}
}
