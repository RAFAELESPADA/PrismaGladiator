package com.br.gabrielsilva.prismamc.gladiator.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import com.br.gabrielsilva.prismamc.commons.bukkit.api.player.VanishManager;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.AdminChangeEvent;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.PlayerGriefEvent;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.ScoreboardChangeEvent;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.UpdateEvent;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.ScoreboardChangeEvent.ChangeType;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.UpdateEvent.UpdateType;
import com.br.gabrielsilva.prismamc.gladiator.Gladiator;
import com.br.gabrielsilva.prismamc.gladiator.commands.ServerCommand;
import com.br.gabrielsilva.prismamc.gladiator.manager.battle.BattleType;
import com.br.gabrielsilva.prismamc.gladiator.manager.battle.types.GladiatorDuo;
import com.br.gabrielsilva.prismamc.gladiator.manager.battle.types.GladiatorSolo;
public class GeneralListeners implements Listener {

	@EventHandler
	public void onSecond(UpdateEvent event) {
		if (event.getType() != UpdateType.SEGUNDO) {
			return;
		}
		
		for (GladiatorSolo rooms : Gladiator.getManager().getGameManager().getBattleSoloList().values()) {
			if (!rooms.isCancelar()) {
				rooms.check();
			}
		}
	}
	
	@EventHandler
	public void onGrief(PlayerGriefEvent event) {
		if (Gladiator.getManager().getGameManager().getSala(event.getPlayer().getUniqueId()) != 0) {
			event.setCancelled(true);
		} else {
			event.setCancelled(false);
		}
	}
	@EventHandler
	public void onPlayerInteract2(PlayerInteractEvent event) {
		if (event.getAction() == Action.PHYSICAL) {
			Block block = event.getClickedBlock();
			if (block == null)
				return;
			if (block.getType() == Material.SOIL) {
				event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
				event.setCancelled(true);
				block.setTypeIdAndData(block.getType().getId(), block.getData(), true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerRegainHealth(EntityRegainHealthEvent event) {
	    if ((event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) || (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN)) {
	         event.setCancelled(true);
	    }
	}
	
	@EventHandler
	public void onAdminChange(AdminChangeEvent event) {
		if (Gladiator.getManager().getGameManager().getSala(event.getPlayer().getUniqueId()) != 0) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onChangeScoreboard(ScoreboardChangeEvent event) {
		if (event.getChangeType() == ChangeType.ATIVOU) {
			Gladiator.getManager().getScoreboardManager().createSideBar(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		final UUID uuid = event.getPlayer().getUniqueId();
		if (ServerCommand.autorizados.contains(uuid)) {
			ServerCommand.autorizados.remove(uuid);
		}
		InviteListener.removeUtils(event.getPlayer());
	}
	
	@EventHandler
	public void spread(BlockSpreadEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onFood(FoodLevelChangeEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onChuva(WeatherChangeEvent event) {
		if (event.toWeatherState()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void ignite(BlockIgniteEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onEntitySpawn(CreatureSpawnEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void pickUp(PlayerPickupItemEvent event) {
		if (VanishManager.inAdmin(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		
		int sala = Gladiator.getManager().getGameManager().getSala(event.getPlayer().getUniqueId());
		if (sala != 0) {
			if (Gladiator.getManager().getGameManager().getBattleType(event.getPlayer().getUniqueId()) == BattleType.DUPLA) {
				if (Gladiator.getManager().getGameManager().getBatalhaDuplaStatus(sala).isEspectador(event.getPlayer().getUniqueId())) {
					event.setCancelled(true);
				}
			}
		} else {
			event.setCancelled(true);
		}
	}

	@EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
        Player player = (Player)event.getEntity();
        if (Gladiator.getManager().getGameManager().getSala(player.getUniqueId()) != 0 && event.getCause().equals(DamageCause.VOID) || event.getCause().equals(DamageCause.FALL)) {
            int arenaID = Gladiator.getManager().getGameManager().getSala(player.getUniqueId());

            if (arenaID != 0) {
                GladiatorSolo gladiatorArena = Gladiator.getManager().getGameManager().getBatalhaSoloStatus(arenaID);
event.setCancelled(true);
                final UUID outroplayer = gladiatorArena.getOutroPlayer(player.getUniqueId());
                Player ganhador = Bukkit.getPlayer(outroplayer);
            player.teleport(ganhador);
        }
        }
        }
        }
	@EventHandler
	public void drop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if (Gladiator.getManager().getGameManager().getSala(player.getUniqueId()) != 0) {
			if (event.getItemDrop().getItemStack().getType().equals(Material.DIAMOND_SWORD)) {
				event.setCancelled(true);
				return;
			}
			event.setCancelled(false);
		} else {
			event.setCancelled(true);
			player.updateInventory();
			player.updateInventory();
		}
	}
	
	@EventHandler
	public void cancelInteracts(PlayerInteractEvent event) {
		if ((event.getClickedBlock() != null) && (event.getClickedBlock().getType().equals(Material.ANVIL))) {
			event.setCancelled(true);
			return;
		}
		if (event.getPlayer().getItemInHand().getType().equals(Material.BOAT)) {
			event.setCancelled(true);
			return;
		}
		if (event.getAction().equals(Action.PHYSICAL)) {
			if (Gladiator.getManager().getGameManager().getSala(event.getPlayer().getUniqueId()) == 0) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void damage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			
			int sala = Gladiator.getManager().getGameManager().getSala(player.getUniqueId());
			if (sala != 0) {
				if (Gladiator.getManager().getGameManager().getBattleType(player.getUniqueId()) == BattleType.DUPLA) {
					if (Gladiator.getManager().getGameManager().getBatalhaDuplaStatus(sala).isEspectador(player.getUniqueId())) {
						event.setCancelled(true);
					}
				}
			} else {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void dano(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if ((event.getEntity() instanceof Player) && (event.getDamager() instanceof Player)) {
			 final Player player = (Player) event.getEntity(),
					 damager = (Player) event.getDamager();
			
			 final int sala = Gladiator.getManager().getGameManager().getSala(player.getUniqueId()),
					 sala1 = Gladiator.getManager().getGameManager().getSala(damager.getUniqueId());
			 
			 if (sala == 0 || sala1 == 0) {
				 event.setCancelled(true);
				 return;
			 }
			 if (sala != sala1) {
				 event.setCancelled(true);
			 } else {
				 if (Gladiator.getManager().getGameManager().getBattleType(damager.getUniqueId()) == BattleType.DUPLA) {
					 GladiatorDuo gladiatorArena = Gladiator.getManager().getGameManager().getBatalhaDuplaStatus(sala);
					 if (gladiatorArena.isEspectador(damager.getUniqueId())) {
						 event.setCancelled(true);
						 return;
					 }
					 if (gladiatorArena.isMesmoTime(player.getUniqueId(), damager.getUniqueId())) {
						 event.setCancelled(true);
					 }
				 }
			 }
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onBreak(BlockBreakEvent event) {
		if ((event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) && (ServerCommand.autorizados.contains(event.getPlayer().getUniqueId()))) {
			 event.setCancelled(false);
			 return;
		}
		
		boolean check = false;
		
		int sala = Gladiator.getManager().getGameManager().getSala(event.getPlayer().getUniqueId());
		if (sala != 0) {
			if (Gladiator.getManager().getGameManager().getBattleType(event.getPlayer().getUniqueId()) == BattleType.DUPLA) {
				if (Gladiator.getManager().getGameManager().getBatalhaDuplaStatus(sala).isEspectador(event.getPlayer().getUniqueId())) {
					event.setCancelled(true);
				} else {
					check = true;
				}
			} else {
				check = true;
			}
		} else {
			event.setCancelled(true);
		}
		
		if (check) {
			if (event.getBlock().getType().equals(Material.STAINED_GLASS)) {
				event.setCancelled(true);
			} else if (event.getBlock().getType().equals(Material.GLASS)) {
				event.setCancelled(true);
			} else {
				event.setCancelled(false);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlace(BlockPlaceEvent event) {
		if ((event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) && (ServerCommand.autorizados.contains(event.getPlayer().getUniqueId()))) {
			 event.setCancelled(false);
			 return;
		}
		
		int sala = Gladiator.getManager().getGameManager().getSala(event.getPlayer().getUniqueId());
		if (sala == 0) {
			event.setCancelled(true);
		}
	}
}
