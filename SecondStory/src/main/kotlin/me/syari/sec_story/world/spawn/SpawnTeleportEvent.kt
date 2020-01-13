package me.syari.sec_story.world.spawn

import me.syari.sec_story.lib.event.CustomCancellableEvent
import org.bukkit.World
import org.bukkit.entity.Player

class SpawnTeleportEvent(val player: Player, val world: World): CustomCancellableEvent()