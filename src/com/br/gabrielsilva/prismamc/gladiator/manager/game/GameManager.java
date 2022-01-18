package com.br.gabrielsilva.prismamc.gladiator.manager.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.br.gabrielsilva.prismamc.commons.bukkit.api.itembuilder.ItemBuilder;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.player.PlayerAPI;
import com.br.gabrielsilva.prismamc.gladiator.Gladiator;
import com.br.gabrielsilva.prismamc.gladiator.listeners.InviteListener;
import com.br.gabrielsilva.prismamc.gladiator.manager.battle.BattleType;
import com.br.gabrielsilva.prismamc.gladiator.manager.battle.types.GladiatorDuo;
import com.br.gabrielsilva.prismamc.gladiator.manager.battle.types.GladiatorSolo;

import lombok.Getter;

public class GameManager {

	private ItemStack machado = new ItemBuilder().material(Material.STONE_AXE).name("§fMachado de pedra").build(),
	picareta = new ItemBuilder().material(Material.STONE_PICKAXE).name("§fPicareta de Pedra").build(),
	pote = new ItemBuilder().material(Material.BOWL).amount(64).build(),
	espada = new ItemBuilder().material(Material.DIAMOND_SWORD).name("§fEspada de Diamante").addEnchant(Enchantment.DAMAGE_ALL).build(),
	muro = new ItemBuilder().material(Material.COBBLE_WALL).amount(64).build(),
	sopa = new ItemBuilder().material(Material.MUSHROOM_SOUP).build(),
	cocoa = new ItemBuilder().material(Material.INK_SACK).durability(3).amount(64).name("§fCacau").build(),
	madeira = new ItemBuilder().material(Material.WOOD).amount(64).build(),
	agua = new ItemBuilder().material(Material.WATER_BUCKET).name("§fBalde de água").build(),
	lava = new ItemBuilder().material(Material.LAVA_BUCKET).name("§fBalde de lava").build(),
	capacete = new ItemBuilder().material(Material.IRON_HELMET).name("§fCapacete de Ferro").build(),
	peitoral = new ItemBuilder().material(Material.IRON_CHESTPLATE).name("§fPeitoral de Ferro").build(),
	calça = new ItemBuilder().material(Material.IRON_LEGGINGS).name("§fCalça de Ferro").build(),
	bota = new ItemBuilder().material(Material.IRON_BOOTS).name("§fBota de Ferro").build();
	
	private Integer[] soupSlots = { 4, 5, 6, 7, 29, 30, 31, 32, 33, 34, 35};
	private Integer[] cocoaSlots = { 14, 15, 16, 23, 24, 25};
	
	private HashMap<UUID, Integer> batalhaSala;
	private HashMap<UUID, BattleType> batalhaTipo;
	
	@Getter
	private List<String> salasEmUso;
	
	@Getter
	private ConcurrentHashMap<String, GladiatorSolo> battleSoloList;
	
	private ConcurrentHashMap<String, GladiatorDuo> battleDuplaList;
	
	public GameManager() {
		this.salasEmUso = new ArrayList<>();
		
		this.batalhaSala = new HashMap<>();
		this.batalhaTipo = new HashMap<>();
		
		this.battleSoloList = new ConcurrentHashMap<>();
		this.battleDuplaList = new ConcurrentHashMap<>();
	}
	
	public void addBattle(Player player1, Player player2) {
		int id = getSalaDisponivel();
		if ((salasEmUso.contains("arena" + id)) || (id == 0)) {
			 player1.sendMessage("§cOcorreu um erro ao obter uma sala, tente novamente.");
			 player2.sendMessage("§cOcorreu um erro ao obter uma sala, tente novamente.");
			 return;
		}
		salasEmUso.add("arena" + id);
		player1.hidePlayer(player2);
		player2.hidePlayer(player1);
		
		if (InviteListener.x1rapido.contains(player1)) {
			InviteListener.x1rapido.remove(player1);
		}
		
		if (InviteListener.x1rapido.contains(player2)) {
			InviteListener.x1rapido.remove(player2);
		}
		
		GladiatorSolo gladiatorArena = new GladiatorSolo(id, player1, player2);
		
		removeArena(gladiatorArena.getMainBlock(), BattleType.SOLO);
		createArena(gladiatorArena.getLocal(), BattleType.SOLO);
		
		Gladiator.runLater(() -> {
			gladiatorArena.teleportar();
			
			battleSoloList.put("arena" + id, gladiatorArena);
			batalhaSala.put(player1.getUniqueId(), id);
			batalhaSala.put(player2.getUniqueId(), id);
			batalhaTipo.put(player1.getUniqueId(), BattleType.SOLO);
		    batalhaTipo.put(player2.getUniqueId(), BattleType.SOLO);
			
		   	setGladInventory(player1);
		    setGladInventory(player2);
		    	
	    	InviteListener.removeUtils(player1);
	    	InviteListener.removeUtils(player2);
	    	
			Gladiator.getManager().getScoreboardManager().createSideBar(player1);
			Gladiator.getManager().getScoreboardManager().createSideBar(player2);
			
			player1.sendMessage("§aVoc§ est§ batalhando contra " + player2.getName());
			player2.sendMessage("§aVoc§ est§ batalhando contra " + player1.getName());
			
			Gladiator.runLater(() -> {
				player1.showPlayer(player2);
				player2.showPlayer(player1);
			}, 5);
		}, 2L);
	}
	
	public void addBattleDupla1(Player player1, Player player2, Player player3, Player player4) {
		int id = getSalaDisponivel();
		if ((salasEmUso.contains("arena" + id)) || (id == 0)) {
			 return;
		}
		salasEmUso.add("arena" + id);
		
		ArrayList<Player> players = new ArrayList<>();
		players.add(player1);
		players.add(player2);
		players.add(player3);
		players.add(player4);
		
		for (Player p : players) {
			 if (InviteListener.x1rapido.contains(p)) {
				 InviteListener.x1rapido.remove(p);
			 }
			 if (InviteListener.x2rapido.contains(p)) {
				 InviteListener.x2rapido.remove(p);
			 }
			 
			 batalhaSala.put(p.getUniqueId(), id);
			 batalhaTipo.put(p.getUniqueId(), BattleType.DUPLA);
		}
		
		GladiatorDuo gladiatorArena = new GladiatorDuo(id, player1, player2, player3, player4);
		gladiatorArena.setLocal();
		
		removeArena(gladiatorArena.getMainBlock(), BattleType.DUPLA);
		
		createArena(gladiatorArena.getLocal(), BattleType.DUPLA);
		gladiatorArena.teleportar();
		battleDuplaList.put("arena" + id, gladiatorArena);
		
		for (Player p : players) {
			 setGladInventory(p); 
			 Gladiator.getManager().getScoreboardManager().createSideBar(p);
		}
		
		//startTaskTimer(id, BattleType.DUPLA);
	}
	
	private void setGladInventory(Player player) {
		player.setGameMode(GameMode.SURVIVAL);
		
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);

		player.getInventory().setItem(0, espada);
		player.getInventory().setItem(1, agua);
		player.getInventory().setItem(2, lava);
		player.getInventory().setItem(3, madeira);

		player.getInventory().setItem(8, muro);
		player.getInventory().setItem(9, capacete);
		player.getInventory().setItem(10, peitoral);
		player.getInventory().setItem(11, calça);
		player.getInventory().setItem(12, bota);
		player.getInventory().setItem(17, machado);
		player.getInventory().setItem(18, capacete);
		player.getInventory().setItem(19, peitoral);
		player.getInventory().setItem(20, calça);
		player.getInventory().setItem(21, bota);
		player.getInventory().setItem(26, picareta);
		player.getInventory().setItem(27, lava);
		player.getInventory().setItem(28, lava);

		player.getInventory().setHelmet(capacete);
		player.getInventory().setChestplate(peitoral);
		player.getInventory().setLeggings(calça);
		player.getInventory().setBoots(bota);
		
		player.getInventory().setItem(13, pote);
		player.getInventory().setItem(22, pote);
		
		for (Integer slot : soupSlots) {
			 player.getInventory().setItem(slot, sopa);
		}
		
		for (Integer slot : cocoaSlots) {
		 	 player.getInventory().setItem(slot, cocoa);
		}
		player.updateInventory();
	}

	private int getSalaDisponivel() {
		int sala = 0;
		for (int i = 1; i <= 50; i++) {
			 if (!salasEmUso.contains("arena" + i)) {
				  sala = i;
				  break;
			 }
		}
		return sala;
	}

	
	public void createArena(Location loc, BattleType battleType) {
		int amount = 8;
		
		if (battleType == BattleType.DUPLA) {
			amount = 12;
		}
		
		Random r = new Random();
		
		List<Location> cuboid = new ArrayList<>();
		for (int bX = -amount; bX <= amount; bX++) {
			for (int bZ = -amount; bZ <= amount; bZ++) {
				for (int bY = -1; bY <= 16; bY++) {
					if (bY == 16) {
						cuboid.add(loc.clone().add(bX, bY, bZ));
					} else if (bY == -1) {
						cuboid.add(loc.clone().add(bX, bY, bZ));
					} else if ((bX == -amount) || (bZ == -amount)
							|| (bX == amount) || (bZ == amount)) {
						cuboid.add(loc.clone().add(bX, bY, bZ));
					}
				}
			}
		}

		for (Location loc1 : cuboid) {
		     loc1.getBlock().setType(Material.GLASS);
		     loc1.getBlock().setData((byte) 0);
		}

		cuboid.clear();
	}

	public void removeArena(Block mainBlock, BattleType battleType) {
		int amount = 8;
		
		if (battleType == BattleType.DUPLA) {
			amount = 12;
		}
		
		for (int x = -amount; x <= amount; x += 1) {
			 for (int z = -amount; z <= amount; z += 1) {
				  for (int y = 99; y <= 116; y += 1) {
					   Location l = new Location(mainBlock.getWorld(), mainBlock.getX() + x, y, mainBlock.getZ() + z);
					   l.getBlock().setType(Material.AIR);
				  }
			 }
		}
	}

	public void removeInside(Block mainBlock, BattleType battleType) {
		int amount = 7;
		
		if (battleType == BattleType.DUPLA) {
			amount = 11;
		}
		
		for (int x = -amount; x <= amount; x += 1) {
			 for (int z = -amount; z <= amount; z += 1) {
				  for (int y = 100; y <= 115; y += 1) {
				       Location l = new Location(mainBlock.getWorld(), mainBlock.getX() + x, y, mainBlock.getZ() + z);
					   l.getBlock().setType(Material.AIR);
				  }
			 }
		}
	}
	
	public void refreshPlayer(Player player) {
		player.setHealth(20.0D);
		player.setFireTicks(0);
		PlayerAPI.clearEffects(player);
		
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		
		player.getInventory().setHeldItemSlot(1);
		
		player.getInventory().setItem(2, new ItemBuilder().material(Material.IRON_FENCE).name("§aBatalha Normal").build());
		
		player.getInventory().setItem(4, new ItemBuilder().material(Material.INK_SACK).name("§aBatalha Rápida 1v1")
				.durability(8).build());
		
		player.getInventory().setItem(6, new ItemBuilder().material(Material.getMaterial(356)).name("§aBatalha Rápida 2v2").build());
		
		player.updateInventory();
		
		player.teleport(Gladiator.getSpawn());
		
		Gladiator.getManager().getScoreboardManager().createSideBar(player);
		
		if (!player.getGameMode().equals(GameMode.ADVENTURE)) {
			player.setGameMode(GameMode.ADVENTURE);
		}
	}
	
	public BattleType getBattleType(UUID uuid) {
		return batalhaTipo.get(uuid);
	}
	
	public int getSala(UUID uuid) {
		if (!batalhaSala.containsKey(uuid)) {
			return 0;
		}
		return batalhaSala.get(uuid);
	}
	
	public GladiatorSolo getBatalhaSoloStatus(int sala) {
		return battleSoloList.get("arena" + sala);
	}
	
	public GladiatorDuo getBatalhaDuplaStatus(int sala) {
		return battleDuplaList.get("arena" + sala);
	}
	
	public void cancelAllBattles() {
		if (battleSoloList.size() != 0) {
			for (GladiatorSolo battle : battleSoloList.values()) {
				 battle.cancelGlad();
			}
		}
		if (battleDuplaList.size() != 0) {
			for (GladiatorDuo battle : battleDuplaList.values()) {
				 battle.cancelGlad();
			}
		}
	}
	
	public void removeBattleType(UUID uuid) {
		this.batalhaTipo.remove(uuid);
	}
	
	public void removeBattleSala(UUID uuid) {
		this.batalhaSala.remove(uuid);
	}
}