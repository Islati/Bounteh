package com.caved_in.bounteh.players;

import com.caved_in.bounteh.Bounteh;
import com.caved_in.bounteh.bounties.Bounty;
import com.caved_in.bounteh.bounties.BountyManager;
import com.caved_in.commons.Commons;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Hunter {
	private UUID id;
	private String name;
	private final Set<Bounty> issuedBounties = new HashSet<>();
	private final Set<UUID> huntingBounties = new HashSet<>();

	public Hunter(Player player) {
		this.id = player.getUniqueId();
		this.name = player.getName();
		initPlayer();
	}

	private void initPlayer() {
		/* Retrieve a set of all the bounties the player has issued */
		Commons.threadManager.runTaskAsync(() -> {
			issuedBounties.addAll(Bounteh.database.getPlayerIssuedBounties(id));
			huntingBounties.addAll(Bounteh.database.getBountiesPlayerHasActive(id));
			Commons.debug(
				String.format("%s has %s issued bounties", name, issuedBounties.size()),
				String.format("%s is hunting %s bounties", name, huntingBounties.size())
			);
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
		bounty.addHunter(getId());
	}

	public void removeBounty(Bounty bounty) {
		removeBounty(bounty.getBountyId());
	}

	public Set<UUID> getHuntingBounties() {
		return huntingBounties;
	}

	public void removeBounty(UUID id) {
		huntingBounties.remove(id);
	}
}
