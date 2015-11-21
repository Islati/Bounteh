package com.caved_in.bounteh.threads;

import com.caved_in.bounteh.Bounteh;
import com.caved_in.commons.Commons;
import com.caved_in.commons.chat.Chat;

import java.util.UUID;

public class RemoveHunterFromBountyThread implements Runnable {
	private UUID bountyId;
	private UUID playerId;

	public RemoveHunterFromBountyThread(UUID bountyId, UUID playerId) {
		this.bountyId = bountyId;
		this.playerId = playerId;
	}

	@Override
	public void run() {
		Chat.debug("Began removing " + playerId.toString() + " from " + bountyId.toString());
		Bounteh.database.removeHunterFromBounty(playerId,bountyId);
		Chat.debug("Removed! Yays");
	}
}
