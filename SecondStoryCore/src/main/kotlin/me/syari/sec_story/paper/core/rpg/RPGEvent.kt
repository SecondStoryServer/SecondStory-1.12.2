package me.syari.sec_story.paper.core.rpg

import me.syari.sec_story.paper.core.rpg.Quests.openQuest
import me.syari.sec_story.paper.core.rpg.RPG.leaveRPG
import me.syari.sec_story.paper.core.rpg.RPG.nowRPG
import me.syari.sec_story.paper.library.init.EventInit
import net.citizensnpcs.api.event.NPCRightClickEvent
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.InventoryHolder

object RPGEvent: EventInit {
    @EventHandler
    fun on(e: PlayerQuitEvent) {
        e.player.leaveRPG()
    }

    @EventHandler
    fun on(e: NPCRightClickEvent) {
        val p = e.clicker
        val npc = e.npc.name
        p.openQuest(npc)
    }

    @EventHandler
    fun on(e: PlayerDropItemEvent) {
        if(! e.player.nowRPG) return
        val item = e.itemDrop
        val i = item.itemStack
        val bool = i.itemMeta?.hasEnchant(Enchantment.BINDING_CURSE) ?: return
        if(bool) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: PlayerInteractEvent) {
        if(! e.player.nowRPG) return
        if(e.action == Action.LEFT_CLICK_BLOCK) e.isCancelled = true
        else if(e.action == Action.RIGHT_CLICK_BLOCK && e.clickedBlock.state is InventoryHolder) e.isCancelled = true
    }

    @EventHandler
    fun on(e: BlockPlaceEvent) {
        if(! e.player.nowRPG) return
        e.isCancelled = true
    }

    @EventHandler
    fun on(e: BlockBreakEvent) {
        if(! e.player.nowRPG) return
        e.isCancelled = true
    }

    @EventHandler
    fun on(e: EntityDamageByEntityEvent) {
        val victim = e.entity as? Player ?: return
        when(e.damager) {
            is Player -> {
                val attacker = e.damager as? Player ?: return
                if(victim.nowRPG || attacker.nowRPG) {
                    e.isCancelled = true
                }
            }
            is Projectile -> {
                val pr = e.damager as? Projectile ?: return
                val attacker = pr.shooter as? Player ?: return
                if(victim.nowRPG || attacker.nowRPG) {
                    e.isCancelled = true
                }
            }
        }
    }
}