package me.syari.sec_story.paper.core.player

import me.syari.sec_story.paper.core.plugin.SQL.sql
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.element
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.server.Server.getOfflinePlayer
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*

object Time: FunctionInit {
    override fun init() {
        createCmd("play", tab { element("rank") }) { sender, args ->
            if(args.isNotEmpty) {
                if(args[0].toLowerCase() == "rank") {
                    sender.send("&b[PlayTime] &fプレイ時間ランキング")
                    val page = args.getOrNull(1)?.toIntOrNull() ?: 1
                    if(page < 1) return@createCmd sender.send("&b[PlayTime] &cページ数を入力してください")
                    getRank(page).forEachIndexed { i, r ->
                        sender.send(
                            "&6${(page - 1) * 10 + i + 1}. &f${getOfflinePlayer(r.first)?.name}  &a${r.second.show}"
                        )
                    }
                } else {
                    sender.send(
                        """
                            &b[PlayTime] &fコマンド
                            &7- &a/play &7自分のプレイ時間を表示します
                            &7- &a/play rank <Page> &7プレイ時間のランキングを表示します
                        """.trimIndent()
                    )
                }
            } else if(sender is Player) {
                sender.send("&b[PlayTime] &fプレイ時間 : &a${sender.time.show}")
            }
        }
    }

    var OfflinePlayer.time: Int
        get() {
            var w = 0
            sql {
                val res = executeQuery("SELECT PlayTime FROM Story.PlayerData WHERE UUID = '$uniqueId';")
                if(res.next()) {
                    w = res.getInt("PlayTime")
                }
            }
            return w
        }
        set(value) {
            sql {
                executeUpdate(
                    "UPDATE Story.PlayerData SET PlayTime = ${if(0 < value) value else 0} WHERE UUID = '$uniqueId';"
                )
            }
        }

    val Int.show
        get(): String = when {
            this < 60 -> "${this}分"
            this < 60 * 24 -> "${(this / 60)}時間${this % 60}分"
            else -> "${(this / (24 * 60))}日${this % (24 * 60) / 60}時間${this % 60}分"
        }

    private fun getRank(page: Int): List<Pair<UUID, Int>> {
        val ret = mutableListOf<Pair<UUID, Int>>()
        sql {
            val res = executeQuery(
                "SELECT UUID, PlayTime FROM Story.PlayerData WHERE PlayTime > 0 ORDER BY PlayTime DESC LIMIT ${(page - 1) * 10},  10;"
            )
            while(res.next()) {
                ret.add(Pair(UUID.fromString(res.getString("UUID")), res.getInt("PlayTime")))
            }
        }
        return ret
    }
}