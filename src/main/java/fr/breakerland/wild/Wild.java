package fr.breakerland.wild;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.player.LandPlayer;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class Wild extends JavaPlugin implements CommandExecutor {
	private Economy economy;
	private Permission permission;
	private LandsIntegration landsAddon;

	@Override
	public void onEnable() {
		if (!setupEconomy()) {
			getLogger().info("No economy plugin detected. Please install an economy plugin like iConomy6 or Essentials.");
			setEnabled(false);
		}

		if (!setupPermissions()) {
			getLogger().info("No permission plugin detected. Please install an permission plugin like LuckPerms or PermissionEx.");
			setEnabled(false);
		}

		landsAddon = new LandsIntegration(this, false);
		getCommand("wild").setExecutor(this);
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null)
			return false;

		return (economy = rsp.getProvider()) != null;
	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		if (rsp == null)
			return false;

		return (permission = rsp.getProvider()) != null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (! (sender instanceof Player))
			return false;

		Player player = (Player) sender;
		LandPlayer landPlayer = landsAddon.getLandPlayer(player.getUniqueId());
		double cost = getConfig().getDouble("price." + permission.getPrimaryGroup(player), getConfig().getDouble("default", 0));
		if (! (cost > 0) || landPlayer == null || !landPlayer.ownsLand() && !landPlayer.getLands().isEmpty() || economy.withdrawPlayer(player, cost).transactionSuccess()) {
			sender.sendMessage(format(getConfig().getString("messages.wildSucess", "&8Téléporation aléatoire réussie! Coût: %cost%"), cost));
			getServer().dispatchCommand(getServer().getConsoleSender(), getConfig().getString("command", "l wild %money%").replaceFirst("%money%", player.getName()));
		} else
			sender.sendMessage(format(getConfig().getString("messages.wildFailed", "&cVous n'avez pas assez d'argent. Coût: %cost%"), cost));

		return true;
	}

	private String format(String input, double cost) {
		return ChatColor.translateAlternateColorCodes('&', input.replaceFirst("%cost%", Double.toString(cost)));
	}
}