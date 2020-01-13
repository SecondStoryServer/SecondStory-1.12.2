package me.syari.sec_story.item

import com.vexsoftware.votifier.model.VotifierEvent
import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.lib.ItemStackPlus.give
import me.syari.sec_story.lib.message.SendMessage.broadcast
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.lib.command.CreateCommand.onlinePlayers
import me.syari.sec_story.lib.command.CreateCommand.tab
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.SQL.sql
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.*

object Vote: Listener, Init() {
    override fun init() {
        createCmd("testvote", tab { onlinePlayers() }){ sender, args ->
            if(args.isNotEmpty){
                vote(args[0])
                sender.send("&b[TestVote] &f送りました")
            } else {
                sender.send("&b[TestVote] &cプレイヤーを入力してください")
            }
        }
    }

    var items = listOf<CustomItemStack>()

    @EventHandler
    fun on(e: VotifierEvent){
        val user = e.vote.username
        vote(user)
    }

    private fun vote(user: String){
        val player = Bukkit.getOfflinePlayer(user) ?: return
        if(player.name != user) return
        player.give(items, postName = "&b投票報酬")
        broadcast("&7 >> &b&lVote &f&l$user &7https://minecraft.jp/servers/2nd-story.info/vote")
        player.voteCnt += 1
    }

    private val voteCnts = mutableMapOf<UUID, Int>()

    var OfflinePlayer.voteCnt: Int
        get() {
            return voteCnts.getOrPut(uniqueId){
                var v = 0
                sql {
                    val res = executeQuery("SELECT VoteCount FROM Story.PlayerData WHERE UUID = '$uniqueId';")
                    if (res.next()) {
                        v = res.getInt("VoteCount")
                    }
                }
                v
            }
        }
        set(value) {
            sql {
                executeUpdate("UPDATE Story.PlayerData SET VoteCount = ${if(0 < value) value else 0} WHERE UUID = '$uniqueId';")
            }
            voteCnts[uniqueId] = value
        }
}