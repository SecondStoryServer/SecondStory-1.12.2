package me.syari.sec_story.paper.core.server

import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.inv.CreateInventory.inventory
import me.syari.sec_story.paper.library.inv.InventoryPlus.insertItem
import org.bukkit.block.Container
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta

object OpenShulker: EventInit {
    private val ItemStack?.isShulker get() = if(this == null) false else type.toString().contains("_SHULKER_BOX")

    private val Player.hasShulkerInMainHand get() = inventory.itemInMainHand.isShulker

    @EventHandler
    fun on(e: PlayerInteractEvent) {
        val p = e.player
        val i = e.item ?: return
        if(i.isShulker && e.action == Action.RIGHT_CLICK_AIR && p.isSneaking) {
            val s = i.itemMeta as? BlockStateMeta ?: return
            val sh = s.blockState as? Container as? ShulkerBox ?: return
            inventory("&5&lシュルカーボックス") {
                cancel = false
                onClick = { e ->
                    if(! p.hasShulkerInMainHand || e.insertItem.isShulker) {
                        e.isCancelled = true
                    }
                }
                onClose = { e ->
                    if(p.hasShulkerInMainHand) {
                        sh.inventory.contents = e.inventory.contents
                        s.blockState = sh
                        i.itemMeta = s
                        p.inventory.itemInMainHand = i
                    }
                }
                contents = sh.inventory.contents
            }.open(p)
        }
    }
}