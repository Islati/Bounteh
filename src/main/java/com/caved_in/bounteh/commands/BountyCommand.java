package com.caved_in.bounteh.commands;

import com.caved_in.bounteh.Bounteh;
import com.caved_in.bounteh.BountyMessages;
import com.caved_in.bounteh.bounties.Bounty;
import com.caved_in.bounteh.bounties.BountyBuilder;
import com.caved_in.bounteh.bounties.BountyManager;
import com.caved_in.bounteh.players.Hunter;
import com.caved_in.bounteh.players.Hunters;
import com.caved_in.bounteh.threads.InsertBountyThread;
import com.caved_in.commons.Commons;
import com.caved_in.commons.Messages;
import com.caved_in.commons.commands.CommandController;
import com.caved_in.commons.menu.HelpScreen;
import com.caved_in.commons.menu.ItemFormat;
import com.caved_in.commons.menu.Menus;
import com.caved_in.commons.menu.PageDisplay;
import com.caved_in.commons.player.Players;
import com.caved_in.commons.time.TimeHandler;
import com.caved_in.commons.time.TimeType;
import com.caved_in.commons.utilities.StringUtil;
import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class BountyCommand {
	private static HelpScreen helpMenu = Menus.generateHelpScreen("Bounty Help",PageDisplay.DEFAULT,ItemFormat.SINGLE_DASH,ChatColor.YELLOW,ChatColor.YELLOW);

	static {
		helpMenu = helpMenu.addEntry("/bounty help","")
				.addEntry("/bounty list &7[page#]", "")
				.addEntry("/bounty view","")
				.addEntry("/bounty accept &2<target>","")
				.addEntry("/bounty abandon &2<target>","")
				.addEntry("/bounty place &2<target> <value>","")
				.addEntry("/bounty cancel &2<target>","")
				.addEntry("/bounty locate &7[target]","");
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
		bountiesList.addAll(BountyManager.getPlayerBounties());
		Collections.sort(bountiesList);

		HelpScreen bountyList = Menus.generateHelpScreen("Available Bounties",PageDisplay.DEFAULT,ItemFormat.SINGLE_DASH,ChatColor.YELLOW);
		for(Bounty bounty : bountiesList) {
			UUID targetId = bounty.getTargetId();
			if (!Players.isOnline(targetId)) {
				continue;
			}

			Player targetPlayer = Players.getPlayer(targetId);
			//If the player issueing the command is the issuer of the bounty, show them they're the owner
			String formatString = playerId == bounty.getIssuerId() ? "Fee: %s" : "Fee: %s &7(posted by you)";
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

	}

	@CommandController.SubCommandHandler(name = "abandon", parent = "bounty")
	public void onBountyAbandonCommand(Player player, String[] args) {

	}

	@CommandController.SubCommandHandler(name = "place", parent = "bounty")
	public void onBountyPlaceCommand(Player player, String[] args) {
		if (args.length < 1) {
			Players.sendMessage(player, Messages.invalidCommandUsage("player", "amount"));
			return;
		}

		UUID playerId = player.getUniqueId();
		String playerArg = args[1];
		//Get the worth of the bounty (If the player's using that command
		int bountyAmount = StringUtil.getNumberAt(args,2,-1);
		if (args.length > 2 && bountyAmount == -1) {
			Players.sendMessage(player,Messages.invalidCommandUsage("player","amount (number)"));
			return;
		}

		switch(playerArg.toLowerCase()) {
			/* If the player is doing "/bounty place confirm" */
			case "confirm":
				if (!BountyManager.hasPendingBounty(playerId)) {
					Players.sendMessage(player, BountyMessages.NO_PENDING_BOUNTY);
					return;
				}

				Bounty pendingBounty = BountyManager.getPendingBounty(playerId);
				BountyManager.confirmPendingBounty(playerId);
				//Message the player confirming the bounty
				Players.sendMessage(player,BountyMessages.pendingBountyConfirmed(pendingBounty.getPlayerName(),pendingBounty.getWorth(),pendingBounty.getPostingFee()));
				//Broadcast that a new bounty has been issued
				Players.messageAll(BountyMessages.broadcastBountyPlaced(pendingBounty.getWorth()));
				//Create a new thread to insert the bounty to the database
				InsertBountyThread insertBountyThread = new InsertBountyThread(pendingBounty);
				Commons.threadManager.runTaskAsynch(insertBountyThread);
				break;
			/* If the player is doing "/bounty place abort" */
			case "abort":
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
				if (BountyManager.playerHasActiveBounty(bountyTarget)) {
					Players.sendMessage(player,BountyMessages.playerHasBounty(bountyTarget.getName()));
					return;
				}

				//Create the bounty based on the players input
				Bounty bounty = new BountyBuilder(UUID.randomUUID())
						.withIssuer(playerId)
						.withTarget(bountyTarget.getUniqueId())
						.issuedOn(System.currentTimeMillis())
						//Bounties auto-expire in 1 week
						.expiresOn(System.currentTimeMillis() + TimeHandler.getTimeInMilles(1, TimeType.WEEK))
						.withAmount(bountyAmount)
						.build();
				BountyManager.addPendingBounty(playerId,bounty);
				break;
		}
	}

	@CommandController.SubCommandHandler(name = "cancel", parent = "bounty")
	public void onBountyCancelCommand(Player player, String[] args) {

	}

	@CommandController.SubCommandHandler(name = "locate", parent = "bounty")
	public void onBountyLocateCommand(Player player, String[] args) {

	}
}
