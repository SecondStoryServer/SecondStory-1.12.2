package me.syari.sec_story.paper.core.server

import com.shampaggon.crackshot.events.WeaponDamageEntityEvent
import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.chat.Chat.ch
import me.syari.sec_story.paper.core.command.AutoCommand
import me.syari.sec_story.paper.core.data.SaveData.hasSave
import me.syari.sec_story.paper.core.data.SaveData.loadSave
import me.syari.sec_story.paper.core.guild.Guild.guild
import me.syari.sec_story.paper.core.guild.area.GuildArea.getGuild
import me.syari.sec_story.paper.core.item.LoginReward.getDaily
import me.syari.sec_story.paper.core.item.LoginReward.getFirst
import me.syari.sec_story.paper.core.itemCode.ItemCode.checkCodeLimit
import me.syari.sec_story.paper.core.itemPost.ItemPost.checkPostLimit
import me.syari.sec_story.paper.core.itemPost.ItemPost.createPost
import me.syari.sec_story.paper.core.perm.Permission.loadPerm
import me.syari.sec_story.paper.core.perm.Permission.unloadPerm
import me.syari.sec_story.paper.core.player.Money.money
import me.syari.sec_story.paper.core.player.Time.time
import me.syari.sec_story.paper.core.plugin.SQL.sql
import me.syari.sec_story.paper.core.world.portal.Portal.first
import me.syari.sec_story.paper.library.code.StringEditor.toColor
import me.syari.sec_story.paper.library.date.Date
import me.syari.sec_story.paper.library.date.Date.now
import me.syari.sec_story.paper.library.date.Date.today
import me.syari.sec_story.paper.library.date.NextDayEvent
import me.syari.sec_story.paper.library.date.NextTimeEvent
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.message.SendMessage.action
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import me.syari.sec_story.paper.library.scoreboard.CreateScoreBoard.createBoard
import me.syari.sec_story.paper.library.scoreboard.ScoreBoardPriority
import me.syari.sec_story.paper.library.server.Server.maxPlayers
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.player.*
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.time.Instant
import java.time.ZoneId
import java.util.*

object Server: EventInit {
    val board = createBoard(plugin, "&a&lSecond Story", ScoreBoardPriority.Normal){
        line("&e&m------------------------")
        line { "&a&lプレイヤー &7≫ &e${plugin.server.onlinePlayers.size} &7/ &e${maxPlayers}" }
        space()
        line { "&a&l現在時間 &7≫ &e$now" }
        space()
        line { "&a&l所持金 &7≫ &e$money JPY" }
        space()
        line { "&a&lチャット &7≫ &e${ch.jp}" }
        line("&e&m------------------------")
    }

    @EventHandler
    fun on(e: NextDayEvent) {
        checkPostLimit()
        checkCodeLimit()
    }

    @EventHandler
    fun on(e: NextTimeEvent) {
        AutoCommand.run(Date.day, now)
        plugin.server.onlinePlayers.forEach { p ->
            board.updatePlayer(p)
            p.time += 1
            if(now == "00:00") {
                p.getDaily()
                p.send("&b[Reward] &fログインボーナスを貰いました")
            }
        }
    }

    lateinit var news: List<String>

    var textureURL: String? = null

    private val kickOnChat = mutableSetOf<UUID>()

    @EventHandler
    fun on(e: AsyncPlayerChatEvent) {
        val p = e.player
        if(kickOnChat.contains(p.uniqueId)) {
            e.message = null
            runLater(plugin, 1) {
                p.kickPlayer("")
            }
        }
    }

    @EventHandler
    fun on(e: PlayerJoinEvent) {
        val p = e.player
        kickOnChat.add(p.uniqueId)
        sql {
            var hasData = false
            val res = executeQuery("SELECT COUNT(*) FROM Story.PlayerData WHERE UUID = '${p.uniqueId}' LIMIT 1;")
            if(res.next()) hasData = res.getInt(1) == 1
            if(! hasData) executeUpdate(
                "INSERT INTO Story.PlayerData VALUE ('${p.name}', '${p.uniqueId}', 0, null, 0, null, 0, 0);"
            )
        }
        p.loadPerm()
        p.send(*news.toTypedArray())
        createPost(p)
        if(p.hasPlayedBefore()) {
            val ld = Instant.ofEpochMilli(p.lastPlayed).atZone(ZoneId.systemDefault()).toLocalDate()
            if(ld != today) {
                p.getDaily()
                p.send("&b[Reward] &fログインボーナスを貰いました")
            }
        } else {
            runLater(plugin, 1) {
                if(first != null) {
                    p.teleport(first)
                }
            }
            p.getFirst()
            p.send("&b[Reward] &fログインボーナスを貰いました")
        }
        e.joinMessage = null
        p.setPlayerListHeaderFooter(
            TextComponent(
                """
                &e&l&m----------------------------------------
                &a&lSecond Story
                
                """.trimIndent().toColor
            ), TextComponent(
                """
                
                &7&n2nd-story.info
                &e&l&m----------------------------------------
                """.trimIndent().toColor
            )
        )
        runLater(plugin, 5) {
            if(p.hasSave) p.loadSave()
            kickOnChat.remove(p.uniqueId)
            plugin.server.onlinePlayers.forEach {
                board.updatePlayer(it)
            }
        }
        if(textureURL != null) p.setResourcePack(textureURL)
        board.addPlayer(p)
    }

    @EventHandler
    fun on(e: PlayerQuitEvent) {
        val p = e.player
        p.unloadPerm()
        e.quitMessage = null
        runLater(plugin, 5) {
            plugin.server.onlinePlayers.forEach {
                board.updatePlayer(it)
            }
        }
        board.removePlayer(p)
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: EntityDamageEvent) {
        if(e.cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK && (e.entity is Hanging || e.entity is ArmorStand)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: SignChangeEvent) {
        val p = e.player
        if(p.isOp) {
            e.lines.forEachIndexed { i, l ->
                e.setLine(i, l.toColor)
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: BlockBreakEvent) {
        val p = e.player
        val tool = p.inventory.itemInMainHand ?: return
        val meta = tool.itemMeta ?: return
        if(meta.isUnbreakable) e.isCancelled = true
    }

    private fun cancelCauseGuildArea(entity: Entity?, p: Player): Boolean {
        if(entity == null) return false
        if(p.isOp) return false
        val c = entity.location.chunk ?: return false
        val g = getGuild(c)
        return g != null && g != p.guild
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: HangingBreakByEntityEvent) {
        val p = e.remover as? Player
        if(p != null) {
            if(cancelCauseGuildArea(e.entity, p)) e.isCancelled = true
        } else {
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: HangingBreakEvent) {
        if(e.cause !in listOf(HangingBreakEvent.RemoveCause.PHYSICS, HangingBreakEvent.RemoveCause.ENTITY)) {
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: WeaponDamageEntityEvent) {
        val p = e.player
        if(p != null) {
            if(cancelCauseGuildArea(e.victim, p)) e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: EntityDamageByEntityEvent) {
        val p = if(e.damager is Player) e.damager as Player else if(e.damager is Projectile) (e.damager as Projectile).shooter as? Player else return
        if(p != null) {
            if(cancelCauseGuildArea(e.entity, p)) {
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun on(e: EntityExplodeEvent) {
        e.blockList().clear()
    }

    @EventHandler
    fun onBed(e: PlayerInteractEvent) {
        val p = e.player
        if(e.action != Action.RIGHT_CLICK_BLOCK) return
        val b = e.clickedBlock ?: return
        if(b.type == Material.BED_BLOCK || b.type == Material.BED) {
            if(p.hasSave) {
                e.isCancelled = true
                return
            }
            if(e.hasItem() || p.world.time in 12542..23457) {
                return
            }
            e.isCancelled = true
            p.setBedSpawnLocation(b.location, true)
            p.action("&d&lスポーン地点を変更しました".toColor)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onTNT(e: PlayerInteractEvent) {
        if(e.action != Action.RIGHT_CLICK_BLOCK) return
        val b = e.clickedBlock ?: return
        if(b.type == Material.TNT) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: BlockRedstoneEvent) {
        val b = e.block ?: return
        if(b.type == Material.TNT) {
            e.newCurrent = 0
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: BlockPistonRetractEvent) {
        if(e.blocks.isNotEmpty()) {
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: BlockPistonExtendEvent) {
        if(e.blocks.isNotEmpty()) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: PlayerRespawnEvent) {
        val p = e.player
        runLater(plugin, 3) {
            if(p.hasSave) return@runLater
            p.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 5 * 20, 5))
            p.addPotionEffect(PotionEffect(PotionEffectType.UNLUCK, 30 * 20, 5))
        }
    }
}