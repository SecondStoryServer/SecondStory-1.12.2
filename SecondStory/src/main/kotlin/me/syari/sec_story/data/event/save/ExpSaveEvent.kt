package me.syari.sec_story.data.event.save

import me.syari.sec_story.data.event.DataEventType
import org.bukkit.entity.Player

class ExpSaveEvent(player: Player): DataSaveEvent(player, DataEventType.Exp)