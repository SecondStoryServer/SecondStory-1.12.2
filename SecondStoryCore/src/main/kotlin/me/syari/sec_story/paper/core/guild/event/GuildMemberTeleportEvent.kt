package me.syari.sec_story.paper.core.guild.event

import me.syari.sec_story.paper.library.event.CustomCancellableEvent
import org.bukkit.entity.Player

class GuildMemberTeleportEvent(val player: Player, val target: Player): CustomCancellableEvent()