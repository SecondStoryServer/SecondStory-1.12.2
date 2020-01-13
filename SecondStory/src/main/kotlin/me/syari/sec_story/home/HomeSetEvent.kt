package me.syari.sec_story.home

import me.syari.sec_story.lib.event.CustomCancellableEvent
import org.bukkit.Location
import org.bukkit.entity.Player

class HomeSetEvent(val player: Player, val location: Location): CustomCancellableEvent()