package me.syari.sec_story.paper.core.bungee

import com.google.common.io.ByteStreams
import me.syari.sec_story.paper.core.Main.Companion.plugin
import org.bukkit.entity.Player

object PluginMessage {
    private fun Player.sendPluginMessage(channel: String, contents: Array<out String>) {
        if(contents.isEmpty()) throw PluginMessageSendException("Contents is Empty")
        val out = ByteStreams.newDataOutput()
        contents.forEach { out.writeUTF(it) }
        sendPluginMessage(plugin, channel, out.toByteArray())
    }

    fun Player.sendCustomPluginMessage(channel: CustomChannel, vararg contents: String) {
        sendPluginMessage(channel.id, contents)
    }

    fun Player.sendBungeePluginMessage(vararg contents: String) {
        sendPluginMessage("BungeeCord", contents)
    }
}