package me.syari.sec_story.guild

import com.shampaggon.crackshot.events.WeaponDamageEntityEvent
import me.syari.sec_story.guild.altar.GuildAltar.openAltarTop
import me.syari.sec_story.guild.area.GuildArea.buyPrice
import me.syari.sec_story.guild.area.GuildArea.canBuyWorld
import me.syari.sec_story.guild.area.GuildArea.getGuild
import me.syari.sec_story.guild.area.GuildArea.sellPrice
import me.syari.sec_story.guild.event.GuildMemberTeleportEvent
import me.syari.sec_story.guild.quest.GuildQuest
import me.syari.sec_story.guild.quest.GuildQuest.openQuestTop
import me.syari.sec_story.guild.quest.QuestType
import me.syari.sec_story.guild.war.GuildWar
import me.syari.sec_story.guild.war.GuildWar.nowWar
import me.syari.sec_story.guild.war.GuildWar.sendWar
import me.syari.sec_story.guild.war.GuildWar.useKits
import me.syari.sec_story.guild.war.GuildWar.waitWarGuild
import me.syari.sec_story.guild.war.WarField
import me.syari.sec_story.lib.PlayerPlus.lastPlayedToDay
import me.syari.sec_story.lib.message.SendMessage
import me.syari.sec_story.lib.message.SendMessage.action
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.StringEditor.toUncolor
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.lib.command.CreateCommand.element
import me.syari.sec_story.lib.command.CreateCommand.elementIf
import me.syari.sec_story.lib.command.CreateCommand.elementIfOp
import me.syari.sec_story.lib.command.CreateCommand.onlinePlayers
import me.syari.sec_story.lib.command.CreateCommand.tab
import me.syari.sec_story.player.Money.hasMoney
import me.syari.sec_story.player.Money.money
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.Plugin.plugin
import me.syari.sec_story.plugin.SQL.sql
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.StructureGrowEvent
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object Guild : Listener, Init() {
    override fun init() {
        createCmd("guild",
            tab { sender ->
                element("list", "info", "player", "create", "join", "leave", "tp", "ff", "invite", "kick", "name", "leader", "area", "money", "quest", "altar")
                    .joinIfOp(sender, "cmd")
            },
            tab("info", "join"){ element(allGuild) },
            tab("player", "invite"){
                onlinePlayers()
            },
            tab("leave"){
                element("confirm")
            },
            tab("area"){ sender ->
                element("check", "select", "buy", "sell").joinIfOp(sender, "set")
            },
            tab("money"){
                element("in", "out")
            },
            tab("war"){ sender ->
                elementIf(sender is Player && sender.waitWarGuild(), "join", "leave", "time", "life", "cost", "field", "ready", not = allGuild) },
            tab("war field"){ sender ->
                elementIf(sender is Player && sender.waitWarGuild(), useKits.map { it.id })
            },
            tab("kick", "leader change", "tp *") { sender ->
                element {
                    if(sender is Player){
                        sender.guild?.offlineMember?.map { it.name }
                    } else null
                }
            },
            tab("leader"){
                element("change", "me")
            },
            tab("tp") {
                element("allow", "request")
            },
            tab("cmd"){ sender ->
                elementIfOp(sender, "guildid", "endwar", "resetdaily", "resetweekly", "clean")
            },
            tab("cmd guildid", "area set"){ sender ->
                elementIfOp(sender, allGuild)
            }
        ){ sender, args ->
            fun sendHelp() = sender.send("""
                    &b[Guild] &fコマンド
                    &7- &a/guild list <Page> &7ギルドの一覧を表示します
                    &7- &a/guild info <Name> &7ギルドの情報を表示します
                    &7- &a/guild player <Player> &7プレイヤーのギルドを表示します
                    &7- &a/guild create <Name> &7ギルドを新しく作ります
                    &7- &a/guild join <Name> &7ギルドに加入します
                    &7- &a/guild leave &7ギルドを脱退します
                    &7- &a/guild tp &7ギルドメンバーにテレポートします
                    &7- &a/guild ff &7ギルドメンバー同士の攻撃の有無を切り替えます
                    &7- &a/guild invite <Player> &7ギルドに招待します
                    &7- &a/guild kick <Player> &7ギルドメンバーを追放します
                    &7- &a/guild name <Name> &7ギルド名を変更します
                    &7- &a/guild leader &7ギルドリーダー関連のコマンドです
                    &7- &a/guild area &7ギルドの土地関連のコマンドです
                    &7- &a/guild money &7ギルドのお金関連のコマンドです
                    &7- &a/guild quest &7ギルドクエストを表示します
                    &7- &a/guild altar &7ギルド祭壇を表示します
                """.trimIndent())

            when (args.whenIndex(0)) {
                "list" -> {
                    val page = args.getOrNull(1)?.toIntOrNull() ?: 1
                    sender.send("&b[Guild] &fギルド一覧 Page.$page")
                    getGuilds(page).forEach { g ->
                        sender.send("&7- &a$g")
                    }
                }
                "info" -> {
                    val rawGuild = args.getOrNull(1)
                    val infoGuild = if (rawGuild != null) {
                        getGuild(rawGuild)
                    } else {
                        if(sender is Player) sender.guild else null
                    } ?: return@createCmd sender.notFoundGuildError()
                    sender.send(infoGuild.info)
                }
                "player" -> {
                    val rawPlayer = args.getOrNull(1) ?: return@createCmd sender.notEnterPlayerError()
                    val p = plugin.server.getPlayer(rawPlayer) ?: return@createCmd sender.notFoundPlayerError()
                    val infoGuild = p.guild ?: return@createCmd sender.send("&b[Guild] &a${p.displayName}&fはギルドに所属していません")
                    sender.send(infoGuild.info)
                }
                "create" -> {
                    if(sender is Player){
                        val guildPlayer = sender.guildPlayer
                        val guild = guildPlayer.guild()
                        if(guild != null) return@createCmd sender.hasGuildError()
                        val guildName = args.getOrNull(1) ?: return@createCmd sender.notEnterGuildError()
                        if(getGuild(guildName) != null) return@createCmd sender.notOnlyGuildNameError()
                        val uuid = UUID.randomUUID()
                        val g = GuildData(uuid)
                        sql {
                            executeUpdate("INSERT INTO Story.Guild VALUE ('$uuid', '$guildName', '$guildName', '${sender.uniqueId}', 0, 0, 0, 0);")
                            executeUpdate("UPDATE Story.PlayerData SET Guild = '$uuid' WHERE UUID = '${sender.uniqueId}';")
                        }
                        g.name = guildName
                        guilds.add(g)
                        guildPlayer.guildID = uuid
                        sender.send("&b[Guild] &fギルド&a${guildName}&fを作成しました")
                    }
                }
                "invite" -> {
                    if(sender is Player){
                        val guildPlayer = sender.guildPlayer
                        val guild = guildPlayer.guild() ?: return@createCmd sender.notHasGuildError()
                        val rawPlayer = args.getOrNull(1) ?: return@createCmd sender.notEnterPlayerError()
                        val p = plugin.server.getPlayer(rawPlayer) ?: return@createCmd sender.notFoundPlayerError()
                        val gp = p.guildPlayer
                        if(gp.guild() != null) return@createCmd sender.alreadyHasGuildError()
                        if(guild.hadInvite(p)) return@createCmd sender.alreadySendGuildInviteError()
                        val name = guild.name
                        guild.addInvite(p)
                        p.send("&b[Guild] &a$name&fから招待されました &a/guild join $name &fで参加できます")
                        guild.announce("&b[Guild] &a${p.displayName}&fをギルドに招待しました")
                        object : BukkitRunnable(){
                            override fun run() {
                                if(guild.canJoin(p)){
                                    guild.removeInvite(p)
                                    p.send("&b[Guild] &a$name&cからの招待がキャンセルされました")
                                    guild.announce("&b[Guild] &a$name&fへの招待がキャンセルされました")
                                }
                            }
                        }.runTaskLater(plugin, 60 * 20)
                    }
                }
                "join" -> {
                    if(sender is Player){
                        val guildPlayer = sender.guildPlayer
                        val guild = guildPlayer.guild()
                        if(guild != null) return@createCmd sender.hasGuildError()
                        val rawGuild = args.getOrNull(1) ?: return@createCmd sender.notEnterGuildError()
                        val g = getGuild(rawGuild) ?: return@createCmd sender.notFoundGuildError()
                        if(g.canJoin(sender)){
                            g.removeInvite(sender)
                            g.addMember(sender)
                            guildPlayer.guildID = g.id
                            guildPlayer.clearGuildInvite()
                            g.announce("&b[Guild] &fギルドに&a${sender.displayName}&fが加入しました")
                        } else {
                            sender.send("&b[Guild] &c招待されないと参加出来ません")
                            g.announce("&b[Guild] &a${sender.displayName}&fがギルドに加入しようとしました")
                        }
                    }

                }
                "leave" -> {
                    if(sender is Player){
                        val guildPlayer = sender.guildPlayer
                        val guild = guildPlayer.guild() ?: return@createCmd sender.notHasGuildError()
                        val confirm = args.getOrNull(1)
                        if(confirm?.toLowerCase() != "confirm"){
                            sender.send("&b[Guild] &f本当にギルドを脱退しますか？ &a/guild leave confirm &fで脱退する")
                        } else {
                            when {
                                guild.isLeader(sender) -> {
                                    guild.announce("&b[Guild] &fギルドが解散しました")
                                    guild.deleteGuild()
                                }
                                guild.isMember(sender) -> {
                                    guild.announce("&b[Guild] &fギルドから&a${sender.displayName}&fが脱退しました")
                                    guild.removeMember(sender)
                                    guildPlayer.guildID = null
                                    guildPlayer.clearTpReq()
                                }
                                else -> sender.send("&b[Guild] &cギルドメンバーではありません")
                            }
                        }
                    }

                }
                "kick" -> {
                    if(sender is Player){
                        val guildPlayer = sender.guildPlayer
                        val guild = guildPlayer.guild() ?: return@createCmd sender.notHasGuildError()
                        if(!guild.isLeader(sender)) return@createCmd sender.notLeaderError()
                        val rawMember = args.getOrNull(1) ?: return@createCmd sender.notEnterGuildMemberError()
                        val member = guild.getOfflineMember(rawMember) ?: return@createCmd sender.notFoundGuildMemberError()
                        if(member == sender) return@createCmd sender.send("&b[Guild] &c自分をキックすることはできません")
                        guild.announce("&b[Guild] &a${member.name}&fがギルドからキックされました")
                        guild.removeMember(member)
                    }
                }
                "leader" -> {
                    if(sender is Player){
                        val guildPlayer = sender.guildPlayer
                        val guild = guildPlayer.guild() ?: return@createCmd sender.notHasGuildError()
                        val leader = guild.leader
                        when(args.whenIndex(1)){
                            "change" -> {
                                if(sender != leader) return@createCmd sender.notLeaderError()
                                val rawMember = args.getOrNull(2) ?: return@createCmd sender.notEnterGuildMemberError()
                                val member = guild.getOfflineMember(rawMember) ?: return@createCmd sender.notFoundGuildMemberError()
                                if(member == sender) return@createCmd sender.send("&b[Guild] &cあなたがギルドリーダーです")
                                guild.leader = member
                                guild.announce("&b[Guild] &a${member.name}&fがギルドのリーダーになりました")
                            }
                            "me" -> {
                                if(sender == leader) return@createCmd sender.send("&b[Guild] &cあなたがギルドリーダーです")
                                if(leader == null || 30 < leader.lastPlayedToDay){
                                    if(args.whenIndex(2) == "confirm"){
                                        guild.leader = sender
                                        guild.announce("&b[Guild] &a${sender.name}&fがギルドのリーダーになりました")
                                    } else {
                                        sender.send("""
                                            &b[Guild] &fギルドリーダーは１ヶ月以上ログインしていないので、あなたがリーダーになることが出来ます
                                            &f本当になりますか？ なるなら、&a/guild me confirm
                                        """.trimIndent())
                                    }
                                } else {
                                    sender.send("&b[Guild] &cギルドリーダーが１ヶ月ログインしていない場合は、ギルドリーダーになることが出来ます")
                                }
                            }
                            else -> {
                                sender.send("""
                                    &b[Guild] &fコマンド
                                    &7- &a/guild leader change &7ギルドリーダーを別のメンバーに変更します
                                    &7- &a/guild leader me &7自分をギルドリーダーに変更します
                                """.trimIndent())
                            }
                        }
                    }
                }
                "name" -> {
                    if(sender is Player){
                        val guildPlayer = sender.guildPlayer
                        val guild = guildPlayer.guild() ?:  return@createCmd sender.notHasGuildError()
                        if(!guild.isLeader(sender)) return@createCmd sender.notLeaderError()
                        val rawGuildName = args.getOrNull(1) ?: return@createCmd sender.send("&b[Guild] &c新しいギルド名を入力してください")
                        val guildName = rawGuildName.toUncolor
                        if(getGuild(guildName) != null) return@createCmd sender.send("&b[Guild] &c存在するギルド名です")
                        guild.name = guildName
                        return@createCmd sender.send("&b[Guild] &fギルド名が&a${guildName}&fになりました")
                    }

                }
                "ff" -> {
                    if(sender is Player){
                        val guildPlayer = sender.guildPlayer
                        val guild = guildPlayer.guild() ?:  return@createCmd sender.notHasGuildError()
                        if(!guild.isLeader(sender)) return@createCmd sender.notLeaderError()
                        val bool = !guild.ff
                        guild.ff = bool
                        guild.announce("&b[Guild] &fフレンドリーファイアが${if(bool) "&a有効" else "&c無効"}&eになりました")
                    }

                }
                "tp" -> {
                    if(sender is Player){
                        val guildPlayer = sender.guildPlayer
                        val guild = guildPlayer.guild() ?: return@createCmd sender.notHasGuildError()
                        when(args.whenIndex(1)){
                            "allow" -> {
                                val rawMember = args.getOrNull(2) ?: return@createCmd sender.notEnterGuildOnlineMemberError()
                                val member = guild.getMember(rawMember) ?: return@createCmd sender.notFoundGuildMemberError()
                                if(guild != member.guild) return@createCmd sender.send("&b[Guild] &c同じギルドではありません")
                                if(guildPlayer.hasTpReq(member)){
                                    val run = GuildMemberTeleportEvent(member, sender).callEvent()
                                    if(run){
                                        member.teleport(sender)
                                        member.send("&b[Guild] &a${sender.displayName}&fへのテレポートリクエストが承諾されました")
                                        sender.send("&b[Guild] &a${member.displayName}&fからのテレポートリクエストを承諾しました")
                                    } else {
                                        listOf(member, sender).send("&b[Guild] &cテレポートはキャンセルされました")
                                    }
                                    guildPlayer.removeTpReq(member)
                                } else {
                                    sender.send("&b[Guild] &cテレポートリクエストが来てません")
                                }
                            }
                            "request" -> {
                                val rawMember = args.getOrNull(2) ?: return@createCmd sender.notEnterGuildOnlineMemberError()
                                val member = guild.getMember(rawMember) ?: return@createCmd sender.notFoundGuildMemberError()
                                val memberGuildPlayer = member.guildPlayer
                                if(member.guild != guild) return@createCmd sender.send("&b[Guild] &c同じギルドではありません")
                                if(memberGuildPlayer.hasTpReq(sender)) return@createCmd sender.send("&b[Guild] &c既にテレポートリクエストを送信しています")
                                memberGuildPlayer.addTpReq(sender)
                                member.send("&b[Guild] &a${sender.displayName}&fからテレポートリクエストが来ています")
                                sender.send("&b[Guild] &a${member.displayName}&fへテレポートリクエストを送信しました")
                                object : BukkitRunnable(){
                                    override fun run() {
                                        if(memberGuildPlayer.hasTpReq(sender)){
                                            member.send("&b[Guild] &a${sender.displayName}&fへのテレポートリクエストが承諾されませんでした")
                                            sender.send("&b[Guild] &a${member.displayName}&fからのテレポートリクエストがキャンセルされました")
                                            memberGuildPlayer.removeTpReq(sender)
                                        }
                                    }
                                }
                            }
                            else -> {
                                return@createCmd  sender.send("""
                                    &b[Guild] &fコマンド
                                    &7- &a/guild tp allow <Player> &7ギルドメンバーのテレポートを許可します
                                    &7- &a/guild tp request <Player> &7ギルドメンバーにテレポートします
                                """.trimIndent())
                            }
                        }
                    }
                }
                "area" -> {
                    if(sender is Player){
                        val guildPlayer = sender.guildPlayer
                        val c = sender.chunk
                        if (c.world.name !in canBuyWorld) return@createCmd  sender.send("&b[Guild] &c購入不可能なワールドです")

                        fun sendHelpArea(){
                            sender.send("""
                                &b[Guild] &fコマンド
                                &7- &a/guild area select &7土地を選択します
                                &7- &a/guild area buy &7選択した土地を購入します
                                &7- &a/guild area sell &7選択した土地を売却します
                            """.trimIndent() + if(sender.isOp) """
                                
                                &7- &a/guild area set <Guild> &7選択した土地を別のギルドの土地に変更します
                            """.trimIndent() else "")
                        }

                        when (args.whenIndex(1)) {
                            "check" -> {
                                val guild = guildPlayer.guild() ?: return@createCmd sender.notHasGuildError()
                                val cGuild = getGuild(c)
                                return@createCmd  sender.send("&b[Guild] " +
                                        when(cGuild) {
                                            guild -> "&f所有している土地です"
                                            null -> "&f購入可能な土地です"
                                            else -> "&a${cGuild.name}&fが所有している土地です"
                                        }
                                        + " &a(${c.x}, ${c.z})")
                            }
                            "select" -> {
                                val bool = !guildPlayer.isAreaSelectMode
                                guildPlayer.isAreaSelectMode = bool
                                if (bool) {
                                    sender.send("""
                                        &b[Guild] &f土地選択モードを&a有効&fにしました
                                        &f右クリックと左クリックで土地の端を選択出来ます
                                    """.trimIndent())
                                } else {
                                    sender.send("&b[Guild] &f土地選択モードを&c無効&fにしました")
                                }
                            }
                            "buy" -> {
                                val guild = guildPlayer.guild() ?: return@createCmd sender.notHasGuildError()
                                val pos1 = guildPlayer.selectPos1
                                val pos2 = guildPlayer.selectPos2
                                if (pos1 == null || pos2 == null) return@createCmd  sender.send("&b[Guild] &c土地が選択されていません")
                                val buy = guildPlayer.getSelectChunk().filter { f -> getGuild(f) == null }
                                if (buy.isEmpty()) return@createCmd  sender.send("&b[Guild] &c購入可能な土地が選択されていません")
                                val m: Long = buy.size * buyPrice
                                if (!guild.hasMoney(m)) return@createCmd  sender.send("&b[Guild] &cギルドの所持金が足りません")
                                buy.forEach { guild.addArea(it) }
                                guild.money -= m
                                sender.send("&b[Guild] &f土地を&a${buy.size}チャンク&f購入しました")
                            }
                            "sell" -> {
                                val guild = guildPlayer.guild() ?: return@createCmd sender.notHasGuildError()
                                val sell = guildPlayer.getSelectChunk().filter { f -> getGuild(f) == guild }
                                if (sell.isEmpty()) return@createCmd  sender.send("&b[Guild] &c売却可能な土地が選択されていません")
                                sell.forEach { guild.removeArea(it) }
                                guild.money += sell.size * sellPrice
                                sender.send("&b[Guild] &f土地を&a${sell.size}チャンク&f売却しました")
                            }
                            "set" -> {
                                if(sender.isOp){
                                    val select = guildPlayer.getSelectChunk()
                                    if (select.isEmpty()) return@createCmd  sender.send("&b[Guild] &c土地が選択されていません")
                                    val rawGuild = args.getOrNull(2) ?: return@createCmd sender.notEnterGuildError()
                                    val g = getGuild(rawGuild) ?: return@createCmd sender.notFoundGuildError()
                                    select.forEach {
                                        getGuild(it)?.removeArea(it)
                                        g.addArea(it)
                                    }
                                    sender.send("&b[Guild] &a${select.size}チャンク&fの土地を&a${g.name}&fのものにしました")
                                } else sendHelpArea()
                            }
                            else -> sendHelpArea()
                        }
                    }

                }
                "money" -> {
                    if(sender is Player){
                        val guildPlayer = sender.guildPlayer
                        val guild = guildPlayer.guild() ?: return@createCmd sender.notHasGuildError()
                        when (args.whenIndex(1)) {
                            "in" -> {
                                val money = args.getOrNull(2)?.toLongOrNull() ?: return@createCmd  sender.send("&b[Guild] &cギルドに入れる金額を入力してください")
                                if(money < 1) return@createCmd sender.send("&b[Guild] &c金額は1JPY以上にしてください")
                                if (!sender.hasMoney(money)) return@createCmd sender.send("&b[Guild] &c所持金が足りません")
                                sender.money -= money
                                guild.money += money
                                guild.announce("&b[Guild] &a${sender.displayName}&fが&a${String.format("%,d", money)}JPY&f入金しました")
                            }
                            "out" -> {
                                val money = args.getOrNull(2)?.toLongOrNull() ?: return@createCmd  sender.send("&b[Guild] &cギルドから引き出す金額を入力してください")
                                if(money < 1) return@createCmd sender.send("&b[Guild] &c金額は1JPY以上にしてください")
                                if(!guild.hasMoney(money)) return@createCmd sender.send("&b[Guild] &cギルドの所持金が足りません")
                                guild.money -= money
                                sender.money += money
                                guild.announce("&b[Guild] &a${sender.displayName}&fが&a${String.format("%,d", money)}JPY&f出金しました")
                            }
                            "check" -> sender.send("&b[Guild] &fギルドの所持金: &a${String.format("%,d", guild.money)}JPY")
                            else -> {
                                sender.send("""
                                    &b[Guild] &fコマンド
                                    &7- &a/guild money check &7ギルドのお金を表示します
                                    &7- &a/guild money in <Money> &7ギルドにお金を預けます
                                    &7- &a/guild money out <Money> &7ギルドからお金を引き出します
                                """.trimIndent())
                            }
                        }
                    }

                }
                "war" -> {
                    if(sender is Player){
                        val guild = sender.guild ?: return@createCmd sender.notHasGuildError()
                        if (sender.waitWarGuild()) {
                            val war = guild.war ?: return@createCmd sender.send("&b[Guild] &cギルド戦争が開催されていません")
                            when (args.whenIndex(1)) {
                                "join" -> {
                                    if(war.containPlayer(sender)) return@createCmd sender.send("&b[Guild] &c既にギルド戦争に参加しています")
                                    war.addMember(sender)
                                    war.announce("&7 >> &b${guild.name}&fの&b${sender.displayName}&fがギルド戦争に参加しました")
                                }
                                "leave" -> {
                                    if(!war.containPlayer(sender)) return@createCmd sender.send("&b[Guild] &cギルド戦争に参加していません")
                                    war.remMember(sender)
                                    war.announce("&7 >> &b${guild.name}&fの&b${sender.displayName}&fがギルド戦争から脱退しました")
                                }
                                "time" -> {
                                    if (args.size < 3) {
                                        return@createCmd  sender.send("&b[Guild] &c時間を分単位で入力してください")
                                    }
                                    val time = args[2].toIntOrNull()
                                    if (time == null || time < 1 || 10 < time) {
                                        return@createCmd  sender.send("&b[Guild] &c1～10の整数で入力してください")
                                    }
                                    war.time = time
                                    war.announce("&7 >> &b${sender.displayName}&fによって戦争の時間が&b${time}分&fになりました")
                                }
                                "life" -> {
                                    if (args.size < 3) {
                                        return@createCmd  sender.send("&b[Guild] &c残機を入力してください")
                                    }
                                    val life = args[2].toIntOrNull()
                                    if (life == null || life < 1 || 5 < life) {
                                        return@createCmd  sender.send("&b[Guild] &c1～5の整数で入力してください")
                                    }
                                    war.life = life
                                    war.announce("&7 >> &b${sender.displayName}&fによって戦争の残機が&b$life&fになりました")
                                }
                                "cost" -> {
                                    if (args.size < 3) {
                                        return@createCmd  sender.send("&b[Guild] &c参加費を入力してください")
                                    }
                                    val cost = args[2].toLongOrNull()
                                    if (cost == null || cost < 0 || 1000000 < cost) {
                                        return@createCmd  sender.send("&b[Guild] &c0~1,000,000の整数で入力してください")
                                    }
                                    war.cost = cost
                                    war.announce("&7 >> &b${sender.displayName}&fによって戦争の参加費が&b${String.format("%,d", cost)}JPY&fになりました")
                                }
                                "field" -> {
                                    val name = args.getOrNull(2) ?: return@createCmd sender.send("&b[Guild] &cフィールド名を入力してください")
                                    val field = WarField.get(name) ?: return@createCmd sender.send("&b[Guild] &cフィールドが見つかりませんでした")
                                    if(field.isUsed) return@createCmd sender.send("&b[Guild] &c既に使われているフィールドです")
                                    war.useField = field
                                    war.announce("&7 >> &b${sender.displayName}&fによって戦争のフィールドが&b${name}&fになりました")
                                }
                                "ready" -> {
                                    guild.warGuild?.readyOK()
                                }
                                else -> {
                                    sender.send("""
                                        &b[Guild] &fコマンド
                                        &7- &a/guild war join &7ギルド戦争に参加します
                                        &7- &a/guild war leave &7ギルド戦争から脱退します
                                        &7- &a/guild war time <Minutes> &7ギルド戦争の時間を変更します
                                        &7- &a/guild war life <Life> &7ギルド戦争の残機を変更します
                                        &7- &a/guild war cost <Money> &7ギルド戦争の参加費を変更します
                                        &7- &a/guild war field <Field> &7ギルド戦争のフィールドを変更します
                                        &7- &a/guild war ready &7ギルド戦争の参加準備を完了します
                                        """.trimIndent())
                                }
                            }
                        } else {
                            val name = args.getOrNull(1) ?: return@createCmd sender.send("&b[Guild] &c宣戦布告するギルドの名前を入力してください")
                            val vs = getGuild(name) ?: return@createCmd sender.send("&b[Guild] &cギルドが見つかりませんでした")
                            sender.sendWar(guild, vs)
                        }
                    }

                }
                "quest" -> {
                    if(sender is Player){
                        if(sender.guild == null) return@createCmd sender.notHasGuildError()
                        sender.openQuestTop()
                    }
                }
                "altar" -> {
                    if(sender is Player){
                        if(sender.guild == null) return@createCmd sender.notHasGuildError()
                        sender.openAltarTop()
                    }
                }
                "cmd" -> {
                    if(sender.isOp){
                        when(args.whenIndex(1)){
                            "guildid" -> {
                                val rawGuild = args.getOrNull(2) ?: return@createCmd sender.send("&b[Guild] &cギルド名を入力してください")
                                val guild = getGuild(rawGuild) ?: return@createCmd sender.send("&b[Guild] &cギルドが見つかりませんでした")
                                sender.send("&b[Guild] &a${guild.name}&fのギルドIDは&a" to null, "${guild.id}" to SendMessage.Action(click = SendMessage.ClickType.TypeText to guild.id.toString()))
                            }
                            "endwar" -> {
                                GuildWar.forceEnd()
                                sender.send("&b[GuildWar] &f強制終了しました")
                            }
                            "resetdaily" -> {
                                GuildQuest.resetQuest(QuestType.Daily)
                                sender.send("&b[Guild] &fデイリークエストをリセットしました")
                            }
                            "resetweekly" -> {
                                GuildQuest.resetQuest(QuestType.Weekly)
                                sender.send("&b[Guild] &fウィークリークエストをリセットしました")
                            }
                            "clean" -> {
                                var delete = 0
                                guilds.forEach loop@{ g ->
                                    g.clearCash()
                                    g.offlineMember.forEach { m ->
                                        if (m.lastPlayedToDay < 30) {
                                            return@loop
                                        }
                                    }
                                    object : BukkitRunnable(){
                                        override fun run() {
                                            g.deleteGuild()
                                        }
                                    }.runTaskLater(plugin, 1 * 20)
                                    delete ++
                                }
                                sender.send("&b[Guild] &a${delete}&fのギルドを削除しました")
                            }
                        }
                    } else return@createCmd sendHelp()
                }
                else -> return@createCmd sendHelp()
            }
        }

        sql {
            val res = executeQuery("SELECT GuildID FROM Story.Guild;")
            while(res.next()){
                val uuid = UUID.fromString(res.getString("GuildID"))
                guilds.add(GuildData(uuid))
            }
        }
    }

    val guilds = mutableListOf<GuildData>()

    fun getGuild(guildID: UUID?): GuildData? {
        if(guildID == null) return null
        return guilds.firstOrNull { g -> g.id == guildID }
    }

    val OfflinePlayer.guildOfflinePlayer get(): GuildOfflinePlayer {
        return if(this is Player) guildPlayer else GuildOfflinePlayer(this)
    }

    private val guildPlayers = mutableListOf<GuildPlayer>()

    val Player.guildPlayer get(): GuildPlayer {
        return guildPlayers.firstOrNull { f -> f.player == this } ?: {
            val tmp = GuildPlayer(this)
            guildPlayers.add(tmp)
            tmp
        }.invoke()
    }

    val Player.guild get() = guildPlayer.guild()

    val guildFromName = mutableMapOf<String, GuildData?>()

    fun getGuild(name: String): GuildData?{
        return guildFromName.getOrPut(name){ guilds.firstOrNull{ g -> g.name == name } }
    }

    @EventHandler
    fun on(e: PlayerJoinEvent){
        val p = e.player
        guildPlayers.add(GuildPlayer(p))
    }

    @EventHandler
    fun on(e: PlayerQuitEvent){
        val p = e.player
        guildPlayers.removeIf { r -> r.player.uniqueId == p.uniqueId }
    }

    @EventHandler
    fun on(e: PlayerMoveEvent){
        val p = e.player
        if(p.location.world.name in canBuyWorld){
            val gp = p.guildPlayer
            val g = gp.enterGuild
            val c = getGuild(p.chunk)
            if(g != c){
                p.action(("&2&l" + (c?.name ?: "荒地")) + "&7&lに入りました")
            }
            gp.enterGuild = c
        }
    }

    private fun cancelCauseFF(ev: Entity?, ea: Entity?): Boolean{
        val v = ev as? Player ?: return false
        val a = ea as? Player ?: return false
        val vgp = v.guildPlayer
        val vg = vgp.guild() ?: return false
        val agp = a.guildPlayer
        val ag = agp.guild() ?: return false
        if(v.nowWar() && a.nowWar()) return false
        return vg == ag && !vg.ff
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: EntityDamageByEntityEvent){
        val v = e.entity
        val a = e.damager
        val cancel = cancelCauseFF(v, a)
        if(cancel){
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: WeaponDamageEntityEvent){
        val v = e.victim
        val a = e.damager
        val cancel = cancelCauseFF(v, a)
        if(cancel){
            e.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun on(e: BlockPlaceEvent){
        val p = e.player
        if(p.isOp) return
        val b = e.block ?: return
        val g = p.guild
        val bg = getGuild(b.chunk)
        if(bg != null && bg != g) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: StructureGrowEvent){
        val g = getGuild(e.location?.chunk)
        e.blocks?.forEach { b ->
            val bg = getGuild(b.chunk)
            if(bg != null && g != bg){
                e.isCancelled = true
                return
            }
        }
    }

    @EventHandler
    fun on(e: BlockFromToEvent){
        val g = getGuild(e.block?.chunk)
        val t = getGuild(e.toBlock?.chunk)
        if(t != null && g != t){
            e.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun on(e: PlayerInteractEvent){
        val b = e.clickedBlock ?: return
        val p = e.player ?: return
        val g = p.guild
        val c = b.chunk
        val cg = getGuild(c)
        selectChunk(e)
        if(!e.isCancelled && !p.isOp && cg != null && cg != g) {
            e.isCancelled = true
        }
    }

    private fun selectChunk(e: PlayerInteractEvent){
        val b = e.clickedBlock ?: return
        val p = e.player
        val gp = p.guildPlayer
        val c = b.chunk
        if(gp.isAreaSelectMode && c.world.name in canBuyWorld){
            val a = e.action
            if(a in listOf(Action.RIGHT_CLICK_BLOCK, Action.LEFT_CLICK_BLOCK)){
                e.isCancelled = true
                if(a == Action.RIGHT_CLICK_BLOCK){
                    if(gp.selectPos1 == c) return
                    gp.selectPos1 = c
                } else {
                    if(gp.selectPos2 == c) return
                    gp.selectPos2 = c
                }
                p.send("&b[Guild] &a${gp.getSelectChunk().size}チャンク&fが選択されています")
            }
        }
    }

    private val allGuild: List<String>
        get() {
            val ret = mutableListOf<String>()
            guilds.forEach { guild ->
                ret.add(guild.name)
            }
            return ret
        }

    private fun getGuilds(page: Int): List<String> {
        val list = mutableListOf<String>()
        sql {
            val res = executeQuery("SELECT Name FROM Story.Guild LIMIT ${(page - 1) * 10}, 10;")
            while(res.next()) list.add(res.getString(1))
        }
        return list
    }

    private fun CommandSender.hasGuildError(){
        send("&b[Guild] &c既にギルドに所属しています")
    }

    private fun CommandSender.notHasGuildError(){
        send("&b[Guild] &cギルドに所属していません")
    }

    private fun CommandSender.notLeaderError(){
        send("&b[Guild] &cリーダーではありません")
    }

    private fun CommandSender.notOnlyGuildNameError(){
        send("&b[Guild] &c既に存在するギルド名です")
    }

    private fun CommandSender.notEnterGuildError(){
        send("&b[Guild] &cギルド名を入力してください")
    }

    private fun CommandSender.notFoundGuildError(){
        send("&b[Guild] &cギルドが見つかりませんでした")
    }

    private fun CommandSender.notEnterGuildOnlineMemberError(){
        send("&b[Guild] &cオンラインのギルドメンバーを入力してください")
    }

    private fun CommandSender.notEnterGuildMemberError(){
        send("&b[Guild] &cギルドメンバーを入力してください")
    }

    private fun CommandSender.notFoundGuildMemberError(){
        send("&b[Guild] &cギルドメンバーが見つかりませんでした")
    }

    private fun CommandSender.notEnterPlayerError(){
        send("&b[Guild] &cプレイヤーを入力してください")
    }

    private fun CommandSender.notFoundPlayerError(){
        send("&b[Guild] &cプレイヤーが見つかりませんでした")
    }

    private fun CommandSender.alreadyHasGuildError(){
        send("&b[Guild] &cそのプレイヤーはギルドに所属しています")
    }

    private fun CommandSender.alreadySendGuildInviteError(){
        send("&b[Guild] &c既に招待を送っています")
    }

    fun onDisable() {
        GuildWar.forceEnd()
        GuildQuest.forceSave()
    }
}