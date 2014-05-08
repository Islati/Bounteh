package com.caved_in.bounteh.config;

import com.caved_in.commons.config.SqlConfiguration;
import org.simpleframework.xml.Element;

public class Configuration {

	@Element(name = "bounty-min-contract-worth")
	private double bountyMinContractWorth = 20;

	@Element(name="bounty-fee-percent")
	private double bountyFeePercent = 0.1;

	@Element(name = "contract-fee-percent")
	private double contractFeePercent = 0.05;

	@Element(name = "death-penalty-percent")
	private double deathPenaltyPercent = 0.05;

	@Element(name = "cancellation-fee-percent")
	private double cancellationFeePercent = 0.05;

	@Element(name = "bounty-duration-minutes")
	private double bountyDurationMinutes = 1440;

	@Element(name = "bounty-accept-delay-minutes")
	private double bountyAcceptDelay = 14;

	@Element(name = "anonymous-targets")
	private boolean anonymousTargets = false;

	@Element(name = "pay-inconvenience")
	private boolean payInconcenience = true;

	@Element(name = "location-rounding")
	private int locationRounding = 100;

	@Element(name="clear-own-bounties")
	private boolean playersPayOwnBounty = true;

	@Element(name = "database_config", type = SqlConfiguration.class)
	private SqlConfiguration sqlConfiguration;

	public Configuration() {}

	public Configuration(@Element(name = "bounty-min-contract-worth")double bountyMinContractWorth, @Element(name="bounty-fee-percent")double bountyFeePercent, @Element(name = "contract-fee-percent")double contractFeePercent, @Element(name = "death-penalty-percent")double deathPenaltyPercent,
						 @Element(name = "cancellation-fee-percent")double cancellationFeePercent, @Element(name = "bounty-duration-minutes")double bountyDurationMinutes, @Element(name = "bounty-accept-delay-minutes")double bountyAcceptDelay, @Element(name = "anonymous-targets")boolean anonymousTargets,
						 @Element(name = "pay-inconvenience")boolean payInconcenience, @Element(name = "location-rounding")int locationRounding, @Element(name="clear-own-bounties")boolean playersPayOwnBounty, @Element(name = "database_config", type = SqlConfiguration.class)SqlConfiguration sqlConfiguration) {
		this.bountyMinContractWorth = bountyMinContractWorth;
		this.bountyFeePercent = bountyFeePercent;
		this.contractFeePercent = contractFeePercent;
		this.deathPenaltyPercent = deathPenaltyPercent;
		this.cancellationFeePercent = cancellationFeePercent;
		this.bountyDurationMinutes = bountyDurationMinutes;
		this.bountyAcceptDelay = bountyAcceptDelay;
		this.anonymousTargets = anonymousTargets;
		this.payInconcenience = payInconcenience;
		this.locationRounding = locationRounding;
		this.playersPayOwnBounty = playersPayOwnBounty;
		this.sqlConfiguration = sqlConfiguration;
	}

	public SqlConfiguration getSqlConfig() {
		return sqlConfiguration;
	}

	public double getBountyMinContractWorth() {
		return bountyMinContractWorth;
	}

	public double getBountyFeePercent() {
		return bountyFeePercent;
	}

	public double getContractFeePercent() {
		return contractFeePercent;
	}

	public double getDeathPenaltyPercent() {
		return deathPenaltyPercent;
	}

	public double getCancellationFeePercent() {
		return cancellationFeePercent;
	}

	public double getBountyDurationMinutes() {
		return bountyDurationMinutes;
	}

	public double getBountyAcceptDelay() {
		return bountyAcceptDelay;
	}

	public boolean hasAnonymousTargets() {
		return anonymousTargets;
	}

	public boolean shouldPayInconcenience() {
		return payInconcenience;
	}

	public int getLocationRounding() {
		return locationRounding;
	}

	public boolean canPlayersPayOwnBounty() {
		return playersPayOwnBounty;
	}
}
