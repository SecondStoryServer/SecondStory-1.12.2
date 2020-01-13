package me.syari.sec_story.paper.core.home

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.plugin.SQL.sql
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.element
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.server.Server.getWorld
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

object Home: FunctionInit {
    override fun init() {
        createCmd("home", tab { element("set", "unset", "list", "tp") }, tab("unset", "tp") { sender ->
            element {
                if(sender is Player) {
                    sender.getHomeList()
                } else null
            }
        }) { sender, args ->
            if(sender is Player) {
                when(args.whenIndex(0)) {
                    "set" -> {
                        if(args.size < 2) {
                            sender.send("&b[Home] &cホーム名を入力してください")
                        } else {
                            val home = args[1]
                            val loc = sender.location
                            val e = HomeSetEvent(sender, loc)
                            e.callEvent()
                            if(!e.isCancelled) {
                                sender.send(
                                    "&b[Home] &f$home" + if(sender.containHome(home)) "のテレポート先を更新しました"
                                    else "を追加しました"
                                )
                                sender.addHome(loc, home)
                            } else {
                                sender.send("&b[Home] &cホーム設定出来ませんでした")
                            }
                        }
                    }
                    "unset" -> {
                        if(args.size < 2) {
                            sender.send("&b[Home] &cホーム名を入力してください")
                        } else {
                            val home = args[1]
                            if(sender.containHome(home)) {
                                sender.remHome(home)
                                sender.send("&b[Home] &f${home}を削除しました")
                            } else {
                                sender.send("&b[Home] &c${home}は登録されていません")
                            }
                        }
                    }
                    "list" -> {
                        sender.send("&b[Home] &fホーム一覧")
                        sender.getHomeList().forEach { f ->
                            sender.send("&7- &a$f")
                        }
                    }
                    "tp" -> {
                        if(args.size < 2) return@createCmd sender.send("&b[Home] &cホーム名を入力してください")
                        val home = args[1]
                        if(sender.containHome(home)) {
                            val loc = sender.getHomeLoc(home)
                            sender.teleport(loc)
                        } else {
                            sender.send("&b[Home] &c${home}は登録されていません")
                        }
                    }
                    else -> {
                        val home = args.getOrNull(0)
                        if(home != null && sender.containHome(home)) {
                            val loc = sender.getHomeLoc(home)
                            sender.teleport(loc)
                        } else {
                            sender.send(
                                """
                        &b[Home] &fコマンド
                        &7- &a/home tp <Name> &7ホームにテレポートします
                        &7- &a/home set <Name> &7ホームに登録します
                        &7- &a/home unset <Name> &7ホームの登録を解除します
                        &7- &a/home list &7登録したホームの一覧を表示します
                        """.trimIndent()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun Player.addHome(loc: Location, s: String) {
        val home = homes.getOrDefault(uniqueId, listOf()).toMutableList()
        home.add(s)
        homes[uniqueId] = home
        sql {
            executeUpdate(
                "INSERT INTO Story.Home VALUE ('$name', '$uniqueId', '$s', '${loc.world.name}', ${loc.x}, ${loc.y}, ${loc.z})" + "ON DUPLICATE KEY UPDATE World = '${loc.world.name}', X = ${loc.x}, Y = ${loc.y}, Z = ${loc.z};"
            )
        }
    }

    private fun Player.remHome(s: String) {
        val home = homes.getOrDefault(uniqueId, listOf()).toMutableList()
        home.remove(s)
        homes[uniqueId] = home
        sql {
            executeUpdate("DELETE FROM Story.Home WHERE UUID = '$uniqueId' and Name = '$s'")
        }
    }

    private fun Player.getHomeLoc(s: String): Location? {
        var loc: Location? = null
        sql {
            val res = executeQuery(
                "SELECT World, X, Y, Z FROM Story.Home WHERE UUID = '$uniqueId' and Name = '$s' LIMIT 1;"
            )
            if(res.next()) {
                val w = getWorld(res.getString("World"))
                val x = res.getDouble("X")
                val y = res.getDouble("Y")
                val z = res.getDouble("Z")
                val pitch = location.pitch
                val yaw = location.yaw
                if(w != null) {
                    loc = Location(w, x, y, z, yaw, pitch)
                }
            }
        }
        return loc
    }

    private fun Player.containHome(s: String) = getHomeList().contains(s)

    private val homes = mutableMapOf<UUID, List<String>>()

    private fun Player.getHomeList(): List<String> {
        return homes.getOrPut(uniqueId) {
            val list = mutableListOf<String>()
            sql {
                val res = executeQuery("SELECT Name FROM Story.Home WHERE UUID = '$uniqueId'")
                while(res.next()) {
                    list.add(res.getString("Name"))
                }
            }
            list
        }
    }

    fun clearCashe() {
        homes.clear()
    }
}