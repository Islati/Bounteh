package com.caved_in.bounteh.bounties;

import org.bukkit.entity.Player;

import java.util.UUID;

public class BountyBuilder {
	private UUID bountyId;
	private UUID targetId;
	private UUID issuerId;
	private double bountyAmount;
	private long timeIssued;
	private long timeExpired;

	public BountyBuilder(UUID bountyId) {
		this.bountyId = bountyId;
	}

	public BountyBuilder withTarget(UUID id) {
		this.targetId = id;
		return this;
	}

	public BountyBuilder withTarget(Player player) {
		return withTarget(player.getUniqueId());
	}

	public BountyBuilder withIssuer(Player player) {
		return withIssuer(player.getUniqueId());
	}

	public BountyBuilder withIssuer(UUID id) {
		this.issuerId = id;
		return this;
	}

	public BountyBuilder withAmount(double amount) {
		this.bountyAmount = amount;
		return this;
	}

	public BountyBuilder issuedOn(long timeStamp) {
		timeIssued = timeStamp;
		return this;
	}

	public BountyBuilder expiresOn(long timeStamp) {
		timeExpired = timeStamp;
		return this;
	}

	public Bounty build() {
		Bounty bounty = new Bounty(bountyId);
		bounty.setTargetId(targetId);
		bounty.setIssuerId(issuerId);
		bounty.setWorth(bountyAmount);
		bounty.setIssueTime(timeIssued);
		bounty.setExpireTime(timeExpired);
		return bounty;
	}
}
