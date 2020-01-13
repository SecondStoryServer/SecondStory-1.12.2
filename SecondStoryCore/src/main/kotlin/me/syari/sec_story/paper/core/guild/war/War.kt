package me.syari.sec_story.paper.core.guild.war

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.command.CommandCancel
import me.syari.sec_story.paper.core.command.CommandCancel.addDisAllowCmd
import me.syari.sec_story.paper.core.guild.Guild.guild
import me.syari.sec_story.paper.core.guild.GuildData
import me.syari.sec_story.paper.core.guild.event.GuildWarStartEvent
import me.syari.sec_story.paper.core.guild.war.GuildWar.board
import me.syari.sec_story.paper.core.guild.war.GuildWar.kitSel
import me.syari.sec_story.paper.core.guild.war.GuildWar.wars
import me.syari.sec_story.paper.library.display.CreateBossBar.createBossBar
import me.syari.sec_story.paper.library.message.SendMessage.broadcast
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.message.SendMessage.title
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runRepeatTimes
import org.bukkit.attribute.Attribute
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player

class War(val redGuildData: GuildData, val blueGuildData: GuildData) {
    val redGuild = WarGuild(redGuildData, WarTeam.RED)
    val blueGuild = WarGuild(blueGuildData, WarTeam.BLUE)

    init {
        redGuildData.setWar(this, redGuild, blueGuild)
        blueGuildData.setWar(this, blueGuild, redGuild)
    }

    val allWarMember get() = redGuild.member + blueGuild.member

    fun getMember(player: Player): WarPlayer? {
        allWarMember.forEach { m ->
            if(m.player == player) return m
        }
        return null
    }

    fun containPlayer(player: Player): Boolean {
        allWarMember.forEach { m ->
            if(m.player == player) return true
        }
        return false
    }

    var phase = WarPhase.Wait
    var life = 3
    var time = 5
    var cost = 100000L
    var useField = WarField.random()
        set(value) {
            field?.isUsed = false
            field = value
            value?.isUsed = true
        }

    fun addMember(p: Player) {
        val g = p.guild ?: return
        val wg = g.warGuild ?: return
        wg.addMember(p)
    }

    fun remMember(p: Player) {
        val g = p.guild ?: return
        val wg = g.warGuild ?: return
        wg.removeMember(p)
    }

    val bar = createBossBar("&b&l戦争開始まで", BarColor.BLUE, BarStyle.SOLID)

    fun ready() {
        val field = useField
        GuildWarStartEvent(redGuildData).callEvent()
        GuildWarStartEvent(blueGuildData).callEvent()
        if(field == null) {
            announce("&7 >> &cギルド戦争のフィールドを設定出来ませんでした")
            end()
        } else if(redGuild.checkReady() && blueGuild.checkReady()) {
            phase = WarPhase.Ready
            redGuild.ready()
            redGuild.setSpawn(field.redSpawn)
            blueGuild.ready()
            blueGuild.setSpawn(field.blueSpawn)
            updateBoard()
            broadcast(
                "&7 >> &b${redGuildData.name}&7(${redGuild.member.size})&fと&b${blueGuildData.name}&7(${blueGuild.member.size})&fの戦争が始まります"
            )
            allWarMember.forEach { m ->
                bar.addPlayer(m.player)
                m.player.addDisAllowCmd(CommandCancel.CommandAddCause.GuildWar, "*")
                m.tpSpawn()
                m.player.activePotionEffects.clear()
            }
            runRepeatTimes(plugin, 20, 30) {
                if(!wars.contains(this@War)) {
                    bar.delete()
                    cancel()
                    return@runRepeatTimes
                }
                bar.progress = repeatRemain / 30.0
            }?.onEndRepeat {
                start()
            }
        } else {
            end()
        }
    }

    fun updateBoard(){
        allWarMember.forEach { m ->
            board.updatePlayer(m.player)
        }
    }

    private var rem = 0

    val remainTime get() = "%02d:%02d".format(rem / 60, rem % 60)

    fun start() {
        phase = WarPhase.Now
        allWarMember.forEach { m ->
            val p = m.player
            p.closeInventory()
            p.inventory.remove(kitSel)
            p.health = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).value
            p.foodLevel = 20
            p.title("&b&l戦争開始！！", "", 0, 50, 0)
        }
        bar.title = "&b&l戦争終了まで"
        runRepeatTimes(plugin, 20, time * 60) {
            updateBoard()
            if(! wars.contains(this@War)) {
                cancel()
                bar.delete()
                return@runRepeatTimes
            }
            bar.progress = repeatRemain / (time * 60.0)
        }?.onEndRepeat {
            end()
            broadcast("&7 >> &b${redGuildData.name}&fと&b${blueGuildData.name}&fの戦争は引き分けでした")
            bar.delete()
        }
    }

    fun end() {
        allWarMember.forEach { m ->
            m.leave()
        }
        bar.delete()
        wars = wars.filter { w -> w != this }.toMutableSet()
        redGuildData.endWar()
        blueGuildData.endWar()
        useField?.isUsed = false
    }

    fun announce(msg: String) {
        if(phase == WarPhase.Wait) {
            redGuildData.member.forEach { m ->
                m.player.send(msg)
            }
            blueGuildData.member.forEach { m ->
                m.player.send(msg)
            }
        } else {
            allWarMember.forEach { m ->
                m.player.send(msg)
            }
        }
    }
}