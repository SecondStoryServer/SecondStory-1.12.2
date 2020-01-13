package me.syari.sec_story.paper.core.vote

import me.syari.sec_story.paper.core.item.GiveItem.give
import me.syari.sec_story.paper.core.plugin.SQL.sql
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.onlinePlayers
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.item.CustomItemStack
import me.syari.sec_story.paper.library.message.SendMessage.broadcast
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.server.Server.getOfflinePlayer
import org.bukkit.OfflinePlayer
import java.util.*

object Vote: FunctionInit {
    override fun init() {
        createCmd("testvote", tab { onlinePlayers }) { sender, args ->
            if(args.isNotEmpty) {
                vote(args[0])
                sender.send("&b[TestVote] &f送りました")
            } else {
                sender.send("&b[TestVote] &cプレイヤーを入力してください")
            }
        }
    }

    var items = listOf<CustomItemStack>()

    fun vote(user: String) {
        val player = getOfflinePlayer(user) ?: return
        if(player.name != user) return
        player.give(items, postName = "&b投票報酬", postPeriod = 14)
        broadcast("&7 >> &b&lVote &f&l$user &7https://minecraft.jp/servers/2nd-story.info/vote")
        player.voteCnt += 1
    }

    private val voteCnts = mutableMapOf<UUID, Int>()

    var OfflinePlayer.voteCnt: Int
        get() {
            return voteCnts.getOrPut(uniqueId) {
                var v = 0
                sql {
                    val res = executeQuery("SELECT VoteCount FROM Story.PlayerData WHERE UUID = '$uniqueId';")
                    if(res.next()) {
                        v = res.getInt("VoteCount")
                    }
                }
                v
            }
        }
        set(value) {
            sql {
                executeUpdate(
                    "UPDATE Story.PlayerData SET VoteCount = ${if(0 < value) value else 0} WHERE UUID = '$uniqueId';"
                )
            }
            voteCnts[uniqueId] = value
        }
}