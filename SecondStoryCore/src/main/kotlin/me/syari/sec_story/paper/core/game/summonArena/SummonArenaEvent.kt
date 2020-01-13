package me.syari.sec_story.paper.core.game.summonArena

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.game.summonArena.MobPoint.addWeekPoint
import me.syari.sec_story.paper.core.game.summonArena.SummonArena.getArena
import me.syari.sec_story.paper.core.game.summonArena.SummonArena.getMob
import me.syari.sec_story.paper.core.game.summonArena.SummonArena.selectMob
import me.syari.sec_story.paper.core.guild.event.GuildMemberTeleportEvent
import me.syari.sec_story.paper.core.guild.event.GuildWarStartEvent
import me.syari.sec_story.paper.core.home.HomeSetEvent
import me.syari.sec_story.paper.core.world.spawn.SpawnTeleportEvent
import me.syari.sec_story.paper.library.code.StringEditor.toColor
import me.syari.sec_story.paper.library.code.StringEditor.toUncolor
import me.syari.sec_story.paper.library.event.PlayerDeathWithCtEvent
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.message.SendMessage.action
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.ItemMergeEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import java.util.*

object SummonArenaEvent: EventInit {

    @EventHandler
    fun on(e: PlayerInteractEntityEvent) {
        val frame = e.rightClicked as? ItemFrame ?: return
        val p = e.player ?: return
        val arena = p.getArena() ?: return
        if(arena.itemFrame.getNearbyEntities(1.0, 1.0, 1.0).contains(frame)) {
            p.selectMob(arena)
            e.isCancelled = true
        }
    }

    private val ct = mutableListOf<UUID>()

    @EventHandler
    fun on(e: PlayerInteractEvent) {
        if(e.action != Action.RIGHT_CLICK_BLOCK) return
        val p = e.player ?: return
        if(p.isSneaking) return
        if(! ct.contains(p.uniqueId)) {
            val arena = p.getArena() ?: return
            val b = e.clickedBlock ?: return
            if(b.isBlockIndirectlyPowered) return
            val loc = b.location ?: return
            if(arena.button != loc) return
            val held = p.inventory.itemInMainHand
            if(held != null && held.type != Material.AIR) {
                e.isCancelled = true
                return p.action("&c&l召喚は素手でしか行えません")
            }
            val summon = arena.summon ?: return p.action("&c&l召喚モンスターを選択してください")
            val allSummonPoint = arena.getAllSummonPoint()
            val countPoint = arena.getUsedSummonPoint()
            val summonPoint = summon.summon
            if(allSummonPoint <= countPoint) return p.send(
                "&b[Arena] &c一度に召喚可能な数を上回っています 出したモンスターを倒してください ランクを上げることで上限解放されます"
            )
            arena.spawnMob(summon)
            p.action("&c&n召喚ポイント ${countPoint + summonPoint} / $allSummonPoint")
            ct.add(p.uniqueId)
            runLater(plugin, 1) {
                ct.remove(p.uniqueId)
            }
        }
    }

    @EventHandler
    fun on(e: MythicMobDeathEvent) {
        val p = e.killer as? Player ?: return
        val entity = e.entity ?: return
        val uuid = entity.uniqueId ?: return
        val arena = p.getArena() ?: return
        val mob = arena.getMob(uuid) ?: getMob(e.mobType.internalName) ?: return
        arena.removeMob(uuid)
        val r = mob.reward
        val xp = mob.exp
        p.addWeekPoint(r)
        p.giveExp(xp)
        p.action("&a討伐ポイント${r} と 経験値${xp} を取得しました")
        safeDrop(p, e.entity.location, e.drops)
        e.drops.clear()
    }

    @EventHandler
    fun on(e: PlayerQuitEvent) {
        val p = e.player
        p.getArena()?.leave(p)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun on(e: EntityPickupItemEvent) {
        val p = e.entity as? Player ?: return
        val item = e.item ?: return
        val name = item.customName ?: return
        if(item.isCustomNameVisible && name.toUncolor != p.displayName) e.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: ItemMergeEvent) {
        val t = e.target
        val b = e.entity
        if(t.isCustomNameVisible != b.isCustomNameVisible || t.customName != b.customName) e.isCancelled = true
    }

    private fun safeDrop(p: Player, loc: Location, items: Collection<ItemStack>) {
        items.forEach { i ->
            val item = loc.world.dropItemNaturally(loc, i)
            item.customName = "&a${p.displayName}".toColor
            item.isCustomNameVisible = true
            runLater(plugin, 20 * 60) {
                item.customName = null
                item.isCustomNameVisible = false
            }
        }
    }

    @EventHandler
    fun on(e: CreatureSpawnEvent) {
        if(e.spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            val entity = e.entity
            runLater(plugin, 1) {
                if(! entity.isDead) {
                    entity.remove()
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onClickSign(e: PlayerInteractEvent) {
        val p = e.player
        if(e.action != Action.RIGHT_CLICK_BLOCK) return
        if(e.clickedBlock.type == Material.SIGN || e.clickedBlock.type == Material.SIGN_POST || e.clickedBlock.type == Material.WALL_SIGN) {
            val sign = e.clickedBlock.state as? Sign ?: return
            val arena = p.getArena() ?: return
            if(sign.getLine(0) == "&6[SummonArena]".toColor) {
                e.isCancelled = true
                when(sign.getLine(1)?.toLowerCase()?.toUncolor) {
                    "leave" -> arena.leave(p)
                    "option" -> arena.openOption(p)
                    else -> if(p.isOp) p.send("&6[SummonArena] &c看板の2行目を「leave, option」にしてください")
                }
            }
        }
    }

    @EventHandler
    fun on(e: PlayerDeathWithCtEvent) {
        val p = e.player
        val a = p.getArena() ?: return
        e.isCancelled = true
        runLater(plugin, 3) {
            p.teleport(a.tpTo)
            PlayerRespawnEvent(p, a.tpTo, false).callEvent()
        }
    }

    @EventHandler
    fun on(e: GuildWarStartEvent) {
        val g = e.guild
        g.warGuild?.member?.forEach { m ->
            m.player.getArena()?.leave(m.player)
        }
    }

    @EventHandler
    fun on(e: GuildMemberTeleportEvent) {
        val p = e.player
        val t = e.target
        val pa = p.getArena()
        val ta = t.getArena()
        if(ta != null) {
            if(pa != ta) {
                if(ta.canJoin()) {
                    pa?.leave(p)
                    ta.join(p)
                } else {
                    e.isCancelled = true
                }
            }
        } else {
            pa?.leave(p)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: HomeSetEvent) {
        val p = e.player
        if(p.getArena() != null) e.isCancelled = true
    }

    @EventHandler
    fun on(e: SpawnTeleportEvent) {
        val p = e.player
        val a = p.getArena() ?: return
        a.leave(p)
        e.isCancelled = true
    }

    @EventHandler
    fun on(e: PlayerMoveEvent) {
        val p = e.player ?: return
        val a = p.getArena() ?: return
        val t = e.to
        if(! a.inRegion(t)) {
            p.teleport(a.tpTo)
        }
    }

}