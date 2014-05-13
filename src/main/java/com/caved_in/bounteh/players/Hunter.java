package com.caved_in.bounteh.players;

import com.caved_in.bounteh.bounties.Bounty;
import com.caved_in.bounteh.bounties.BountyManager;
import com.caved_in.bounteh.threads.RetrieveHuntingBountiesCallable;
import com.caved_in.bounteh.threads.RetrieveIssuedBountiesCallable;
import com.caved_in.commons.Commons;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Hunter {
	private UUID id;
	private String name;
	private Set<Bounty> issuedBounties = new HashSet<>();
	private Set<UUID> huntingBounties = new HashSet<>();

	public Hunter(Player player) {
		this.id = player.getUniqueId();
		this.name = player.getName();
		initPlayer();
	}

	private void initPlayer() {
		/* Retrieve a set of all the bounties the player has issued */
		ListenableFuture<Set<Bounty>> futureIssuedBounties = Commons.asyncExecutor.submit(new RetrieveIssuedBountiesCallable(id));
		ListenableFuture<Set<UUID>> futureHuntingBounties = Commons.asyncExecutor.submit(new RetrieveHuntingBountiesCallable(id));
		Futures.addCallback(futureIssuedBounties, new FutureCallback<Set<Bounty>>() {
			@Override
			public void onSuccess(Set<Bounty> bounties) {
				if (bounties.size() > 0) {
					issuedBounties = bounties;
				} else {
					Commons.debug("No issued bounties found for " + name);
				}
			}

			@Override
			public void onFailure(Throwable throwable) {
				Commons.debug("Unable to initialize data for " + name);
			}
		});

		Futures.addCallback(futureHuntingBounties, new FutureCallback<Set<UUID>>() {
			@Override
			public void onSuccess(Set<UUID> uuids) {
				if (uuids.size() > 0) {
					huntingBounties = uuids;
				} else {
					Commons.debug(name + " is not hunting any bounties");
				}
			}

			@Override
			public void onFailure(Throwable throwable) {
				Commons.debug("Unable to initialize data for " + name);
			}
		});
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Hunter) && !(o instanceof Player)) {
			return false;
		}

		if (o instanceof Hunter) {
			Hunter gp = (Hunter)o;
			return (gp.getId() == id  && gp.getName().equals(name));
		}

		Player p = (Player)o;
		return p.getUniqueId() == id && p.getName().equals(name);
	}

	public boolean hasActiveBounty() {
		return BountyManager.isPlayerTarget(id);
	}

	public boolean hasIssuedBounty() {
		return issuedBounties.size() > 0;
	}

	public Bounty getActiveBounty() {
		return BountyManager.getBounty(id);
	}

	public void addIssuedBounty(Bounty bounty) {
		issuedBounties.add(bounty);
	}

	public boolean isHunterOn(Bounty bounty) {
		return huntingBounties.contains(bounty.getBountyId());
	}

	public boolean isHunting(String playerName) {
		for(Bounty bounty : BountyManager.getBountiesById(huntingBounties)) {
			if (bounty.getTargetName().equalsIgnoreCase(playerName)) {
				return true;
			}
		}
		return false;
	}

	public void huntBounty(Bounty bounty) {
		huntingBounties.add(bounty.getBountyId());
	}

	public void abandonBounty(Bounty bounty) {
		abandonBounty(bounty.getBountyId());
	}

	public Set<UUID> getHuntingBounties() {
		return huntingBounties;
	}

	public void abandonBounty(UUID id) {
		huntingBounties.remove(id);
	}


}
