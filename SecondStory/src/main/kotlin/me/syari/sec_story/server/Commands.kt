package me.syari.sec_story.server

import me.syari.sec_story.config.Config.loadAllConfig
import me.syari.sec_story.guild.Guild.guilds
import me.syari.sec_story.home.Home
import me.syari.sec_story.lib.message.SendMessage
import me.syari.sec_story.lib.message.SendMessage.Action
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.StringEditor.toColor
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.lib.command.CreateCommand.element
import me.syari.sec_story.lib.command.CreateCommand.onlinePlayers
import me.syari.sec_story.lib.command.CreateCommand.tab
import me.syari.sec_story.player.Money
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.Plugin
import me.syari.sec_story.plugin.Plugin.cmd
import me.syari.sec_story.plugin.Plugin.plugin
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.Listener

object Commands : Listener, Init(){
    override fun init() {
        createCmd("sr",
            tab { element("all", "cache") }
        ) { sender, args ->
            if (args.getOrNull(0)?.toLowerCase() == "cache") {
                guilds.forEach { g ->
                    g.clearCash()
                }
                Home.clearCash()
                Money.clearCash()
                sender.send("&7>>> &eCache Clear")
            } else {
                sender.send("&7>>> &eAll Config Reload")
                sender.loadAllConfig()
                sender.send("&7>>> &eConfig Reloaded")
            }
        }

        createCmd("cmat"){ sender, args ->
            val mat = if(args.isNotEmpty){
                val id = args[0].toIntOrNull() ?: return@createCmd sender.send("&b[Mat] &c数字IDを入力してください")
                Material.getMaterial(id)
            } else if(sender is Player && sender.inventory.itemInMainHand != null) {
                sender.inventory.itemInMainHand.type
            } else {
                return@createCmd
            }
            sender.send("&b[Mat] &a" to null, mat.name to Action(click = SendMessage.ClickType.TypeText to mat.name))
        }

        createCmd("kickall"){ _, args ->
            val msg = (if(args.isNotEmpty) args.joinToString(separator = " ") else "&6プレイヤー全員がキックされました").toColor
            plugin.server.onlinePlayers.forEach { p ->
                if(!p.isOp) p.kickPlayer(msg)
            }
        }

        createCmd("uuid",
                tab { onlinePlayers() }
        ){ sender, args ->
            val p = if(args.isNotEmpty) plugin.server.getOfflinePlayer(args[0]) else sender as? OfflinePlayer ?: return@createCmd
            sender.send("&b[UUID] &a${p.name}&fのUUIDは" to null, "&a${p.uniqueId}" to Action(click = SendMessage.ClickType.TypeText to "${p.uniqueId}"))
        }

        createCmd("wtp"){ sender, args ->
            if(sender is Player){
                fun help(){
                    sender.send("&b[WTP] &a/wtp World X Y Z [Yaw] [Pitch]")
                }

                if(args.size !in 4..6) return@createCmd help()
                val w = plugin.server.getWorld(args[0]) ?: return@createCmd sender.send("&b[WTP] &cワールドが存在しません")
                val x = args[1].toDoubleOrNull() ?: return@createCmd help()
                val y = args[2].toDoubleOrNull() ?: return@createCmd help()
                val z = args[3].toDoubleOrNull() ?: return@createCmd help()

                val loc = if(args.size in 5..6) {
                    val yaw = args[4].toFloatOrNull() ?: return@createCmd help()
                    val pitch = args[5].toFloatOrNull() ?: return@createCmd help()
                    Location(w, x, y, z, yaw, pitch)
                } else {
                    Location(w, x, y, z)
                }
                sender.teleport(loc)
            }
        }

        createCmd("sudo",
            tab { onlinePlayers() }
        ){ sender, args ->
            val name = args.getOrNull(0) ?: return@createCmd sender.send("&b[Sudo] &cプレイヤーを入力してくださいを入力してください")
            val p = plugin.server.getPlayer(name)
            if(p?.name != name) return@createCmd sender.send("&b[Sudo] &cプレイヤーが見つかりませんでした")
            if(args.size == 1) return@createCmd sender.send("&b[Sudo] &cコマンドを入力してください")
            val cmd = args.joinToString(" ").substring(args[0].length + 1)
            p.cmd(cmd)
        }
    }

    private val auto = mutableMapOf<Pair<Int, String>, Set<String>>()

    fun add(day: Int, time: String, set: Set<String>){
        auto[day to time] = set
    }

    fun clear(){
        auto.clear()
    }

    fun run(day: Int, time: String){
        auto[day to time]?.forEach{ f ->
            Plugin.console(f)
        }
        auto[0 to time]?.forEach { f ->
            Plugin.console(f)
        }
    }
}