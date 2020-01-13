package me.syari.sec_story.guild.event

import me.syari.sec_story.lib.event.CustomCancellableEvent
import org.bukkit.entity.Player

class GuildMemberTeleportEvent(val player: Player, val target: Player): CustomCancellableEvent()