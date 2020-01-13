package me.syari.sec_story.paper.core.data.event.load

import me.syari.sec_story.paper.core.data.event.DataEventType
import me.syari.sec_story.paper.core.data.event.PlayerDataEvent
import org.bukkit.entity.Player

open class DataLoadEvent(player: Player, type: DataEventType): PlayerDataEvent(player, type)