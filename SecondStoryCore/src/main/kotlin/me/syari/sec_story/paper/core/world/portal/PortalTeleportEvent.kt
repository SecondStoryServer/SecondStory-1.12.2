package me.syari.sec_story.paper.core.world.portal

import me.syari.sec_story.paper.library.event.CustomCancellableEvent
import org.bukkit.entity.Player

class PortalTeleportEvent(val player: Player): CustomCancellableEvent()