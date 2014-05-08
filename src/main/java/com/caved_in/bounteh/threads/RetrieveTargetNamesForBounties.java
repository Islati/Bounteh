package com.caved_in.bounteh.threads;

import com.caved_in.bounteh.bounties.Bounty;
import com.caved_in.bounteh.bounties.BountyManager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RetrieveTargetNamesForBounties implements Runnable {
	@Override
	public void run() {
		Set<UUID> targetIds = new HashSet<>();
		for(Bounty bounty : BountyManager.getPlayerBounties()) {
			if (bounty.getTargetName() != null) {
				continue;
			}
			
		}
	}
}
