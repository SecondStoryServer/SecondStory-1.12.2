package me.syari.sec_story.paper.core.game.event

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.element
import me.syari.sec_story.paper.library.command.CreateCommand.offlinePlayers
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.inv.CreateInventory.close
import me.syari.sec_story.paper.library.inv.CreateInventory.inventory
import me.syari.sec_story.paper.library.message.JsonAction
import me.syari.sec_story.paper.library.message.JsonClickType
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import me.syari.sec_story.paper.library.scheduler.CustomTask
import me.syari.sec_story.paper.library.server.Server.getOfflinePlayer
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

object Contest: FunctionInit {
    override fun init() {
        createCmd("contest", tab { element("vote", "end") }, tab("vote * *") { offlinePlayers }) { sender, args ->
            when(args.whenIndex(0)) {
                "vote" -> {
                    if(sender is Player) {
                        if(voteTask != null) return@createCmd sender.send("&b[Contest] &c現在進行中の投票があります")
                        val radius = args.getOrNull(1)?.toIntOrNull() ?: return@createCmd sender.send(
                            "&b[Contest] &c投票参加者が含まれる半径を入力してください"
                        )
                        val time = args.getOrNull(2)?.toIntOrNull() ?: return@createCmd sender.send(
                            "&b[Contest] &c投票時間を入力してください"
                        )
                        val name = args.getOrNull(3) ?: return@createCmd sender.send(
                            "&b[Contest] &c投票されるプレイヤーを入力してください"
                        )
                        val player = getOfflinePlayer(name)
                        if(player?.name != name) return@createCmd sender.send("&b[Contest] &c投票されるプレイヤーが見つかりませんでした")
                        startVote(player, sender.location, radius, time)
                    }
                }
                "end" -> {
                    if(voteTask == null) return@createCmd sender.send("&b[Contest] &c現在進行中の投票がありません")
                    endVote()
                    sender.send("&b[Contest] &c投票を終了しました")
                }
                else -> {
                    sender.send(
                        """
                        &b[Contest] &fコマンド
                        &7- &a/contest vote <Radius> <Time> <Player> &7投票を募集します
                        &7- &a/contest end &7投票を終了します
                        """.trimIndent()
                    )
                }
            }
        }
    }

    private var voteTask: CustomTask? = null
    private var sumPoint = 0
    private var sumMax = 0
    private var voteName: String? = null

    private fun startVote(player: OfflinePlayer, center: Location, radius: Int, time: Int) {
        voteName = player.name
        center.getNearbyPlayers(radius.toDouble()).forEach { p ->
            if(p == player) return@forEach
            val list = if(p.hasPermission("ContestJudge")) {
                listOf<Triple<Int, Int, Short>>(
                    Triple(2, 5, 10), Triple(3, 4, 9), Triple(4, 3, 13), Triple(5, 2, 5), Triple(6, 1, 8)
                ) to 5
            } else {
                listOf<Triple<Int, Int, Short>>(
                    Triple(2, 3, 10), Triple(4, 2, 9), Triple(6, 1, 8)
                ) to 3
            }

            var send = false
            inventory("&9&l${voteName}の採点", 1) {
                id = "Event-Contest-Vote"
                onClose = {
                    if(! send) {
                        val point = list.second
                        p.send("&b[Contest] &f投票が終了しましたので自動で&a${voteName}&fを&a${point}点&fと採点しました")
                        sendVote(point, point)
                    }
                }
                list.first.forEach { (index, point, damage) ->
                    item(index, Material.INK_SACK, "&6&l${point}点", damage = damage).event(ClickType.LEFT) {
                        p.send("&b[Contest] &a${voteName}&fを&a${point}点&fと採点しました")
                        sendVote(point, list.second)
                        send = true
                        p.closeInventory()
                    }
                }
            }.open(p)
        }
        voteTask = runLater(plugin, time.toLong() * 20) {
            endVote()
        }
    }

    private fun endVote() {
        voteTask?.cancel()
        close("Event-Contest-Vote")
        val result = "%.1f".format(sumPoint.toDouble() / sumMax * 100)
        plugin.server.onlinePlayers.filter { it.hasPermission("ContestJudge") }.send(
            "&b[Contest] " to null,
            "&a${voteName}" to JsonAction(hover = "&6クリック", click = JsonClickType.TypeText to voteName.toString()),
            "&fの得点率は" to null,
            "&a${result}%&7($sumPoint&8/&7$sumMax)" to JsonAction(
                hover = "&6クリック", click = JsonClickType.TypeText to "${result}% $sumPoint/$sumMax"
            ),
            "&fでした " to null,
            "&7[まとめてコピー]" to JsonAction(hover = "&6クリック", click = JsonClickType.TypeText to "$voteName $result")
        )
        voteTask = null
        voteName = null
        sumMax = 0
        sumPoint = 0
    }

    private fun sendVote(point: Int, max: Int) {
        sumPoint += point
        sumMax += max
    }
}