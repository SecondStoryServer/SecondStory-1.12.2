package me.syari.sec_story.paper.core.guild.war

import me.syari.sec_story.paper.core.guild.war.GuildWar.isEnableBoard
import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.command.CommandCancel
import me.syari.sec_story.paper.core.command.CommandCancel.clearDisAllowCmd
import me.syari.sec_story.paper.core.data.SaveData.loadSave
import me.syari.sec_story.paper.core.guild.Guild.guild
import me.syari.sec_story.paper.core.guild.Guild.guildPlayer
import me.syari.sec_story.paper.core.item.GiveItem.give
import me.syari.sec_story.paper.core.player.Money.money
import me.syari.sec_story.paper.library.message.SendMessage.broadcast
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.round

data class WarPlayer(val player: Player, val warGuild: WarGuild) {
    var death = 0
    var nowWar = false

    fun isDead(life: Int) = life <= death

    fun leave() {
        nowWar = false
        player.clearDisAllowCmd(CommandCancel.CommandAddCause.GuildWar)
        runLater(plugin, 5) {
            player.loadSave()
            player.isEnableBoard = false
        }
    }

    fun tpSpawn() {
        val loc = warGuild.spawn ?: return
        loc.pitch = player.location.pitch
        loc.yaw = player.location.yaw
        player.teleport(loc)
    }

    fun onDeath(quit: Boolean = false) {
        val guild = player.guild ?: return
        val war = guild.war ?: return
        if(quit) {
            death = war.life
        } else {
            death ++
        }
        val killer = player.killer?.displayName
        war.announce(
            "&7 >> ${if(killer != null) "&b$killer&fによって" else ""}&b${player.displayName}&fの残機が&b${war.life - death}&fになりました"
        )
        if(isDead(war.life)) {
            if(warGuild.livingMember.isEmpty()) {
                if(quit) {
                    broadcast("&7 >> &b${war.redGuildData.name}&fと&b${war.blueGuildData.name}&fの戦争は引き分けでした")
                } else {
                    val winner = if(warGuild.team == WarTeam.RED) war.blueGuild else war.redGuild
                    broadcast("&7 >> &b${guild.warEnemyGuild?.guild?.name}&fが&b${guild.name}&fに戦争で勝ちました")
                    val money = round(1.3 * war.cost * war.allWarMember.size / winner.member.size).toLong()
                    runLater(plugin, 10) {
                        winner.member.forEach { m ->
                            val p = m.player
                            if(100000 <= war.cost) {
                                p.give(GuildWar.reward, postName = "&a戦争報酬", postPeriod = 7)
                                p.send("&b[Guild] &f戦争の報酬でアイテムを手に入れました")
                            } else {
                                p.send("&b[Guild] &c掛け金10万JPYからアイテムが手に入ります")
                            }
                            p.money += money
                            val gp = p.guildPlayer
                            gp.win ++
                            p.send("&b[Guild] &f戦争の報酬で&a${String.format("%,d", money)}JPY&fを手に入りました")
                        }
                    }
                }
                war.end()
            } else {
                leave()
                war.bar.removePlayer(player)
            }
        } else {
            if(nowWar) {
                runLater(plugin, 3) {
                    tpSpawn()
                    player.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 8 * 20, 5))
                }
            }
        }
        war.updateBoard()
    }
}