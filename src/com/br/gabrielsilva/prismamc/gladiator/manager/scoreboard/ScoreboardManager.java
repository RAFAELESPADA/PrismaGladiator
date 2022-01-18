package com.br.gabrielsilva.prismamc.gladiator.manager.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.account.BukkitPlayer;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.scoreboard.Sidebar;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.scoreboard.animation.WaveAnimation;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.core.rank.PlayerRank;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.StringUtils;
import com.br.gabrielsilva.prismamc.commons.core.utils.system.DateUtils;
import com.br.gabrielsilva.prismamc.gladiator.Gladiator;
import com.br.gabrielsilva.prismamc.gladiator.manager.battle.BattleType;
import com.br.gabrielsilva.prismamc.gladiator.manager.battle.types.GladiatorSolo;

public class ScoreboardManager {

	private WaveAnimation waveAnimation;
	private String text = "";
	
	public void init() {
		waveAnimation = new WaveAnimation(" GLADIATOR ", "§f§l", "§b§l", "§3§l", 3);
		text = waveAnimation.next();
		
		Bukkit.getScheduler().runTaskTimer(Gladiator.getInstance(), new Runnable() {
			public void run() {
				text = waveAnimation.next();
				
				for (Player onlines : Bukkit.getOnlinePlayers()) {
					 if (onlines == null) {
						 continue;
					 }
					 if (!onlines.isOnline()) {
						 continue;
					 }
					 if (onlines.isDead()) {
						 continue;
					 }
				 	 Scoreboard score = onlines.getScoreboard();
					 if (score == null) {
						 continue;
					 }
					 Objective objective = score.getObjective(DisplaySlot.SIDEBAR);
					 if (objective == null) {
						 continue;
					 }
					 objective.setDisplayName(text);
				}
			}
		}, 40, 2L);
	}
	
	public void createSideBar(Player player) {
		if (player == null) {
			return;
		}
		if (!player.isOnline()) {
			return;
		}
		BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId());
		
		Sidebar sideBar = bukkitPlayer.getSideBar();
		if (sideBar == null) {
			bukkitPlayer.setSideBar(sideBar = new Sidebar(player.getScoreboard()));
			sideBar.show();
		}
		if (sideBar.isHided())
			return;
		
		sideBar.hide();
		sideBar.show();
		sideBar.setTitle("§b§lGLADIATOR");
		
		int sala = Gladiator.getManager().getGameManager().getSala(player.getUniqueId());
		
		if (sala == 0) {
			sideBar.setText(9, "");
			sideBar.setText(8, "Vitórias: §e0");
			sideBar.setText(7, "Derrotas: §e0");
			sideBar.setText(6, "");
			sideBar.setText(5, "Rank: ...");
			sideBar.setText(4, "");
			sideBar.setText(3, "WinStreak: §a0");
		} else {
			if (Gladiator.getManager().getGameManager().getBattleType(player.getUniqueId()) == BattleType.SOLO) {
				sideBar.setText(7, "");
				sideBar.setText(6, "Duração: §b0m 0s");
				sideBar.setText(5, "");
				sideBar.setText(4, "§eAdversario:");
				sideBar.setText(3, "...");
			} else {
				sideBar.setText(8, "");
				sideBar.setText(7, "Duração: §b0m 0s");
				sideBar.setText(6, "");
				sideBar.setText(5, "§eAdversários:");
				sideBar.setText(4, "...");
				sideBar.setText(3, "...");
			}
		}
		
		sideBar.setText(2, "");
		sideBar.setText(1, "§ekombopvp.net");
		updateScoreBoard(player, sala);
	}
	
	public void updateScoreBoard(Player player, int arenaID) {
		if (player == null) {
			return;
		}
		if (!player.isOnline()) {
			return;
		}
		
		final BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId());
		
		Sidebar sideBar = bukkitPlayer.getSideBar();
		
		if (sideBar == null) {
			return;
		}
		if (sideBar.isHided()) {
			return;
		}
		
		if (arenaID == 0) {
			final PlayerRank playerRank = bukkitPlayer.getRank();
			sideBar.setText(8, "Vitórias: §a" + StringUtils.reformularValor(bukkitPlayer.getInt(DataType.GLADIATOR_WINS)));
			sideBar.setText(7, "Derrotas: §c" + StringUtils.reformularValor(bukkitPlayer.getInt(DataType.GLADIATOR_LOSES)));
			sideBar.setText(5, "Rank: §7[" + playerRank.getCor() + playerRank.getSimbolo() + "§7] " + playerRank.getCor()  + playerRank.getNome());
			sideBar.setText(3, "WinStreak: §6" + StringUtils.reformularValor(bukkitPlayer.getInt(DataType.GLADIATOR_WINSTREAK)));
		} else {
			final GladiatorSolo glad = Gladiator.getManager().getGameManager().getBatalhaSoloStatus(arenaID);
			sideBar.setText(6, "Duração: §b" + DateUtils.formatarSegundos2(glad.getTempo()));
			sideBar.setText(3, "§7- " + Bukkit.getPlayer(glad.getOutroPlayer(player.getUniqueId())).getName());
		}
	}
}