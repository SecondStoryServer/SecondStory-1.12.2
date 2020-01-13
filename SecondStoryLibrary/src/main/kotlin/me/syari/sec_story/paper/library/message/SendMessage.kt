package me.syari.sec_story.paper.library.message

import me.syari.sec_story.paper.library.Main.Companion.plugin
import me.syari.sec_story.paper.library.code.StringEditor.toColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object SendMessage {
    fun sendConsole(msg: String) {
        plugin.server.consoleSender.send(msg)
    }

    private fun sendConsole(textComponent: TextComponent) {
        plugin.server.consoleSender.sendMessage(textComponent)
    }

    fun broadcast(msg: String, sendConsole: Boolean = true) {
        val c = msg.toColor
        plugin.server.onlinePlayers.forEach { p ->
            p.sendMessage(c)
        }
        if(sendConsole) sendConsole(msg)
    }

    fun broadcast(vararg msg: Pair<String, JsonAction?>, sendConsole: Boolean = true) {
        val textComponent = getTextComponent(msg)
        plugin.server.onlinePlayers.forEach { p ->
            p.sendMessage(textComponent)
        }
        if(sendConsole) sendConsole(textComponent)
    }

    fun CommandSender.send(msg: StringBuilder) {
        send(msg.toString())
    }

    fun CommandSender.send(vararg msg: String) {
        val send = msg.toColor
        sendMessage(send.toTypedArray())
    }

    fun CommandSender.send(vararg msg: Pair<String, JsonAction?>) {
        sendMessage(getTextComponent(msg))
    }

    fun Player.title(main: String, sub: String, fadein: Int, stay: Int, fadeout: Int) {
        sendTitle(main.toColor, sub.toColor, fadein, stay, fadeout)
    }

    fun Player.action(msg: String) {
        sendActionBar(msg.toColor)
    }

    fun Iterable<CommandSender>.send(vararg msg: String) {
        val send = msg.toColor
        forEach { p ->
            p.sendMessage(send.toTypedArray())
        }
    }

    fun Iterable<CommandSender>.send(vararg msg: Pair<String, JsonAction?>) {
        val t = getTextComponent(msg)
        forEach { p ->
            p.sendMessage(t)
        }
    }

    private fun getTextComponent(msg: Array<out Pair<String, JsonAction?>>): TextComponent {
        val t = TextComponent()
        msg.forEach { f ->
            val a = TextComponent(f.first.toColor)
            val s = f.second
            if(s != null) {
                val c = s.click
                if(c != null) {
                    a.clickEvent = ClickEvent(c.first.event, c.second.toColor)
                }
                val h = s.hover
                if(h != null) {
                    a.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent(h.toColor)))
                }
            }
            t.addExtra(a)
        }
        return t
    }
}