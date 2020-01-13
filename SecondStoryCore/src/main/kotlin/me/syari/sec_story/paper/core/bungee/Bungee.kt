package me.syari.sec_story.paper.core.bungee

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.bungee.PluginMessage.sendBungeePluginMessage
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.message.SendMessage.send
import org.bukkit.entity.Player

object Bungee: FunctionInit {
    override fun init() {
        createCmd("stp") { sender, args ->
            if(sender is Player) {
                val name = args.getOrNull(0) ?: return@createCmd sender.send("&b[Server] &cサーバーが選択されていません")
                if(! sender.hasPermission("stp.$name")) return@createCmd sender.send("&b[Server] &c接続権限がありません")
                sender.connectServer(name)
            }
        }

        registerChannel("BungeeCord")
        CustomChannel.values().forEach {
            registerChannel(it.id)
        }
    }

    private fun registerChannel(channel: String) {
        plugin.server.messenger.registerOutgoingPluginChannel(plugin, channel)
    }

    private fun Player.connectServer(name: String) {
        sendBungeePluginMessage("Connect", name)
    }
}