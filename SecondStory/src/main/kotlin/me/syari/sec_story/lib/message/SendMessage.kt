package me.syari.sec_story.lib.message

import me.syari.sec_story.lib.StringEditor.toColor
import me.syari.sec_story.lib.StringEditor.toUncolor
import me.syari.sec_story.plugin.Plugin.info
import me.syari.sec_story.plugin.Plugin.plugin
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object SendMessage {
    fun broadcast(msg: String){
        val c = msg.toColor
        plugin.server.onlinePlayers.forEach { p ->
            p.sendMessage(c)
        }
        info(msg.toUncolor)
    }

    fun broadcast(vararg msg: Pair<String, Action?>){
        val t = getTextComponent(msg)
        plugin.server.onlinePlayers.forEach { p ->
            p.sendMessage(t)
        }
        plugin.server.consoleSender.sendMessage(t)
    }

    fun CommandSender.send(msg: StringBuilder){
        send(msg.toString())
    }

    fun CommandSender.send(vararg msgs: String){
        val msg = mutableListOf<String>()
        msgs.forEach { m -> msg.add(m.toColor) }
        sendMessage(msg.toTypedArray())
    }

    fun CommandSender.send(vararg msg: Pair<String, Action?>){
        sendMessage(getTextComponent(msg))
    }

    fun Player.title(main: String, sub: String, fadein: Int, stay: Int, fadeout: Int){
        sendTitle(main.toColor, sub.toColor, fadein, stay, fadeout)
    }

    fun Player.action(msg: String){
        sendActionBar(msg.toColor)
    }

    fun Iterable<CommandSender>.send(vararg msgs: String){
        val msg = mutableListOf<String>()
        msgs.forEach { m -> msg.add(m.toColor) }
        forEach { p ->
            p.sendMessage(msg.toTypedArray())
        }
    }

    fun Iterable<CommandSender>.send(vararg msg: Pair<String, Action?>){
        val t = getTextComponent(msg)
        forEach { p ->
            p.sendMessage(t)
        }
    }

    private fun getTextComponent(msg: Array<out Pair<String, Action?>>): TextComponent {
        val t = TextComponent()
        msg.forEach { f ->
            val a = TextComponent(f.first.toColor)
            val s = f.second
            if(s != null){
                val c = s.click
                if(c != null){
                    a.clickEvent = ClickEvent(c.first.event, c.second.toColor)
                }
                val h = s.hover
                if(h != null){
                    a.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent(h.toColor)))
                }
            }
            t.addExtra(a)
        }
        return t
    }

    data class Action(val hover: String? = null, val click: Pair<ClickType, String>? = null)

    enum class ClickType(val event: ClickEvent.Action){
        RunCommand(ClickEvent.Action.RUN_COMMAND), TypeText(ClickEvent.Action.SUGGEST_COMMAND), OpenURL(ClickEvent.Action.OPEN_URL)
    }
}