package com.caved_in.bounteh;

import com.caved_in.bounteh.bounties.Bounty;
import com.caved_in.bounteh.bounties.BountyManager;
import com.caved_in.bounteh.commands.BountyCommand;
import com.caved_in.bounteh.config.Configuration;
import com.caved_in.bounteh.listeners.PlayerDeathListener;
import com.caved_in.bounteh.listeners.PlayerJoinListener;
import com.caved_in.bounteh.listeners.PlayerQuitListener;
import com.caved_in.bounteh.players.Hunters;
import com.caved_in.bounteh.sql.ServerDatabaseConnector;
import com.caved_in.bounteh.threads.BountyExpirationCheckThread;
import com.caved_in.bounteh.threads.GetAllBountiesCallable;
import com.caved_in.commons.Commons;
import com.caved_in.commons.command.CommandController;
import com.caved_in.commons.player.Players;
import com.caved_in.commons.plugin.Plugins;
import com.caved_in.commons.threading.executors.BukkitExecutors;
import com.caved_in.commons.threading.executors.BukkitScheduledExecutorService;
import com.caved_in.commons.time.TimeHandler;
import com.caved_in.commons.time.TimeType;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.Set;

public class Bounteh extends JavaPlugin {
	private static Configuration configuration;
	private static BukkitScheduledExecutorService async;
	public static ServerDatabaseConnector database;
	public static Economy economy = null;

	@Override
	public void onEnable() {

		if (!setupEconomy()) {
			Commons.messageConsole("Failed to setup / hook vault economy");
			Plugins.disablePlugin(this);
			return;
		}
		async = BukkitExecutors.newAsynchronous(this);

		//Initialize our configuration for the plugin, and if it fails disable this plugin
		if (!initConfiguration()) {
			Commons.messageConsole("&cError initializing Bounteh configuration, disabling plugin");
			Plugins.disablePlugin(this);
			return;
		}

		//Register the listeners
		registerListeners();
		//Register the bounty commands
		CommandController.registerCommands(this, new BountyCommand());
		//Create the server database connector
		database = new ServerDatabaseConnector(configuration.getSqlConfig());

		//Load all the bounties in the database into memory, for performance / synchronization concerns
		ListenableFuture<Set<Bounty>> getBountiesListener = async.submit(new GetAllBountiesCallable());
		Futures.addCallback(getBountiesListener, new FutureCallback<Set<Bounty>>() {
			@Override
			public void onSuccess(Set<Bounty> bounties) {
				bounties.forEach(BountyManager::addBounty);
				Commons.messageConsole("Loaded in " + bounties.size() + " bounties from the databases");
			}

			@Override
			public void onFailure(Throwable throwable) {
			}
		});

		//Create the task to check for expired bounties
		Commons.threadManager.registerSyncRepeatTask("Bounty Expiration Check", new BountyExpirationCheckThread(), TimeHandler.getTimeInTicks(10, TimeType.MINUTE), TimeHandler.getTimeInTicks(2, TimeType.MINUTE));

		for (Player player : Players.allPlayers()) {
			Hunters.addData(player);
		}
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
		getServer().getScheduler().cancelTasks(this);
	}

	private boolean initConfiguration() {
		Serializer configSerializer = new Persister();

		if (!Plugins.hasDataFolder(this)) {
			Plugins.makeDataFolder(this);
		}

		try {
			File configFile = new File(getDataFolder() + "/Config.xml");
			if (!configFile.exists()) {
				configSerializer.write(new Configuration(), configFile);
			}
			configuration = configSerializer.read(Configuration.class, configFile);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	private void registerListeners() {
		Plugins.registerListeners(this,
				new PlayerJoinListener(),
				new PlayerDeathListener(),
				new PlayerQuitListener()
		);
	}

	public static Configuration getConfiguration() {
		return configuration;
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}
}
