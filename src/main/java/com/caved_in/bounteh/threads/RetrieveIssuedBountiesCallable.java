package com.caved_in.bounteh.threads;

import com.caved_in.bounteh.Bounteh;
import com.caved_in.bounteh.bounties.Bounty;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

public class RetrieveIssuedBountiesCallable implements Callable<Set<Bounty>> {
	private UUID playerId;

	public RetrieveIssuedBountiesCallable(UUID playerId) {
		this.playerId = playerId;
	}

	@Override
	public Set<Bounty> call() throws Exception {
		return Bounteh.database.getPlayerIssuedBounties(playerId);
	}
}
