package me.syari.sec_story.paper.core.ip

import me.syari.sec_story.paper.core.discord.DiscordChannel
import me.syari.sec_story.paper.core.discord.SendDiscord
import me.syari.sec_story.paper.library.init.EventInit
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent

object IPBlackList: EventInit {
    lateinit var IPs: List<String>

    @EventHandler
    fun on(e: PlayerJoinEvent) {
        val p = e.player
        val ip = p.address.address.hostAddress
        if(IPs.contains(ip)) {
            SendDiscord.message(p, DiscordChannel.ServerLog, "${p.name} - $ip")
        }
    }
}