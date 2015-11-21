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
import com.caved_in.commons.chat.Chat;
import com.caved_in.commons.command.Arg;
import com.caved_in.commons.command.Command;
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
    private static HelpScreen helpMenu = Menus.generateHelpScreen("Bounty Help", PageDisplay.DEFAULT, ItemFormat.SINGLE_DASH, ChatColor.YELLOW, ChatColor.YELLOW);

    private static Hunters users = null;

    static {
        helpMenu = helpMenu.addEntry("/bounty help", "Help Screen for the bounty command")
                .addEntry("/bounty list [page#]", "View a list of the active bounties")
                .addEntry("/bounty view", "View a list of your accepted bounties")
                .addEntry("/bounty accept &2<target>", "Accept the task of killing another player for a reward")
                .addEntry("/bounty abandon &2<target>", "Abandon the bounty your pursuing on a player")
                .addEntry("/bounty place &2<target> <value>", "Place a bounty on another player")
                .addEntry("/bounty cancel &2<target>", "Cancel an issued bounty")
                .addEntry("/bounty locate &7[target]", "Locate all (or one) of your active targets");
    }

    public BountyCommand() {
        users = Bounteh.getInstance().getUserManager();
    }

    @Command(identifier = "bounty")
    public void onBountyCommand(Player player) {
        helpMenu.sendTo(player, 1, 7);
    }

    @Command(identifier = "bounty help")
    public void onBountyHelpCommand(Player player, @Arg(name = "page", def = "1") int page) {
        helpMenu.sendTo(player, page, 7);
    }

    @Command(identifier = "bounty list")
    public void onBountyListCommand(Player player, @Arg(name = "page", def = "1") int page) {
        UUID playerId = player.getUniqueId();
        //Get all the bounties and sort them
        List<Bounty> bountiesList = new ArrayList<>();
        bountiesList.addAll(BountyManager.getActiveBounties());
        if (bountiesList.size() == 0) {
            Chat.message(player, "&eThere's currently no active bounties.");
            return;
        }
        Collections.sort(bountiesList);

        HelpScreen bountyList = Menus.generateHelpScreen("Available Bounties", PageDisplay.DEFAULT, ItemFormat.SINGLE_DASH, ChatColor.YELLOW);
        for (Bounty bounty : bountiesList) {
            UUID targetId = bounty.getTargetId();
            if (!Players.isOnline(targetId)) {
                continue;
            }

            Player targetPlayer = Players.getPlayer(targetId);
            //If the player issueing the command is the issuerId of the bounty, show them they're the owner
            String formatString = !playerId.equals(bounty.getIssuerId()) ? "Fee: %s" : "Fee: %s &7(posted by you)";
            String bountyValue = Bounteh.economy.format(bounty.getWorth());
            bountyList.setEntry(targetPlayer.getName(), String.format(formatString, bountyValue));
        }
        bountyList.sendTo(player, page, 6);
    }

    @Command(identifier = "bounty view")
    public void onBountyViewCommand(Player player) {
        Hunter hunter = users.getUser(player);
        UUID playerId = player.getUniqueId();
        Set<UUID> playerAcceptedBounties = hunter.getHuntingBounties();
        if (playerAcceptedBounties.size() == 0) {
            Chat.message(player, BountyMessages.PLAYER_NO_ACTIVE_BOUNTIES);
            return;
        }
        List<Bounty> playerBounties = Lists.newArrayList(BountyManager.getBountiesById(playerAcceptedBounties));
        Collections.sort(playerBounties);
        Chat.message(player, "&cAccepted Bounties");
        for (int i = 0; i < playerBounties.size(); i++) {
            Bounty playerBounty = playerBounties.get(i);
            if (playerBounty.isExpired()) {
                continue;
            }
            Chat.format(player, "&f%s. &e %s - %s - %s", (i + 1), playerBounty.getTargetName(), Bounteh.economy.format(playerBounty.getWorth()), TimeHandler.timeDurationToWords(playerBounty.getDurationLeft()));
        }
    }

    @Command(identifier = "bounty accept")
    public void onBountyAcceptCommand(Player player, @Arg(name = "target") String targetName) {
        Hunter hunter = users.getUser(player);

        String playerName = player.getName();

        if (playerName.equalsIgnoreCase(targetName)) {
            Chat.message(player, "&eYou can't set a bounty on yourself");
            return;
        }

        if (!Players.isOnline(targetName)) {
            //todo allowing accepting bounties of players that are offline?
            Chat.message(player, Messages.playerOffline(targetName));
            return;
        }

        Player targetPlayer = Players.getPlayer(targetName);

        if (!BountyManager.isPlayerTarget(targetPlayer)) {
            Chat.message(player, String.format("&cThere's no bounty on &e%s", targetName));
            return;
        }

        Bounty bounty = BountyManager.getBounty(targetPlayer);
        if (bounty.isHunter(player)) {
            Chat.message(player, "&eYou're already pursuing this bounty.");
            return;
        }

        String bountyOwnerName = bounty.getPlayerName();

        //Check if the player issueing this command is the owner
        if (bountyOwnerName.equalsIgnoreCase(playerName)) {
            Chat.message(player, "&eYou can't pursue a bounty you've issued.");
            return;
        }

        if (!Players.hasPermission(player, BountyPermissions.BOUNTY_ACCEPT_PURSUE)) {
            Chat.message(player, Messages.permissionRequired(BountyPermissions.BOUNTY_ACCEPT_PURSUE));
            return;
        }

        Economy economy = Bounteh.economy;
        double contractFee = bounty.getContractFee();
        if (economy.getBalance(playerName) < contractFee) {
            Chat.message(player, "&eYou don't have enough funds to pursue this bounty.");
            return;
        }

        economy.withdrawPlayer(playerName, contractFee);
        hunter.huntBounty(bounty);
        Chat.message(player,
                String.format("&aBounty Accepted. You've been charged &e%s", contractFee),
                String.format("&aYour target is &e%s&a. This bounty expires in %s", targetPlayer.getName(), TimeHandler.timeDurationToWords(bounty.getExpireTime()))
        );

        if (Players.isOnline(bountyOwnerName)) {
            Chat.message(Players.getPlayer(bountyOwnerName), String.format("&aYour bounty on &e%s&a has been accepted by &e%s&a.", targetName, playerName));
        }
    }

    @Command(identifier = "bounty abandon")
    public void onBountyAbandonCommand(Player player, @Arg(name = "target") String targetName) {
        Hunter hunter = users.getUser(player);

        if (!hunter.isHunting(targetName)) {
            Chat.message(player, "&cBounty not found.");
            return;
        }

        Bounty bounty = BountyManager.getBounty(targetName);
        bounty.removeHunter(player);
        Chat.message(player, "&eBounty abandoned!");
    }

    @Command(identifier = "bounty place")
    public void onBountyPlaceCommand(Player player, @Arg(name = "player") String playerArg, @Arg(name = "value", def = "0") int amount) {

        UUID playerId = player.getUniqueId();
        //Get the worth of the bounty (If the player's using that command
        int bountyAmount = amount;

        //Debug the command info
        Chat.debug("Command Info for /bounty place:",
                "Player argument: " + playerArg,
                "Bounty Amount Arg: " + bountyAmount
        );

        switch (playerArg.toLowerCase()) {
            /* If the player is doing "/bounty place confirm" */
            case "confirm":
            case "accept":
                if (!BountyManager.hasPendingBounty(playerId)) {
                    Chat.message(player, BountyMessages.NO_PENDING_BOUNTY);
                    return;
                }

                Bounty pendingBounty = BountyManager.getPendingBounty(playerId);
                BountyManager.confirmPendingBounty(playerId);
                //Message the player confirming the bounty
                Chat.message(player, BountyMessages.pendingBountyConfirmed(pendingBounty.getTargetName(), pendingBounty.getWorth(), pendingBounty.getPostingFee()));
                //Broadcast that a new bounty has been issued
                Chat.broadcast(BountyMessages.broadcastBountyPlaced(pendingBounty.getWorth()));
                //Create a new thread to insert the bounty to the database
                InsertBountyThread insertBountyThread = new InsertBountyThread(pendingBounty);
                Bounteh.getInstance().getThreadManager().runTaskAsync(insertBountyThread);
                break;
            /* If the player is doing "/bounty place abort" */
            case "abort":
            case "cancel":
                if (!BountyManager.hasPendingBounty(playerId)) {
                    Chat.message(player, BountyMessages.NO_PENDING_BOUNTY);
                    return;
                }

                Bounty abortingBounty = BountyManager.getPendingBounty(playerId);
                BountyManager.abortPendingBounty(playerId);
                Chat.message(player, BountyMessages.pendingBountyAborted(abortingBounty.getPlayerName(), abortingBounty.getWorth()));
                break;
            /* If the player is trying to place a bounty on another place with /bounty place <player> <value> */
            default:
                if (!Players.isOnline(playerArg)) {
                    Chat.message(player, Messages.playerOffline(playerArg));
                    return;
                }
                //If the requested player already has an active bounty
                Player bountyTarget = Players.getPlayer(playerArg);

                if (BountyManager.isPlayerTarget(bountyTarget)) {
                    Chat.message(player, BountyMessages.playerHasBounty(bountyTarget.getName()));
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

                BountyManager.addPendingBounty(playerId, bounty);
                Chat.message(player, "&aPlease do &e/bounty place confirm &ato accept, or &e/bounty place cancel&a to cancel");
                break;
        }
    }

    @Command(identifier = "bounty cancel")
    public void onBountyCancelCommand(Player player, @Arg(name = "action") String cancelAction) {
        UUID playerId = player.getUniqueId();
        if (BountyManager.hasPendingCancelation(player)) {

            cancelAction = cancelAction.toLowerCase();
            switch (cancelAction) {
                case "abort":
                    BountyManager.abortPendingCancellation(playerId);
                    Chat.message(player, "&aYour bounty remains active");
                    break;
                case "confirm":
                    BountyManager.confirmPendingCancellation(player);
                    break;
                default:
                    Chat.message(player, Messages.invalidCommandUsage("abort / confirm"));
                    break;
            }
        } else {

            String targetName = cancelAction;
            if (!BountyManager.isPlayerTarget(targetName)) {
                Chat.message(player, "&eBounty not found.");
                return;
            }

            Bounty bounty = BountyManager.getBounty(targetName);
            //if the player trying to cancel this bounty is not the owner
            if (!bounty.getPlayerName().equalsIgnoreCase(targetName)) {
                Chat.message(player, "&eYou don't own this bounty.");
                return;
            }

            double cancellationFee = bounty.getCancellationFee();
            if (cancellationFee > 0) {
                Chat.message(player, "&eYou'll be charged &l%s&r&e for cancelling this bounty.");
            }

            BountyManager.addPendingCancellation(player, targetName);
        }
    }

    @Command(identifier = "bounty locate", permissions = BountyPermissions.BOUNTY_TARGET_LOCATE)
    public void onBountyLocateCommand(Player player, @Arg(name = "target", def = "all") String name) {
        Hunter hunter = users.getUser(player);

        if (hunter.getHuntingBounties().size() == 0) {
            Chat.message(player, "&eYou currently have no accepted bounties");
            return;
        }

        if (name.equalsIgnoreCase("all")) {
            Chat.message(player, "&cLast Known Target Locations: (x, y, z)");
            int hunterNumber = 0;
            //Loop through all the hunters and give their last known location
            for (UUID id : hunter.getHuntingBounties()) {
                hunterNumber += 1;
                Bounty bounty = BountyManager.getBountyById(id);
                if (bounty == null) {
                    Chat.debug("[ERROR] Apparantly the bounty " + id.toString() + " is null... Please message Brandon ASAP");
                    continue;
                }
                UUID targetId = bounty.getTargetId();
                Chat.message(player, String.format("&f%s. &6%s: &e%s", hunterNumber, bounty.getTargetName(), Players.isOnline(targetId) ? Messages.locationCoords(Players.getPlayer(targetId).getLocation()) : "offline"));
            }
            return;
        }

        String targetName = name;
        if (!Players.isOnline(targetName)) {
            Chat.message(player, Messages.playerOffline(targetName));
            return;
        }

        Player targetPlayer = Players.getPlayer(targetName);
        if (!BountyManager.isPlayerTarget(targetPlayer)) {
            Chat.message(player, String.format("&eUnable to find a bounty for %s", targetName));
            return;
        }

        if (!hunter.isHunterOn(BountyManager.getBounty(targetPlayer))) {
            Chat.message(player, String.format("&cYou're not hunting &e%s", targetName));
            return;
        }

        Location targetLocation = Locations.getRoundedCompassLocation(targetPlayer.getLocation(), Bounteh.getConfiguration().getLocationRounding());
        player.setCompassTarget(targetLocation);
        Chat.message(player, String.format("&aYour compass now points at &e%s", targetName));
    }
}
