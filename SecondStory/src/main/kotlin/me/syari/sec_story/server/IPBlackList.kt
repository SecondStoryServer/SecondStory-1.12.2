package me.syari.sec_story.server

import me.syari.sec_story.lib.message.SendMessage
import me.syari.sec_story.lib.message.SendMessage.Action
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.lib.command.CreateCommand.onlinePlayers
import me.syari.sec_story.lib.command.CreateCommand.tab
import me.syari.sec_story.message.discord.DiscordChannel
import me.syari.sec_story.message.discord.SendDiscord
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.Plugin
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

object IPBlackList : Listener, Init() {

    override fun init(){
        createCmd("cip",
                tab { onlinePlayers() }
        ){ sender, args ->
            val p = if(args.isNotEmpty) Plugin.plugin.server.getPlayer(args[0]) else sender as? Player ?: return@createCmd
            sender.send("&b[IP] &a${p.name}&fのIPは" to null, "&a${p.address.address.hostAddress}" to Action(click = SendMessage.ClickType.TypeText to p.address.address.hostAddress))
        }
    }

    lateinit var IPs: List<String>

    @EventHandler
    fun on(e: PlayerJoinEvent){
        val p = e.player
        val ip = p.address.address.hostAddress
        if(IPs.contains(ip)){
            SendDiscord.message(p, DiscordChannel.ServerLog, "${p.name} - $ip")
        }
    }
}