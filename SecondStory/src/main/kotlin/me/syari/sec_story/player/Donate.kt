package me.syari.sec_story.player

import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.StringEditor.toColor
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.lib.command.CreateCommand.offlinePlayers
import me.syari.sec_story.lib.command.CreateCommand.tab
import me.syari.sec_story.message.Chat.clearSuffix
import me.syari.sec_story.message.Chat.suffix
import me.syari.sec_story.perm.Permission.loadPerm
import me.syari.sec_story.perm.PermissionLoadEvent
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.SQL.sql
import me.syari.sec_story.server.CommandBlock
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object Donate : Init(), Listener {
    override fun init() {
        createCmd("donate", tab { offlinePlayers() }){ sender, args ->
            if(args.isNotEmpty){
                val p = Bukkit.getOfflinePlayer(args[0]) ?: return@createCmd sender.send("&cNull player")
                if(args.size < 2){
                    return@createCmd sender.send("&b[Donate] &a${p.name}&fの寄付額は&a${p.donate}円&fです")
                }
                val price = args[1].toIntOrNull() ?: return@createCmd sender.send("&cNull price")
                sender.send("&b[Donate] &a${p.name}&fの寄付額を&a${price}円&fにしました")
                sql {
                    if(price == 0) {
                        executeUpdate("DELETE FROM Story.Donate WHERE UUID = '${p.uniqueId}' LIMIT 1")
                    } else {
                        executeUpdate("INSERT INTO Story.Donate VALUES ('${p.name}', '${p.uniqueId}', $price, '&b [Donator]') ON DUPLICATE KEY UPDATE Price = $price")
                    }
                }
                p.clearSuffix()
                if(p is Player) p.loadPerm()
            } else {
                sender.send("&b[Donate] &f/donate ID Price")
            }
        }

        createCmd("suffix"){ sender, args ->
            if(sender is Player){
                sender.suffix = args.joinToString(separator = " ").toColor
                sender.send("&b[Suffix] &fSuffixを更新しました")
            }
        }
    }

    val ranks = mutableSetOf<DonateRank>()

    data class DonateRank(val price: Int, val cmd : List<String>, val perm : List<String>)

    fun donateCmd(): Set<String>{
        val list = mutableSetOf<String>()
        ranks.forEach { r ->
            list.addAll(r.cmd)
        }
        return list
    }

    var OfflinePlayer.donate: Int
    get(){
        var m = -1
        sql {
            val res = executeQuery("SELECT Price FROM Story.Donate WHERE UUID = '$uniqueId';")
            if (res.next()) {
                m = res.getInt("Price")
            }
        }
        return m
    }

    set(value) {
        sql {
            if(0 < value){
                executeUpdate("INSERT INTO Story.Donate VALUE ('$name', '$uniqueId', $value) ON DUPLICATE KEY UPDATE Price = $value;")
            } else {
                executeUpdate("DELETE FROM Story.Donate WHERE UUID = '$uniqueId'")
            }
        }
    }

    fun getRank(cmd: String): Int{
        ranks.forEach { f ->
            if(cmd in f.cmd) return f.price
        }
        return -1
    }

    @EventHandler
    fun on(e: PermissionLoadEvent){
        val p = e.player
        e.setAllowCommand(CommandBlock.CommandAddCause.Donate, ranks.filter { f -> f.price <= p.donate }.flatMap { f -> f.cmd })
    }
}