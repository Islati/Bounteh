package com.caved_in.bounteh;

import com.caved_in.bounteh.bounties.Bounty;
import com.caved_in.commons.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static java.util.Arrays.*;

public class BountyMessages {
	private static final String MESSAGE_PREFIX = "&8[&6Bounty&8] &r";
	public static String NO_PENDING_BOUNTY = "&eYou don't have any bounties requiring confirmation, sorry.";

	public static String PLAYER_NO_ACTIVE_BOUNTIES = "&eYou currently don't have any active bounties";

	public static String playerHasBounty(String name) {
		return String.format("%s&7There's already a bounty on %s",MESSAGE_PREFIX,name);
	}

	public static String[] pendingBountyConfirmed(String name, double amount, double postingFee) {
		return new String[] {
				String.format("&7Placed a bounty on %s's head for %s.",name,Bounteh.economy.format(amount)),
				String.format("&7You've been charged %s for posting this bounty.", Bounteh.economy.format(postingFee))
		};
	}

	public static String broadcastBountyPlaced(double amount) {
		return String.format("%s&eA bounty worth &6%s&e been issued!",MESSAGE_PREFIX, Bounteh.economy.format(amount));
	}

	public static String pendingBountyAborted(String name, double amount) {
		return String.format("%s&7You're no longer hunting %s for %s",MESSAGE_PREFIX,name, Bounteh.economy.format(amount));
	}

	public static String bountyCompleted(String hunterName, String targetName, double worth) {
		return String.format("%s&e%s has collected a bounty on %s for %s",MESSAGE_PREFIX, hunterName,targetName,Bounteh.economy.format(worth));
	}

	public static List<String> bountyInfo(Bounty bounty) {
		List<String> bountyInfo = new ArrayList<>();
		bountyInfo.addAll(asList(
				String.format("%sTarget Name: %s",Messages.YELLOW_INDENT_ARROW,bounty.getTargetName()),
				String.format("%sIssuer Name: %s",Messages.YELLOW_INDENT_ARROW,bounty.getPlayerName()),
				String.format("%sBounty Worth: %s",Messages.YELLOW_INDENT_ARROW,Bounteh.economy.format(bounty.getWorth()))
		));
		return bountyInfo;
	}
}