package me.syari.sec_story.paper.core.itemFrame

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.library.code.StringEditor.toColor
import me.syari.sec_story.paper.library.command.RunCommand.runCommand
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack

object ItemFrameCommand: EventInit {
    private val prefix = "&b額縁コマンド: /".toColor

    private val ItemStack?.isFrameCommandItem
        get(): Boolean {
            if(this == null) return false
            val item = CustomItemStack(this)
            item.lore.forEach { l ->
                if(l.startsWith(prefix)) {
                    return true
                }
            }
            return false
        }

    @EventHandler
    fun on(e: PlayerInteractEntityEvent) {
        val p = e.player ?: return
        if(p.isOp && p.isSneaking) return
        val f = e.rightClicked as? ItemFrame ?: return
        val item = CustomItemStack(f.item)
        val command = item.lore.filter { it.startsWith(prefix) }.map { it.substringAfter(prefix) }
        if(command.isEmpty()) return
        val event = RunCommandItemFrameEvent(p, command)
        event.callEvent()
        if(!event.isCancelled) {
            command.forEach {
                runCommand(p, it)
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: EntityDamageEvent) {
        val f = e.entity as? ItemFrame ?: return
        if(f.item.isFrameCommandItem) {
            if(e is EntityDamageByEntityEvent) {
                val p = e.damager as? Player
                if(p != null && p.isOp && p.isSneaking) return
            }
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: EntityPickupItemEvent) {
        if(e.item.itemStack.isFrameCommandItem) {
            val p = e.entity as? Player
            if(p != null && p.isOp) return
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: PlayerDropItemEvent) {
        if(e.itemDrop.itemStack.isFrameCommandItem) {
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: ItemSpawnEvent) {
        if(e.entity.itemStack.isFrameCommandItem) {
            e.isCancelled = true
        }
    }
}