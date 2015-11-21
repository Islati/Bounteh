package com.caved_in.bounteh.bounties;

import com.caved_in.bounteh.Bounteh;
import com.caved_in.bounteh.config.Configuration;
import com.caved_in.bounteh.threads.AddHunterToBountyThread;
import com.caved_in.bounteh.threads.RemoveHunterFromBountyThread;
import com.caved_in.commons.Commons;
import com.caved_in.commons.utilities.StringUtil;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Bounty implements Comparable<Bounty> {

	private String playerName;
	private UUID bountyId = null;
	private UUID targetId;
	private String targetName = null;
	private UUID issuerId;
	private double worth;
	private long issueTime;
	private long expireTime;
	private double contractFee = -1;
	private double postingFee = -1;
	private double deathPenaltyFee = -1;
	private boolean initialized = false;
	private Set<UUID> hunters = new HashSet<>();

	public Bounty(UUID bountyId) {
		this.bountyId = bountyId;
	}

	public void setPlayerName(String name) {
		this.playerName = name;
	}

	public double getContractFee() {
		return contractFee;
	}

	public UUID getTargetId() {
		return targetId;
	}

	public UUID getIssuerId() {
		return issuerId;
	}

	public double getWorth() {
		return worth;
	}

	public void setWorth(double worth) {
		this.worth = worth;
		Configuration config = Bounteh.getConfiguration();
		contractFee = worth * (config.getContractFeePercent());
		postingFee = worth * config.getContractFeePercent();

	}

	/**
	 * @return The total worth of this bounty. (The worth + posting fee)
	 */
	public double getTotalWorth() {
		return getWorth() + getPostingFee();
	}

	public long getIssueTime() {
		return issueTime;
	}

	public void setIssueTime(long issueTime) {
		this.issueTime = issueTime;
	}

	public long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

	public boolean isExpired() {
		return System.currentTimeMillis() > getExpireTime();
	}

	public long getDurationLeft() {
		if (isExpired()) {
			return 0L;
		}

		return getExpireTime() - System.currentTimeMillis();
	}

	public boolean isHunter(Player player) {
		return isHunter(player.getUniqueId());
	}

	public boolean isHunter(UUID id) {
		return hunters.contains(id);
	}

	public UUID getBountyId() {
		if (bountyId == null) {
			bountyId = UUID.randomUUID();
		}
		return bountyId;
	}

	public void setBountyId(UUID id) {
		if (bountyId == null) {
			bountyId = id;
		}
	}

	public boolean addHunter(Player player) {
		return addHunter(player.getUniqueId());
	}

	public boolean addHunter(UUID id) {
		if (isHunter(id) || !isInitialized()) {
			return false;
		}
		hunters.add(id);
		Bounteh.getInstance().getThreadManager().runTaskAsync(new AddHunterToBountyThread(id, bountyId));
		return true;
	}

	public boolean removeHunter(Player player) {
		return removeHunter(player.getUniqueId());
	}

	public boolean removeHunter(UUID id) {
		if (!isHunter(id) || !isInitialized()) {
			return false;
		}
		hunters.remove(id);
		Bounteh.getInstance().getThreadManager().runTaskAsync(new RemoveHunterFromBountyThread(bountyId, id));
		return true;
	}

	public void initHunters(Collection<UUID> ids) {
		hunters.addAll(ids);
	}


	public void setTargetId(UUID targetId) {
		this.targetId = targetId;
	}

	public void setIssuerId(UUID issuerId) {
		this.issuerId = issuerId;
	}

	public double getPostingFee() {
		return postingFee;
	}

	public double getDeathPenalty() {
		if (deathPenaltyFee == -1) {
			deathPenaltyFee = getWorth() * Bounteh.getConfiguration().getDeathPenaltyPercent();
		}
		return deathPenaltyFee;
	}

	@Override
	public int compareTo(Bounty o) {
		double oWorth = o.getWorth();
		if (getWorth() > oWorth) {
			return -1;
		}

		if (getWorth() < oWorth) {
			return 1;
		}

		return 0;
	}

	@Override
	public String toString() {
		return "Bounty [Target ID: " + getTargetId().toString() + "]\n"
				+ "Worth: " + Bounteh.economy.format(getWorth()) + "\n"
				+ "Hunters: [" + StringUtil.joinString(hunters,",",0) + "]";
	}

	public String getPlayerName() {
		return playerName;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public Set<UUID> getHunters() {
		return hunters;
	}

	public double getReturnAmount() {
		return getWorth() - (getWorth() + getPostingFee()) * Bounteh.getConfiguration().getCancellationFeePercent();
	}

	public double getInconvienance(int hunterAmount) {
		return Math.round(Math.floor(getWorth() / hunterAmount));
	}

	public double getCancellationFee() {
		return (int)((getWorth() + getPostingFee()) * Bounteh.getConfiguration().getCancellationFeePercent());
	}
}
