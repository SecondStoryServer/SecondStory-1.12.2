package me.syari.sec_story.bungee

import me.syari.sec_story.bungee.PluginMessage.sendBungeePluginMessage
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.Plugin.plugin
import org.bukkit.entity.Player

object Bungee : Init(){
    override fun init() {
        createCmd("stp"){ sender, args ->
            if(sender is Player){
                val name = args.getOrNull(0) ?: return@createCmd sender.send("&b[Server] &cサーバーが選択されていません")
                if(!sender.hasPermission("stp.$name")) return@createCmd sender.send("&b[Server] &c接続権限がありません")
                sender.connectServer(name)
            }
        }

        registerChannel("BungeeCord")
        CustomChannel.values().forEach {
            registerChannel(it.id)
        }
    }

    private fun registerChannel(channel: String){
        plugin.server.messenger.registerOutgoingPluginChannel(plugin, channel)
    }

    private fun Player.connectServer(name: String){
        sendBungeePluginMessage("Connect", name)
    }
}