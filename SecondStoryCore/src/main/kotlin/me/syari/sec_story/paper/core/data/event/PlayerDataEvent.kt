package me.syari.sec_story.paper.core.data.event

import me.syari.sec_story.paper.library.event.CustomEvent
import org.bukkit.entity.Player

open class PlayerDataEvent(val player: Player, val type: DataEventType): CustomEvent()