package com.br.gabrielsilva.prismamc.gladiator.manager.hologram;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.UpdateEvent;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.UpdateEvent.UpdateType;
import com.br.gabrielsilva.prismamc.commons.bukkit.hologram.types.PlayerRankingHologram;
import com.br.gabrielsilva.prismamc.commons.bukkit.hologram.types.SimpleHologram;
import com.br.gabrielsilva.prismamc.gladiator.Gladiator;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class HologramManager {

	private SimpleHologram topWinstreak, topWins;
	private PlayerRankingHologram playerRanking;
	
	public void init() {
		setPlayerRanking(new PlayerRankingHologram(Gladiator.getInstance(), "ranking"));
		setTopWins(new SimpleHologram(Gladiator.getInstance(), "WINS", "gladiator", "wins"));
		setTopWinstreak(new SimpleHologram(Gladiator.getInstance(), "WINSTREAK", "gladiator", "maxWinstreak"));
		
		getPlayerRanking().create();
		getTopWinstreak().create();
		getTopWins().create();
		
		registerListener();
	}

	public void registerListener() {
		Bukkit.getServer().getPluginManager().registerEvents(new Listener() {
			
			int minutos = 0;
			
			@EventHandler
			public void updateEvent(UpdateEvent event) {
				if (event.getType() != UpdateType.MINUTO) {
					return;
				}
				
				minutos++;
				
				if (minutos == 10) {
					synchronized(this) {
						getPlayerRanking().updateValues();	
						getTopWinstreak().updateValues();
						getTopWins().updateValues();
					}
					
					minutos = 0;
				}
			}
		}, Gladiator.getInstance());
	}
}