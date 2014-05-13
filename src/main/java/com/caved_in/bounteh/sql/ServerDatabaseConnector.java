package com.caved_in.bounteh.sql;

import com.caved_in.bounteh.bounties.Bounty;
import com.caved_in.bounteh.bounties.BountyBuilder;
import com.caved_in.commons.Commons;
import com.caved_in.commons.config.SqlConfiguration;
import com.caved_in.commons.event.StackTraceEvent;
import com.caved_in.commons.sql.DatabaseConnector;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ServerDatabaseConnector extends DatabaseConnector {
	private static final String TABLE_NAME = "server_bounty_bounties";
	private static final String INSERT_NEW_BOUNTY = "INSERT INTO server_bounty_bounties (bounty_id,target_id,bnty_issuer_id,bnty_worth,bnty_time_issued," +
			"bnty_time_expire,bnty_filled) VALUES (?,?,?,?,?,?,?)";
	private static final String GET_ACTIVE_PLAYER_BOUNTY = "SELECT * FROM server_bounty_bounties WHERE player_id=? LIMIT 1";
	private static final String GET_ISSUED_BOUNTIES = "SELECT * FROM server_bounty_bounties WHERE bnty_issuer_id=? AND bnty_time_expire > ? AND bnty_filled=0";
	private static final String GET_ALL_ACTIVE_BOUNTIES_STATEMENT = "SELECT * FROM server_bounty_bounties WHERE bnty_time_expire > ? AND bnty_filled=0";
	private static final String GET_ALL_HUNTERS_FOR_BOUNTY_STATEMENT = "SELECT * FROM server_bounty_hunters WHERE bounty_id=?";
	private static final String GET_ALL_BOUNTIES_FOR_HUNTER = "SELECT * FROM server_bounty_hunters WHERE hunter_id=?";
	//	private static final String GET_BOUNTY_IF_ACTIVE = "SELECT * FROM server_bounty_bounties WHERE bounty_id=? AND bnty_time_expire ? AND bnty_filled=0";
	private static final String SET_BOUNTY_FILLED = "UPDATE server_bounty_bounties SET bnty_filled = ? WHERE bounty_id=?";
	private static final String GET_BOUNTY_IF_ACTIVE = "SELECT server_bounty_hunters.bounty_id FROM server_bounty_bounties WHERE server_bounty_hunters.hunter_id = ? AND server_bounty_bounties.bounty_id = server_bounty_hunters.bounty_id AND server_bounty_bounties.bnty_time_expire > ? AND server_bounty_bounties.bnty_filled=0";
	private static final String INSERT_HUNTER_FOR_BOUNTY = "REPLACE INTO server_bounty_hunters (hunter_id,bounty_id) VALUES (?,?)";
	private static final String REMOVE_HUNTER_FROM_BOUNTY = "DELETE FROM server_bounty_hunters WHERE hunter_id=? AND bounty_id=? ";

	private static final String[] TABLE_CREATION_STATEMENTS = {
			"CREATE TABLE IF NOT EXISTS `server_bounty_bounties` (`bounty_id` varchar(36) NOT NULL, `target_id` varchar(36) NOT NULL, `bnty_issuer_id` varchar(36) NOT NULL, `bnty_worth` double unsigned NOT NULL, `bnty_time_issued` bigint(20) unsigned NOT NULL, `bnty_time_expire` bigint(20) unsigned NOT NULL, `bnty_filled` tinyint(1) NOT NULL DEFAULT '0', UNIQUE KEY `bounty_id` (`bounty_id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1;",
			"CREATE TABLE IF NOT EXISTS `server_bounty_hunters` (`id` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `hunter_id` varchar(36) NOT NULL, `bounty_id` varchar(36) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;"
	};

	public ServerDatabaseConnector(SqlConfiguration sqlConfiguration) {
		super(sqlConfiguration);
		executeCreationStatements();
	}

	private void executeCreationStatements() {
		for (String sqlStatement : TABLE_CREATION_STATEMENTS) {
			PreparedStatement statement = prepareStatement(sqlStatement);
			try {
				statement.executeUpdate();
				Commons.debug("Executed creation statement:",sqlStatement);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				close(statement);
			}
		}
	}

	public boolean playerHasActiveBounty(UUID playerId) {
		return getPlayerActiveBounty(playerId) != null;
	}

	public Bounty getPlayerActiveBounty(UUID playerId) {
		PreparedStatement statement = prepareStatement(GET_ACTIVE_PLAYER_BOUNTY);
		Bounty bounty = null;
		try {
			statement.setString(1, playerId.toString());
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				bounty = new BountyBuilder(UUID.fromString(resultSet.getString("bounty_id")))
						.targetId(playerId)
						.issuerId(UUID.fromString(resultSet.getString("bnty_issuer_id")))
						.worth(resultSet.getDouble("bnty_worth"))
						.build();
			}
		} catch (SQLException e) {
			StackTraceEvent.handle(e);

		} finally {
			close(statement);
		}
		return bounty;
	}

	public boolean setBountyCompleted(UUID bountyId, boolean completed) {
		boolean success = false;
		PreparedStatement statement = prepareStatement(SET_BOUNTY_FILLED);
		try {
			statement.setBoolean(1, completed);
			statement.setString(2, bountyId.toString());
			statement.executeUpdate();
			success = true;
		} catch (SQLException e) {
			StackTraceEvent.handle(e);
		} finally {
			close(statement);
		}
		return success;
	}

	public Set<Bounty> getPlayerIssuedBounties(UUID id) {
		Set<Bounty> issuedBounties = new HashSet<Bounty>();
		PreparedStatement statement = prepareStatement(GET_ISSUED_BOUNTIES);
		try {
			statement.setString(1, id.toString());
			statement.setLong(2, System.currentTimeMillis());
			ResultSet results = statement.executeQuery();
			while (results.next()) {
				Bounty bounty = new BountyBuilder(UUID.fromString(results.getString("bounty_id")))
						.targetId(UUID.fromString(results.getString("target_id")))
						.issuerId(id)
						.worth(results.getDouble("bnty_worth"))
						.issuedOn(results.getLong("bnty_time_issued"))
						.expiresOn(results.getLong("bnty_time_expire"))
						.build();
				issuedBounties.add(bounty);
			}
		} catch (SQLException e) {
			StackTraceEvent.handle(e);
		} finally {
			close(statement);
		}
		return issuedBounties;
	}

	public Set<Bounty> getAllActiveBounties() {
		Set<Bounty> allActiveBounties = new HashSet<Bounty>();
		PreparedStatement statement = prepareStatement(GET_ALL_ACTIVE_BOUNTIES_STATEMENT);
		try {
			statement.setLong(1, System.currentTimeMillis());
			ResultSet results = statement.executeQuery();
			while (results.next()) {
				Bounty bounty = new BountyBuilder(UUID.fromString(results.getString("bounty_id")))
						.targetId(UUID.fromString(results.getString("target_id")))
						.issuerId(UUID.fromString(results.getString("bnty_issuer_id")))
						.worth(results.getDouble("bnty_worth"))
						.issuedOn(results.getLong("bnty_time_issued"))
						.expiresOn(results.getLong("bnty_time_expire"))
						.build();
				allActiveBounties.add(bounty);
			}
		} catch (SQLException e) {
			StackTraceEvent.handle(e);
		}
		return allActiveBounties;
	}

	public boolean insertBounty(Bounty bounty) {
		boolean bountyAdded = false;
		String bountyUid = bounty.getBountyId().toString();
		String targetUid = bounty.getTargetId().toString();
		String issuerUid = bounty.getIssuerId().toString();
		double bountyWorth = bounty.getWorth();
		long bountyIssued = bounty.getIssueTime();
		long bountyExpires = bounty.getExpireTime();
		PreparedStatement statement = prepareStatement(INSERT_NEW_BOUNTY);
		try {
			statement.setString(1, bountyUid);
			statement.setString(2, targetUid);
			statement.setString(3, issuerUid);
			statement.setDouble(4, bountyWorth);
			statement.setLong(5, bountyIssued);
			statement.setLong(6, bountyExpires);
			statement.setBoolean(7, false);
			statement.executeUpdate();
			bountyAdded = true;
		} catch (SQLException e) {
			StackTraceEvent.handle(e);
		} finally {
			close(statement);
		}
		return bountyAdded;
	}

	public boolean addHunterToBounty(UUID playerId, UUID bountyId) {
		boolean added = false;
		PreparedStatement statement = prepareStatement(INSERT_HUNTER_FOR_BOUNTY);
		try {
			statement.setString(1, playerId.toString());
			statement.setString(2, bountyId.toString());
			statement.executeUpdate();
			added = true;
		} catch (SQLException ex) {
			StackTraceEvent.handle(ex);
		} finally {
			close(statement);
		}
		return added;
	}

	public boolean removeHunterFromBounty(UUID playerId, UUID bountyId) {
		boolean removed = false;
		PreparedStatement statement = prepareStatement(REMOVE_HUNTER_FROM_BOUNTY);
		try {
			statement.setString(1, playerId.toString());
			statement.setString(2,bountyId.toString());
			statement.executeUpdate();
			removed = true;
		} catch (SQLException e) {
			StackTraceEvent.handle(e);
		} finally {
			close(statement);
		}
		return removed;
	}

	public Set<UUID> getBountiesPlayerHasActive(UUID playerId) {
		Set<UUID> activeBounties = new HashSet<>();
		PreparedStatement statement = prepareStatement(GET_BOUNTY_IF_ACTIVE);
		try {
			statement.setString(1, playerId.toString());
			statement.setLong(2, System.currentTimeMillis());
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				activeBounties.add(UUID.fromString(resultSet.getString("bounty_id")));
			}
		} catch (SQLException e) {
			StackTraceEvent.handle(e);
		} finally {
			close(statement);
		}
		return activeBounties;
	}

	public Set<UUID> getAllHuntersForBounty(UUID bountyId) {
		Set<UUID> hunters = new HashSet<>();
		PreparedStatement statement = prepareStatement(GET_ALL_HUNTERS_FOR_BOUNTY_STATEMENT);
		try {
			statement.setString(1, bountyId.toString());
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				hunters.add(UUID.fromString(resultSet.getString("hunter_id")));
			}
		} catch (SQLException e) {
			StackTraceEvent.handle(e);
		} finally {
			close(statement);
		}
		return hunters;
	}

}