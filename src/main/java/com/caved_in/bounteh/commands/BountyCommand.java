package com.caved_in.bounteh.commands;

import com.caved_in.bounteh.Bounteh;
import com.caved_in.bounteh.BountyMessages;
import com.caved_in.bounteh.BountyPermissions;
import com.caved_in.bounteh.bounties.Bounty;
import com.caved_in.bounteh.bounties.BountyBuilder;
import com.caved_in.bounteh.bounties.BountyManager;
import com.caved_in.bounteh.players.Hunter;
import com.caved_in.bounteh.players.Hunters;
import com.caved_in.bounteh.threads.InsertBountyThread;
import com.caved_in.commons.Commons;
import com.caved_in.commons.Messages;
import com.caved_in.commons.commands.CommandController;
import com.caved_in.commons.location.Locations;
import com.caved_in.commons.menu.HelpScreen;
import com.caved_in.commons.menu.ItemFormat;
import com.caved_in.commons.menu.Menus;
import com.caved_in.commons.menu.PageDisplay;
import com.caved_in.commons.player.Players;
import com.caved_in.commons.time.TimeHandler;
import com.caved_in.commons.time.TimeType;
import com.caved_in.commons.utilities.StringUtil;
import com.google.common.collect.Lists;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class BountyCommand {
	private static HelpScreen helpMenu = Menus.generateHelpScreen("Bounty Help",PageDisplay.DEFAULT,ItemFormat.SINGLE_DASH,ChatColor.YELLOW,ChatColor.YELLOW);

	static {
		helpMenu = helpMenu.addEntry("/bounty help","Help Screen for the bounty command")
				.addEntry("/bounty list [page#]", "View a list of the active bounties")
				.addEntry("/bounty view","View a list of your accepted bounties")
				.addEntry("/bounty accept &2<target>","Accept the task of killing another player for a reward")
				.addEntry("/bounty abandon &2<target>","Abandon the bounty your pursuing on a player")
				.addEntry("/bounty place &2<target> <value>","Place a bounty on another player")
				.addEntry("/bounty cancel &2<target>","Cancel an issued bounty")
				.addEntry("/bounty locate &7[target]","Locate all (or one) of your active targets");
	}

	@CommandController.CommandHandler(name = "bounty")
	public void onBountyCommand(Player player, String[] args) {
		if (args.length == 0) {
			helpMenu.sendTo(player,1,7);
		}
	}

	@CommandController.SubCommandHandler(name = "help", parent = "bounty")
	public void onBountyHelpCommand(Player player, String[] args) {
		int page = 1;
		page = StringUtil.getNumberAt(args,1,page);
		helpMenu.sendTo(player,page,7);
	}

	@CommandController.SubCommandHandler(name = "list", parent = "bounty")
	public void onBountyListCommand(Player player, String[] args) {
		UUID playerId = player.getUniqueId();
		int page = 1;
		page = StringUtil.getNumberAt(args,1,page);
		//Get all the bounties and sort them
		List<Bounty> bountiesList = new ArrayList<>();
		bountiesList.addAll(BountyManager.getActiveBounties());
		if (bountiesList.size() == 0) {
			Players.sendMessage(player, "&eThere's currently no active bounties.");
			return;
		}
		Collections.sort(bountiesList);

		HelpScreen bountyList = Menus.generateHelpScreen("Available Bounties",PageDisplay.DEFAULT,ItemFormat.SINGLE_DASH,ChatColor.YELLOW);
		for(Bounty bounty : bountiesList) {
			UUID targetId = bounty.getTargetId();
			if (!Players.isOnline(targetId)) {
				continue;
			}

			Player targetPlayer = Players.getPlayer(targetId);
			//If the player issueing the command is the issuerId of the bounty, show them they're the owner
			String formatString = !playerId.equals(bounty.getIssuerId()) ? "Fee: %s" : "Fee: %s &7(posted by you)";
			String bountyValue = Bounteh.economy.format(bounty.getWorth());
			bountyList.setEntry(targetPlayer.getName(),String.format(formatString, bountyValue));
		}
		bountyList.sendTo(player,page,6);
	}

	@CommandController.SubCommandHandler(name = "view", parent = "bounty")
	public void onBountyViewCommand(Player player, String[] args) {
		Hunter hunter = Hunters.getData(player);
		UUID playerId = player.getUniqueId();
		Set<UUID> playerAcceptedBounties = hunter.getHuntingBounties();
		if (playerAcceptedBounties.size() == 0) {
			Players.sendMessage(player, BountyMessages.PLAYER_NO_ACTIVE_BOUNTIES);
			return;
		}
		List<Bounty> playerBounties = Lists.newArrayList(BountyManager.getBountiesById(playerAcceptedBounties));
		Collections.sort(playerBounties);
		Players.sendMessage(player,"&cAccepted Bounties");
		for(int i = 0; i < playerBounties.size(); i++) {
			Bounty playerBounty = playerBounties.get(i);
			if (playerBounty.isExpired()) {
				continue;
			}
			Players.sendMessage(player,String.format("&f%s. &e %s - %s - %s",(i + 1),playerBounty.getTargetName(),Bounteh.economy.format(playerBounty.getWorth()),TimeHandler.timeDurationToWords(playerBounty.getDurationLeft())));
		}
	}

	@CommandController.SubCommandHandler(name = "accept", parent = "bounty")
	public void onBountyAcceptCommand(Player player, String[] args) {
		Hunter hunter = Hunters.getData(player);
		if (args.length == 1) {
			Players.sendMessage(player,Messages.invalidCommandUsage("&a/bounty accept &e<player>"));
			return;
		}
		String playerName = player.getName();
		String targetName = args[1];

		if (playerName.equalsIgnoreCase(targetName)) {
			Players.sendMessage(player, "&eYou can't set a bounty on yourself");
			return;
		}

		if (!Players.isOnline(targetName)) {
			Players.sendMessage(player, Messages.playerOffline(targetName));
			return;
		}

		Player targetPlayer = Players.getPlayer(targetName);

		if (!BountyManager.isPlayerTarget(targetPlayer)) {
			Players.sendMessage(player, String.format("&cThere's no bounty on &e%s",targetName));
			return;
		}

		Bounty bounty = BountyManager.getBounty(targetPlayer);
		if (bounty.isHunter(player)) {
			Players.sendMessage(player, "&eYou're already pursuing this bounty.");
			return;
		}

		String bountyOwnerName = bounty.getPlayerName();

		//Check if the player issueing this command is the owner
		if (bountyOwnerName.equalsIgnoreCase(playerName)) {
			Players.sendMessage(player, "&eYou can't pursue a bounty you've issued.");
			return;
		}

		if (!Players.hasPermission(player, BountyPermissions.BOUNTY_ACCEPT_PURSUE)) {
			Players.sendMessage(player, Messages.permissionRequired(BountyPermissions.BOUNTY_ACCEPT_PURSUE));
			return;
		}

		Economy economy = Bounteh.economy;
		double contractFee = bounty.getContractFee();
		if (economy.getBalance(playerName) < contractFee) {
			Players.sendMessage(player, "&eYou don't have enough funds to pursue this bounty.");
			return;
		}

		economy.withdrawPlayer(playerName,contractFee);
		hunter.huntBounty(bounty);
		Players.sendMessage(player,
				String.format("&aBounty Accepted. You've been charged &e%s",contractFee),
				String.format("&aYour target is &e%s&a. This bounty expires in %s",targetPlayer.getName(), TimeHandler.timeDurationToWords(bounty.getExpireTime()))
		);

		if (Players.isOnline(bountyOwnerName)) {
			Players.sendMessage(Players.getPlayer(bountyOwnerName),String.format("&aYour bounty on &e%s&a has been accepted by &e%s&a.",targetName,playerName));
		}
	}

	@CommandController.SubCommandHandler(name = "abandon", parent = "bounty")
	public void onBountyAbandonCommand(Player player, String[] args) {
		Hunter hunter = Hunters.getData(player);
		if (args.length < 2) {
			Players.sendMessage(player,Messages.invalidCommandUsage("player"));
			return;
		}
		String targetName = args[1];
		if (!hunter.isHunting(targetName)) {
			Players.sendMessage(player, "&cBounty not found.");
			return;
		}

		Bounty bounty = BountyManager.getBounty(targetName);
		bounty.removeHunter(player);
		Players.sendMessage(player,"&eBounty abandoned!");
	}

	@CommandController.SubCommandHandler(name = "place", parent = "bounty")
	public void onBountyPlaceCommand(Player player, String[] args) {
		if (args.length < 2) {
			Players.sendMessage(player, Messages.invalidCommandUsage("player", "amount"));
			return;
		}

		UUID playerId = player.getUniqueId();
		String playerArg = args[1];
		//Get the worth of the bounty (If the player's using that command
		int bountyAmount = StringUtil.getNumberAt(args,2,-1);
		//Debug the command info
		Commons.debug("Command Info for /bounty place:",
				"Player argument: " + playerArg,
				"Bounty Amount Arg: " + bountyAmount,
				"Argument Length: " + args.length,
				"Arguments: " + StringUtil.joinString(args,", ")
		);

		if (args.length > 2 && bountyAmount == -1) {
			Players.sendMessage(player,Messages.invalidCommandUsage("player","amount (number)"));
			return;
		}

		switch(playerArg.toLowerCase()) {
			/* If the player is doing "/bounty place confirm" */
			case "confirm":
			case "accept":
				if (!BountyManager.hasPendingBounty(playerId)) {
					Players.sendMessage(player, BountyMessages.NO_PENDING_BOUNTY);
					return;
				}

				Bounty pendingBounty = BountyManager.getPendingBounty(playerId);
				BountyManager.confirmPendingBounty(playerId);
				//Message the player confirming the bounty
				Players.sendMessage(player,BountyMessages.pendingBountyConfirmed(pendingBounty.getTargetName(),pendingBounty.getWorth(),pendingBounty.getPostingFee()));
				//Broadcast that a new bounty has been issued
				Players.messageAll(BountyMessages.broadcastBountyPlaced(pendingBounty.getWorth()));
				//Create a new thread to insert the bounty to the database
				InsertBountyThread insertBountyThread = new InsertBountyThread(pendingBounty);
				Commons.threadManager.runTaskAsynch(insertBountyThread);
				break;
			/* If the player is doing "/bounty place abort" */
			case "abort":
			case "cancel":
				if (!BountyManager.hasPendingBounty(playerId)) {
					Players.sendMessage(player, BountyMessages.NO_PENDING_BOUNTY);
					return;
				}

				Bounty abortingBounty = BountyManager.getPendingBounty(playerId);
				BountyManager.abortPendingBounty(playerId);
				Players.sendMessage(player, BountyMessages.pendingBountyAborted(abortingBounty.getPlayerName(), abortingBounty.getWorth()));
				break;
			/* If the player is trying to place a bounty on another place with /bounty place <player> <value> */
			default:
				if (!Players.isOnline(playerArg)) {
					Players.sendMessage(player,Messages.playerOffline(playerArg));
				}
				//If the requested player already has an active bounty
				Player bountyTarget = Players.getPlayer(playerArg);
				if (BountyManager.isPlayerTarget(bountyTarget)) {
					Players.sendMessage(player,BountyMessages.playerHasBounty(bountyTarget.getName()));
					return;
				}

				//Create the bounty based on the players input
				Bounty bounty = new BountyBuilder(UUID.randomUUID())
						.issuer(player)
						.target(bountyTarget)
						.issuedOn(System.currentTimeMillis())
						//Bounties auto-expire in 1 week
						.expiresOn(System.currentTimeMillis() + TimeHandler.getTimeInMilles(1, TimeType.WEEK))
						.worth(bountyAmount)
						.build();
				BountyManager.addPendingBounty(playerId,bounty);
				Players.sendMessage(player,"&aPlease do &e/bounty place confirm &ato accept, or &e/bounty place cancel&a to cancel");
				break;
		}
	}

	@CommandController.SubCommandHandler(name = "cancel", parent = "bounty")
	public void onBountyCancelCommand(Player player, String[] args) {
		UUID playerId = player.getUniqueId();
		if (BountyManager.hasPendingCancelation(player)) {
			if (args.length < 2) {
				Players.sendMessage(player, Messages.invalidCommandUsage("abort/confirm"));
				return;
			}

			String cancelAction = args[1].toLowerCase();
			switch (cancelAction) {
				case "abort":
					BountyManager.abortPendingCancellation(playerId);
					Players.sendMessage(player,"&aYour bounty remains active");
					break;
				case "confirm":
					BountyManager.confirmPendingCancellation(player);
					break;
				default:
					break;
			}
		} else {
			if (args.length < 2) {
				Players.sendMessage(player, Messages.invalidCommandUsage("player"));
				return;
			}

			String targetName = args[1];
			if (!BountyManager.isPlayerTarget(targetName)) {
				Players.sendMessage(player, "&eBounty not found.");
				return;
			}

			Bounty bounty = BountyManager.getBounty(targetName);
			//if the player trying to cancel this bounty is not the owner
			if (!bounty.getPlayerName().equalsIgnoreCase(targetName)) {
				Players.sendMessage(player,"&eYou don't own this bounty.");
				return;
			}

			double cancellationFee = bounty.getCancellationFee();
			if (cancellationFee > 0) {
				Players.sendMessage(player, "&eYou'll be charged &l%s&r&e for cancelling this bounty.");
			}

			BountyManager.addPendingCancellation(player,targetName);
		}
	}

	@CommandController.SubCommandHandler(name = "locate", parent = "bounty")
	public void onBountyLocateCommand(Player player, String[] args) {
		Hunter hunter = Hunters.getData(player);

		if (!Players.hasPermission(player,BountyPermissions.BOUNTY_TARGET_LOCATE)) {
			Players.sendMessage(player, "You don't have permission to locate targets");
			return;
		}

		if (hunter.getHuntingBounties().size() == 0) {
			Players.sendMessage(player, "&eYou currently have no accepted bounties");
			return;
		}

		if (args.length <= 1) {
			Players.sendMessage(player,"&cLast Known Target Locations: (x, y, z)");
			int hunterNumber = 0;
			//Loop through all the hunters and give their last known location
			for(UUID id : hunter.getHuntingBounties()) {
				hunterNumber += 1;
				Bounty bounty = BountyManager.getBountyById(id);
				if (bounty == null) {
					Commons.debug("[ERROR] Apparantly the bounty " + id.toString() + " is null... Please message Brandon ASAP");
					continue;
				}
				UUID targetId = bounty.getTargetId();
				Players.sendMessage(player, String.format("&f%s. &6%s: &e%s",hunterNumber,bounty.getTargetName(),Players.isOnline(targetId) ? Messages.locationCoords(Players.getPlayer(targetId).getLocation()) : "offline"));
			}
			return;
		}

		String targetName = args[1];
		if (!Players.isOnline(targetName)) {
			Players.sendMessage(player, Messages.playerOffline(targetName));
			return;
		}

		Player targetPlayer = Players.getPlayer(targetName);
		if (!BountyManager.isPlayerTarget(targetPlayer)) {
			Players.sendMessage(player, String.format("&eUnable to find a bounty for %s",targetName));
			return;
		}

		if (!hunter.isHunterOn(BountyManager.getBounty(targetPlayer))) {
			Players.sendMessage(player, String.format("&cYou're not hunting &e%s",targetName));
			return;
		}

		Location targetLocation = Locations.getRoundedCompassLocation(targetPlayer.getLocation(),Bounteh.getConfiguration().getLocationRounding());
		player.setCompassTarget(targetLocation);
		Players.sendMessage(player,String.format("&aYour compass now points at &e%s",targetName));
	}
}
