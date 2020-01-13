package me.syari.sec_story.player

import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.lib.command.CreateCommand.element
import me.syari.sec_story.lib.command.CreateCommand.elementIfOp
import me.syari.sec_story.lib.command.CreateCommand.offlinePlayers
import me.syari.sec_story.lib.command.CreateCommand.onlinePlayers
import me.syari.sec_story.lib.command.CreateCommand.tab
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.SQL.sql
import me.syari.sec_story.server.Server.board
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import java.util.*

object Money : Init() {

    /*
    TODO add confirm
     */

    override fun init() {
        createCmd("money",
                tab { sender ->
                    element("rank", "pay").joinIfOp(sender, "check", "set", "add")
                },
                tab("check", "set", "add"){ sender ->
                    elementIfOp(sender, offlinePlayers())
                },
                tab("pay"){ onlinePlayers() }
        ) { sender, args ->
            fun sendHelp() {
                sender.send("""
                            &b[Money] &fコマンド
                            &7- &a/money &7自分の所持金を確認します
                            &7- &a/money rank <Page> &7所持金のランキングを表示します
                            &7- &a/money pay <Player> <Money> &7プレイヤーに送金します
                        """.trimIndent() +
                        if (sender is ConsoleCommandSender || sender.isOp) {
                            """
                            
                            &7- &a/money check <Player> &7お金の確認をします
                            &7- &a/money set <Player> <Money> &7プレイヤーの所持金を変更します
                            &7- &a/money add <Player> <Money> &7プレイヤーの所持金を足します
                            """.trimIndent()
                        } else ""
                )
            }

            when {
                args.isNotEmpty -> {
                    if (sender is ConsoleCommandSender || sender.isOp) {
                        when (args[0].toLowerCase()) {
                            "check" -> {
                                val t = (if (2 <= args.size) Bukkit.getOfflinePlayer(args[1]) else null) ?: return@createCmd sender.send("&b[Money] &cプレイヤーを入力してください")
                                return@createCmd sender.send("&b[Money] &f${t.name}の所持金 : &a${String.format("%,d", t.money)}JPY")
                            }
                            "set" -> {
                                val t = (if (2 <= args.size) Bukkit.getOfflinePlayer(args[1]) else null) ?: return@createCmd sender.send("&b[Money] &cプレイヤーを入力してください")
                                if(args.size < 3) {
                                    return@createCmd sender.send("&b[Money] &f金額を入力してください")
                                }
                                val tmp = args[2].toLongOrNull()
                                val m = if (tmp == null || tmp < 0) 0 else tmp
                                sender.send("&b[Money] &a${t.name}&fの所持金を&a${String.format("%,d", t.money)}JPY&fから&a${String.format("%,d", m)}JPY&fにしました")
                                t.money = m
                                return@createCmd
                            }
                            "add" -> {
                                val t = (if (2 <= args.size) Bukkit.getOfflinePlayer(args[1]) else null) ?: return@createCmd sender.send("&b[Money] &cプレイヤーを入力してください")
                                if(args.size < 3) {
                                    return@createCmd sender.send("&b[Money] &f金額を入力してください")
                                }
                                val m = args[2].toLongOrNull() ?: 0
                                sender.send("&b[Money] &a${t.name}&fの所持金に&a${String.format("%,d", m)}JPY&fを足し&a${String.format("%,d", t.money + m)}JPY&fにしました")
                                t.money += m
                                return@createCmd
                            }
                        }
                    }
                    when (args[0].toLowerCase()) {
                        "rank" -> {
                            sender.send("&b[Money] &f所持金ランキング")
                            val page = if (2 <= args.size) {
                                val v = args[1].toIntOrNull()
                                if (v != null && 1 <= v) {
                                    v
                                } else {
                                    1
                                }
                            } else {
                                1
                            }
                            return@createCmd getRank(page).forEachIndexed { i, r ->
                                sender.send("&6${(page - 1) * 10 + i + 1}. &f${Bukkit.getOfflinePlayer(r.first)?.name}  &a${String.format("%,d", r.second)}JPY")
                            }
                        }
                        "pay" -> {
                            if (args.size < 2) {
                                return@createCmd sendHelp()
                            } else {
                                val t = Bukkit.getPlayer(args[1]) ?: return@createCmd sender.send("&b[Money] &c送金先のプレイヤーを入力してください")
                                if (args.size < 3) {
                                    return@createCmd sender.send("&b[Money] &c送金額を入力してください")
                                }
                                val m = args[2].toLongOrNull()
                                if (m == null || m < 1) {
                                    return@createCmd sender.send("&b[Money] &c1JPY以上送ってください")
                                } else if (sender is Player) {
                                    if(sender == t) {
                                        return@createCmd sender.send("&b[Money] &c自分に送金することは出来ません")
                                    }
                                    if(sender.hasMoney(m)) {
                                        sender.money -= m
                                        t.money += m
                                        sender.send("&b[Money] &a${t.displayName}&fに&a${String.format("%,d", m)}JPY&f送りました")
                                        return@createCmd t.send("&b[Money] &a${sender.displayName}&fから&a${String.format("%,d", m)}JPY&f受け取りました")
                                    } else{
                                        return@createCmd sender.send("&b[Money] &c所持金不足です")
                                    }
                                } else {
                                    t.money += m
                                    sender.send("&b[Money] &a${t.displayName}&fに&a${String.format("%,d", m)}JPY&f送りました")
                                    return@createCmd t.send("&b[Money] &aConsole&fから&a${String.format("%,d", m)}JPY&f受け取りました")
                                }
                            }
                        }
                    }
                }
                sender is Player -> {
                    return@createCmd sender.send("&b[Money] &f所持金 : &a${String.format("%,d", sender.money)}JPY")
                }
            }
            sendHelp()
        }
    }

    private val moneys = mutableMapOf<UUID, Long>()

    var OfflinePlayer.money: Long
        get(){
            return moneys.getOrPut(uniqueId){
                var m = 0L
                sql {
                    val res = executeQuery("SELECT Money FROM Story.PlayerData WHERE UUID = '$uniqueId';")
                    if (res.next()) {
                        m = res.getLong("Money")
                    }
                }
                m
            }
        }
        set(value) {
            val money = if(0 < value) value else 0
            sql {
                executeUpdate("UPDATE Story.PlayerData SET Money = $money WHERE UUID = '$uniqueId';")
            }
            moneys[uniqueId] = money
            if(this is Player) board(this)
        }

    fun Player.hasMoney(value: Long) = money >= value

    private fun getRank(page: Int): List<Pair<UUID, Long>> {
        val ret = mutableListOf<Pair<UUID, Long>>()
        sql {
            val res = executeQuery("SELECT UUID, Money FROM Story.PlayerData WHERE Money > 0 ORDER BY Money DESC LIMIT ${(page - 1) * 10},  10;")
            while(res.next()){
                ret.add(Pair(UUID.fromString(res.getString("UUID")), res.getLong("Money")))
            }
        }
        return ret
    }

    fun clearCash() {
        moneys.clear()
    }
}