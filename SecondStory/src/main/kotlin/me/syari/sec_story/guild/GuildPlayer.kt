package me.syari.sec_story.guild

import me.syari.sec_story.guild.Guild.guilds
import org.bukkit.Chunk
import org.bukkit.entity.Player
import java.util.*

class GuildPlayer(val player: Player) : GuildOfflinePlayer(player) {
    var enterGuild: GuildData? = null

    var isAreaSelectMode = false
        get() {
            return if(guildID != null){
                field
            } else {
                false
            }
        }

    var selectPos1: Chunk? = null

    var selectPos2: Chunk? = null

    fun getSelectChunk(): Set<Chunk>{
        val f = selectPos1 ?: return setOf()
        val s = selectPos2 ?: return setOf()
        if(f.world != s.world) return setOf()
        val x = if(f.x < s.x) f.x to s.x else s.x to f.x
        val z = if(f.z < s.z) f.z to s.z else s.z to f.z
        val ret = mutableSetOf<Chunk>()
        for(fx in x.first .. x.second){
            for(fz in z.first .. z.second){
                ret.add(f.world.getChunkAt(fx, fz))
            }
        }
        return ret
    }

    private val tpReq = mutableListOf<UUID>()

    fun hasTpReq(from: Player) = tpReq.contains(from.uniqueId)

    fun addTpReq(from: Player){
        tpReq.add((from.uniqueId))
    }

    fun removeTpReq(from: Player){
        tpReq.remove(from.uniqueId)
    }

    fun clearTpReq(){
        tpReq.clear()
    }

    fun clearGuildInvite(){
        guilds.forEach { g ->
            g.removeInvite(player)
        }
    }
}