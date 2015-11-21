package com.caved_in.bounteh.threads;

import com.caved_in.bounteh.Bounteh;
import com.caved_in.commons.Commons;
import com.caved_in.commons.chat.Chat;

import java.util.UUID;

public class UpdateBountyStatusThread implements Runnable {
	private boolean bountyFilled = false;
	private UUID bountyId;

	public UpdateBountyStatusThread(boolean bountyFilled, UUID bountyId) {
		this.bountyFilled = bountyFilled;
		this.bountyId = bountyId;
	}

	@Override
	public void run() {
		boolean updatedStatus = Bounteh.database.setBountyCompleted(bountyId,bountyFilled);
		Chat.debug("Updated bounty status for " + bountyId.toString() + " is " + String.valueOf(updatedStatus));
	}
}
