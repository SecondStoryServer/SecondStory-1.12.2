package me.syari.sec_story.data.event

import me.syari.sec_story.lib.event.CustomEvent
import org.bukkit.entity.Player

open class PlayerDataEvent(val player: Player, val type: DataEventType): CustomEvent()