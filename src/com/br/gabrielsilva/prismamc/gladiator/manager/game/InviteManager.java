package com.br.gabrielsilva.prismamc.gladiator.manager.game;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.br.gabrielsilva.prismamc.gladiator.listeners.InviteListener.ConviteTipo;

import lombok.Getter;

@Getter
public class InviteManager {
	
	private UUID uuid;
	private ConviteTipo tipo;
	private Long invited;
	
	public InviteManager(UUID uuid, ConviteTipo convite) {
		this.uuid = uuid;
		this.tipo = convite;
		this.invited = System.currentTimeMillis();
	}

	public boolean isValid() {
		if (invited + TimeUnit.SECONDS.toMillis(5) > System.currentTimeMillis()) {
			return true;
		}
		return false;
	}
}