package me.syari.sec_story.paper.core.home

import me.syari.sec_story.paper.library.event.CustomCancellableEvent
import org.bukkit.Location
import org.bukkit.entity.Player

class HomeSetEvent(val player: Player, val location: Location): CustomCancellableEvent()