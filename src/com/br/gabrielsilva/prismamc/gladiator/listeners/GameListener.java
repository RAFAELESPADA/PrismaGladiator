package com.br.gabrielsilva.prismamc.gladiator.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.account.BukkitPlayer;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.player.PlayerAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.UpdateEvent;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.UpdateEvent.UpdateType;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.data.category.DataCategory;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.PluginMessages;
import com.br.gabrielsilva.prismamc.commons.custompackets.BukkitClient;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets.PacketBungeeUpdateField;
import com.br.gabrielsilva.prismamc.gladiator.Gladiator;
import com.br.gabrielsilva.prismamc.gladiator.manager.battle.types.GladiatorSolo;

public class GameListener implements Listener {
	
	public static int seconds = 0;
	
	private final int MEMBERS_SLOTS = 100;
	
	@EventHandler
	public void updateServer(UpdateEvent event) {
		if (event.getType() != UpdateType.SEGUNDO) {
			return;
		}
		
		if (seconds == 5) {
			Core.getServersHandler().sendUpdateNetworkServer("gladiator", 
					Bukkit.getOnlinePlayers().size(), Bukkit.getMaxPlayers());
			seconds = 0;
		}
		seconds++;
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onLogin(PlayerLoginEvent event) {
		if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
			return;
		}
		
		final Player player = event.getPlayer();
		
		if (Bukkit.getOnlinePlayers().size() >= MEMBERS_SLOTS) {
			if (!player.hasPermission("gladiator.entrar")) {
				event.disallow(Result.KICK_OTHER, "§cOs Slots para membros acabaram, compre VIP e tenha slot reservado.");
				BukkitMain.getManager().getDataManager().removeBukkitPlayerIfExists(player.getUniqueId());
				return;
			}
		}
		
		if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
			return;
		}

		if (!BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).getDataHandler().load(DataCategory.GLADIATOR)) {
			event.disallow(Result.KICK_OTHER, "§cOcorreu um erro ao tentar carregar suas informa§§es...");
			return;
		}
	}
	
	@EventHandler
	public void joinEvent(PlayerJoinEvent event) {
		Gladiator.getManager().getGameManager().refreshPlayer(event.getPlayer());
	}
	
	@EventHandler
	public void onFastRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		
		event.setRespawnLocation(Gladiator.getSpawn());
		
		int arenaID = Gladiator.getManager().getGameManager().getSala(player.getUniqueId());
			
		Gladiator.runLater(() -> {
			if (!player.isOnline()) {
				return;
			}
			
			Gladiator.getManager().getGameManager().refreshPlayer(player);
		}, 5L);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onDeath(PlayerDeathEvent event) {
		event.setDeathMessage(null);
		event.getDrops().clear();

		Player morreu = event.getEntity();
		
		int arenaID = Gladiator.getManager().getGameManager().getSala(morreu.getUniqueId());
		
		if (arenaID != 0) {
			GladiatorSolo gladiatorArena = Gladiator.getManager().getGameManager().getBatalhaSoloStatus(arenaID);
			
			final UUID uuidWinner = gladiatorArena.getOutroPlayer(morreu.getUniqueId());
			gladiatorArena.cancelGlad();
				
			Player ganhador = Bukkit.getPlayer(uuidWinner);
			morreu.sendMessage("§cVoce perdeu a batalha contra " + ganhador.getName());
				
			handleStats(morreu, ganhador);
			
			Gladiator.getManager().getGameManager().refreshPlayer(ganhador);
		}
		
		morreu.spigot().respawn();
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onQuit(PlayerQuitEvent event) {
		Player quitou = event.getPlayer();
		
		int arenaID = Gladiator.getManager().getGameManager().getSala(quitou.getUniqueId());
		
		if (arenaID != 0) {
			GladiatorSolo gladiatorArena = Gladiator.getManager().getGameManager().getBatalhaSoloStatus(arenaID);
				
			Player ganhador = Bukkit.getPlayer(gladiatorArena.getOutroPlayer(quitou.getUniqueId()));
			gladiatorArena.cancelGlad();
				
			handleStats(quitou, ganhador);
			Gladiator.getManager().getGameManager().refreshPlayer(ganhador);
		}
	}
	
	private void handleStats(Player loser, Player winner) {
		BukkitPlayer bukkitPlayerLoser = BukkitMain.getManager().getDataManager().getBukkitPlayer(loser.getUniqueId());
		bukkitPlayerLoser.removeXP(PlayerAPI.DEATH_XP);
		bukkitPlayerLoser.getData(DataType.COINS).remove(PlayerAPI.DEATH_COINS);
		bukkitPlayerLoser.getData(DataType.GLADIATOR_LOSES).add();
		
		checkKillStreakLose(bukkitPlayerLoser.getInt(DataType.GLADIATOR_WINSTREAK), winner.getName(), loser.getName());
		
		bukkitPlayerLoser.getData(DataType.GLADIATOR_WINSTREAK).setValue(0);
		
		bukkitPlayerLoser.getDataHandler().updateValues(DataCategory.GLADIATOR,
				DataType.GLADIATOR_WINSTREAK, DataType.GLADIATOR_LOSES);
		
		bukkitPlayerLoser.getDataHandler().updateValues(DataCategory.PRISMA_PLAYER,
				DataType.COINS, DataType.XP);
		//
		
		BukkitPlayer bukkitPlayerWinner = BukkitMain.getManager().getDataManager().getBukkitPlayer(winner.getUniqueId());
		
		winner.sendMessage("§aVoc§ ganhou a batalha contra §7" + loser.getName());
		final int xp = PlayerAPI.getXPKill(winner, loser);
		winner.sendMessage("§6+ " + PlayerAPI.KILL_COINS + " moedas");
		
		bukkitPlayerWinner.addXP(xp);
		bukkitPlayerWinner.getData(DataType.COINS).add(PlayerAPI.KILL_COINS);
		bukkitPlayerWinner.getData(DataType.GLADIATOR_WINS).add();
		
		int atualKillStreak = bukkitPlayerWinner.getData(DataType.GLADIATOR_WINSTREAK).getInt() + 1;
		bukkitPlayerWinner.getData(DataType.GLADIATOR_WINSTREAK).setValue(atualKillStreak);
		if (atualKillStreak > bukkitPlayerWinner.getData(DataType.GLADIATOR_MAXWINSTREAK).getInt()) {
			bukkitPlayerWinner.getData(DataType.GLADIATOR_MAXWINSTREAK).setValue(atualKillStreak);
		}
		
		bukkitPlayerWinner.getDataHandler().updateValues(DataCategory.GLADIATOR,
				DataType.GLADIATOR_WINS, DataType.GLADIATOR_WINSTREAK, DataType.GLADIATOR_MAXWINSTREAK);
		
		bukkitPlayerWinner.getDataHandler().updateValues(DataCategory.PRISMA_PLAYER,
				DataType.COINS, DataType.XP);
		
		checkKillStreakWin(winner.getName(), atualKillStreak);
		
		handleClanElo(winner, loser,
				bukkitPlayerWinner.getString(DataType.CLAN), bukkitPlayerLoser.getString(DataType.CLAN), bukkitPlayerWinner.getNick());
	}
	
	private static void handleClanElo(Player winner, Player loser, String clanWin, String clanLoser, String nick) {
		if (!clanWin.equalsIgnoreCase("Nenhum")) {
			if (!clanLoser.equalsIgnoreCase(clanWin)) {
				BukkitClient.sendPacket(winner, new PacketBungeeUpdateField(nick, "Clan", "AddElo", clanWin, "10"));

				winner.sendMessage(PluginMessages.CLAN_WIN_ELO.replace("%quantia%", "10"));
			}
		}
		
		if (!clanLoser.equalsIgnoreCase("Nenhum")) {
			if (!clanLoser.equalsIgnoreCase(clanWin)) {
				BukkitClient.sendPacket(winner, new PacketBungeeUpdateField(nick, "Clan", "RemoveElo", clanLoser, "5"));
				if (loser != null && loser.isOnline()) {
					loser.sendMessage(PluginMessages.CLAN_LOSE_ELO.replace("%quantia%", "5"));
				}
			}
		}
	}
	
	private void checkKillStreakWin(String nick, int winstreak) {
        if (winstreak >= 10 && winstreak % 10 == 0) {
        	Bukkit.broadcastMessage(PluginMessages.PLAYER_ALCANÇOU_WINSTREAK.replace("%nick%", nick).replace("%valor%", "" + winstreak));
        }
    }
	
	private void checkKillStreakLose(int winstreak, String winner, String loser) {
		if (winstreak >= 10) {
			Bukkit.broadcastMessage(PluginMessages.PLAYER_PERDEU_WINSTREAK.replace("%perdedor%", loser).
					replace("%matou%", winner).replace("%valor%", "" + winstreak));
		}
	}
}