package me.syari.sec_story.paper.core.chat

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.chat.Chat.ch
import me.syari.sec_story.paper.core.chat.Chat.isObsDisable
import me.syari.sec_story.paper.core.chat.Chat.replyTo
import me.syari.sec_story.paper.core.chat.Chat.send
import me.syari.sec_story.paper.core.chat.Chat.toCh
import me.syari.sec_story.paper.core.chat.Chat.toggleObsDisable
import me.syari.sec_story.paper.core.game.mobArena.MobArena.inMobArena
import me.syari.sec_story.paper.core.guild.Guild.guild
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.element
import me.syari.sec_story.paper.library.command.CreateCommand.elementIfOp
import me.syari.sec_story.paper.library.command.CreateCommand.onlinePlayers
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.message.SendMessage
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.server.Server.getPlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object ChatCommand: FunctionInit {
    override fun init() {
        createCmd("ch", tab { sender ->
            element("global", "guild").joinIfOp(sender, "admin", "obs").joinIf(
                sender is Player && sender.inMobArena, "mobarena"
            )
        }, tab("obs") { sender ->
            elementIfOp(sender, "guild", "mobarena", "private")
        }) { sender, args ->
            if(args.isEmpty) {
                if(sender is Player) {
                    return@createCmd sender.send(
                        """
                        &b[Chat] &f今のチャンネルは&a${sender.ch.jp}&fです
                        &7- &a/ch global &7グローバルチャットに変更します
                        &7- &a/ch guild &7ギルドチャットに変更します
                        &7- &a/ch mobarena &7モブアリーナチャットに変更します
                    """.trimIndent() + if(sender.isOp) "\n" + """
                        &7- &a/ch admin &7アドミンチャットに変更します
                        &7- &a/ch obs &7チャット監視の切り替えをします
                    """.trimIndent() else ""
                    )
                }
            } else {
                if(sender is Player && sender.isOp && args.whenIndex(0) == "obs") {
                    when(val input = args.whenIndex(1)) {
                        "guild", "mobarena", "private" -> {
                            if(sender.toggleObsDisable(input)) {
                                sender.send("&b[Chat] &a${input.toUpperCase()}&fチャンネルを&c非表示&fにしました")
                            } else {
                                sender.send("&b[Chat] &a${input.toUpperCase()}&fチャンネルを&a表示&fにしました")
                            }
                        }
                        else -> {
                            sender.send(
                                """
                                &b[Chat] &fコマンド
                                &7- &a/ch obs guild &7ギルドチャットを監視します  ${if(sender.isObsDisable(
                                        "guild"
                                    )) "&c非表示" else "&a表示"}
                                &7- &a/ch obs mobarena &7モブアリーナチャットを監視します  ${if(sender.isObsDisable(
                                        "mobarena"
                                    )) "&c非表示" else "&a表示"}
                                &7- &a/ch obs private &7プライベートチャットを監視します  ${if(sender.isObsDisable(
                                        "private"
                                    )) "&c非表示" else "&a表示"}
                            """.trimIndent()
                            )
                        }
                    }
                } else if(sender is Player) {
                    val ch = args[0].toCh ?: Chat.Channel.Global
                    if(ch == Chat.Channel.Guild && sender.guild == null) return@createCmd sender.send(
                        "&b[Chat] &cギルドに所属していません"
                    )
                    val c = if(ch == Chat.Channel.Admin && ! sender.hasPermission(
                            "admin-chat"
                        )) Chat.Channel.Global else ch
                    if(sender.ch == c) return@createCmd
                    if(c == Chat.Channel.MobArena && ! sender.inMobArena) return@createCmd sender.send(
                        "&b[Chat] &cモブアリーナに入っていません"
                    )
                    sender.send("&aTips: &7「#${c.name.toLowerCase()} メッセージ」でショートカット送信できます")
                    sender.ch = c
                    sender.send("&b[Chat] &fチャットのチャンネルが&a${c.jp}&fになりました")
                }
            }
        }

        createCmd("t", tab { onlinePlayers }) { sender, args ->
            if(args.isEmpty) return@createCmd sender.send("&b[Tell] &c送信先を入力してください")
            val send: CommandSender = if(args[0].toLowerCase() != "console") getPlayer(
                args[0]
            ) ?: return@createCmd sender.send("&b[Tell] &c送信先が見つかりませんでした") else plugin.server.consoleSender
            if(sender == send) return@createCmd sender.send("&b[Tell] &c自分に送ることは出来ません")
            if(args.size < 2) return@createCmd sender.send("&b[Tell] &cメッセージを入力してください")
            val msg = args.joinToString(" ").substring(args[0].length + 1)
            if(sender.replyTo == send) sender.send("&aTips: &7/rで返信できます")
            else sender.send("&aTips: &7「@${send.name} メッセージ」で送信できます")
            sender.send(send, msg)
        }

        createCmd("r") { sender, args ->
            val send = sender.replyTo ?: return@createCmd sender.send("&b[Tell] &c返信先が見つかりませんでした")
            if(args.isEmpty) return@createCmd sender.send("&b[Tell] &cメッセージを入力してください")
            val msg = args.joinToString(separator = " ")
            sender.send(send, msg)
        }

        createCmd("echo") { sender, args ->
            if(args.isNotEmpty) sender.send(args.joinToString(" "))
        }

        createCmd("bc") { _, args ->
            if(args.isNotEmpty) SendMessage.broadcast(args.joinToString(" "))
        }
    }
}