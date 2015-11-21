package com.caved_in.bounteh.threads;

import com.caved_in.bounteh.Bounteh;
import com.caved_in.bounteh.bounties.Bounty;
import com.caved_in.commons.Commons;
import com.caved_in.commons.chat.Chat;

public class InsertBountyThread implements Runnable {

	private Bounty bounty;

	public InsertBountyThread(Bounty bounty) {
		this.bounty = bounty;
	}

	@Override
	public void run() {
		Chat.debug("Pushing " + bounty + " to the database");
		Bounteh.database.insertBounty(bounty);
		Chat.debug("Bounty has been pushed to the database");
	}
}
