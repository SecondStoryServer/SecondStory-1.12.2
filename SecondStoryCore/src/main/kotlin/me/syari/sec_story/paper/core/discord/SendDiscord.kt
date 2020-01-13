package me.syari.sec_story.paper.core.discord

import me.syari.sec_story.paper.core.bungee.CustomChannel
import me.syari.sec_story.paper.core.bungee.PluginMessage.sendCustomPluginMessage
import org.bukkit.entity.Player

object SendDiscord {
    fun message(from: Player, ch: DiscordChannel, msg: String) {
        from.sendCustomPluginMessage(CustomChannel.Discord, "msg", ch.raw, msg)
    }
}