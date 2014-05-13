package com.caved_in.bounteh.threads;

import com.caved_in.bounteh.Bounteh;
import com.caved_in.bounteh.BountyMessages;
import com.caved_in.bounteh.bounties.Bounty;
import com.caved_in.commons.Commons;
import com.caved_in.commons.player.Players;
import com.caved_in.commons.threading.executors.BukkitFutures;
import com.caved_in.commons.threading.tasks.CallableGetPlayerName;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Set;
import java.util.UUID;

public class InitBountyThread implements Runnable {
	private final Bounty bounty;

	public InitBountyThread(final Bounty bounty) {
		this.bounty = bounty;
	}

	@Override
	public void run(){
		String targetName = Players.getNameFromUUID(bounty.getTargetId());
		String issuerName = Players.getNameFromUUID(bounty.getIssuerId());
		Set<UUID> hunterIds = Bounteh.database.getAllHuntersForBounty(bounty.getBountyId());
		Commons.debug("Loaded " + hunterIds.size() + " hunters to the bounty on " + targetName);
		bounty.initHunters(hunterIds);
		bounty.setPlayerName(issuerName);
		bounty.setTargetName(targetName);
		bounty.setInitialized(true);
		Commons.debug("Just initialized bounty: ");
		Commons.debug(BountyMessages.bountyInfo(bounty));
	}
}
