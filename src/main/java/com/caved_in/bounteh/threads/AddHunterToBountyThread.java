package com.caved_in.bounteh.threads;

import com.caved_in.bounteh.Bounteh;
import com.caved_in.commons.Commons;

import java.util.UUID;

public class AddHunterToBountyThread implements Runnable {
	private UUID playerId;
	private UUID bountyId;

	public AddHunterToBountyThread(UUID playerId, UUID bountyId) {
		this.playerId = playerId;
		this.bountyId = bountyId;
	}

	@Override
	public void run() {
		Bounteh.database.addHunterToBounty(playerId, bountyId);
		Commons.debug("Added Player {%s} to the bounty %s",playerId.toString(),bountyId.toString());
	}
}
