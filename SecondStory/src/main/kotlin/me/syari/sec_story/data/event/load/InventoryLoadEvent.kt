package me.syari.sec_story.data.event.load

import me.syari.sec_story.data.event.DataEventType
import org.bukkit.entity.Player

class InventoryLoadEvent(player: Player): DataLoadEvent(player, DataEventType.Inventory)