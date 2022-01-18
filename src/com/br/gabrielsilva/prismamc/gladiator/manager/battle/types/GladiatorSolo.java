package com.br.gabrielsilva.prismamc.gladiator.manager.battle.types;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.br.gabrielsilva.prismamc.gladiator.Gladiator;
import com.br.gabrielsilva.prismamc.gladiator.manager.battle.BattleType;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GladiatorSolo {

	private Block mainBlock;
	private Player player1, player2;
	private int tempo, arenaID;
	private boolean cancelar;
	private Location local;
	
	public GladiatorSolo(int arenaID, Player p1, Player p2) {
		this.player1 = p1;
		this.player2 = p2;
		this.tempo = 0;
		this.arenaID = arenaID;
		this.cancelar = false;
		
		setLocal();
	}
	
	public void check() {
		if (cancelar) {
			return;
		}
		
		Gladiator.getManager().getScoreboardManager().updateScoreBoard(player1, arenaID);
		Gladiator.getManager().getScoreboardManager().updateScoreBoard(player2, arenaID);
		
		if (player1.getLocation().getBlockY() > 114) {
			player1.teleport(new Location(player1.getWorld(), mainBlock.getX(), mainBlock.getY() + 3.5D, mainBlock.getZ()));
		}
		
		if (player2.getLocation().getBlockY() > 114) {
			player2.teleport(new Location(player1.getWorld(), mainBlock.getX(), mainBlock.getY() + 3.5D, mainBlock.getZ()));
		}
		
		if (this.tempo % 180 == 0) {
			Gladiator.getManager().getGameManager().removeInside(mainBlock, BattleType.SOLO);
			removerItems();
		}
		this.tempo++;
	}
	
	public void removerItems() {
		for (Entity e : player1.getNearbyEntities(20.0D, 20.0D, 20.0D)) {
			 if (e instanceof Item) {
				 e.remove();
			 }
		}
	}
	
	public UUID getOutroPlayer(UUID uniqueId) {
		if (player1.getUniqueId() == uniqueId) {
			return player2.getUniqueId();
		} else {
			return player1.getUniqueId();
		}
	}
	
	public void teleportar() {
	    Location l1 = new Location(mainBlock.getWorld(), mainBlock.getX() + 6.5D, mainBlock.getY() + 1.5D, mainBlock.getZ() + 6.5D);
	    l1.setYaw(135.0F);
	    Location l2 = new Location(mainBlock.getWorld(), mainBlock.getX() - 5.5D, mainBlock.getY() + 1.5D, mainBlock.getZ() - 5.5D);
	    l2.setYaw(315.0F);
	    
	    player1.teleport(l1);
	    player2.teleport(l2);
	}
	
	public void cancelGlad() {
		this.cancelar = true;
		
		Gladiator.getManager().getGameManager().removeBattleSala(player1.getUniqueId());
		Gladiator.getManager().getGameManager().removeBattleType(player1.getUniqueId());
		
		Gladiator.getManager().getGameManager().removeBattleSala(player2.getUniqueId());
		Gladiator.getManager().getGameManager().removeBattleType(player2.getUniqueId());
		
		if (player1.isOnline()) {
			if (player1.hasPotionEffect(PotionEffectType.WITHER)) {
				player1.removePotionEffect(PotionEffectType.WITHER);
			}
			player1.setFallDistance(-5);
			player1.setNoDamageTicks(30);
			player1.setFireTicks(0);
			Gladiator.getManager().getScoreboardManager().createSideBar(player1);
		}
		
		if (player2.isOnline()) {
			if (player2.hasPotionEffect(PotionEffectType.WITHER)) {
				player2.removePotionEffect(PotionEffectType.WITHER); 
			}
			player2.setFallDistance(-5);
			player2.setNoDamageTicks(30);
			player2.setFireTicks(0);
			
			Gladiator.getManager().getScoreboardManager().createSideBar(player2);
		}
		
		Gladiator.getManager().getGameManager().removeArena(getMainBlock(), BattleType.SOLO);
		Gladiator.getManager().getGameManager().getBattleSoloList().remove("arena" + getArenaID());
		Gladiator.getManager().getGameManager().getSalasEmUso().remove("arena" + getArenaID());
	}
	
	public void setLocal() {
		Location newLocal = null;
		if (arenaID == 1) {
			newLocal = new Location(Bukkit.getWorld("world"), 20000, 100, 0);
		} else {
			newLocal = new Location(Bukkit.getWorld("world"), (20000 + ((arenaID -1) * 150)), 100, 0);
		}
		this.local = newLocal;
		this.mainBlock = newLocal.getBlock();
	}
}