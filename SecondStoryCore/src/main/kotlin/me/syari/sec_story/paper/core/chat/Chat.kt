package me.syari.sec_story.paper.core.chat

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.chat.Conv.convJP
import me.syari.sec_story.paper.core.discord.DiscordChannel
import me.syari.sec_story.paper.core.discord.SendDiscord
import me.syari.sec_story.paper.core.donate.Donate.donate
import me.syari.sec_story.paper.core.game.mobArena.MobArena.arena
import me.syari.sec_story.paper.core.guild.Guild.guild
import me.syari.sec_story.paper.core.guild.Guild.guildPlayer
import me.syari.sec_story.paper.core.player.Money.money
import me.syari.sec_story.paper.core.player.Time.show
import me.syari.sec_story.paper.core.player.Time.time
import me.syari.sec_story.paper.core.plugin.SQL.sql
import me.syari.sec_story.paper.core.rank.Ranks.rank
import me.syari.sec_story.paper.core.server.Server.board
import me.syari.sec_story.paper.core.vote.Vote.voteCnt
import me.syari.sec_story.paper.library.code.StringEditor.toUncolor
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.message.JsonAction
import me.syari.sec_story.paper.library.message.JsonClickType
import me.syari.sec_story.paper.library.message.SendMessage.broadcast
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.message.SendMessage.sendConsole
import me.syari.sec_story.paper.library.player.UUIDPlayer
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import me.syari.sec_story.paper.library.server.Server.getPlayer
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerQuitEvent

object Chat: EventInit {
    private val obsDisable = mutableMapOf<UUIDPlayer, MutableList<String>>()

    fun Player.isObsDisable(ch: String) = obsDisable[UUIDPlayer(this)]?.contains(ch) ?: false

    fun Player.toggleObsDisable(ch: String): Boolean {
        val uuidPlayer = UUIDPlayer(this)
        val obs = obsDisable.getOrDefault(uuidPlayer, mutableListOf())
        return if(obs.contains(ch)) {
            obs.remove(ch)
            false
        } else {
            obs.add(ch)
            true
        }
    }

    private fun sendObs(ch: String, vararg msg: Pair<String, JsonAction?>, ignore: List<CommandSender> = listOf()) {
        plugin.server.onlinePlayers.forEach { p ->
            if(p.isOp && ! p.isObsDisable(ch) && p !in ignore) {
                p.send(*msg)
            }
        }
        val s = StringBuilder()
        msg.forEach { f -> s.append(f.first) }
        sendConsole(s.toString())
    }

    private fun sendObs(ch: Channel, vararg msg: Pair<String, JsonAction?>, ignore: List<CommandSender> = listOf()) {
        sendObs(ch.name, *msg, ignore = ignore)
    }

    private val chs = mutableMapOf<UUIDPlayer, Channel>()

    private val Player.info
        get(): Pair<String, JsonAction> {
            val gp = guildPlayer
            val g = gp.guild()
            return "${rank.prefix}&f${displayName}$suffix" to JsonAction(
                hover = """
            &a&l${displayName}${if(0 < donate) " &b[Donator]" else ""}
            &7ランク: &6${rank.name}
            &7プレイ時間: &6${time.show}
            &7投票回数: &6${voteCnt}回
            &7戦争勝利回数: &6${gp.win}回
            &7所持金: &6${String.format("%,d", money)}JPY
            &7ギルド: &6${g?.name ?: "&c未所属"}
            
            &aクリックで個人メッセージを送信
        """.trimIndent(), click = JsonClickType.TypeText to "/t $name "
            )
        }

    @EventHandler
    fun on(e: AsyncPlayerChatEvent) {
        e.isCancelled = true
        val p = e.player
        var conv = true
        var msg = e.message
        if(msg.startsWith('.')) {
            conv = false
        }
        val ch = if(msg.startsWith('#')) {
            val s = msg.split(Regex("\\s+"))
            val m = if(s[0] == "#") {
                if(1 < s.size) s[1]
                else return p.send("&b[Chat] &c送信先のチャンネルを入力してください")
            } else s[0].substring(1)
            msg = msg.substringAfter("$m ")
            m.toCh ?: return p.send("&b[Chat] &c${m}で見つかるチャンネルはありませんでした")
        } else {
            p.ch
        }
        if(msg.startsWith('@')) {
            val s = msg.split(Regex("\\s+"))
            val m = if(s[0] == "@") {
                if(1 < s.size) s[1]
                else return p.send("&b[Chat] &c送信先のプレイヤーを入力してください")
            } else s[0].substring(1)
            val send = getPlayer(m) ?: return p.send("&b[Chat] &c${m}で見つかるプレイヤーがいませんでした")
            msg = msg.substringAfter("$m ")
            if(p == send) return p.send("&b[Chat] &c自分に送ることは出来ません")
            return p.send(send, msg)
        }
        msg = if(! conv) {
            msg.substring(1).toUncolor
        } else if(msg.matches(Regex("^.*[^\\p{ASCII}].*")) || msg.contains(Regex("http"))) {
            msg.toUncolor
        } else {
            val jp = msg.convJP
            if(msg == jp) {
                msg.toUncolor
            } else {
                "${jp.toUncolor} &b(${msg.toUncolor})"
            }
        }
        if(msg.isEmpty()) return p.send("&b[Chat] &cメッセージを入力してください")
        val m = msg.canURLAccess
        when(ch) {
            Channel.Admin -> {
                plugin.server.onlinePlayers.filter { f -> f.hasPermission("admin-ch") }.send(
                    "&9&lAdmin " to JsonAction(
                        hover = "&aアドミンチャンネルに変更する", click = JsonClickType.RunCommand to "/ch admin"
                    ), p.info, " &b≫ &f" to null, *m.toTypedArray()
                )
                return
            }
            Channel.Guild -> {
                val g = p.guild
                if(g != null) {
                    val mem = g.member
                    mem.send(
                        "&3&lGuild " to JsonAction(
                            hover = "&aギルドチャンネルに変更する", click = JsonClickType.RunCommand to "/ch guild"
                        ), p.info, " &b≫ &f" to null, *m.toTypedArray()
                    )
                    sendObs(
                        Channel.Guild,
                        "&9&lObs &7Guild(${g.name}) " to null,
                        p.info,
                        "&8: &f" to null,
                        *m.toTypedArray(),
                        ignore = mem
                    )
                    return
                } else {
                    p.ch = Channel.Global
                }
            }
            Channel.MobArena -> {
                val arena = p.arena
                if(arena != null) {
                    arena.announce(
                        "&d&lMobArena " to JsonAction(
                            hover = "&aモブアリーナチャンネルに変更する", click = JsonClickType.RunCommand to "/ch mobarena"
                        ), p.info, " &b≫ &f" to null, *m.toTypedArray()
                    )
                    sendObs(
                        Channel.MobArena,
                        "&9&lObs &7MobArena(${arena.name}) " to null,
                        p.info,
                        "&8: &f" to null,
                        *m.toTypedArray(),
                        ignore = arena.players.map { it.player })
                    return
                } else {
                    p.ch = Channel.Global
                }
            }
        }
        broadcast(
            "&6&lGlobal " to JsonAction(hover = "&aグローバルチャンネルに変更する", click = JsonClickType.RunCommand to "/ch global"),
            p.info,
            " &b≫ &f" to null,
            *m.toTypedArray()
        )
        SendDiscord.message(p, DiscordChannel.Global, "${p.displayName.toUncolor} ≫ ${msg.toUncolor}")
    }

    private val String.canURLAccess
        get(): List<Pair<String, JsonAction?>> {
            val ret = mutableListOf<Pair<String, JsonAction?>>()
            val url = """https?://[\w/:%#\$&\?\(\)~\.=\+\-]+"""
            val mat = url.toPattern().matcher(this)
            val urlList = mutableListOf<String>()
            while(mat.find()) {
                urlList.add(mat.group())
            }
            val new = replace(url.toRegex(), "\t§r[URL]§r\t").split('\t')
            var cnt = 0
            new.forEach { f ->
                if(f == "§r[URL]§r") {
                    ret.add(urlList[0] to JsonAction(hover = "&aリンクを開く", click = JsonClickType.OpenURL to urlList[cnt]))
                    cnt ++
                } else {
                    ret.add(f to null)
                }
            }
            return ret
        }

    private val suffixList = mutableMapOf<UUIDPlayer, String>()

    var OfflinePlayer.suffix: String
        get() {
            return suffixList.getOrPut(UUIDPlayer(this)) {
                var s = ""
                sql {
                    val res = executeQuery("SELECT Suffix FROM Story.Donate WHERE UUID = '$uniqueId';")
                    if(res.next()) {
                        s = res.getString("Suffix") ?: ""
                    }
                }
                s
            }
        }
        set(value) {
            sql {
                executeUpdate("UPDATE Story.Donate VALUE SET Suffix = '$value' WHERE UUID = '$uniqueId';")
            }
            suffixList[UUIDPlayer(this)] = value
        }

    fun OfflinePlayer.clearSuffix() {
        suffixList.remove(UUIDPlayer(this))
    }

    enum class Channel(val jp: String) {
        Global("グローバル"),
        Admin("アドミン"),
        Guild("ギルド"),
        MobArena("モブアリーナ")
    }

    var Player.ch
        get() = chs.getOrDefault(UUIDPlayer(this), Channel.Global)
        set(value) {
            if(value == Channel.Global) chs.remove(UUIDPlayer(this))
            else chs[UUIDPlayer(this)] = value
            runLater(plugin, 1) {
                board.updatePlayer(this@ch)
            }
        }

    val String.toCh get() = Channel.values().firstOrNull { f -> f.name.toLowerCase().startsWith(toLowerCase()) }

    private val reply = mutableMapOf<CommandSender, CommandSender>()

    var CommandSender.replyTo
        get(): CommandSender? {
            return reply[this]
        }
        set(value) {
            if(value != null) {
                reply[this] = value
            } else {
                reply.remove(this)
            }
        }

    @EventHandler
    fun on(e: PlayerQuitEvent) {
        val p = e.player ?: return
        reply.remove(p)
        reply.values.remove(p)
    }

    fun CommandSender.send(send: CommandSender, msg: String) {
        val jp = msg.convJP
        val m = (if(msg.matches(Regex("^.*[^\\p{ASCII}].*")) || msg.contains(Regex("http")) || msg == jp) {
            msg.toUncolor
        } else if(msg.startsWith('.')) {
            msg.substring(1).toUncolor
        } else {
            "${jp.toUncolor} &e(${msg.toUncolor})"
        }).canURLAccess
        send("&eTell &7${name} → ${send.name} &e≫ &f" to null, *m.toTypedArray())
        send.send("&eTell &7${name} → ${send.name} &e≫ &f" to null, *m.toTypedArray())
        send.replyTo = this
        this.replyTo = send
        sendObs(
            "private",
            "&9&lObs &7Private($name → ${send.name})" to null,
            "&8: &f" to null,
            *m.toTypedArray(),
            ignore = listOf(send, this)
        )
    }
}