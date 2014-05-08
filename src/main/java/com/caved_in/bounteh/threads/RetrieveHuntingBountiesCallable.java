package com.caved_in.bounteh.threads;

import com.caved_in.bounteh.Bounteh;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

public class RetrieveHuntingBountiesCallable implements Callable<Set<UUID>> {
	private UUID playerId;

	public RetrieveHuntingBountiesCallable(UUID playerId) {
		this.playerId = playerId;
	}

	@Override
	public Set<UUID> call() throws Exception {
		return Bounteh.database.getBountiesPlayerHasActive(playerId);
	}
}
