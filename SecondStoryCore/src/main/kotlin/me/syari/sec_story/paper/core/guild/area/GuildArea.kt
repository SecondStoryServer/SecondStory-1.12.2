package me.syari.sec_story.paper.core.guild.area

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.guild.Guild
import me.syari.sec_story.paper.core.guild.GuildData
import me.syari.sec_story.paper.core.plugin.SQL
import me.syari.sec_story.paper.library.config.CreateConfig.config
import org.bukkit.Chunk
import org.bukkit.command.CommandSender
import java.util.*

object GuildArea {
    var buyPrice = 500000L
    var sellPrice = 400000L
    var canBuyWorld = listOf<String>()

    fun CommandSender.loadGuildAreaConfig() {
        config(plugin, "Guild/area.yml") {
            output = this@loadGuildAreaConfig

            buyPrice = getLong("buy", 500000)
            sellPrice = getLong("sell", 400000)
            canBuyWorld = getStringList("world", listOf())
        }
    }

    private val guildFromChunk = mutableMapOf<Chunk, GuildData?>()

    fun setGuild(chunk: Chunk, guild: GuildData?) {
        guildFromChunk[chunk] = guild
    }

    fun getGuild(chunk: Chunk?): GuildData? {
        if(chunk == null) return null
        return guildFromChunk.getOrPut(chunk) {
            var uuid: UUID? = null
            SQL.sql {
                val res = executeQuery(
                    "SELECT GuildID FROM Story.GuildArea WHERE World = '${chunk.world.name}' and X = ${chunk.x} and Z = ${chunk.z};"
                )
                if(res.next()) uuid = UUID.fromString(res.getString(1))
            }
            Guild.getGuild(uuid)
        }
    }
}