package com.caved_in.bounteh;

import com.caved_in.bounteh.bounties.Bounty;
import com.caved_in.bounteh.bounties.BountyManager;
import com.caved_in.bounteh.commands.BountyCommand;
import com.caved_in.bounteh.config.Configuration;
import com.caved_in.bounteh.listeners.PlayerDeathListener;
import com.caved_in.bounteh.players.Hunters;
import com.caved_in.bounteh.sql.ServerDatabaseConnector;
import com.caved_in.bounteh.threads.BountyExpirationCheckThread;
import com.caved_in.bounteh.threads.GetAllBountiesCallable;
import com.caved_in.commons.chat.Chat;
import com.caved_in.commons.game.MiniGame;
import com.caved_in.commons.player.Players;
import com.caved_in.commons.plugin.Plugins;
import com.caved_in.commons.time.TimeHandler;
import com.caved_in.commons.time.TimeType;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.Set;

public class Bounteh extends MiniGame<Hunters> {
    private static Configuration configuration;
    public static ServerDatabaseConnector database;
    public static Economy economy = null;
    private static Bounteh instance = null;

    public static Bounteh getInstance() {
        return instance;
    }

    @Override
    public void startup() {
        instance = this;

        if (!setupEconomy()) {
            Chat.messageConsole("Failed to setup / hook vault economy");
            Plugins.disablePlugin(this);
            return;
        }

        registerUserManager(
                Hunters.class
        );

        //Register the listeners
        registerListeners(
                new PlayerDeathListener()
        );

        registerCommands(
                new BountyCommand()
        );

        //Create the server database connector
        database = new ServerDatabaseConnector(configuration.getSqlConfig());

        //Load all the bounties in the database into memory, for performance / synchronization concerns
        ListenableFuture<Set<Bounty>> getBountiesListener = getAsyncExecuter().submit(new GetAllBountiesCallable());
        Futures.addCallback(getBountiesListener, new FutureCallback<Set<Bounty>>() {
            @Override
            public void onSuccess(Set<Bounty> bounties) {
                bounties.forEach(BountyManager::addBounty);
                Chat.messageConsole("Loaded in " + bounties.size() + " bounties from the databases");
            }

            @Override
            public void onFailure(Throwable throwable) {
            }
        });

        //Create the task to check for expired bounties
        getThreadManager().registerSyncRepeatTask("Bounty Expiration Check", new BountyExpirationCheckThread(), TimeHandler.getTimeInTicks(10, TimeType.MINUTE), TimeHandler.getTimeInTicks(2, TimeType.MINUTE));

        for (Player player : Players.allPlayers()) {
            getUserManager().addUser(player);
        }
    }

    @Override
    public void shutdown() {

    }

    @Override
    public String getAuthor() {
        return "Brandon Curtis";
    }

    @Override
    public void initConfig() {
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
        } catch (Exception ex) {
            ex.printStackTrace();
            Plugins.disablePlugin(this);
        }
    }

    @Override
    public long tickDelay() {
        return 20;
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
