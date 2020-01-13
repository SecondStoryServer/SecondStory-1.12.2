package me.syari.sec_story.paper.core.game.mobArena

import me.syari.sec_story.paper.core.data.event.PlayerDataEvent
import me.syari.sec_story.paper.core.game.mobArena.MobArena.arena
import me.syari.sec_story.paper.core.game.mobArena.MobArena.arenaPlayer
import me.syari.sec_story.paper.core.game.mobArena.MobArena.getArena
import me.syari.sec_story.paper.core.game.mobArena.MobArena.getArenaInPlay
import me.syari.sec_story.paper.core.game.mobArena.MobArena.inMobArena
import me.syari.sec_story.paper.core.guild.event.GuildMemberTeleportEvent
import me.syari.sec_story.paper.core.guild.event.GuildWarStartEvent
import me.syari.sec_story.paper.library.event.PlayerDeathWithCtEvent
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.inv.InventoryPlus.insertItem
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent

object MobArenaEvent: EventInit {
    @EventHandler(ignoreCancelled = true)
    fun on(e: InventoryClickEvent) {
        val p = e.whoClicked as? Player ?: return
        if(! p.inMobArena) return
        if(CustomItemStack(e.insertItem).containsLore("&c受け渡し不可")) {
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: GuildMemberTeleportEvent) {
        val p = e.player
        val t = e.target
        if(p.inMobArena || t.inMobArena) e.isCancelled = true
    }

    @EventHandler
    fun on(e: GuildWarStartEvent) {
        val g = e.guild
        val w = g.warGuild ?: return
        w.removeMember(w.member.filter { m -> m.player.inMobArena })
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: PlayerMoveEvent) {
        val p = e.player ?: return
        val m = p.arenaPlayer ?: return
        if(! m.isAllowMove(e.to)) e.isCancelled = true
    }

    @EventHandler
    fun on(e: PlayerQuitEvent) {
        val p = e.player ?: return
        val arena = p.arena ?: return
        arena.leave(p)
    }

    @EventHandler
    fun on(e: PlayerDeathWithCtEvent) {
        val p = e.player
        val arena = p.arena ?: return
        e.deathMessage = null
        e.isCancelled = true
        arena.onDeath(p)
    }

    @EventHandler
    fun on(e: EntityDeathEvent) {
        val entity = e.entity ?: return
        val arena = getArena(entity) ?: return
        e.droppedExp = 0
        e.drops.clear()
        arena.onKillEntity(entity)
    }

    @EventHandler
    fun on(e: PlayerInteractEvent) {
        val p = e.player ?: return
        val b = e.clickedBlock ?: return
        val arena = p.arena ?: return
        e.isCancelled = true
        if(b.type == Material.CHEST && e.action == Action.RIGHT_CLICK_BLOCK) {
            p.openInventory(arena.publicChest)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: PlayerItemDamageEvent) {
        val p = e.player ?: return
        if(p.inMobArena) e.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: EntityDamageByEntityEvent) {
        val v = e.entity as? Player ?: return
        val a = (if(e.damager is Player) e.damager as Player else if(e.damager is Projectile) (e.damager as Projectile).shooter as? Player else null) ?: return
        if(v.inMobArena || a.inMobArena) e.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: ItemSpawnEvent) {
        val loc = e.location ?: return
        if(getArenaInPlay(loc) != null) {
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: FoodLevelChangeEvent) {
        val p = e.entity as Player
        if(p.inMobArena) e.isCancelled = true
    }

    @EventHandler
    fun on(e: EntityTargetEvent) {
        val entity = e.entity ?: return
        val arena = getArena(entity) ?: return
        if(e.target !is Player) e.target = arena.livingPlayers.random().player
    }

    @EventHandler
    fun on(e: PlayerDataEvent) {

    }
}