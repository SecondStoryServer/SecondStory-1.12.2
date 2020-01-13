package me.syari.sec_story.lib.inv

import me.syari.sec_story.lib.event.CustomEvent
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class CustomInventoryOpenEvent(val player: Player, val inventory: Inventory): CustomEvent()