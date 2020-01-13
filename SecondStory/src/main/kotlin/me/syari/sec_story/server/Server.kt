package me.syari.sec_story.server

import com.shampaggon.crackshot.events.WeaponDamageEntityEvent
import me.syari.sec_story.data.SaveData.hasSave
import me.syari.sec_story.data.SaveData.loadSave
import me.syari.sec_story.guild.Guild.guild
import me.syari.sec_story.guild.area.GuildArea.getGuild
import me.syari.sec_story.item.ItemPost.checkPostLimit
import me.syari.sec_story.item.ItemPost.infoPost
import me.syari.sec_story.item.LoginReward.getDaily
import me.syari.sec_story.item.LoginReward.getFirst
import me.syari.sec_story.item.code.ItemCode.checkCodeLimit
import me.syari.sec_story.lib.CreateScoreBoard.createBoard
import me.syari.sec_story.lib.message.SendMessage.action
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.StringEditor.toColor
import me.syari.sec_story.message.Chat.ch
import me.syari.sec_story.perm.Permission.loadPerm
import me.syari.sec_story.perm.Permission.unloadPerm
import me.syari.sec_story.player.Money.money
import me.syari.sec_story.player.Time.time
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.Plugin.plugin
import me.syari.sec_story.plugin.SQL.sql
import me.syari.sec_story.world.portal.Portal.first
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.player.*
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

object Server : Listener, Init() {
    private val hideBoards = mutableListOf<UUID>()
    var Player.hideBoard: Boolean
        get() = hideBoards.contains(uniqueId)
        set(value){
            if(value) hideBoards.add(uniqueId)
            else hideBoards.remove(uniqueId)
        }

    fun board(p: Player) = createBoard("&a&lSecond Story", p,
            -1 to "&e&m------------------------&1",
            -2 to "&a&lプレイヤー &7≫ &e${plugin.server.onlinePlayers.size} &7/ &e${Bukkit.getMaxPlayers()}&2",
            -3 to "&3",
            -4 to "&a&l現在時間 &7≫ &e$now&4",
            -5 to "&5",
            -6 to "&a&l所持金 &7≫ &e${p.money} JPY&6",
            -7 to "&7",
            -8 to "&a&lチャット &7≫ &e${p.ch.jp}&8",
            -9 to "&e&m------------------------&9"
    )

    lateinit var today: LocalDate
    var now = "null"
    var bef = "null"
    var day = -1

    fun CommandSender.resetDay(){
        val now = LocalDate.now()
        today = now
        day = now.dayOfWeek.value
        checkPostLimit()
        checkCodeLimit()
        send("&b[Day] &f時間を初期化しました")
    }

    val timer = object : BukkitRunnable(){
        override fun run() {
            now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            if(bef != now){
                if(now == "00:00"){
                    plugin.server.consoleSender.resetDay()
                }
                Commands.run(day, now)
                plugin.server.onlinePlayers.forEach { p ->
                    if(!p.hideBoard){
                        board(p)
                    }
                    p.time += 1
                    if(now == "00:00"){
                        p.getDaily()
                        p.send("&b[Reward] &fログインボーナスを貰いました")
                    }
                }
                bef = now
            }
        }
    }

    lateinit var news: List<String>

    var textureURL: String? = null

    private val kickOnChat = mutableSetOf<UUID>()

    @EventHandler
    fun on(e: AsyncPlayerChatEvent){
        val p = e.player
        if(kickOnChat.contains(p.uniqueId)){
            e.message = null
            object : BukkitRunnable(){
                override fun run() {
                    p.kickPlayer("")
                }
            }.runTaskLater(plugin, 1)
        }
    }

    @EventHandler
    fun on(e: PlayerJoinEvent){
        val p = e.player
        kickOnChat.add(p.uniqueId)
        sql {
            var hasData = false
            val res = executeQuery("SELECT COUNT(*) FROM Story.PlayerData WHERE UUID = '${p.uniqueId}' LIMIT 1;")
            if(res.next()) hasData = res.getInt(1) == 1
            if(!hasData) executeUpdate("INSERT INTO Story.PlayerData VALUE ('${p.name}', '${p.uniqueId}', 0, null, 0, null, 0, 0);")
        }
        p.loadPerm()
        p.send(*news.toTypedArray())
        p.infoPost()
        if(p.hasPlayedBefore()){
            val ld = Instant.ofEpochMilli(p.lastPlayed).atZone(ZoneId.systemDefault()).toLocalDate()
            if(ld != today){
                p.getDaily()
                p.send("&b[Reward] &fログインボーナスを貰いました")
            }
        } else {
            object : BukkitRunnable(){
                override fun run() {
                    if(first != null){
                        p.teleport(first)
                    }
                }
            }.runTaskLater(plugin, 1)
            p.getFirst()
            p.send("&b[Reward] &fログインボーナスを貰いました")
        }
        e.joinMessage = null
        p.setPlayerListHeaderFooter(
            TextComponent("""
                &e&l&m----------------------------------------
                &a&lSecond Story
                
                """.trimIndent().toColor),
            TextComponent("""
                
                &7&n2nd-story.info
                &e&l&m----------------------------------------
                """.trimIndent().toColor)
        )
        object : BukkitRunnable(){
            override fun run() {
                if(p.hasSave) p.loadSave()
                kickOnChat.remove(p.uniqueId)
                plugin.server.onlinePlayers.forEach { o ->
                    board(o)
                }
            }
        }.runTaskLater(plugin, 5)
        if(textureURL != null) p.setResourcePack(textureURL)
    }

    @EventHandler
    fun on(e: PlayerQuitEvent){
        val p = e.player
        p.unloadPerm()
        e.quitMessage = null
        object : BukkitRunnable(){
            override fun run() {
                plugin.server.onlinePlayers.forEach { o ->
                    board(o)
                }
            }
        }.runTaskLater(plugin, 5)
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: EntityDamageEvent){
        if(e.cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK && (e.entity is Hanging || e.entity is ArmorStand)){
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: SignChangeEvent){
        val p = e.player
        if(p.isOp){
            e.lines.forEachIndexed { i, l ->
                e.setLine(i, l.toColor)
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: BlockBreakEvent){
        val p = e.player
        val tool = p.inventory.itemInMainHand ?: return
        val meta = tool.itemMeta ?: return
        if(meta.isUnbreakable) e.isCancelled = true
    }

    private fun cancelCauseGuildArea(entity: Entity?, p: Player): Boolean{
        if(entity == null) return false
        if (p.isOp) return false
        val c = entity.location.chunk ?: return false
        val g = getGuild(c)
        return g != null && g != p.guild
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: HangingBreakByEntityEvent){
        val p = e.remover as? Player
        if(p != null) {
            if(cancelCauseGuildArea(e.entity, p)) e.isCancelled = true
        } else {
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: HangingBreakEvent){
        if(e.cause !in listOf(HangingBreakEvent.RemoveCause.PHYSICS, HangingBreakEvent.RemoveCause.ENTITY)){
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: WeaponDamageEntityEvent){
        val p = e.player
        if(p != null) {
            if(cancelCauseGuildArea(e.victim, p)) e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: EntityDamageByEntityEvent){
        val p = if(e.damager is Player) e.damager as Player else if(e.damager is Projectile) (e.damager as Projectile).shooter as? Player else return
        if(p != null) {
            if(cancelCauseGuildArea(e.entity, p)) {
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun on(e: EntityExplodeEvent){
        e.blockList().clear()
    }

    @EventHandler
    fun onBed(e: PlayerInteractEvent){
        val p = e.player
        if(e.action != Action.RIGHT_CLICK_BLOCK) return
        val b = e.clickedBlock ?: return
        if(b.type == Material.BED_BLOCK || b.type == Material.BED){
            if(p.hasSave){
                e.isCancelled = true
                return
            }
            if(e.hasItem() || p.world.time in 12542..23457){
                return
            }
            e.isCancelled = true
            p.setBedSpawnLocation(b.location, true)
            p.action("&d&lスポーン地点を変更しました".toColor)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onTNT(e: PlayerInteractEvent){
        if(e.action != Action.RIGHT_CLICK_BLOCK) return
        val b = e.clickedBlock ?: return
        if(b.type == Material.TNT){
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: BlockRedstoneEvent){
        val b = e.block ?: return
        if(b.type == Material.TNT){
            e.newCurrent = 0
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: BlockPistonRetractEvent){
        if(e.blocks.isNotEmpty()){
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: BlockPistonExtendEvent){
        if(e.blocks.isNotEmpty()){
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: PlayerRespawnEvent){
        val p = e.player
        object : BukkitRunnable(){
            override fun run() {
                if(p.hasSave) return
                p.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 5 * 20, 5))
                p.addPotionEffect(PotionEffect(PotionEffectType.UNLUCK, 30 * 20, 5))
            }
        }.runTaskLater(plugin, 3)
    }
}