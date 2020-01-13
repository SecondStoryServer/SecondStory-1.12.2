package me.syari.sec_story.message

import me.syari.sec_story.game.mobArena.MobArena.arena
import me.syari.sec_story.game.mobArena.MobArena.inMobArena
import me.syari.sec_story.guild.Guild.guild
import me.syari.sec_story.guild.Guild.guildPlayer
import me.syari.sec_story.item.Vote.voteCnt
import me.syari.sec_story.lib.message.SendMessage
import me.syari.sec_story.lib.message.SendMessage.Action
import me.syari.sec_story.lib.message.SendMessage.broadcast
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.StringEditor.toUncolor
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.lib.command.CreateCommand.element
import me.syari.sec_story.lib.command.CreateCommand.elementIfOp
import me.syari.sec_story.lib.command.CreateCommand.onlinePlayers
import me.syari.sec_story.lib.command.CreateCommand.tab
import me.syari.sec_story.message.Conv.convJP
import me.syari.sec_story.message.discord.DiscordChannel
import me.syari.sec_story.message.discord.SendDiscord
import me.syari.sec_story.player.Donate.donate
import me.syari.sec_story.player.Money.money
import me.syari.sec_story.player.Time.show
import me.syari.sec_story.player.Time.time
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.Plugin.plugin
import me.syari.sec_story.plugin.SQL.sql
import me.syari.sec_story.rank.Ranks.rank
import me.syari.sec_story.server.Server.board
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object Chat : Listener, Init() {
    override fun init(){
        createCmd("ch",
            tab { sender ->
                element("global", "guild").joinIfOp(sender, "admin", "obs").joinIf(sender is Player && sender.inMobArena, "mobarena")
            },
            tab("obs"){ sender ->
                elementIfOp(sender, "guild", "mobarena", "private")
            }
        ){ sender, args ->
            if(args.isEmpty){
                if(sender is Player){
                    return@createCmd sender.send("""
                        &b[Chat] &f今のチャンネルは&a${sender.ch.jp}&fです
                        &7- &a/ch global &7グローバルチャットに変更します
                        &7- &a/ch guild &7ギルドチャットに変更します
                        &7- &a/ch mobarena &7モブアリーナチャットに変更します
                    """.trimIndent() + if(sender.isOp) "\n" + """
                        &7- &a/ch admin &7アドミンチャットに変更します
                        &7- &a/ch obs &7チャット監視の切り替えをします
                    """.trimIndent() else "")
                }
            } else {
                if(sender.isOp && args.whenIndex(0) == "obs"){
                    val obs = obsDisable.getOrDefault(sender, mutableListOf())
                    when(val input = args.whenIndex(1)){
                        "guild", "mobarena", "private" -> {
                            if(obs.contains(input)){
                                obs.remove(input)
                                sender.send("&b[Chat] &a${input.toUpperCase()}&fチャンネルを&a表示&fにしました")
                            } else {
                                obs.add(input)
                                sender.send("&b[Chat] &a${input.toUpperCase()}&fチャンネルを&c非表示&fにしました")
                            }
                            obsDisable[sender] = obs
                        }
                        else -> {
                            sender.send("""
                                &b[Chat] &fコマンド
                                &7- &a/ch obs guild &7ギルドチャットを監視します  ${if(obs.contains("guild")) "&c非表示" else "&a表示"}
                                &7- &a/ch obs mobarena &7モブアリーナチャットを監視します  ${if(obs.contains("mobarena")) "&c非表示" else "&a表示"}
                                &7- &a/ch obs private &7プライベートチャットを監視します  ${if(obs.contains("private")) "&c非表示" else "&a表示"}
                            """.trimIndent())
                        }
                    }
                } else if(sender is Player){
                    val ch = args[0].toCh ?: Channel.Global
                    if(ch == Channel.Guild && sender.guild == null) return@createCmd sender.send("&b[Chat] &cギルドに所属していません")
                    val c = if(ch == Channel.Admin && !sender.hasPermission("admin-chat")) Channel.Global else ch
                    if(sender.ch == c) return@createCmd
                    if(c == Channel.MobArena && !sender.inMobArena) return@createCmd sender.send("&b[Chat] &cモブアリーナに入っていません")
                    sender.send("&aTips: &7「#${c.name.toLowerCase()} メッセージ」でショートカット送信できます")
                    sender.ch = c
                    sender.send("&b[Chat] &fチャットのチャンネルが&a${c.jp}&fになりました")
                }
            }
        }

        createCmd("t",
            tab { onlinePlayers() }
        ){ sender, args ->
            if(args.isEmpty) return@createCmd sender.send("&b[Tell] &c送信先を入力してください")
            val send: CommandSender = if(args[0].toLowerCase() != "console") plugin.server.getPlayer(args[0]) ?: return@createCmd sender.send("&b[Tell] &c送信先が見つかりませんでした") else plugin.server.consoleSender
            if(sender == send) return@createCmd sender.send("&b[Tell] &c自分に送ることは出来ません")
            if(args.size < 2) return@createCmd sender.send("&b[Tell] &cメッセージを入力してください")
            val msg = args.joinToString(" ").substring(args[0].length + 1)
            if(r[sender] == send) sender.send("&aTips: &7/rで返信できます")
            else sender.send("&aTips: &7「@${send.name} メッセージ」で送信できます")
            sender.send(send, msg)
        }

        createCmd("r"){ sender, args ->
            val send = r[sender] ?: return@createCmd sender.send("&b[Tell] &c返信先が見つかりませんでした")
            if(args.isEmpty) return@createCmd sender.send("&b[Tell] &cメッセージを入力してください")
            val msg = args.joinToString(separator = " ")
            sender.send(send, msg)
        }

        createCmd("echo"){ sender, args ->
            if(args.isNotEmpty) sender.send(args.joinToString(" "))
        }

        createCmd("bc"){ _, args ->
            if(args.isNotEmpty) broadcast(args.joinToString(" "))
        }
    }

    private val obsDisable = mutableMapOf<CommandSender, MutableList<String>>()

    fun sendObs(ch: String, vararg msg: Pair<String, Action?>, ignore: List<CommandSender> = listOf()){
        plugin.server.onlinePlayers.forEach { p ->
            if(p.isOp && !obsDisable.getOrElse(p){ emptyList<Channel>() }.contains(ch) && p !in ignore){
                p.send(*msg)
            }
        }
        val console = plugin.server.consoleSender
        if(console !in ignore){
            val s = StringBuilder()
            msg.forEach { f -> s.append(f.first) }
            console.send(s.toString())
        }
    }

    private fun sendObs(ch: Channel, vararg msg: Pair<String, Action?>, ignore: List<CommandSender> = listOf()){
        sendObs(ch.name, *msg, ignore = ignore)
    }

    private val chs = mutableMapOf<UUID, Channel>()

    private val Player.info get(): Pair<String, Action> {
        val gp = guildPlayer
        val g = gp.guild()
        return "${rank.prefix}&f${displayName}$suffix" to Action(hover = """
            &a&l${displayName}${if(0 < donate) " &b[Donator]" else ""}
            &7ランク: &6${rank.name}
            &7プレイ時間: &6${time.show}
            &7投票回数: &6${voteCnt}回
            &7戦争勝利回数: &6${gp.win}回
            &7所持金: &6${String.format("%,d", money)}JPY
            &7ギルド: &6${g?.name ?: "&c未所属"}
            
            &aクリックで個人メッセージを送信
        """.trimIndent(), click = SendMessage.ClickType.TypeText to "/t $name ")
    }

    @EventHandler
    fun on(e: AsyncPlayerChatEvent){
        e.isCancelled = true
        val p = e.player
        var conv = true
        var msg = e.message
        if(msg.startsWith('.')){
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
        if(msg.startsWith('@')){
            val s = msg.split(Regex("\\s+"))
            val m = if(s[0] == "@") {
                if(1 < s.size) s[1]
                else return p.send("&b[Chat] &c送信先のプレイヤーを入力してください")
            } else s[0].substring(1)
            val send = plugin.server.getPlayer(m) ?: return p.send("&b[Chat] &c${m}で見つかるプレイヤーがいませんでした")
            msg = msg.substringAfter("$m ")
            if(p == send) return p.send("&b[Chat] &c自分に送ることは出来ません")
            return p.send(send, msg)
        }
        msg = if(!conv) {
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
        when(ch){
            Channel.Admin -> {
                plugin.server.onlinePlayers.filter{ f -> f.hasPermission("admin-ch") }.send("&9&lAdmin " to Action(hover = "&aアドミンチャンネルに変更する", click = SendMessage.ClickType.RunCommand to "/ch admin"), p.info, " &b≫ &f" to null, *m.toTypedArray())
                return
            }
            Channel.Guild -> {
                val g = p.guild
                if(g != null){
                    val mem = g.member
                    mem.send("&3&lGuild " to Action(hover = "&aギルドチャンネルに変更する", click = SendMessage.ClickType.RunCommand to "/ch guild"), p.info, " &b≫ &f" to null, *m.toTypedArray())
                    sendObs(Channel.Guild, "&9&lObs &7Guild(${g.name}) " to null, p.info, "&8: &f" to null, *m.toTypedArray(), ignore = mem)
                    return
                } else {
                    p.ch = Channel.Global
                }
            }
            Channel.MobArena -> {
                val arena = p.arena
                if (arena != null) {
                    arena.announce("&d&lMobArena " to Action(hover = "&aモブアリーナチャンネルに変更する", click = SendMessage.ClickType.RunCommand to "/ch mobarena"), p.info, " &b≫ &f" to null, *m.toTypedArray())
                    sendObs(Channel.MobArena, "&9&lObs &7MobArena(${arena.name}) " to null, p.info, "&8: &f" to null, *m.toTypedArray(), ignore = arena.players.map { it.player })
                    return
                } else {
                    p.ch = Channel.Global
                }
            }
        }
        broadcast("&6&lGlobal " to Action(hover = "&aグローバルチャンネルに変更する", click = SendMessage.ClickType.RunCommand to "/ch global"),p.info, " &b≫ &f" to null, *m.toTypedArray())
        SendDiscord.message(p, DiscordChannel.Global, "${p.displayName.toUncolor} ≫ ${msg.toUncolor}")
    }

    private val String.canURLAccess get(): List<Pair<String, Action?>>{
        val ret = mutableListOf<Pair<String, Action?>>()
        val url = """https?://[\w/:%#\$&\?\(\)~\.=\+\-]+"""
        val mat = url.toPattern().matcher(this)
        val urlList = mutableListOf<String>()
        while(mat.find()){
            urlList.add(mat.group())
        }
        val new = replace(url.toRegex(), "\t§r[URL]§r\t").split('\t')
        var cnt = 0
        new.forEach { f ->
            if(f == "§r[URL]§r"){
                ret.add(urlList[0] to Action(hover = "&aリンクを開く", click = SendMessage.ClickType.OpenURL to urlList[cnt]))
                cnt ++
            } else {
                ret.add(f to null)
            }
        }
        return ret
    }

    private val suffixs = mutableMapOf<UUID, String>()

    var OfflinePlayer.suffix: String
        get(){
            return suffixs.getOrPut(uniqueId){
                var s = ""
                sql {
                    val res = executeQuery("SELECT Suffix FROM Story.Donate WHERE UUID = '$uniqueId';")
                    if (res.next()) {
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
            suffixs[uniqueId] = value
        }

    fun OfflinePlayer.clearSuffix(){
        suffixs.remove(uniqueId)
    }

    enum class Channel(val jp: String) {
        Global("グローバル"), Admin("アドミン"), Guild("ギルド"), MobArena("モブアリーナ")
    }

    var Player.ch
        get() = chs.getOrDefault(uniqueId, Channel.Global)
        set(value) {
            if(value == Channel.Global) chs.remove(uniqueId)
            else chs[uniqueId] = value
            object : BukkitRunnable(){
                override fun run(){
                    board(this@ch)
                }
            }.runTaskLater(plugin, 1)
        }

    private val String.toCh get() = Channel.values().firstOrNull { f -> f.name.toLowerCase().startsWith(toLowerCase()) }

    private val r = mutableMapOf<CommandSender, CommandSender>()

    fun CommandSender.send(send: CommandSender, msg: String){
        val jp = msg.convJP
        val m = (if(msg.matches(Regex("^.*[^\\p{ASCII}].*")) || msg.contains(Regex("http")) || msg == jp){
            msg.toUncolor
        } else if(msg.startsWith('.')) {
            msg.substring(1).toUncolor
        } else {
            "${jp.toUncolor} &e(${msg.toUncolor})"
        }).canURLAccess
        send("&eTell &7${name} → ${send.name} &e≫ &f" to null, *m.toTypedArray())
        send.send("&eTell &7${name} → ${send.name} &e≫ &f" to null, *m.toTypedArray())
        r[send] = this
        r[this] = send
        sendObs("private", "&9&lObs &7Private($name → ${send.name})" to null, "&8: &f" to null, *m.toTypedArray(), ignore = listOf(send, this))
    }
}