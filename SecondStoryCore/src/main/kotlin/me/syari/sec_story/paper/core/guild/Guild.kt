package me.syari.sec_story.paper.core.guild

import com.shampaggon.crackshot.events.WeaponDamageEntityEvent
import me.syari.sec_story.paper.core.guild.area.GuildArea.canBuyWorld
import me.syari.sec_story.paper.core.guild.area.GuildArea.getGuild
import me.syari.sec_story.paper.core.guild.quest.GuildQuest
import me.syari.sec_story.paper.core.guild.war.GuildWar
import me.syari.sec_story.paper.core.guild.war.GuildWar.nowWar
import me.syari.sec_story.paper.core.plugin.SQL.sql
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.message.SendMessage.action
import me.syari.sec_story.paper.library.message.SendMessage.send
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.StructureGrowEvent
import java.util.*

object Guild: EventInit {
    val guilds = mutableListOf<GuildData>()

    fun getGuild(guildID: UUID?): GuildData? {
        if(guildID == null) return null
        return guilds.firstOrNull { g -> g.id == guildID }
    }

    val OfflinePlayer.guildOfflinePlayer
        get(): GuildOfflinePlayer {
            return if(this is Player) guildPlayer else GuildOfflinePlayer(this)
        }

    private val guildPlayers = mutableListOf<GuildPlayer>()

    val Player.guildPlayer
        get(): GuildPlayer {
            return guildPlayers.firstOrNull { f -> f.player == this } ?: {
                val tmp = GuildPlayer(this)
                guildPlayers.add(tmp)
                tmp
            }.invoke()
        }

    val Player.guild get() = guildPlayer.guild()

    val guildFromName = mutableMapOf<String, GuildData?>()

    fun getGuild(name: String): GuildData? {
        return guildFromName.getOrPut(name) { guilds.firstOrNull { g -> g.name == name } }
    }

    @EventHandler
    fun on(e: PlayerJoinEvent) {
        val p = e.player
        guildPlayers.add(GuildPlayer(p))
    }

    @EventHandler
    fun on(e: PlayerQuitEvent) {
        val p = e.player
        guildPlayers.removeIf { r -> r.player.uniqueId == p.uniqueId }
    }

    @EventHandler
    fun on(e: PlayerMoveEvent) {
        val p = e.player
        if(p.location.world.name in canBuyWorld) {
            val gp = p.guildPlayer
            val g = gp.enterGuild
            val c = getGuild(p.chunk)
            if(g != c) {
                p.action(("&2&l" + (c?.name ?: "荒地")) + "&7&lに入りました")
            }
            gp.enterGuild = c
        }
    }

    private fun cancelCauseFF(ev: Entity?, ea: Entity?): Boolean {
        val v = ev as? Player ?: return false
        val a = ea as? Player ?: return false
        val vgp = v.guildPlayer
        val vg = vgp.guild() ?: return false
        val agp = a.guildPlayer
        val ag = agp.guild() ?: return false
        if(v.nowWar() && a.nowWar()) return false
        return vg == ag && ! vg.ff
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: EntityDamageByEntityEvent) {
        val v = e.entity
        val a = e.damager
        val cancel = cancelCauseFF(v, a)
        if(cancel) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: WeaponDamageEntityEvent) {
        val v = e.victim
        val a = e.damager
        val cancel = cancelCauseFF(v, a)
        if(cancel) {
            e.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun on(e: BlockPlaceEvent) {
        val p = e.player
        if(p.isOp) return
        val b = e.block ?: return
        val g = p.guild
        val bg = getGuild(b.chunk)
        if(bg != null && bg != g) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: StructureGrowEvent) {
        val g = getGuild(e.location?.chunk)
        e.blocks?.forEach { b ->
            val bg = getGuild(b.chunk)
            if(bg != null && g != bg) {
                e.isCancelled = true
                return
            }
        }
    }

    @EventHandler
    fun on(e: BlockFromToEvent) {
        val g = getGuild(e.block?.chunk)
        val t = getGuild(e.toBlock?.chunk)
        if(t != null && g != t) {
            e.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun on(e: PlayerInteractEvent) {
        val b = e.clickedBlock ?: return
        val p = e.player ?: return
        val g = p.guild
        val c = b.chunk
        val cg = getGuild(c)
        selectChunk(e)
        if(! e.isCancelled && ! p.isOp && cg != null && cg != g) {
            e.isCancelled = true
        }
    }

    private fun selectChunk(e: PlayerInteractEvent) {
        val b = e.clickedBlock ?: return
        val p = e.player
        val gp = p.guildPlayer
        val c = b.chunk
        if(gp.isAreaSelectMode && c.world.name in canBuyWorld) {
            val a = e.action
            if(a in listOf(Action.RIGHT_CLICK_BLOCK, Action.LEFT_CLICK_BLOCK)) {
                e.isCancelled = true
                if(a == Action.RIGHT_CLICK_BLOCK) {
                    if(gp.selectPos1 == c) return
                    gp.selectPos1 = c
                } else {
                    if(gp.selectPos2 == c) return
                    gp.selectPos2 = c
                }
                p.send("&b[Guild] &a${gp.getSelectChunk().size}チャンク&fが選択されています")
            }
        }
    }

    val allGuild: List<String>
        get() {
            return guilds.map { it.name }
        }

    fun getGuilds(page: Int): List<String> {
        val list = mutableListOf<String>()
        sql {
            val res = executeQuery("SELECT Name FROM Story.Guild LIMIT ${(page - 1) * 10}, 10;")
            while(res.next()) list.add(res.getString(1))
        }
        return list
    }

    fun onDisable() {
        GuildWar.forceEnd()
        GuildQuest.forceSave()
    }
}