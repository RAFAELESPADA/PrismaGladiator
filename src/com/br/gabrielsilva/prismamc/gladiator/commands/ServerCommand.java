package com.br.gabrielsilva.prismamc.gladiator.commands;

import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.br.gabrielsilva.prismamc.commons.bukkit.commands.BukkitCommandSender;
import com.br.gabrielsilva.prismamc.commons.bukkit.manager.config.PluginConfig;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandClass;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandFramework.Command;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.gladiator.Gladiator;

public class ServerCommand implements CommandClass {

	public static ArrayList<UUID> autorizados = new ArrayList<>();
	
	@Command(name = "setloc", groupsToUse= {Groups.DONO})
	public void setloc(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
		if (args.length == 0) {
			sendHelp(commandSender);
			return;
		}
		Player player = commandSender.getPlayer();
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("spawn")) {
				PluginConfig.putNewLoc(Gladiator.getInstance(), "spawn", player);
				Gladiator.setSpawn(PluginConfig.getNewLoc(Gladiator.getInstance(), "spawn"));
				commandSender.sendMessage("§aSpawn setado!");
			} else {
				sendHelp(commandSender);
			}
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("hologramas")) {
				if (isValidHologram(args[1])) {
					String name = args[1];
					PluginConfig.putNewLoc(Gladiator.getInstance(), "hologramas." + name.toLowerCase(), player);
					player.sendMessage("§aHolograma setado, § preciso reiniciar o servidor para aplicar o novo local.");
				} else {
					commandSender.sendMessage("§cHolograma inv§lido.");
				}
			} else {
				sendHelp(commandSender);
			}
		} else {
			sendHelp(commandSender);
		}
	}
	
	public boolean isValidHologram(String name) {
		if (name.equalsIgnoreCase("ranking")) {
			return true;
		}
		if (name.equalsIgnoreCase("winstreak")) {
			return true;
		}
		if (name.equalsIgnoreCase("Wins")) {
			return true;
		}
		return false;
	}
	
	private void sendHelp(BukkitCommandSender commandSender) {
		commandSender.sendMessage("");
		commandSender.sendMessage("§cUse: /setloc spawn");
		commandSender.sendMessage("§cUse: /setloc hologramas <Ranking/Winstreak/Wins>");
		commandSender.sendMessage("");
	}
	
	@Command(name = "build", aliases= {"b"}, groupsToUse= {Groups.ADMIN})
	public void build(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
		Player player = commandSender.getPlayer();
		if (autorizados.contains(player.getUniqueId())) {
			autorizados.remove(player.getUniqueId());
			player.sendMessage("§aVocê desativou o modo construção.");
			player.setGameMode(GameMode.ADVENTURE);
		} else {
			autorizados.add(player.getUniqueId());
			player.sendMessage("§aVocê ativou o modo construção.");
			player.setGameMode(GameMode.CREATIVE);
		}
	}
}