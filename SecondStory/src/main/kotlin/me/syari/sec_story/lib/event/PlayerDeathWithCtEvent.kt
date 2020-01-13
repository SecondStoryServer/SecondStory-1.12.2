package me.syari.sec_story.lib.event

import org.bukkit.entity.Player

class PlayerDeathWithCtEvent(val player: Player, var deathMessage: String?): CustomCancellableEvent()