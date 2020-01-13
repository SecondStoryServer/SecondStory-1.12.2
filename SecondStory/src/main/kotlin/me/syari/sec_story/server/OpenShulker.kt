package me.syari.sec_story.server

import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.lib.InventoryPlus.insertItem
import me.syari.sec_story.lib.inv.CreateInventory.inventory
import me.syari.sec_story.lib.inv.CreateInventory.open
import me.syari.sec_story.plugin.Init
import org.bukkit.block.ShulkerBox
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.meta.BlockStateMeta

object OpenShulker : Listener, Init() {
    @EventHandler
    fun on(e: PlayerInteractEvent){
        val p = e.player
        val i = e.item ?: return
        if(i.type.toString().contains("_SHULKER_BOX") && e.action == Action.RIGHT_CLICK_AIR && p.isSneaking){
            val s = i.itemMeta as? BlockStateMeta ?: return
            val sh = s.blockState as? ShulkerBox ?: return
            inventory("&5&lシュルカーボックス"){
                cancel = false
                onClick = { e ->
                    if(CustomItemStack(e.insertItem).type.toString().contains("_SHULKER_BOX")){
                        e.isCancelled = true
                    }
                }
                onClose = { e ->
                    sh.inventory.contents = e.inventory.contents
                    s.blockState = sh
                    i.itemMeta = s
                    p.inventory.itemInMainHand = i
                }
                contents = sh.inventory.contents
            }.open(p)
        }
    }
}