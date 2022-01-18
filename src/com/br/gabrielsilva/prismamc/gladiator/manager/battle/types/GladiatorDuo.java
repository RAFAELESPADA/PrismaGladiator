package com.br.gabrielsilva.prismamc.gladiator.manager.battle.types;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.account.BukkitPlayer;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.player.PlayerAPI;
import com.br.gabrielsilva.prismamc.commons.core.data.category.DataCategory;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.gladiator.Gladiator;
import com.br.gabrielsilva.prismamc.gladiator.manager.battle.BattleType;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GladiatorDuo {
	
	private int batalhaID, tempo;
	private Location local;
	private Block mainBlock;
	private BattleType battleType;
	private ArrayList<UUID> Quitou, Espectadores, dupla1, dupla2, time1mortos, time2mortos;
	private ArrayList<String> nicksDupla1, nicksDupla2;
	private boolean cancelar;
	
	public GladiatorDuo(int batalhaID, Player P1T1, Player P2T1, Player P1T2, Player P2T2) {
		this.tempo = 0;
		this.batalhaID = batalhaID;
		this.Quitou = new ArrayList<>();
		this.time1mortos = new ArrayList<>();
		this.dupla1 = new ArrayList<>();
		this.dupla2 = new ArrayList<>();
		this.time2mortos = new ArrayList<>();
		this.nicksDupla1 = new ArrayList<>();
		this.nicksDupla2 = new ArrayList<>();
		this.Espectadores = new ArrayList<>();
		this.battleType = BattleType.DUPLA;
		this.cancelar = false;
		
		this.dupla1.add(P1T1.getUniqueId());
		this.dupla1.add(P2T1.getUniqueId());
		
		this.dupla2.add(P1T2.getUniqueId());
		this.dupla2.add(P2T2.getUniqueId());
		
		this.nicksDupla1.add(P1T1.getName());
		this.nicksDupla1.add(P2T1.getName());
		
		this.nicksDupla2.add(P1T2.getName());
		this.nicksDupla2.add(P2T2.getName());
	}

	public void tpToMainBlock(Player p) {
		p.teleport(new Location(p.getWorld(), mainBlock.getX(), mainBlock.getY() + 1.5, mainBlock.getZ()));
	}
	
	public void check() {
		this.tempo++;
		
		for (Player p : getPlayers()) {
			 if ((p != null) && (p.isOnline()) && (!Quitou.contains(p.getUniqueId()))) {
				  Gladiator.getManager().getScoreboardManager().updateScoreBoard(p, batalhaID);
				  checkLoc(p);
			 }
		}
		
		if (this.tempo % 300 == 0) {
			Gladiator.getManager().getGameManager().removeInside(mainBlock, battleType);
			removerItems();
		}
	}
	
	void checkLoc(Player p) {		
		if ((p.getLocation().getBlockY() > 114) || (p.getLocation().getBlockY() < 0))
			tpToMainBlock(p);
	}
	
	public void removerItems() {
		for (Player p : getPlayers()) {
			 if ((p != null) && (p.isOnline()) && (!Quitou.contains(p.getUniqueId()))) {
				 for (Entity e : p.getNearbyEntities(30.0D, 30.0D, 30.0D)) {
					  if (e instanceof Item)
					      e.remove();
				 }
				 break;
			 }
		}
	}
	
	public void teleportar() {
	    Location l1 = new Location(mainBlock.getWorld(), mainBlock.getX() + 10.5D, mainBlock.getY() + 1.5, mainBlock.getZ() + 10.5D);
	    l1.setYaw(135.0F);
	    
	    Location l2 = new Location(mainBlock.getWorld(), mainBlock.getX() - 10.5D, mainBlock.getZ() + 1.5, mainBlock.getZ() - 10.5D);
	    l2.setYaw(315.0F);
	    
	    for (UUID uuid1 : dupla1) {
	    	 Player time1 = Bukkit.getPlayer(uuid1);
	    	 time1.teleport(l1);
	    }
	    
	    for (UUID uuid2 : dupla2) {
	    	 Player time2 = Bukkit.getPlayer(uuid2);
	    	 time2.teleport(l2);
	    }
	}

	public void setLocal() {
		Location newLocal = null;
		if (batalhaID == 1) {
			newLocal = new Location(Bukkit.getWorld("world"), 0, 100, 300);
		} else {
			newLocal = new Location(Bukkit.getWorld("world"), 0, 100, (300 + ((batalhaID -1) * 300)));
		}
		this.local = newLocal;
		this.mainBlock = local.getBlock();
	}
	
	public String getAdversario1(int time) {
		if (time == 1) {
			return nicksDupla2.get(0);
		} else {
			return nicksDupla1.get(0);
		}
	}
	
	public String getAdversario2(int time) {
		if (time == 1) {
			return nicksDupla2.get(1);
		} else {
			return nicksDupla1.get(1);
		}
	}
	
	public void addTime1Mortos(Player p) {
		if (!time1mortos.contains(p.getUniqueId())) {
		    time1mortos.add(p.getUniqueId());
		}
		
		if (p.isOnline()) {
			tpToMainBlock(p);
		}
	}
	
	public void addQuit(UUID uuid) {
		Quitou.add(uuid);
	}
	
	public void addTime2Mortos(Player p) {
		if (!time2mortos.contains(p.getUniqueId())) {
		    time2mortos.add(p.getUniqueId());
		}
		
		if (p.isOnline()) {
			tpToMainBlock(p);
		}
	}
	
	public ArrayList<UUID> getUUIDS() {
		ArrayList<UUID> uuids = new ArrayList<>();
		uuids.addAll(dupla1);
		uuids.addAll(dupla2);
		return uuids;
	}
	
	public ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<>();
		
		for (UUID uuid : getUUIDS())
			 players.add(Bukkit.getPlayer(uuid));
		
		return players;
	}
	
	public void checkWin() {
		int teamWinner = 0;
		if (time1mortos.size() == 2) {
			teamWinner = 2;
		} else if (time2mortos.size() == 2) {
			teamWinner = 1;
		}
		
		if (teamWinner != 0) {
			Random random = new Random();
			
			for (UUID uuid : dupla1) {
				 if (Quitou.contains(uuid)) {
					 continue;
				 }
				 Player target = Bukkit.getPlayer(uuid);
				 if ((target == null) || (!target.isOnline())) {
					 return;
				 }
				 
				 if (teamWinner == 1) {
					 final int coins = PlayerAPI.KILL_COINS;
					 
					 target.sendMessage("§aVocê ganhou a batalha 2v2 contra: " + getAdversario1(1) + " e " + getAdversario2(1));
					 target.sendMessage("§6+" + coins + " moedas");
					 
					 BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(target.getUniqueId());
					 bukkitPlayer.getDataHandler().getData(DataType.COINS).add(coins);
					 bukkitPlayer.getDataHandler().updateValues(DataCategory.PRISMA_PLAYER, DataType.COINS);
				 } else {
					 BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(target.getUniqueId());
					 bukkitPlayer.getDataHandler().getData(DataType.COINS).remove(PlayerAPI.DEATH_COINS);
					 bukkitPlayer.getDataHandler().updateValues(DataCategory.PRISMA_PLAYER, DataType.COINS);
					 target.sendMessage("§aVocê perdeu a batalha 2v2 contra: " + getAdversario1(1) + " e " + getAdversario2(1));
				 }
			}
			
			for (UUID uuid : dupla2) {
				 if (Quitou.contains(uuid)) {
					 continue;
				 }
				 Player target = Bukkit.getPlayer(uuid);
				 if ((target == null) || (!target.isOnline())) {
					 return;
				 }
				 
				 if (teamWinner == 2) {
					 final int coins = PlayerAPI.KILL_COINS;
					 
					 target.sendMessage("§aVocê ganhou a batalha 2v2 contra: " + getAdversario1(2) + " e " + getAdversario2(2));
					 target.sendMessage("§6+" + coins + " moedas");
					 
					 BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(target.getUniqueId());
					 bukkitPlayer.getDataHandler().getData(DataType.COINS).add(coins);
					 bukkitPlayer.getDataHandler().updateValues(DataCategory.PRISMA_PLAYER, DataType.COINS);
				 } else {
					 BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(target.getUniqueId());
					 bukkitPlayer.getDataHandler().getData(DataType.COINS).remove(PlayerAPI.DEATH_COINS);
					 bukkitPlayer.getDataHandler().updateValues(DataCategory.PRISMA_PLAYER, DataType.COINS);
					 target.sendMessage("§aVocê perdeu a batalha 2v2 contra: " + getAdversario1(2) + " e " + getAdversario2(2));
				 }
			}
			
			cancelGlad();
		}
	}
	
	public int getTeam(UUID uuid) {
		if (dupla1.contains(uuid)) {
			return 1;
		} else if (dupla2.contains(uuid)) {
			return 2;
		}
		return 0;
	}
	
	public boolean isMesmoTime(UUID p1, UUID p2) {
		if ((this.dupla1.contains(p1)) && (this.dupla1.contains(p2))) {
			return true;
		} else if ((this.dupla2.contains(p1)) && (this.dupla2.contains(p2))) {
			return true;
		} else {
			return false;
		}
	}
	
	public void cancelGlad() {
		removerItems();
		
		for (Player p : getPlayers()) {
			 if ((p != null) && (p.isOnline()) && (!Quitou.contains(p.getUniqueId()))) {
				  Gladiator.getManager().getGameManager().removeBattleType(p.getUniqueId());
				  Gladiator.getManager().getGameManager().removeBattleSala(p.getUniqueId());
				  
				  for (Player p1 : getPlayers()) {
					   if ((p1 != null) && (p1.isOnline()) && (!Quitou.contains(p1.getUniqueId()))) {
					        p.showPlayer(p1);
					        p1.showPlayer(p1);
					   }
				  }
				  
				  Gladiator.getManager().getGameManager().refreshPlayer(p);
			 }
		}
		
		cancelar = true;
	}
	
	public void addEspectador(UUID uuid) {
		if (!Espectadores.contains(uuid)) {
			Espectadores.add(uuid);
		}
	}
	
	public boolean isEspectador(UUID uuid) {
		return Espectadores.contains(uuid);
	}
	
	public boolean hasQuited(UUID uuid) {
		return Quitou.contains(uuid);
	}
}