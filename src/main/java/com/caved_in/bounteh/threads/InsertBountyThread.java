package com.caved_in.bounteh.threads;

import com.caved_in.bounteh.Bounteh;
import com.caved_in.bounteh.bounties.Bounty;
import com.caved_in.commons.Commons;

public class InsertBountyThread implements Runnable {

	private Bounty bounty;

	public InsertBountyThread(Bounty bounty) {
		this.bounty = bounty;
	}

	@Override
	public void run() {
		Commons.debug("Pushing " + bounty + " to the database");
		Bounteh.database.insertBounty(bounty);
		Commons.debug("Bounty has been pushed to the database");
	}
}
