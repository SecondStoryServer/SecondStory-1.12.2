package me.syari.sec_story.guild.war

import me.syari.sec_story.data.SaveData.saveInventory
import me.syari.sec_story.data.SaveData.saveLocation
import me.syari.sec_story.guild.GuildData
import me.syari.sec_story.guild.war.GuildWar.setKit
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.player.Money.hasMoney
import me.syari.sec_story.player.Money.money
import me.syari.sec_story.server.Server.hideBoard
import org.bukkit.Location
import org.bukkit.entity.Player

class WarGuild(val guild: GuildData, val team: WarTeam) {
    private var warMember = mutableListOf<WarPlayer>()

    fun getMember() = warMember

    fun getLivingMember() = warMember.filter { f -> !f.isDead(guild.war?.life ?: 1) }

    fun addMember(player: Player){
        if(warMember.firstOrNull { f -> f.player == player } != null) return
        warMember.add(WarPlayer(player, this))
    }

    fun removeMember(player: Player){
        warMember.removeIf { r -> r.player == player }
    }

    fun removeMember(players: Iterable<WarPlayer>){
        warMember.removeAll(players)
    }

    private var spawn: Location? = null

    fun getSpawn() = spawn

    fun setSpawn(loc: Location){
        spawn = loc
    }

    private var readyOK = false

    fun readyOK(){
        val war = guild.war ?: return
        val enemy = guild.warEnemyGuild ?: return
        readyOK = !readyOK
        if(readyOK){
            if(enemy.readyOK){
                war.ready()
            } else {
                war.announce("&7 >> &b${guild.name}&fの戦争の準備が完了しました")
            }
        } else {
            war.announce("&7 >> &b${guild.name}&fの準備完了が取り消されました")
        }
    }

    fun checkReady(): Boolean{
        val war = guild.war ?: return false
        warMember = warMember.filter { m ->
            val p = m.player
            if(!p.isOnline || p.isDead) {
                false
            } else {
                if(!p.hasMoney(war.cost)){
                    war.announce("&7 >> &f参加費を持っていないプレイヤーがいたので&a0JPY&fに設定されました")
                    war.cost = 0
                }
                true
            }
        }.toMutableList()
        if(warMember.isEmpty()){
            war.announce("&7 >> &cギルド戦争は人数不足で取り消されました")
            return false
        }
        return true
    }
    
    fun ready(){
        val war = guild.war ?: return
        warMember.forEach{ m ->
            val p = m.player
            if(war.cost != 0L){
                p.money -= war.cost
                p.send("&b[Guild] &f参加費として&a${String.format("%,d", war.cost)}JPY&fを支払いました")
            }
            p.closeInventory()
            p.saveInventory()
            p.saveLocation()
            p.hideBoard = true
            m.nowWar = true
            m.tpSpawn()
            p.setKit(null, team)
        }
    }
}