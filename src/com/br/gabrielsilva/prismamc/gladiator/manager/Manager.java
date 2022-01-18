package com.br.gabrielsilva.prismamc.gladiator.manager;

import com.br.gabrielsilva.prismamc.gladiator.manager.game.GameManager;
import com.br.gabrielsilva.prismamc.gladiator.manager.hologram.HologramManager;
import com.br.gabrielsilva.prismamc.gladiator.manager.scoreboard.ScoreboardManager;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Manager {

	private GameManager gameManager;
	private ScoreboardManager scoreboardManager;
	private HologramManager hologramManager;
	
	public Manager() {
		setGameManager(new GameManager());
		
		setScoreboardManager(new ScoreboardManager());
		getScoreboardManager().init();
		
		setHologramManager(new HologramManager());
		getHologramManager().init();
	}
}