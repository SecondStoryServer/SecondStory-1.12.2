package me.syari.sec_story.paper.library

import me.syari.sec_story.paper.library.message.JsonAction
import me.syari.sec_story.paper.library.message.JsonClickType
import me.syari.sec_story.paper.library.message.SendMessage.send
import org.bukkit.command.CommandSender

object CreateHelp {
    fun createHelp(
        prefix: String, vararg content: Pair<String, String>, helpCmd: String? = null
    ) = Help(prefix, helpCmd, content.toList())

    class Help(
        private val prefix: String, private val helpCmd: String?, private val content: List<Pair<String, String>>
    ) {
        fun send(sender: CommandSender, page: Int) {
            if(page < 1) return sender.send("&b[Help] &cページを入力してください")
            if(helpCmd == null) {
                sender.send("&b[$prefix] &fコマンド")
                content.forEach { t ->
                    sender.send(
                        "&7- " to null,
                        "&a${t.first}" to JsonAction(
                            hover = "&a入力",
                            click = JsonClickType.TypeText to t.first.substringBefore(" <")
                        ),
                        " ${t.second}" to null
                    )
                }
            } else {
                sender.send(
                    "&b[$prefix] &fコマンド    " to null,
                    if(1 < page) "&a<<" to JsonAction(
                        hover = "&a前へ",
                        click = JsonClickType.RunCommand to "$helpCmd ${page - 1}"
                    ) else "&c<<" to null,
                    "   " to null,
                    if(page * 5 < content.size) "&a>>" to JsonAction(
                        hover = "&a次へ",
                        click = JsonClickType.RunCommand to "$helpCmd ${page + 1}"
                    ) else "&c>>" to null
                )
                for(i in (page - 1) * 5 until page * 5) {
                    val t = content.getOrNull(i)
                    if(t != null) {
                        sender.send(
                            "&7- " to null,
                            "&a${t.first}" to JsonAction(
                                hover = "&a入力",
                                click = JsonClickType.TypeText to t.first.substringBefore(" <")
                            ),
                            " ${t.second}" to null
                        )
                    } else {
                        sender.send("")
                    }
                }
            }
        }
    }
}