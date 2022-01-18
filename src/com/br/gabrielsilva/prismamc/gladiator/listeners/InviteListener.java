package com.br.gabrielsilva.prismamc.gladiator.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.br.gabrielsilva.prismamc.commons.bukkit.api.itembuilder.ItemBuilder;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.player.VanishManager;
import com.br.gabrielsilva.prismamc.gladiator.Gladiator;
import com.br.gabrielsilva.prismamc.gladiator.manager.game.InviteManager;

public class InviteListener implements Listener {
	
	public static HashMap<UUID, InviteManager> Convites = new HashMap<>();
	
	public static ArrayList<Player> x1rapido = new ArrayList<>(),
			x2rapido = new ArrayList<>();
	
	private static HashMap<UUID, Long> inviteCooldown = new HashMap<>();
	
	private ArrayList<Player> delay = new ArrayList<>();
	
    @EventHandler
	public void desafiar(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof Player) {
			event.setCancelled(true);
			
	        Player player = event.getPlayer(),
	        		clicado = (Player) event.getRightClicked();
	        
        	if ((VanishManager.inAdmin(player)) || (Gladiator.getManager().getGameManager().getSala(player.getUniqueId()) != 0)
        			|| (VanishManager.inAdmin(clicado))) {
        		return;
        	}
	        
	        if (player.getItemInHand().getType() == Material.IRON_FENCE) {
	    		if ((inviteCooldown.containsKey(player.getUniqueId())) &&
	    				(inviteCooldown.get(player.getUniqueId()) > System.currentTimeMillis())) {
	    			return;
	    		}
	        	if (Gladiator.getManager().getGameManager().getSala(clicado.getUniqueId()) != 0) {
	        		player.sendMessage("§cEste jogador j§ est§ em um Gladiator.");
	        		return;
	        	}
	        	
	        	if (x2rapido.contains(player)) {
	        		player.sendMessage("§cSaia da fila do 2v2!");
	        		return;
	        	}
	        	if (x2rapido.contains(clicado)) {
	        		player.sendMessage("§cO jogador est§ na fila do 2v2.");
	        		return;
	        	}
	        	
	        	if (recebeuConvite(player, clicado, ConviteTipo.NORMAL)) {
	        		removeUtils(player);
	        		removeUtils(clicado);
	        		
	        		Gladiator.getManager().getGameManager().addBattle(player, clicado);
	        	} else {
	        		Convites.put(player.getUniqueId(), new InviteManager(clicado.getUniqueId(), ConviteTipo.NORMAL));
	        		inviteCooldown.put(player.getUniqueId(), System.currentTimeMillis() + 5000L);
	        		player.sendMessage("§eVoc§ desafiou " + clicado.getName() + "!");
	            	clicado.sendMessage("§cVoc§ foi desafiado por " + player.getName() + " para um Gladiator Normal.");
	        	}
	        }
		}
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
    	if (event.getAction() == Action.PHYSICAL) {
    		event.setCancelled(true);
    		return;
    	}
    	Player player = event.getPlayer();
    	if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR && player.getItemInHand().hasItemMeta()) {
    		if (Gladiator.getManager().getGameManager().getSala(player.getUniqueId()) != 0) {
    			return;
    		}
    		event.setCancelled(true);
    		if (player.getItemInHand().getDurability() == 8) {
    			if (delay.contains(player)) {
    				player.sendMessage("§cAguarde para entrar na fila.");
    				return;
    			}
		    	if (x2rapido.contains(player)) {
		    		player.sendMessage("§cSaia da fila do 2v2.");
		    		return;
		    	}
    			player.sendMessage("§aBuscando oponente...");
    			player.getInventory().setItemInHand(new ItemBuilder().material(Material.INK_SACK).name("§aBatalha R§pida 1v1")
    					.durability(10).build());
    			x1rapido.add(player);
    			checkX1Rapido();
    			addDelay(player);
    		} else if (player.getItemInHand().getDurability() == 10) {
    			if (delay.contains(player)) {
    				player.sendMessage("§cAguarde para sair da fila.");
    				return;
    			}
		    	if (x2rapido.contains(player)) {
		    		player.sendMessage("§cSaia da fila do 2v2.");
		    		return;
		    	}
    			player.getInventory().setItemInHand(new ItemBuilder().material(Material.INK_SACK).name("§aBatalha R§pida 1v1")
    					.durability(8).build());
    			x1rapido.remove(player);
    			addDelay(player);
    		}
    		
    		if (player.getItemInHand().getType() == Material.getMaterial(356)) {
    			boolean a = true;
    			if (a) {
    				player.sendMessage("§c2v2 em manuten§§o.");
    				return;
    			}
    			
	    	    if (x1rapido.contains(player)) {
	    		    player.sendMessage("§cSaia da fila do 1v1.");
	    		    return;
	    	    }
	    	    if (x2rapido.contains(player)) {
					if (delay.contains(player)) {
						player.sendMessage("§cAguarde para sair da fila.");
						return;
					}
	 		    	player.getInventory().setItemInHand(new ItemBuilder().material(Material.getMaterial(356)).name("§aBatalha R§pida 2v2").build());
	    		    player.sendMessage("§aVoc§ saiu da fila do Gladiator R§pido 2v2.");
	    		    x2rapido.remove(player);
	    		    addDelay(player);
	    	    } else {
					if (delay.contains(player)) {
						player.sendMessage("§cAguarde para entrar na fila.");
						return;
					}
	    	    	player.getInventory().setItemInHand(new ItemBuilder().material(Material.getMaterial(356)).name("§aBatalha R§pida 2v2")
	    			.glow().build());
	    		    	
	    		    x2rapido.add(player);
	    		    player.sendMessage("§aVoce entrou na fila do Gladiator R§pido 2v2. (" + x2rapido.size() + "/4)");
	    		    checkX2Rapido();
	    		    addDelay(player);
	    	    }
    		}
    	}
    }
    
	public void checkX1Rapido() {
		if (x1rapido.size() != 1) {
			
	        Player player1 = (Player)x1rapido.get(0),
	        		player2 = (Player)x1rapido.get(1);
	        
	        x1rapido.clear();
            
            Gladiator.getManager().getGameManager().addBattle(player1, player2);
		}
	}

	public void checkX2Rapido() {
		if (x2rapido.size() == 4) {
			Player player1 = (Player)x2rapido.get(0), 
					player2 = (Player)x2rapido.get(1), 
					player3 = (Player)x2rapido.get(2), 
					player4 = (Player)x2rapido.get(3);
        
			//Gladiator.getManager().getGameManager().addBattleDupla(player1, player2, player3, player4);
			
			x2rapido.clear();
		}
	}
	
	public void addDelay(final Player player) {
    	delay.add(player);
    	Gladiator.runLater(() -> {
    		delay.remove(player);
    	}, 40);
    }
	
	public boolean recebeuConvite(Player p, Player d, ConviteTipo tipo) {
		return Convites.containsKey(d.getUniqueId()) &&
				Convites.get(d.getUniqueId()).getUuid().equals(p.getUniqueId()) && 
				Convites.get(d.getUniqueId()).getTipo().equals(tipo) &&
				Convites.get(d.getUniqueId()).isValid();
	}
	
	public enum ConviteTipo {
		NORMAL;
	}
	
	public static void removeUtils(Player player) {
		if (x1rapido.contains(player)) {
			x1rapido.remove(player);
	    }
		if (x2rapido.contains(player)) {
			x2rapido.remove(player);
	    }
		if (Convites.containsKey(player.getUniqueId())) {
			Convites.remove(player.getUniqueId());
		}
		if (inviteCooldown.containsKey(player.getUniqueId())) {
			inviteCooldown.remove(player.getUniqueId());
		}
	}
}