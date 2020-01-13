package me.syari.sec_story.paper.library.event

import org.bukkit.entity.Player

class PlayerDeathWithCtEvent(val player: Player, var deathMessage: String?): CustomCancellableEvent()