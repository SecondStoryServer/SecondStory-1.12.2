package me.syari.sec_story.paper.core.guild

import me.syari.sec_story.paper.core.guild.Guild.getGuild
import me.syari.sec_story.paper.core.plugin.SQL.sql
import org.bukkit.OfflinePlayer
import java.util.*

open class GuildOfflinePlayer(private val offlinePlayer: OfflinePlayer) {
    var guildID: UUID? = null

    init {
        sql {
            val res = executeQuery(
                "SELECT Guild FROM Story.PlayerData WHERE UUID = '${offlinePlayer.uniqueId}' LIMIT 1;"
            )
            if(res.next()) {
                val s = res.getString(1)
                if(s != null) {
                    guildID = UUID.fromString(s)
                }
            }
        }
    }

    fun guild() = getGuild(guildID)

    private var rawWin: Int? = null

    var win: Int
        get() {
            val tmp = rawWin
            return if(tmp != null) {
                tmp
            } else {
                var w = 0
                sql {
                    val res = executeQuery(
                        "SELECT WarWin FROM Story.PlayerData WHERE UUID = '${offlinePlayer.uniqueId}';"
                    )
                    if(res.next()) {
                        w = res.getInt("WarWin")
                    }
                }
                rawWin = w
                w
            }
        }
        set(value) {
            sql {
                executeUpdate(
                    "UPDATE Story.PlayerData SET WarWin = ${if(0 < value) value else 0} WHERE UUID = '${offlinePlayer.uniqueId}';"
                )
            }
            rawWin = value
        }
}