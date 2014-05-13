package com.caved_in.bounteh.threads;

import com.caved_in.bounteh.Bounteh;
import com.caved_in.bounteh.bounties.Bounty;

import java.util.Set;
import java.util.concurrent.Callable;

public class GetAllBountiesCallable implements Callable<Set<Bounty>> {
	@Override
	public Set<Bounty> call() throws Exception {
		return Bounteh.database.getAllActiveBounties();
	}
}
