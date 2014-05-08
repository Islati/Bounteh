package com.caved_in.bounteh;

import com.caved_in.bounteh.bounties.Bounty;
import com.caved_in.bounteh.bounties.BountyManager;
import com.caved_in.bounteh.config.Configuration;
import com.caved_in.bounteh.listeners.PlayerJoinListener;
import com.caved_in.bounteh.sql.ServerDatabaseConnector;
import com.caved_in.bounteh.threads.RetrieveIssuedBountiesCallable;
import com.caved_in.commons.Commons;
import com.caved_in.commons.config.SqlConfiguration;
import com.caved_in.commons.plugin.Plugins;
import com.caved_in.commons.threading.executors.BukkitExecutors;
import com.caved_in.commons.threading.executors.BukkitScheduledExecutorService;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import net.milkbowl.vault.economy.Economy;
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
		}
		async = BukkitExecutors.newAsynchronous(this);

		//Initialize our configuration for the plugin, and if it fails disable this plugin
		if (!initConfiguration()) {
			Commons.messageConsole("&cError initializing Bounteh configuration, disabling plugin");
			Plugins.disablePlugin(this);
			return;
		}

		registerListeners();
		database = new ServerDatabaseConnector(configuration.getSqlConfig());

		ListenableFuture<Set<Bounty>> retrieveActiveBountiesListenable = async.submit(new RetrieveIssuedBountiesCallable());
		Futures.addCallback(retrieveActiveBountiesListenable, new FutureCallback<Set<Bounty>>() {
			@Override
			public void onSuccess(Set<Bounty> bounties) {
				for (Bounty bounty : bounties) {
					BountyManager.addPlayerBounty(bounty);
				}
				Commons.messageConsole("Loaded in " + bounties.size() + " bounties from the databases");
			}

			@Override
			public void onFailure(Throwable throwable) {

			}
		});
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
			File configFile = new File(getDataFolder() + "Config.xml");
			if (!configFile.exists()) {
				configSerializer.write(new SqlConfiguration(), configFile);
			}
			configuration = configSerializer.read(Configuration.class, configFile);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	private void registerListeners() {
		Commons.registerListener(this, new PlayerJoinListener());
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
