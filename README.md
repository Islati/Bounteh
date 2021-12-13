Bounteh - Bounty Hunting Reborn!
----

Put a hit on that player who grieved your farm, and watch the whole server hunt him down!

With a super simple design, and an intense amount of options, Bounteh aims to be the ultimate bounty hunting plugin; Staying entirely free and open source to prove the power of Commons!

This is just one of many plugins to be released, as examples and additions to the wonderful Spigot community :)

Features
======
* Bounty locator, showing your targets last known locations!
* Place, and accept multiple bounties, to be the ultimate bounty hunter!
* Allow others to add to your bounty, making the reward greater for whoever can slay your foe!
* Permission based, allowing you to grant features as you desire!
* Lightweight, won't lag your server!
* Super easy to use; GUI's instead of Commands where available!
* Bounties can Expire after a configurable amount of time, making the stakes even higher!
* Much more, so download it and check it out!
* Note: If you have a feature suggestion, post it below and I'll do my best to implement it!

Commands & Permissions
----

* /bounty ? [page]
Detailed help menu regarding the commands in Bounteh
[page] by default is 1.
* /bounty list [page]
View a list of all the active bounties.
[page] by default is 1. The more bounties that are available, the higher this argument can be.
* /bounty view
See a list of all the bounties you're currently pursuing, and how long you have left to slay your target.
* /bounty accept <target>
Pursue a bounty on the target, allowing you to collect a reward if you kill them!
<target> - The target you wish to pursue!
Permissions Required:
bounty.accept
* /bounty abandon <target>
Abandon a bounty you're pursuing on a specific target.
<target> - The target who's bounty you wish to stop pursuing.
* /bounty place <player> <value>
Place a bounty on a player worth <value>, allowing hunters to take their contract and get a reward if they slay the player!
<player> - Player to place the bounty on!
<value> - Amount to place the bounty at; Aka, the reward a player will receive if they fulfil the contract!
Permissions Required:
bounty.accept
* /bounty add <player> <amount>
Add your fee to an already existing bounty; Giving the potential hunters of the contract a bit more cash for their kill!
<player> - The target player whos bounty you wish to add more money to!
<amount> - The amount to add to the already existing bounty fee!
Permissions Required:
bounty.accept
* /bounty cancel <target>
Cancel the bounty you've issued on a target, forcing all hunters to drop the contract, and returning the money deposited on the bounty to its owners.
<target> - Player of the bounty you wish to cancel.
* /bounty locate [target]
View a rough estimation of the location(s) for your current target(s).
[target] - If left blank, you'll receive a list of all the known locations; Otherwise you'll see the location for a specific target.
Permissions Required:
bounty.locate
* /bounty clear
Clear the active bounty on your head, by paying off the reward for your death, yourself!
Permissions Required:
bounty.clear

Configuration
-----
```xml
<configuration>
   <!-- This is the minimum amount that a bounty can be worth in order to be considered valid! -->
   <bounty-min-contract-worth>20.0</bounty-min-contract-worth>
   <!-- This is how much money will get deducted (ontop of the reward) when placing a bounty. (Percentage) 1 being 100% -->
   <bounty-fee-percent>0.1</bounty-fee-percent>
   <!-- Percent of the total contract worth that a bounty hunter will be charged for taking up a contract, so players can't mass-hoarde contracts without having to pay for it! -->
   <contract-fee-percent>0.05</contract-fee-percent>
   <!-- When hunting a contract and the player dies, they can be charged a percentage for failing to achieve! Change to 0 to disable. -->
   <death-penalty-percent>0.05</death-penalty-percent>
   <!-- If cancelling a contract there's a fee that can be deducted to prevent players from placing bounties on their head and then cancelling them without reprocussions! -->
   <cancellation-fee-percent>0.05</cancellation-fee-percent>
   <!-- How long (in minutes) from the time a bounty is created, before it will expire. -->
   <bounty-duration-minutes>1440.0</bounty-duration-minutes>
   <!-- How long before players can accept another bounty (in minutes from the last time) -->
   <bounty-accept-delay-minutes>14.0</bounty-accept-delay-minutes>
   <!-- Whether or not to refund players an inconvienance fee whenever someone cancels a contract that they're pursueing. -->
   <pay-inconvenience>true</pay-inconvenience>
   <!-- How far around to search for the players target; The greater the number, the more area it will search for their last known location. -->
   <location-rounding>100</location-rounding>
   <!-- If true, a player can do /bounty clear to clear the bounty on their own head. Letting them pay the fee, instead of having to have their head hunted. -->
   <clear-own-bounties>true</clear-own-bounties>
</configuration>
```
If you have any questions regarding configuration, let me know!

Each bounty has its own .xml file, index by the bounties UUID.
Inside this bounty is information regarding the target name, targets UUID, original placer of the bounty, all the active hunters on the bounty, all the extra additions that have been placed on the bounty to increase its value!

Tampering with data inside the Bounty.xml file will likely result in errors, but if you wish to, do so at your own discretion!

Note: Due to the design of Bounteh, utilizing the Commons MiniGame engine, it creates the 'Arenas' folder to manage worlds and their interactions; Creating a configuration file for the default world. The plugin doesn't depend on this, though requires the file to be there. In the future there will be additional options to manage interactions in these config files.

Installation
---

Bounteh Requires the following Dependencies: Commons, Vault, Java 8
Download the Dependencies listed above, place them in your /plugins/ folder and assure they're working!
Download Bounteh, and place it in your /plugins/ folder.
Start your server, then stop your server.
Configure Bounteh to your desired setup,  then startup your server again!
Let players place bounties, and watch the havoc begin!
