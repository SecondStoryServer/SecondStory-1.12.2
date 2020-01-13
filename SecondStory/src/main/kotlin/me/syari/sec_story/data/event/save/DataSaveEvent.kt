package me.syari.sec_story.data.event.save

import me.syari.sec_story.data.event.DataEventType
import me.syari.sec_story.data.event.PlayerDataEvent
import org.bukkit.entity.Player

open class DataSaveEvent(player: Player, type: DataEventType): PlayerDataEvent(player, type)