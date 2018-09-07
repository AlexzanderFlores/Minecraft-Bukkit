package network.server;

import network.Network;
import network.server.util.ConfigurationUtil;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public enum DB {
	// Account & other
	PLAYERS_ACCOUNTS("uuid VARCHAR(40), name VARCHAR(16), address VARCHAR(40), rank VARCHAR(20), join_time VARCHAR(10), PRIMARY KEY(uuid)"),
	PLAYERS_IP_ADDRESSES("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), address VARCHAR(40), time VARCHAR(25), PRIMARY KEY(id)"),
	PLAYERS_LOCATIONS("uuid VARCHAR(40), prefix VARCHAR(100), location VARCHAR(100), PRIMARY KEY(uuid)"),
	PLAYERS_DISABLED_MESSAGES("uuid VARCHAR(40), PRIMARY KEY(uuid)"),
	PLAYERS_CHAT_LOGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), rank VARCHAR(20), server VARCHAR(25), time VARCHAR(50), message VARCHAR(250), PRIMARY KEY(id)"),
	PLAYERS_BLOCKED_MESSAGES("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), server VARCHAR(25), message VARCHAR(250), PRIMARY KEY(id)"),
	PLAYERS_ARROW_TRAILS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), name VARCHAR(25), active INT, amount_owned INT, unlocked_time VARCHAR(10), PRIMARY KEY(id)"),
	PLAYERS_ACHIEVEMENTS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), game_name VARCHAR(25), achievement VARCHAR(100), PRIMARY KEY(id)"),
	PLAYERS_SETTINGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), setting VARCHAR(50), state INT, PRIMARY KEY(id)"),
	PLAYERS_WIN_EFFECTS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), name VARCHAR(25), active INT, amount_owned INT, unlocked_time VARCHAR(10), PRIMARY KEY(id)"),
	PLAYERS_NOTIFICATIONS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), text VARCHAR(250), seen INT, PRIMARY KEY(id)"),
	PLAYERS_LEVELS("uuid VARCHAR(40), level INT, exp INT, PRIMARY KEY(uuid)"),
	PLAYERS_CHAT_COLOR("uuid VARCHAR(40), color VARCHAR(2), PRIMARY KEY(uuid)"),
	PLAYERS_CHAT_LANGUAGE("uuid VARCHAR(40), language VARCHAR(15), PRIMARY KEY(uuid)"),
	PLAYERS_WORLD_DOWNLOADER("uuid VARCHAR(40), PRIMARY KEY(uuid)"),
	PLAYERS_COIN_BOOSTERS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), game_name VARCHAR(25), amount INT, PRIMARY KEY(id)"),
	PLAYERS_SKY_WARS_LOOT_PASSES("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	PLAYERS_DOMINATION_AUTO_RESPAWN("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	PLAYERS_SPEED_UHC_RESCATTER("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	PLAYERS_KIT_PVP_LEVEL_PURCHASES("uuid VARCHAR(40), purchases INT, PRIMARY KEY(uuid)"),
	PLAYERS_KIT_PVP_AUTO_REGEN("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	PLAYERS_KITPVP_CHESTS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), id_owned INT, PRIMARY KEY(id)"),
	PLAYERS_KITPVP_CHEST_1("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), material VARCHAR(25), data INT, enchant VARCHAR(100), durability INT, amount INT, slot INT, PRIMARY KEY(id)"),
	PLAYERS_KITPVP_CHEST_2("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), material VARCHAR(25), data INT, enchant VARCHAR(100), durability INT, amount INT, slot INT, PRIMARY KEY(id)"),
	PLAYERS_KITPVP_CHEST_3("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), material VARCHAR(25), data INT, enchant VARCHAR(100), durability INT, amount INT, slot INT, PRIMARY KEY(id)"),
	// Play time
	PLAYERS_LIFETIME_PLAYTIME("uuid VARCHAR(40), days INT, hours INT, minutes INT, seconds INT, PRIMARY KEY(uuid)"),
	PLAYERS_MONTHLY_PLAYTIME("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), days INT, hours INT, minutes INT, seconds INT, month INT,  PRIMARY KEY(id)"),
	PLAYERS_WEEKLY_PLAYTIME("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), days INT, hours INT, minutes INT, seconds INT, week INT, PRIMARY KEY(id)"),
	// Kits
	PLAYERS_KITS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), kit VARCHAR(40), PRIMARY KEY(id)"),
	PLAYERS_DEFAULT_KITS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), game VARCHAR(25), type VARCHAR(25), kit VARCHAR(40), PRIMARY KEY(id)"),
	// Statistics
	PLAYERS_STATS_DOMINATION("uuid VARCHAR(40), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(uuid)"),
	PLAYERS_STATS_DOMINATION_MONTHLY("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), date VARCHAR(10), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(id)"),
	PLAYERS_STATS_DOMINATION_WEEKLY("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), date VARCHAR(10), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(id)"),
	PLAYERS_DOMINATION_ELO("uuid VARCHAR(40), elo INT, PRIMARY KEY(uuid)"),
	PLAYERS_DOMINATION_RANK("uuid VARCHAR(40), rank VARCHAR(10), PRIMARY KEY(uuid)"),
	PLAYERS_STATS_SPEED_UHC("uuid VARCHAR(40), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(uuid)"),
	PLAYERS_STATS_SPEED_UHC_MONTHLY("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), date VARCHAR(10), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(id)"),
	PLAYERS_STATS_SPEED_UHC_WEEKLY("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), date VARCHAR(10), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(id)"),
	PLAYERS_STATS_SKY_WARS("uuid VARCHAR(40), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(uuid)"),
	PLAYERS_STATS_SKY_WARS_MONTHLY("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), date VARCHAR(10), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(id)"),
	PLAYERS_STATS_SKY_WARS_WEEKLY("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), date VARCHAR(10), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(id)"),
	PLAYERS_STATS_KIT_PVP("uuid VARCHAR(40), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(uuid)"),
	PLAYERS_STATS_KIT_PVP_MONTHLY("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), date VARCHAR(10), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(id)"),
	PLAYERS_STATS_KIT_PVP_WEEKLY("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), date VARCHAR(10), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(id)"),
	PLAYERS_STATS_ONE_VS_ONE("uuid VARCHAR(40), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(uuid)"),
	PLAYERS_STATS_ONE_VS_ONE_MONTHLY("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), date VARCHAR(10), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(id)"),
	PLAYERS_STATS_ONE_VS_ONE_WEEKLY("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), date VARCHAR(10), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(id)"),
	PLAYERS_ONE_VS_ONE_ELO("uuid VARCHAR(40), elo INT, PRIMARY KEY(uuid)"),
	PLAYERS_ONE_VS_ONE_RANKED("uuid VARCHAR(40), rank VARCHAR(10), PRIMARY KEY(uuid)"),
	PLAYERS_ONE_VS_ONE_RANKED_MATCHES("uuid VARCHAR(40), amount INT, date VARCHAR(10), PRIMARY KEY(uuid)"),
	PLAYERS_STAT_RESETS("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	// Votes
	PLAYERS_LIFETIME_VOTES("uuid VARCHAR(40), amount INT, day INT, streak INT, highest_streak INT, PRIMARY KEY(uuid)"),
	PLAYERS_MONTHLY_VOTES("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), amount INT, month INT, PRIMARY KEY(id)"),
	PLAYERS_WEEKLY_VOTES("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), amount INT, week INT, PRIMARY KEY(id)"),
	// Coins
	PLAYERS_COINS_DOMINATION("uuid VARCHAR(40), coins INT, PRIMARY KEY(uuid)"),
	PLAYERS_COINS_SKY_WARS("uuid VARCHAR(40), coins INT, PRIMARY KEY(uuid)"),
	PLAYERS_COINS_SPEED_UHC("uuid VARCHAR(40), coins INT, PRIMARY KEY(uuid)"),
	PLAYERS_COINS_KIT_PVP("uuid VARCHAR(40), coins INT, PRIMARY KEY(uuid)"),
	// Keys
	PLAYERS_KEY_FRAGMENTS("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	// Speed UHC
	PLAYERS_HARDCORE_ELIMINATION_VOTES("uuid VARCHAR(40), votes INT, PRIMARY KEY(uuid)"),
	// Twitter
	PLAYERS_TWITTER_AUTH_URLS("address VARCHAR(40), url VARCHAR(250), PRIMARY KEY(address)"),
	PLAYERS_TWITTER_API_KEYS("uuid VARCHAR(40), access_token VARCHAR(50), access_secret VARCHAR(50), PRIMARY KEY(uuid)"),

	NETWORK_PROXIES("server VARCHAR(25), PRIMARY KEY(server)"),
	NETWORK_POPULATIONS("server VARCHAR(25), population INT, PRIMARY KEY(server)"),
	NETWORK_COMMAND_DISPATCHER("id INT NOT NULL AUTO_INCREMENT, server VARCHAR(25), command VARCHAR(250), PRIMARY KEY(id)"),
	NETWORK_SERVER_STATUS("id INT NOT NULL AUTO_INCREMENT, game_name VARCHAR(25), server_number INT, listed_priority INT, lore VARCHAR(100), players INT, max_players INT, PRIMARY KEY(id)"),
	NETWORK_SERVER_LIST("data_type VARCHAR(15), data_value VARCHAR(100), PRIMARY KEY(data_type)"),
	NETWORK_MINI_GAME_PERFORMANCE("id INT NOT NULL AUTO_INCREMENT, server VARCHAR(25), map VARCHAR(250), maxPlayers INT, maxMemory DOUBLE, maxMemoryTime VARCHAR(50), lowestTPS DOUBLE, PRIMARY KEY(id)"),
	NETWORK_MAP_VOTES("id INT NOT NULL AUTO_INCREMENT, game_name VARCHAR(25), map VARCHAR(250), times_voted INT, PRIMARY KEY(id)"),
	NETWORK_MAP_RATINGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), game VARCHAR(25), map VARCHAR(250), rating INT, PRIMARY KEY(id)"),
	NETWORK_AUTO_ALERTS("text_id VARCHAR(25), how_often INT, text VARCHAR(100), PRIMARY KEY(text_id)"),
	NETWORK_BUKKIT_COMMAND_DISPATCHER("id INT NOT NULL AUTO_INCREMENT, server VARCHAR(25), command VARCHAR(250), PRIMARY KEY(id)"),
	NETWORK_PREGEN_USES("world INT, uses INT, PRIMARY KEY(world)"),
	NETWORK_MAP_IDS("id INT NOT NULL AUTO_INCREMENT, name VARCHAR(100), map_id INT, PRIMARY KEY(id)"),
	NETWORK_RECENT_SUPPORTERS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), package VARCHAR(50), PRIMARY KEY(id)"),
	NETWORK_UHC_TIMES("id INT NOT NULL AUTO_INCREMENT, day INT, hour INT, started BOOL, options VARCHAR(25), scenarios VARCHAR(100), PRIMARY KEY(id)"),
	NETWORK_UHC_URL("server INT, url VARCHAR(150), PRIMARY KEY(server)"),
	NETWORK_ERROR_LOGS("id INT NOT NULL AUTO_INCREMENT, error VARCHAR(250), PRIMARY KEY(id)"),
	//NETWORK_BOOSTERS("id INT NOT NULL AUTO_INCREMENT, plugin VARCHAR(30), minute INT, PRIMARY KEY(id)"),
	NETWORK_ANTI_CHEAT_DATA("cheat VARCHAR(25), bans INT, PRIMARY KEY(cheat)"),
	NETWORK_ANTI_CHEAT_BAN_QUEUE("uuid VARCHAR(40), cheat VARCHAR(25), PRIMARY KEY(uuid)"),
	NETWORK_ATTACK_DISTANCE_LOGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), distance DOUBLE, PRIMARY KEY(id)"),
	NETWORK_CPS_LOGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), cps INT, PRIMARY KEY(id)"),
	NETWORK_DISTANCE_LOGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), distance DOUBLE, PRIMARY KEY(id)"),
	NETWORK_POWER_BOW_LOGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), percentage_fast_bow INT, PRIMARY KEY(id)"),
	NETWORK_FAST_EAT_TEST("uuid VARCHAR(40), PRIMARY KEY(uuid)"),
	NETWORK_AUTO_STEAL_TEST("uuid VARCHAR(40), PRIMARY KEY(uuid)"),
	NETWORK_AUTO_EAT_TEST("uuid VARCHAR(40), PRIMARY KEY(uuid)"),
	NETWORK_WATER_WALK_TEST("uuid VARCHAR(40), PRIMARY KEY(uuid)"),
	NETWORK_ANTI_CHEAT_FLOATING_KICKS("uuid VARCHAR(40), PRIMARY KEY(uuid)"),
	NETWORK_ANTI_CHEAT_FLY_KICKS("uuid VARCHAR(40), PRIMARY KEY(uuid)"),
	NETWORK_AUTO_SPRINT_TEST("uuid VARCHAR(40), PRIMARY KEY(uuid)"),
	NETWORK_AUTO_ARMOR_TEST("uuid VARCHAR(40), PRIMARY KEY(uuid)"),

	// Perks
	HUB_ARMOR("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), name VARCHAR(25), type VARCHAR(15), active INT, amount_owned INT, unlocked_time VARCHAR(10), PRIMARY KEY(id)"),
	HUB_SPINNING_BLOCKS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), name VARCHAR(25), amount_owned INT, unlocked_time VARCHAR(10), PRIMARY KEY(id)"),
	HUB_HALO_PARTICLES("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), name VARCHAR(25), active INT, amount_owned INT, unlocked_time VARCHAR(10), PRIMARY KEY(id)"),
	HUB_PETS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), name VARCHAR(30), attributes VARCHAR(100), active INT, amount_owned INT, unlocked_time VARCHAR(10), PRIMARY KEY(id)"),
	// Parkour
	HUB_PARKOUR_CHECKPOINT_LOCATIONS("uuid VARCHAR(40), x DOUBLE, y DOUBLE, z DOUBLE, yaw DOUBLE, pitch DOUBLE, PRIMARY KEY(uuid)"),
	HUB_PARKOUR_CHECKPOINTS("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	HUB_PARKOUR_TIMES("uuid VARCHAR(40), seconds INT, PRIMARY KEY(uuid)"),
	HUB_PARKOUR_ENDLESS_SCORES("uuid VARCHAR(40), best_score INT, PRIMARY KEY(uuid)"),
	HUB_PARKOUR_ENDLESS_RESPAWNS("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	// Crates
	HUB_CRATE_KEYS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), type VARCHAR(25), amount INT, PRIMARY KEY(id)"),
	HUB_LIFETIME_CRATES_OPENED("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), type VARCHAR(25), amount INT, PRIMARY KEY(id)"),
	HUB_MONTHLY_CRATES_OPENED("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), type VARCHAR(25), amount INT, month INT, PRIMARY KEY(id)"),
	HUB_WEEKLY_CRATES_OPENED("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), type VARCHAR(25), amount INT, week INT, PRIMARY KEY(id)"),
	HUB_CRATE_LOGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), type VARCHAR(25), reward VARCHAR(100), time VARCHAR(25), PRIMARY KEY(id)"),
	// Sky Wars Crates
	HUB_SKY_WARS_CRATE_KEYS("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	HUB_LIFETIME_SKY_WARS_CRATES_OPENED("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	HUB_MONTHLY_SKY_WARS_CRATES_OPENED("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), amount INT, month INT, PRIMARY KEY(id)"),
	HUB_WEEKLY_SKY_WARS_CRATES_OPENED("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), amount INT, week INT, PRIMARY KEY(id)"),
	HUB_SKY_WARS_CRATE_LOGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), reward VARCHAR(100), PRIMARY KEY(id)"),
	// Speed UHC Crates
	HUB_SPEED_UHC_CRATE_KEYS("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	HUB_LIFETIME_SPEED_UHC_CRATES_OPENED("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	HUB_MONTHLY_SPEED_UHC_CRATES_OPENED("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), amount INT, month INT, PRIMARY KEY(id)"),
	HUB_WEEKLY_SPEED_UHC_CRATES_OPENED("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), amount INT, week INT, PRIMARY KEY(id)"),
	HUB_SPEED_UHC_CRATE_LOGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), reward VARCHAR(100), PRIMARY KEY(id)"),

	STAFF_ONLINE("uuid VARCHAR(40), prefix VARCHAR(100), server VARCHAR(100), PRIMARY KEY(uuid)"),
	STAFF_CHAT("id INT NOT NULL AUTO_INCREMENT, command VARCHAR(250), PRIMARY KEY(id)"),
	STAFF_MUTES("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), attached_uuid VARCHAR(40), staff_uuid VARCHAR(40), who_unmuted VARCHAR(40), reason VARCHAR(100), date VARCHAR(10), time VARCHAR(25), unmute_date VARCHAR(10), unmute_time VARCHAR(25), expires VARCHAR(25), active INT, PRIMARY KEY(id)"),
	STAFF_MUTE_PROOF("id INT NOT NULL AUTO_INCREMENT, mute_id INT, proof VARCHAR(100), PRIMARY KEY(id)"),
	STAFF_BAD_NAMES("uuid VARCHAR(40), staff_uuid VARCHAR(40), bad_name VARCHAR(16), PRIMARY KEY(uuid)"),
	STAFF_SHADOW_MUTES("uuid VARCHAR(40), PRIMARY KEY(uuid)"),
	STAFF_BAN("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), address VARCHAR(40), attached_uuid VARCHAR(40), staff_uuid VARCHAR(40), who_unbanned VARCHAR(40), reason VARCHAR(100), date VARCHAR(10), time VARCHAR(25), unban_date VARCHAR(10), unban_time VARCHAR(25), day INT, active INT, PRIMARY KEY(id)"),
	STAFF_BAN_PROOF("id INT NOT NULL AUTO_INCREMENT, ban_id INT, proof VARCHAR(100), PRIMARY KEY(id)"),
	STAFF_REPORTS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), reported_uuid VARCHAR(40), staff_uuid VARCHAR(40), reason VARCHAR(100), reason_closed VARCHAR(30), comments VARCHAR(100), playtime VARCHAR(40), proof VARCHAR(100), time_opened VARCHAR(25), date_closed VARCHAR(10), time_closed VARCHAR(25), opened BOOL, PRIMARY KEY(id)"),
	STAFF_REPORTS_CLOSED("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), date_closed VARCHAR(10), amount INT, PRIMARY KEY(id)"),
	STAFF_COMMANDS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), time VARCHAR(25), command VARCHAR(250), PRIMARY KEY(id)");

	// TODO: Move this to a config
	public static final boolean ENABLED = false;
	private String table = null;
	private String keys = "";
	private Databases database = null;
	private static boolean displayErrors = true;
	
	private DB(String query) {
		if(!ENABLED) {
			return;
		}
		String databaseName = toString().split("_")[0];
		database = Databases.valueOf(databaseName);
		table = toString().replace(databaseName, "");
		table = table.substring(1, table.length()).toLowerCase();
		String [] declarations = query.split(", ");
		for(int a = 0; a < declarations.length - 1; ++a) {
			String declaration = declarations[a].split(" ")[0];
			if(!declaration.equals("id")) {
				keys += "`" + declaration + "`, ";
			}
		}
		keys = keys.substring(0, keys.length() - 2); 
		database.connect();
		try {
			if(database.getConnection() != null) {
				database.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + table + " (" + query + ")").execute();
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public String getName() {
		return table;
	}
	
	public Connection getConnection() {
		return ENABLED ? this.database.getConnection() : null;
	}
	
	public boolean isKeySet(String key, String value) {
		if(!ENABLED) {
			return false;
		}
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement("SELECT COUNT(" + key + ") FROM " + getName() + " WHERE " + key + " = '" + value + "' LIMIT 1");
			resultSet = statement.executeQuery();
			return resultSet.next() && resultSet.getInt(1) > 0;
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement, resultSet);
		}
		return false;
	}
	
	public boolean isKeySet(String [] keys, String [] values) {
		if(!ENABLED) {
			return false;
		}
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String query = "SELECT COUNT(" + keys[0] + ") FROM " + getName() + " WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 1; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query + " LIMIT 1");
			resultSet = statement.executeQuery();
			return resultSet.next() && resultSet.getInt(1) > 0;
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement, resultSet);
		}
		return false;
	}
	
	public boolean isUUIDSet(UUID uuid) {
		return isUUIDSet("uuid", uuid);
	}
	
	public boolean isUUIDSet(String key, UUID uuid) {
		return isKeySet(key, uuid.toString());
	}
	
	public int getInt(String key, String value, String requested) {
		if(!ENABLED) {
			return 0;
		}
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement("SELECT " + requested + " FROM " + getName() + " WHERE " + key + " = '" + value + "' LIMIT 1");
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getInt(requested);
			}
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement, resultSet);
		}
		return 0;
	}
	
	public int getInt(String [] keys, String [] values, String requested) {
		if(!ENABLED) {
			return 0;
		}
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String query = "SELECT " + requested + " FROM " + getName() + " WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 1; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query);
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getInt(requested);
			}
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement, resultSet);
		}
		return 0;
	}
	
	public void updateInt(String set, int update, String key, String value) {
		if(!ENABLED) {
			return;
		}
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement("UPDATE " + getName() + " SET " + set + " = '" + update + "' WHERE " + key + " = '" + value + "'");
			statement.execute();
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement);
		}
	}
	
	public void updateInt(String set, int update, String [] keys, String [] values) {
		if(!ENABLED) {
			return;
		}
		PreparedStatement statement = null;
		try {
			String query = "UPDATE " + getName() + " SET " + set + " = '" + update + "' WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 0; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query);
			statement.execute();
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement);
		}
	}
	
	public void updateDouble(String set, double update, String key, String value) {
		if(!ENABLED) {
			return;
		}
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement("UPDATE " + getName() + " SET " + set + " = '" + update + "' WHERE " + key + " = '" + value + "'");
			statement.execute();
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement);
		}
	}
	
	public void updateBoolean(String set, boolean update, String key, String value) {
		if(!ENABLED) {
			return;
		}
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement("UPDATE " + getName() + " SET " + set + " = '" + (update ? "1" : "0") + "' WHERE " + key + " = '" + value + "'");
			statement.execute();
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement);
		}
	}
	
	public String getString(String key, String value, String requested) {
		if(!ENABLED) {
			return "";
		}
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement("SELECT " + requested + " FROM " + getName() + " WHERE " + key + " = '" + value + "' LIMIT 1");
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getString(requested);
			}
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement, resultSet);
		}
		return null;
	}
	
	public String getString(String [] keys, String [] values, String requested) {
		if(!ENABLED) {
			return "";
		}
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String query = "SELECT " + requested + " FROM " + getName() + " WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 1; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query + " LIMIT 1");
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getString(requested);
			}
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement, resultSet);
		}
		return null;
	}
	
	public List<String> getAllStrings(String colum) {
		return getAllStrings(colum, "", "");
	}
	
	public List<String> getAllStrings(String requested, String key, String value) {
		if(!ENABLED) {
			return new ArrayList<>();
		}
		List<String> results = new ArrayList<String>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String query = "SELECT " + requested + " FROM " + getName();
			if(key != null && !key.equals("") && value != null && !value.equals("")) {
				query += " WHERE " + key + " = '" + value + "'";
			}
			statement = getConnection().prepareStatement(query);
			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				results.add(resultSet.getString(requested));
			}
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement, resultSet);
		}
		return results;
	}
	
	public List<String> getAllStrings(String colum, String [] keys, String [] values) {
		if(!ENABLED) {
			return new ArrayList<>();
		}
		List<String> results = new ArrayList<String>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String query = "SELECT " + colum + " FROM " + getName() + " WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 1; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query);
			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				results.add(resultSet.getString(colum));
			}
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement, resultSet);
		}
		return results;
	}

	public List<String> getAllStrings(String colum, String key, String value, String limit) {
		if(!ENABLED) {
			return new ArrayList<>();
		}
		List<String> results = new ArrayList<String>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String query = "SELECT " + colum + " FROM " + getName();
			if(key != null && value != null) {
				query += " WHERE " + key + " = '" + value + "'";
			}
			query += " LIMIT " + limit;
			statement = getConnection().prepareStatement(query);
			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				results.add(resultSet.getString(colum));
			}
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement, resultSet);
		}
		return results;
	}
	
	public void updateString(String set, String update, String key, String value) {
		if(!ENABLED) {
			return;
		}
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement("UPDATE " + getName() + " SET " + set + " = '" + update + "' WHERE " + key + " = '" + value + "'");
			statement.execute();
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement);
		}
	}
	
	public void updateString(String set, String update, String [] keys, String [] values) {
		if(!ENABLED) {
			return;
		}
		PreparedStatement statement = null;
		try {
			String query = "UPDATE " + getName() + " SET " + set + " = '" + update + "' WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 1; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query);
			statement.execute();
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement);
		}
	}
	
	public boolean getBoolean(String key, String value, String requested) {
		if(!ENABLED) {
			return false;
		}
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement("SELECT " + requested + " FROM " + getName() + " WHERE " + key + " = '" + value + "'");
			resultSet = statement.executeQuery();
			return resultSet.next() && resultSet.getBoolean(requested);
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement, resultSet);
		}
		return false;
	}
	
	public int getSize() {
		if(!ENABLED) {
			return 0;
		}
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement("SELECT COUNT(*) FROM " + getName());
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getInt(1);
			}
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement, resultSet);
		}
		return 0;
	}
	
	public int getSize(String key, String value) {
		if(!ENABLED) {
			return 0;
		}
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement("SELECT COUNT(" + key + ") FROM " + getName() + " WHERE " + key + " = '" + value + "'");
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getInt(1);
			}
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement, resultSet);
		}
		return 0;
	}
	
	public int getSize(String [] keys, String [] values) {
		if(!ENABLED) {
			return 0;
		}
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String query = "SELECT COUNT(" + keys[0] + ") FROM " + getName() + " WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 1; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query);
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getInt(1);
			}
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement, resultSet);
		}
		return 0;
	}
	
	public List<String> getOrdered(String orderBy, String requested, String key, String value, long limit) {
		return getOrdered(orderBy, requested, key, value, limit, false);
	}
	
	public List<String> getOrdered(String orderBy, String requested, String key, String value, long limit, boolean descending) {
		if(!ENABLED) {
			return new ArrayList<>();
		}
		List<String> results = new ArrayList<String>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String desc = descending ? " DESC " : " ASC ";
			String max = limit > 0 ? " LIMIT " + limit : "";
			statement = getConnection().prepareStatement("SELECT " + requested + " FROM " + getName() + " WHERE " + key + " = '" + value + "' ORDER BY " + orderBy + desc + max);
			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				results.add(resultSet.getString(requested));
			}
			return results;
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement, resultSet);
		}
		return null;
	}
	
	public List<String> getOrdered(String orderBy, String requested, int limit) {
		return getOrdered(orderBy, requested, limit, false);
	}
	
	public List<String> getOrdered(String orderBy, String requested, int limit, boolean descending) {
		if(!ENABLED) {
			return new ArrayList<>();
		}
		List<String> results = new ArrayList<String>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String desc = descending ? " DESC " : " ASC ";
			String max = limit > 0 ? " LIMIT " + limit : "";
			statement = getConnection().prepareStatement("SELECT " + requested + " FROM " + getName() + " ORDER BY " + orderBy + desc + max);
			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				results.add(resultSet.getString(requested));
			}
			return results;
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement, resultSet);
		}
		return null;
	}
	
	public List<String> getOrdered(String orderBy, String requested, int [] limit) {
		if(!ENABLED) {
			return new ArrayList<>();
		}
		return getOrdered(orderBy, requested, limit, false);
	}
	
	public List<String> getOrdered(String orderBy, String requested, int [] limit, boolean descending) {
		if(!ENABLED) {
			return new ArrayList<>();
		}
		List<String> results = new ArrayList<String>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String desc = descending ? " DESC " : " ASC ";
			String max = limit[0] >= 0 && limit[1] >= 0 ? " LIMIT " + limit[0] + "," + limit[1] : "";
			statement = getConnection().prepareStatement("SELECT " + requested + " FROM " + getName() + " ORDER BY " + orderBy + desc + max);
			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				results.add(resultSet.getString(requested));
			}
			return results;
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement, resultSet);
		}
		return null;
	}
	
	public void delete(String key, String value) {
		if(!ENABLED) {
			return;
		}
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement("DELETE FROM " + getName() + " WHERE " + key + " = '" + value + "'");
			statement.execute();
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement);
		}
	}
	
	public void delete(String [] keys, String [] values) {
		if(!ENABLED) {
			return;
		}
		PreparedStatement statement = null;
		try {
			String query = "DELETE FROM " + getName() + " WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 1; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query);
			statement.execute();
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement);
		}
	}
	
	public void deleteUUID(UUID uuid) {
		deleteUUID("uuid", uuid);
	}
	
	public void deleteUUID(String key, UUID uuid) {
		delete(key, uuid.toString());
	}
	
	public void deleteAll() {
		if(!ENABLED) {
			return;
		}
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement("DELETE FROM " + getName());
			statement.execute();
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement);
		}
	}
	
	public boolean insert(String values) {
		if(!ENABLED) {
			return false;
		}
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement("INSERT INTO " + getName() + " (" + keys + ") VALUES (" + values + ")");
			statement.execute();
			return true;
		} catch(SQLException e) {
			if(!e.getMessage().startsWith("Duplicate entry")) {
				if(displayErrors) {
					e.printStackTrace();
				} else {
					Bukkit.getLogger().info(e.getMessage());
				}
			}
		} finally {
			close(statement);
		}
		return false;
	}
	
	public boolean execute(String sql) {
		if(!ENABLED) {
			return false;
		}
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement(sql);
			statement.execute();
			return true;
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		} finally {
			close(statement);
		}
		return false;
	}
	
	public static void close(PreparedStatement statement, ResultSet resultSet) {
		close(statement);
		close(resultSet);
	}
	
	public static void close(PreparedStatement statement) {
		try {
			if(statement != null) {
				statement.close();
			}
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		}
	}
	
	public static void close(ResultSet resultSet) {
		try {
			if(resultSet != null) {
				resultSet.close();
			}
		} catch(SQLException e) {
			if(displayErrors) {
				e.printStackTrace();
			} else {
				Bukkit.getLogger().info(e.getMessage());
			}
		}
	}
	
	public enum Databases {
		PLAYERS, NETWORK, HUB, STAFF;
		
		private Connection connection = null;
		
		public void connect() {
			if(!ENABLED) {
				return;
			}
			try {
				if(connection == null || connection.isClosed()) {
					ConfigurationUtil config = new ConfigurationUtil(Network.getInstance().getDataFolder() + "/db.yml");
					String address = config.getConfig().getString("address");
					int port = config.getConfig().getInt("port");
					String user = config.getConfig().getString("user");
					String password = config.getConfig().getString("password");
                    String url = "jdbc:mysql://" + address + ":" + port + "/" + toString().toLowerCase();
                    Bukkit.getLogger().info("");
                    Bukkit.getLogger().info(url);
                    Bukkit.getLogger().info("");
					connection = DriverManager.getConnection(url, user, password);
				}
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
		
		public Connection getConnection() {
			return this.connection;
		}
		
		public void disconnect() {
			if(connection != null) {
				try {
					connection.close();
				} catch(SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
