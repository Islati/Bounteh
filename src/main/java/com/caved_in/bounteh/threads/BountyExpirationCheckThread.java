package com.caved_in.bounteh.threads;

import com.caved_in.bounteh.bounties.Bounty;
import com.caved_in.bounteh.bounties.BountyManager;

public class BountyExpirationCheckThread implements Runnable {

	@Override
	public void run() {
		for(Bounty bounty : BountyManager.getBounties()) {
			if (!bounty.isExpired()) {
				continue;
			}

			BountyManager.expireBounty(bounty);
		}
	}
}
