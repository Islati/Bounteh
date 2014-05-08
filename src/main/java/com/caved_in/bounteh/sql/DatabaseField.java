package com.caved_in.bounteh.sql;

public enum DatabaseField {
	TARGET_ID("player_id"),
	ISSUER_ID("bnty_issuer_id"),
	BOUNTY_WORTH("bnty_worth"),
	BOUNTY_ISSUE_TIME("bnty_time_issued"),
	BOUNTY_EXPIRE_TIME("bnty_time_expire"),
	BOUNTY_FILLED("bnty_filled");

	private String columnName;
	DatabaseField(String column) {
		columnName = column;
	}

	@Override
	public String toString() {
		return columnName;
	}
}
