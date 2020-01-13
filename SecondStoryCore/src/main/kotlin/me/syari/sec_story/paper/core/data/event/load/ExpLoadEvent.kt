package me.syari.sec_story.paper.core.data.event.load

import me.syari.sec_story.paper.core.data.event.DataEventType
import org.bukkit.entity.Player

class ExpLoadEvent(player: Player): DataLoadEvent(player, DataEventType.Exp)