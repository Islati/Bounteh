package com.caved_in.bounteh.bounties;

import org.bukkit.entity.Player;

import java.util.UUID;

public class BountyBuilder {
	private UUID bountyId;
	private UUID targetId;
	private UUID issuerId;
	private String issuerName;
	private String targetName;
	private double bountyAmount;
	private long timeIssued;
	private long timeExpired;

	public BountyBuilder(UUID bountyId) {
		this.bountyId = bountyId;
	}

	public BountyBuilder targetId(UUID id) {
		this.targetId = id;
		return this;
	}

	public BountyBuilder target(Player player) {
		targetName = player.getName();
		return targetId(player.getUniqueId());
	}

	public BountyBuilder targetName(String name) {
		targetName = name;
		return this;
	}

	public BountyBuilder issuer(Player player) {
		this.issuerName = player.getName();
		return issuerId(player.getUniqueId());
	}

	public BountyBuilder issuerId(UUID id) {
		this.issuerId = id;
		return this;
	}


	public BountyBuilder issuerName(String name) {
		this.issuerName = name;
		return this;
	}


	public BountyBuilder worth(double amount) {
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
		bounty.setTargetName(targetName);
		bounty.setPlayerName(issuerName);
		return bounty;
	}
}
