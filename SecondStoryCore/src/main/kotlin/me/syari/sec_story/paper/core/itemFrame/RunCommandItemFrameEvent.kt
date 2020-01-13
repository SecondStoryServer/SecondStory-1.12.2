package me.syari.sec_story.paper.core.itemFrame

import me.syari.sec_story.paper.library.event.CustomCancellableEvent
import org.bukkit.entity.Player

class RunCommandItemFrameEvent(val player: Player, val command: List<String>): CustomCancellableEvent()