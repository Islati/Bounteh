package com.caved_in.bounteh.threads;

import com.caved_in.bounteh.Bounteh;
import com.caved_in.bounteh.BountyMessages;
import com.caved_in.bounteh.bounties.Bounty;
import com.caved_in.commons.chat.Chat;
import com.caved_in.commons.player.Players;

import java.util.Set;
import java.util.UUID;

public class InitBountyThread implements Runnable {
	private final Bounty bounty;

	public InitBountyThread(final Bounty bounty) {
		this.bounty = bounty;
	}

	@Override
	public void run(){
		String targetName = null;
		String issuerName = null;
		try {
			targetName = Players.getNameFromUUID(bounty.getTargetId());
			issuerName = Players.getNameFromUUID(bounty.getIssuerId());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		Set<UUID> hunterIds = Bounteh.database.getAllHuntersForBounty(bounty.getBountyId());
		Chat.debug("Loaded " + hunterIds.size() + " hunters to the bounty on " + targetName);
		bounty.initHunters(hunterIds);
		bounty.setPlayerName(issuerName);
		bounty.setTargetName(targetName);
		bounty.setInitialized(true);
		Chat.debug("Just initialized bounty: ");
		BountyMessages.bountyInfo(bounty).forEach(Chat::debug);
	}
}
