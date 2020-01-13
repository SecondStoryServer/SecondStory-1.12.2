package me.syari.sec_story.data.event.load

import me.syari.sec_story.data.event.DataEventType
import org.bukkit.entity.Player

class LocationLoadEvent(player: Player): DataLoadEvent(player, DataEventType.Location)