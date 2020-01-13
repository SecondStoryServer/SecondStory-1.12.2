package me.syari.sec_story.paper.core.guild.war

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.game.kit.GameKit.getKitsWithFilter
import me.syari.sec_story.paper.core.game.kit.GameKitData
import me.syari.sec_story.paper.core.guild.Guild.guild
import me.syari.sec_story.paper.core.guild.GuildData
import me.syari.sec_story.paper.core.guild.event.GuildMemberTeleportEvent
import me.syari.sec_story.paper.library.config.CreateConfig.config
import me.syari.sec_story.paper.library.display.CreateBossBar.createBossBar
import me.syari.sec_story.paper.library.event.PlayerDeathWithCtEvent
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.inv.CreateInventory.inventory
import me.syari.sec_story.paper.library.item.CustomItemStack
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runRepeatTimes
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runTimer
import me.syari.sec_story.paper.library.scoreboard.CreateScoreBoard
import me.syari.sec_story.paper.library.scoreboard.CreateScoreBoard.createBoard
import me.syari.sec_story.paper.library.scoreboard.ScoreBoardPriority
import org.bukkit.Material
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack

object GuildWar: EventInit {
    fun Player.sendWar(from: GuildData, guild: GuildData) {
        when {
            from.hadWarInvite(guild) -> {
                val war = War(from, guild)
                wars.add(war)
                from.clearWarInvite()
                guild.clearWarInvite()
                from.announce("&b[Guild] &a${guild.name}&fとの戦争が始まります")
                guild.announce("&b[Guild] &a${from.name}&fとの戦争が始まります")
                val bar = createBossBar("&f&l戦争が始まります &b&l/guild war join &f&lで参加しましょう", BarColor.BLUE, BarStyle.SOLID)
                bar.addAllPlayer(from.offlineMember)
                bar.addAllPlayer(guild.offlineMember)
                runRepeatTimes(plugin, 20, 60) {
                    bar.progress = repeatRemain / 60.0
                }?.onEndRepeat {
                    war.ready()
                    bar.delete()
                }
            }
            guild.hadWarInvite(from) -> {
                send("&b[Guild] &c既に宣戦布告しています")
            }
            else -> {
                guild.addWarInvite(from)
                from.announce("&b[Guild] &a${guild.name}&fに宣戦布告しました")
                guild.announce("&b[Guild] &a${from.name}&fから宣戦布告されました")
                runLater(plugin, 60 * 20) {
                    if(guild.hadWarInvite(from)) {
                        guild.removeWarInvite(from)
                        from.announce("&b[Guild] &a${guild.name}&fからの宣戦布告が取り消されました")
                        guild.announce("&b[Guild] &a${from.name}&fへの宣戦布告が取り消されました")
                    }
                }
            }
        }
    }

    var useKits = listOf<GameKitData>()
        private set
    var reward = listOf<CustomItemStack>()

    fun CommandSender.loadGuildWarConfig() {
        config(plugin, "Guild/War/config.yml", false) {
            output = this@loadGuildWarConfig

            useKits = getKitsWithFilter(getStringList("kit", listOf()))
            reward = getCustomItemStackListFromStringList("reward", listOf())
        }

        config(plugin, "Guild/War/field.yml", false) {
            output = this@loadGuildWarConfig

            val newFields = mutableMapOf<String, WarField>()
            getSection("")?.forEach { f ->
                val rL = getLocation("$f.Red")
                val bL = getLocation("$f.Blue")
                if(rL != null && bL != null) {
                    newFields[f] = WarField(rL, bL)
                } else {
                    send("&cGuildField - $f Location error")
                }
            }
            WarField.list = newFields
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: FoodLevelChangeEvent) {
        val p = e.entity as Player
        if(p.nowWar()) e.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: InventoryClickEvent) {
        val p = e.whoClicked as? Player ?: return
        if(e.slotType == InventoryType.SlotType.ARMOR && p.nowWar()) e.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: PlayerDropItemEvent) {
        val p = e.player
        if(p.nowWar()) e.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: EntityPickupItemEvent) {
        val p = e.entity as? Player ?: return
        if(p.nowWar()) e.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: PlayerInteractEvent) {
        val p = e.player
        if(p.nowWar() && e.action == Action.LEFT_CLICK_BLOCK) e.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: EntityRegainHealthEvent) {
        val p = e.entity as? Player ?: return
        if(p.nowWar() && (e.regainReason == EntityRegainHealthEvent.RegainReason.REGEN || e.regainReason == EntityRegainHealthEvent.RegainReason.SATIATED)) e.isCancelled = true
    }

    @EventHandler
    fun on(e: PlayerQuitEvent) {
        val p = e.player
        if(p.nowWar()) {
            val m = p.warPlayer ?: return
            m.onDeath(true)
        }
    }

    @EventHandler
    fun on(e: PlayerDeathWithCtEvent) {
        val p = e.player
        if(! p.nowWar()) return
        val m = p.warPlayer ?: return
        e.isCancelled = true
        m.onDeath()
    }

    fun Player.waitWarGuild(): Boolean {
        val w = guild?.war ?: return false
        return w.phase == WarPhase.Wait
    }

    fun Player.nowWar(): Boolean {
        val wp = warPlayer ?: return false
        return wp.nowWar
    }

    private val Player.war get() = wars.firstOrNull { it.containPlayer(this) }

    private val Player.warPlayer
        get(): WarPlayer? {
            wars.forEach { w ->
                w.getMember(this)?.let { return it }
            }
            return null
        }

    var wars = mutableSetOf<War>()

    val kitSel
        get(): ItemStack {
            val item = CustomItemStack(Material.STICK, 1)
            item.display = "&6キット選択"
            return item.toOneItemStack
        }

    private fun Player.openSelect() {
        if(war?.phase != WarPhase.Ready) return
        val wp = warPlayer ?: return
        val team = wp.warGuild.team
        inventory("&b&lキット選択") {
            useKits.forEach {
                item(it.icon).event(ClickType.LEFT) {
                    setKit(it, team)
                }
            }
        }.open(this)
    }

    fun Player.setKit(data: GameKitData?, team: WarTeam) {
        (data ?: useKits.random()).setKit(this@setKit)
        inventory.setItem(39, team.helmet)
        inventory.setItem(8, kitSel)
    }

    val board = createBoard(plugin, "&b&lGuild War", ScoreBoardPriority.High) {
        line("&e&m------------------------")
        line {
            val war = war ?: return@line ""
            "&a&l残り時間 &7≫ &e" + war.remainTime
        }
        space()
        line {
            val war = war ?: return@line ""
            "&a&l残り人数 &7≫ &c${war.redGuild.livingMember.size}人 &7- &b${war.blueGuild.livingMember.size}人"
        }
        space()
        line {
            val war = war ?: return@line ""
            val m = war.getMember(this) ?: return@line ""
            "&a&l残機 &7≫ &e${war.life - m.death}"
        }
        line("&e&m------------------------")
    }

    var Player.isEnableBoard: Boolean
        get() = board.containsPlayer(this)
        set(value) {
            if(value){
                board.addPlayer(this)
            } else {
                board.removePlayer(this)
            }
        }

    @EventHandler
    fun onSelKit(e: PlayerInteractEvent) {
        if(e.hasItem()) {
            if(e.item.isSimilar(kitSel)) {
                e.player.openSelect()
            }
        }
    }

    @EventHandler
    fun on(e: PlayerMoveEvent) {
        val p = e.player
        val g = p.guild ?: return
        val w = g.war ?: return
        if(w.phase != WarPhase.Ready) return
        if(w.containPlayer(p) && (e.from.x != e.to.x || e.from.y != e.to.y || e.from.z != e.to.z)) {
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onReady(e: EntityDamageEvent) {
        val p = e.entity as? Player ?: return
        val g = p.guild ?: return
        val w = g.war ?: return
        if(w.phase == WarPhase.Ready && w.containPlayer(p)) {
            e.isCancelled = true
        }
    }

    fun forceEnd() {
        wars.forEach { f ->
            f.end()
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: GuildMemberTeleportEvent) {
        val p = e.player
        val t = e.target
        if(p.nowWar() || t.nowWar()) e.isCancelled = true
    }
}