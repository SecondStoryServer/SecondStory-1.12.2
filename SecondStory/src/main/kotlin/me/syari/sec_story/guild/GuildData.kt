package me.syari.sec_story.guild

import me.syari.sec_story.guild.Guild.guildFromName
import me.syari.sec_story.guild.Guild.guildPlayer
import me.syari.sec_story.guild.Guild.guilds
import me.syari.sec_story.guild.altar.AltarData
import me.syari.sec_story.guild.altar.GuildAltar.getDataFromExp
import me.syari.sec_story.guild.area.GuildArea.sellPrice
import me.syari.sec_story.guild.area.GuildArea.setGuild
import me.syari.sec_story.guild.quest.GuildQuest.getDailyQuest
import me.syari.sec_story.guild.quest.GuildQuest.getWeeklyQuest
import me.syari.sec_story.guild.quest.QuestGuildData
import me.syari.sec_story.guild.war.War
import me.syari.sec_story.guild.war.WarGuild
import me.syari.sec_story.lib.PlayerPlus.lastPlayedToDay
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.player.Money.money
import me.syari.sec_story.plugin.Plugin.plugin
import me.syari.sec_story.plugin.SQL.sql
import org.bukkit.Chunk
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.*

class GuildData(val id: UUID) {
    private var rawName: String? = null

    var name: String
        set(value) {
            val preName = rawName
            if(preName != null && guildFromName.containsKey(preName)){
                guildFromName.remove(preName)
                guildFromName[value] = this
            }
            this.rawName = value
            sql {
                executeUpdate("UPDATE Story.Guild SET Name = '$value' WHERE GuildID = '$id';")
            }
        }
        get() {
            return if(rawName != null){
                rawName
            } else {
                var ret: String? = null
                sql {
                    val res = executeQuery("SELECT Name FROM Story.Guild WHERE GuildID = '$id' LIMIT 1;")
                    if(res.next()) ret = res.getString("Name")
                }
                rawName = ret
                ret
            } ?: "GuildName"
        }

    private var inviteMember = mutableListOf<UUID>()

    fun hadInvite(p: Player) = inviteMember.contains(p.uniqueId)

    fun addInvite(p: Player){
        inviteMember.add(p.uniqueId)
    }

    fun removeInvite(p: Player){
        inviteMember.remove(p.uniqueId)
    }

    fun canJoin(p: Player) = inviteMember.contains(p.uniqueId)

    private var rawMember: MutableList<UUID>? = null

    fun isMember(p: OfflinePlayer) = offlineMember.contains(p)

    fun addMember(p: Player){
        val tmp = mutableListOf<UUID>()
        offlineMember.forEach { o ->
            tmp.add(o.uniqueId)
        }
        tmp.add(p.uniqueId)
        sql {
            executeUpdate("UPDATE Story.PlayerData SET Guild = '$id' WHERE UUID = '${p.uniqueId}';")
        }
        rawMember = tmp
    }

    fun removeMember(p: OfflinePlayer){
        if(isLeader(p)){
            deleteGuild()
        } else {
            val tmp = mutableListOf<UUID>()
            offlineMember.forEach { o ->
                tmp.add(o.uniqueId)
            }
            tmp.remove(p.uniqueId)
            sql {
                executeUpdate("UPDATE Story.PlayerData SET Guild = null WHERE UUID = '${p.uniqueId}';")
            }
            if(p is Player){
                p.guildPlayer.guildID = null
            }
            rawMember = tmp
        }
    }

    fun deleteGuild(){
        val tmp = leader
        if(tmp != null){
            tmp.money += money + areas.size * sellPrice
        }
        member.forEach { m ->
            val gp = m.guildPlayer
            gp.guildID = null
            gp.clearTpReq()
        }
        sql {
            executeUpdate("DELETE FROM Story.Guild WHERE GuildID = '$id';")
            executeUpdate("DELETE FROM Story.GuildArea WHERE GuildID = '$id';")
            executeUpdate("UPDATE Story.PlayerData SET Guild = null WHERE Guild = '$id';")
        }
        guilds.remove(this)
    }

    val member: List<Player>
        get() {
            val ret = mutableListOf<Player>()
            offlineMember.forEach { p ->
                if(p.isOnline) ret.add(p.player)
            }
            return ret
        }

    val offlineMember: List<OfflinePlayer>
        get() {
            val tmp = rawMember
            return if(tmp != null){
                val ret = mutableListOf<OfflinePlayer>()
                tmp.forEach { uuid ->
                    val p = plugin.server.getOfflinePlayer(uuid)
                    if(p != null) {
                        ret.add(p)
                    }
                }
                ret
            } else {
                val list = mutableListOf<OfflinePlayer>()
                val save = mutableListOf<UUID>()
                sql {
                    val res = executeQuery("SELECT UUID FROM Story.PlayerData WHERE Guild = '$id';")
                    while (res.next()) {
                        val uuid = UUID.fromString(res.getString("UUID"))
                        if (uuid != null){
                            val p = plugin.server.getOfflinePlayer(uuid)
                            if(p != null){
                                list.add(p)
                                save.add(uuid)
                            }
                        }
                    }
                }
                rawMember = save
                list
            }
        }

    fun getMember(name: String): Player? {
        return member.firstOrNull { f -> f.name == name }
    }

    fun getOfflineMember(name: String): OfflinePlayer? {
        return offlineMember.firstOrNull{ f -> f.name == name}
    }

    private var rawLeader: UUID? = null

    var leader: OfflinePlayer?
        set(value) {
            rawLeader = value?.uniqueId
            if(value != null){
                sql {
                    executeUpdate("UPDATE Story.Guild SET Leader = '${value.name}', LeaderUUID = '${value.uniqueId}' WHERE GuildID = '$id';")
                }
            }
        }
        get() {
            val tmp = rawLeader
            return if(tmp != null){
                plugin.server.getOfflinePlayer(tmp)
            } else {
                var ret: OfflinePlayer? = null
                sql {
                    val res = executeQuery("SELECT LeaderUUID FROM Story.Guild WHERE GuildID = '$id';")
                    if(res.next()) {
                        val rawUnique = res.getString(1)
                        ret = if(rawUnique.split("-".toRegex()).size == 5){
                            val uuid = UUID.fromString(rawUnique)
                            plugin.server.getOfflinePlayer(uuid)
                        } else null
                    }
                }
                rawLeader = ret?.uniqueId
                ret
            }
        }

    fun isLeader(p: OfflinePlayer) = leader == p

    private var rawFF: Boolean? = null

    var ff: Boolean
        set(value) {
            rawFF = value
            sql {
                executeUpdate("UPDATE Story.Guild SET FF = $value WHERE GuildID = '$id';")
            }
        }
        get() {
            val tmp = rawFF
            return if(tmp != null){
                tmp
            } else {
                var ret = false
                sql {
                    val res = executeQuery("SELECT FF FROM Story.Guild WHERE GuildID = '$id';")
                    if(res.next()) ret = res.getBoolean(1)
                }
                rawFF = ret
                ret
            }
        }

    fun announce(msg: String){
        member.forEach { m ->
            m.send(msg)
        }
    }

    private var rawMoney: Long? = null

    var money: Long
        get() {
            val tmp = rawMoney
            return if(tmp != null){
                tmp
            } else {
                var ret = 0L
                sql {
                    val res = executeQuery("SELECT Money FROM Story.Guild WHERE GuildID = '$id';")
                    if (res.next()) ret = res.getLong(1)
                }
                rawMoney = ret
                ret
            }
        }
        set(value) {
            rawMoney = value
            sql {
                executeUpdate("UPDATE Story.Guild SET Money = $value WHERE GuildID = '$id';")
            }
        }

    fun hasMoney(v: Long) = money >= v

    private var rawPoint: Int? = null

    var point: Int
        get() {
            val tmp = rawPoint
            return if(tmp != null){
                tmp
            } else {
                var ret = 0
                sql {
                    val res = executeQuery("SELECT Point FROM Story.Guild WHERE GuildID = '$id';")
                    if (res.next()) ret = res.getInt(1)
                }
                point = ret
                ret
            }
        }
        set(value) {
            rawPoint = value
            sql {
                executeUpdate("UPDATE Story.Guild SET Point = $value WHERE GuildID = '$id';")
            }
        }

    private var rawAreas: Set<Chunk>? = null

    private val areas: Set<Chunk>
        get() {
            val tmp = rawAreas
            return if(tmp != null){
                tmp
            } else {
                val ret = mutableSetOf<Chunk>()
                val loadedWorld = mutableMapOf<String, World>()
                sql {
                    val res = executeQuery("SELECT World, X, Z FROM Story.GuildArea WHERE GuildID = '$id';")
                    while(res.next()){
                        val w = res.getString("World")
                        val x = res.getInt("X")
                        val z = res.getInt("Z")
                        val world = loadedWorld[w]
                        if(world != null){
                            ret.add(world.getChunkAt(x, z))
                        } else {
                            val newWorld = plugin.server.getWorld(w)
                            if(newWorld != null){
                                loadedWorld[w] = newWorld
                                ret.add(newWorld.getChunkAt(x, z))
                            }
                        }
                    }
                }
                rawAreas = ret
                ret
            }
        }

    fun addArea(add: Chunk){
        val area = areas.toMutableSet()
        area.add(add)
        setGuild(add, this)
        sql {
            executeUpdate("INSERT Story.GuildArea VALUE ('${id}', '${add.world.name}', ${add.x}, ${add.z})")
        }
        rawAreas = area
    }

    fun removeArea(rem: Chunk){
        val area = areas.toMutableSet()
        area.remove(rem)
        setGuild(rem, null)
        sql {
            executeUpdate("DELETE FROM Story.GuildArea WHERE World = '${rem.world.name}' AND X = ${rem.x} AND Z = ${rem.z};")
        }
        rawAreas = area
    }

    val info: String
        get() {
            val leader = leader
            val member = offlineMember
            val data = altarData
            val s = StringBuilder()
            s.appendln("&b[Guild] &fギルド情報 &7- &6$name")
            s.appendln("&6ギルド所持金: &f${String.format("%,d", money)} JPY")
            s.appendln("&6祭壇レベル: &f${data.level} Lv")
            s.appendln("&6ギルドポイント: &f${String.format("%,d", point)} GP")
            s.appendln("&6所有土地数: &f${String.format("%,d", areas.size)}")
            s.appendln("&6リーダー: ${if(leader != null) (if(leader.isOnline) "&a" else "&7") + leader.name else "Admin"}")
            s.appendln("&6メンバー (${member.size} / ${data.maxMember}):")
            member.forEach { m ->
                s.appendln("  &7- ${if (m.isOnline) "&a${m.name}" else "&7${m.name} (最終ログイン: ${m.lastPlayedToDay}日前)"}")
            }
            return s.toString()
        }

    private val receiveWarInvite = mutableSetOf<GuildData>()

    fun hadWarInvite(from: GuildData) = receiveWarInvite.contains(from)

    fun addWarInvite(from: GuildData){
        if(receiveWarInvite.contains(from)) return
        receiveWarInvite.add(from)
    }

    fun removeWarInvite(from: GuildData){
        if(!receiveWarInvite.contains(from)) return
        receiveWarInvite.remove(from)
    }

    fun clearWarInvite(){
        receiveWarInvite.clear()
    }

    var war: War? = null

    var warGuild: WarGuild? = null

    var warEnemyGuild: WarGuild? = null

    fun setWar(war: War, warGuild: WarGuild, warEnemyGuild: WarGuild){
        this.war = war
        this.warGuild = warGuild
        this.warEnemyGuild = warEnemyGuild
    }

    fun endWar(){
        war = null
        warGuild = null
        warEnemyGuild = null
    }

    var rawWeeklyQuest: List<QuestGuildData>? = null

    val weeklyQuest: List<QuestGuildData>
        get() {
            val tmp = rawWeeklyQuest
            return if(tmp != null){
                tmp
            } else {
                val q = getWeeklyQuest()
                rawWeeklyQuest = q
                q
            }
        }

    var rawDailyQuest: List<QuestGuildData>? = null

    val dailyQuest: List<QuestGuildData>
        get() {
            val tmp = rawDailyQuest
            return if(tmp != null){
                tmp
            } else {
                val q = getDailyQuest()
                rawDailyQuest = q
                q
            }
        }

    private var rawAltarExp: Int? = null

    var altarExp: Int
        get() {
            val tmp = rawAltarExp
            return if(tmp != null){
                tmp
            } else {
                var ret = 0
                sql {
                    val res = executeQuery("SELECT AltarExp FROM Story.Guild WHERE GuildID = '$id';")
                    if (res.next()) ret = res.getInt(1)
                }
                rawAltarExp = ret
                ret
            }
        }
        set(value) {
            rawAltarExp = value
            sql {
                executeUpdate("UPDATE Story.Guild SET AltarExp = $value WHERE GuildID = '$id';")
            }
        }

    val altarData: AltarData
        get() {
            return getDataFromExp(altarExp)
        }

    fun clearCash(){
        rawName = null
        rawMember = null
        rawLeader = null
        rawFF = null
        rawMoney = null
        rawAreas = null
        rawDailyQuest = null
        rawWeeklyQuest = null
        rawPoint = null
        rawAltarExp = null
    }
}