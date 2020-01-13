package me.syari.sec_story.paper.core.data.event.save

import me.syari.sec_story.paper.core.data.event.DataEventType
import org.bukkit.entity.Player

class InventorySaveEvent(player: Player): DataSaveEvent(player, DataEventType.Inventory)