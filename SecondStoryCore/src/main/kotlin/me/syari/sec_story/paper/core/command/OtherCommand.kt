package me.syari.sec_story.paper.core.command

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.guild.Guild.guilds
import me.syari.sec_story.paper.core.home.Home
import me.syari.sec_story.paper.core.itemPost.ItemPost
import me.syari.sec_story.paper.core.player.Money
import me.syari.sec_story.paper.core.plugin.Config.loadAllConfig
import me.syari.sec_story.paper.library.code.StringEditor.toColor
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.element
import me.syari.sec_story.paper.library.command.CreateCommand.onlinePlayers
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.command.RunCommand.runCommand
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.message.JsonAction
import me.syari.sec_story.paper.library.message.JsonClickType
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.server.Server.getPlayer
import me.syari.sec_story.paper.library.server.Server.getWorldSafe
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player

object OtherCommand: FunctionInit {
    override fun init() {
        createCmd("sr", tab { element("all", "cache") }) { sender, args ->
            if(args.getOrNull(0)?.toLowerCase() == "cache") {
                guilds.forEach { g ->
                    g.clearCashe()
                }
                Home.clearCashe()
                Money.clearCashe()
                ItemPost.clearCache()
                sender.send("&7>>> &eCache Clear")
            } else {
                sender.send("&7>>> &eAll Config Reload")
                sender.loadAllConfig()
                sender.send("&7>>> &eConfig Reloaded")
            }
        }

        createCmd("cmat") { sender, args ->
            val mat = if(args.isNotEmpty) {
                val id = args[0].toIntOrNull() ?: return@createCmd sender.send("&b[Mat] &c数字IDを入力してください")
                Material.getMaterial(id)
            } else if(sender is Player && sender.inventory.itemInMainHand != null) {
                sender.inventory.itemInMainHand.type
            } else {
                return@createCmd
            }
            sender.send("&b[Mat] &a" to null, mat.name to JsonAction(click = JsonClickType.TypeText to mat.name))
        }

        createCmd("kickall") { _, args ->
            val msg = (if(args.isNotEmpty) args.joinToString(separator = " ") else "&6プレイヤー全員がキックされました").toColor
            plugin.server.onlinePlayers.forEach { p ->
                if(! p.isOp) p.kickPlayer(msg)
            }
        }

        createCmd("wtp") { sender, args ->
            if(sender is Player) {
                fun help() {
                    sender.send("&b[WTP] &a/wtp World X Y Z [Yaw] [Pitch]")
                }

                if(args.size !in 4..6) return@createCmd help()
                val w = getWorldSafe(args[0]) ?: return@createCmd sender.send("&b[WTP] &cワールドが存在しません")
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

        createCmd("sudo", tab { onlinePlayers }) { sender, args ->
            val name = args.getOrNull(0) ?: return@createCmd sender.send("&b[Sudo] &cプレイヤーを入力してください")
            val p = getPlayer(name)
            if(p?.name != name) return@createCmd sender.send("&b[Sudo] &cプレイヤーが見つかりませんでした")
            if(args.size == 1) return@createCmd sender.send("&b[Sudo] &cコマンドを入力してください")
            val cmd = args.slice(1).joinToString(" ")
            runCommand(p, cmd)
        }
    }
}