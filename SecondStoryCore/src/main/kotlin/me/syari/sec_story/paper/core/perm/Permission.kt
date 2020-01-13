package me.syari.sec_story.paper.core.perm

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.donate.Donate.donate
import me.syari.sec_story.paper.core.plugin.SQL.sql
import me.syari.sec_story.paper.core.rank.Ranks.rank
import me.syari.sec_story.paper.library.code.StringEditor.toColor
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.element
import me.syari.sec_story.paper.library.command.CreateCommand.offlinePlayers
import me.syari.sec_story.paper.library.command.CreateCommand.onlinePlayers
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.server.Server.getOfflinePlayer
import me.syari.sec_story.paper.library.server.Server.getPlayer
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionAttachment
import java.util.*

object Permission: FunctionInit {
    override fun init() {
        createCmd(
            "perm",
            tab { element("add", "rem", "list", "reload") },
            tab("add", "rem", "list") { offlinePlayers },
            tab("reload") { onlinePlayers }) { sender, args ->
            when(args.whenIndex(0)) {
                "add", "rem" -> {
                    val name = args.getOrNull(1) ?: return@createCmd sender.send("&b[Perm] &cプレイヤーを入力してください")
                    val p = getOfflinePlayer(name) ?: return@createCmd sender.send("&b[Perm] &cプレイヤーが見つかりませんでした")
                    val perm = args.getOrNull(2) ?: return@createCmd sender.send("&b[Perm] &cパーミッションを入力してください")
                    when(args.whenIndex(0)) {
                        "add" -> {
                            if(p.hasSavePerm(perm)) return@createCmd sender.send("&b[Perm] &c既に権限が設定されています")
                            p.addSavePerm(perm)
                            sender.send("&b[Perm] &a${p.name}&fに&a${perm}&fを追加しました")
                        }
                        "rem" -> {
                            if(! p.hasSavePerm(perm)) return@createCmd sender.send("&b[Perm] &c権限が設定されていません")
                            p.remSavePerm(perm)
                            sender.send("&b[Perm] &a${p.name}&fから&a${perm}&fを削除しました")
                        }
                    }
                }
                "list" -> {
                    val name = args.getOrNull(1) ?: return@createCmd sender.send("&b[Perm] &cプレイヤーを入力してください")
                    val p = getOfflinePlayer(name) ?: return@createCmd sender.send("&b[Perm] &cプレイヤーが見つかりませんでした")
                    val s = StringBuilder()
                    s.appendln("&b[Perm] &fパーミッション一覧")
                    p.getSavePerm(false).forEach { s.appendln("&7- &a$it") }
                    sender.send(s)
                }
                "reload" -> {
                    val name = args.getOrNull(1)
                    if(name != null) {
                        val p = getPlayer(name) ?: return@createCmd sender.send("&b[Perm] &cプレイヤーが見つかりませんでした")
                        p.loadPerm()
                        sender.send("&b[Perm] &a${p.name}&fのパーミッションをリロードしました")
                    } else {
                        plugin.server.onlinePlayers.forEach { p ->
                            p.loadPerm()
                        }
                        sender.send("&b[Perm] &fオンラインプレイヤーのパーミッションをリロードしました")
                    }
                }
                else -> {
                    sender.send(
                        """
                        &b[Perm] &fコマンド一覧
                        &7- &a/perm add <Player> &7プレイヤーの権限を追加します
                        &7- &a/perm rem <Player> &7プレイヤーの権限を削除します
                        &7- &a/perm list <Player> &7プレイヤーの権限一覧を表示します
                        &7- &a/perm reload <Player> &7プレイヤーの権限をリロードします
                        &7- &a/perm reload &7全プレイヤーの権限をリロードします
                    """.trimIndent()
                    )
                }
            }
        }
    }

    private val perms = mutableMapOf<UUID, PermissionAttachment>()
    private val savePerm = mutableMapOf<UUID, MutableList<String>>()

    private fun OfflinePlayer.hasSavePerm(perm: String) = getSavePerm(false).contains(perm)

    private fun OfflinePlayer.addSavePerm(perm: String) {
        sql {
            executeUpdate("INSERT INTO Story.Permission VALUE ('$uniqueId', '$perm')")
        }
        if(this is Player) loadPerm()
    }

    private fun OfflinePlayer.remSavePerm(perm: String) {
        sql {
            executeUpdate("DELETE FROM Story.Permission WHERE UUID = '$uniqueId' AND Perm = '$perm'")
        }
        if(this is Player) loadPerm()
    }

    private fun OfflinePlayer.getSavePerm(reload: Boolean): List<String> {
        if(reload) savePerm.remove(uniqueId)
        return savePerm.getOrPut(uniqueId) {
            val perm = mutableListOf<String>()
            sql {
                val res = executeQuery("SELECT Perm FROM Story.Permission WHERE UUID = '$uniqueId'")
                while(res.next()) {
                    val p = res.getString("Perm") ?: continue
                    perm.add(p)
                }
            }
            perm
        }
    }

    fun Player.loadPerm() {
        val perm = addAttachment(plugin)
        perm.setPermission("*", false)
        val e = PermissionLoadEvent(this)
        e.callEvent()
        e.getPermission().forEach { p ->
            perm.setPermission(p, true)
        }
        getSavePerm(true).forEach { p ->
            perm.setPermission(p, true)
        }
        if(perms.containsKey(uniqueId)) {
            unloadPerm()
        }
        playerListName = "${rank.prefix}${if(donate != - 1) "&b" else "&f"}$displayName".toColor
        perms[uniqueId] = perm
    }

    fun Player.unloadPerm() {
        if(perms.containsKey(uniqueId)) {
            removeAttachment(perms[uniqueId])
            perms.remove(uniqueId)
        }
    }
}