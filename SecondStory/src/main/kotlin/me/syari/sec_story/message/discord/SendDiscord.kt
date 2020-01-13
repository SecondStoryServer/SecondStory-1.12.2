package me.syari.sec_story.message.discord

import me.syari.sec_story.bungee.CustomChannel
import me.syari.sec_story.bungee.PluginMessage.sendCustomPluginMessage
import org.bukkit.entity.Player

object SendDiscord {
    fun message(from: Player, ch: DiscordChannel, msg: String){
        from.sendCustomPluginMessage(CustomChannel.Discord, "msg", ch.raw, msg)
    }
}