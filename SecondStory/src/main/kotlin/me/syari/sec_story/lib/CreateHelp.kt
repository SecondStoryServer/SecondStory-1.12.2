package me.syari.sec_story.lib

import me.syari.sec_story.lib.message.SendMessage
import me.syari.sec_story.lib.message.SendMessage.send
import org.bukkit.command.CommandSender

object CreateHelp {
    fun createHelp(prefix: String, vararg content: Pair<String, String>, helpCmd: String? = null) = Help(prefix, helpCmd, content.toList())

    class Help(private val prefix: String, private val helpCmd: String?, private val content: List<Pair<String, String>>){
        fun send(sender: CommandSender, page: Int){
            if(page < 1) return sender.send("&b[Help] &cページを入力してください")
            if(helpCmd == null) {
                sender.send("&b[$prefix] &fコマンド")
                content.forEach { t ->
                    sender.send("&7- " to null, "&a${t.first}" to SendMessage.Action(hover = "&a入力", click = SendMessage.ClickType.TypeText to t.first.substringBefore(" <")),  " ${t.second}" to null)
                }
            } else {
                sender.send("&b[$prefix] &fコマンド    " to null, if(1 < page) "&a<<" to SendMessage.Action(hover = "&a前へ", click = SendMessage.ClickType.RunCommand to "$helpCmd ${page - 1}") else "&c<<" to null, "   " to null, if(page * 5 < content.size) "&a>>" to SendMessage.Action(hover = "&a次へ", click = SendMessage.ClickType.RunCommand to "$helpCmd ${page + 1}") else "&c>>" to null)
                for(i in (page - 1) * 5 until page * 5){
                    val t = content.getOrNull(i)
                    if(t != null){
                        sender.send("&7- " to null, "&a${t.first}" to SendMessage.Action(hover = "&a入力", click = SendMessage.ClickType.TypeText to t.first.substringBefore(" <")),  " ${t.second}" to null)
                    } else {
                        sender.send("")
                    }
                }
            }
        }
    }
}