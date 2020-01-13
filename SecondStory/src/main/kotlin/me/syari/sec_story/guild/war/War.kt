package me.syari.sec_story.guild.war

import me.syari.sec_story.guild.Guild.guild
import me.syari.sec_story.guild.GuildData
import me.syari.sec_story.guild.event.GuildWarStartEvent
import me.syari.sec_story.guild.war.GuildWar.kitSel
import me.syari.sec_story.guild.war.GuildWar.wars
import me.syari.sec_story.lib.CreateBossBar.createBossBar
import me.syari.sec_story.lib.CreateScoreBoard.createBoard
import me.syari.sec_story.lib.message.SendMessage.broadcast
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.message.SendMessage.title
import me.syari.sec_story.plugin.Plugin.plugin
import me.syari.sec_story.server.CommandBlock
import me.syari.sec_story.server.CommandBlock.addDisAllowCmd
import org.bukkit.attribute.Attribute
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class War(val redGuildData: GuildData, val blueGuildData: GuildData){
    val redGuild = WarGuild(redGuildData, WarTeam.RED)
    val blueGuild = WarGuild(blueGuildData, WarTeam.BLUE)

    init {
        redGuildData.setWar(this, redGuild, blueGuild)
        blueGuildData.setWar(this, blueGuild, redGuild)
    }

    fun getAllWarMember() = redGuild.getMember() + blueGuild.getMember()

    fun getMember(player: Player): WarPlayer?{
        getAllWarMember().forEach { m ->
            if(m.player == player) return m
        }
        return null
    }

    fun containPlayer(player: Player): Boolean {
        getAllWarMember().forEach { m ->
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

    fun addMember(p: Player){
        val g = p.guild ?: return
        val wg = g.warGuild ?: return
        wg.addMember(p)
    }

    fun remMember(p: Player){
        val g = p.guild ?: return
        val wg = g.warGuild ?: return
        wg.removeMember(p)
    }

    val bar = createBossBar("&b&l戦争開始まで", BarColor.BLUE, BarStyle.SOLID)

    fun ready(){
        val field = useField
        GuildWarStartEvent(redGuildData).callEvent()
        GuildWarStartEvent(blueGuildData).callEvent()
        if(field == null){
            announce("&7 >> &cギルド戦争のフィールドを設定出来ませんでした")
            end()
        } else if(redGuild.checkReady() && blueGuild.checkReady()){
            phase = WarPhase.Ready
            redGuild.ready()
            redGuild.setSpawn(field.redSpawn)
            blueGuild.ready()
            blueGuild.setSpawn(field.blueSpawn)
            showBoard()
            broadcast("&7 >> &b${redGuildData.name}&7(${redGuild.getMember().size})&fと&b${blueGuildData.name}&7(${blueGuild.getMember().size})&fの戦争が始まります")
            var cnt = 30
            getAllWarMember().forEach { m ->
                bar.addPlayer(m.player)
                m.player.addDisAllowCmd(CommandBlock.CommandAddCause.GuildWar, "*")
                m.tpSpawn()
                m.player.activePotionEffects.clear()
            }
            object : BukkitRunnable(){
                override fun run(){
                    if(!wars.contains(this@War)) {
                        cancel()
                        return
                    }
                    if(cnt == 0) {
                        start()
                        cancel()
                        return
                    }
                    bar.progress = cnt / 30.0
                    cnt --
                }
            }.runTaskTimer(plugin, 0, 20)
        } else {
            end()
        }
    }

    private var rem = 0

    fun start(){
        phase = WarPhase.Now
        getAllWarMember().forEach { m ->
            val p = m.player
            p.closeInventory()
            p.inventory.remove(kitSel)
            p.health = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).value
            p.foodLevel = 20
            p.title("&b&l戦争開始！！", "", 0, 50, 0)
        }
        bar.title = "&b&l戦争終了まで"
        rem = time * 60
        object : BukkitRunnable(){
            override fun run(){
                showBoard()
                if(!wars.contains(this@War)) {
                    cancel()
                    bar.delete()
                    return
                }
                if(rem == 0){
                    end()
                    broadcast("&7 >> &b${redGuildData.name}&fと&b${blueGuildData.name}&fの戦争は引き分けでした")
                    bar.delete()
                    cancel()
                    return
                }
                bar.progress = rem / (time * 60.0)
                rem --
            }
        }.runTaskTimer(plugin, 0, 20)
    }

    fun showBoard(){
        getAllWarMember().forEach { m ->
            val p = m.player
            createBoard("&b&lGuild War", p,
                    -1 to "&e&m------------------------&1",
                    -2 to "&a&l残り時間 &7≫ &e%02d:%02d&2".format(rem / 60, rem % 60),
                    -3 to "&3",
                    -4 to "&a&l残り人数 &7≫ &c${redGuild.getLivingMember().size}人 &7- &b${blueGuild.getLivingMember().size}人&4",
                    -5 to "&5",
                    -6 to "&a&l残機 &7≫ &e${life - m.death}&6",
                    -7 to "&e&m------------------------&7"
            )
        }
    }

    fun end(){
        getAllWarMember().forEach { m ->
            m.leave()
        }
        bar.delete()
        wars = wars.filter { w -> w != this }.toMutableSet()
        redGuildData.endWar()
        blueGuildData.endWar()
        useField?.isUsed = false
    }

    fun announce(msg: String){
        if(phase == WarPhase.Wait){
            redGuildData.member.forEach { m ->
                m.player.send(msg)
            }
            blueGuildData.member.forEach { m ->
                m.player.send(msg)
            }
        } else {
            getAllWarMember().forEach { m ->
                m.player.send(msg)
            }
        }
    }
}