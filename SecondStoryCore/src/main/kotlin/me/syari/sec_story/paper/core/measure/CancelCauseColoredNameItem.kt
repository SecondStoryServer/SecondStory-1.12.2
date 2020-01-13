package me.syari.sec_story.paper.core.measure

import me.syari.sec_story.paper.library.code.StringEditor.toUncolor
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.event.EventHandler
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.inventory.BrewEvent
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack

object CancelCauseColoredNameItem: EventInit {
    private fun checkColoredNameItem(item: ItemStack): Boolean {
        val cItem = CustomItemStack(item)
        val display = cItem.display ?: return false
        return display.toUncolor != display
    }

    @EventHandler
    fun on(e: FurnaceBurnEvent) {
        val item = e.fuel ?: return
        if(checkColoredNameItem(item)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: BrewEvent) {
        val item = e.contents.fuel ?: return
        if(checkColoredNameItem(item)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: EnchantItemEvent) {
        val item = e.item ?: return
        if(checkColoredNameItem(item)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: PrepareAnvilEvent) {
        val item = e.inventory.getItem(0) ?: return
        if(checkColoredNameItem(item)) {
            e.result = null
        }
    }
}