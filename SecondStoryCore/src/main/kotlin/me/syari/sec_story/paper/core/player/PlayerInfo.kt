package me.syari.sec_story.paper.core.player

import me.syari.sec_story.paper.core.guild.Guild.guildOfflinePlayer
import me.syari.sec_story.paper.core.player.Money.money
import me.syari.sec_story.paper.core.player.Time.show
import me.syari.sec_story.paper.core.player.Time.time
import me.syari.sec_story.paper.core.rank.Ranks.rank
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.offlinePlayers
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.message.JsonAction
import me.syari.sec_story.paper.library.message.JsonClickType
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.server.Server.getOfflinePlayer
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object PlayerInfo: FunctionInit {
    override fun init() {
        createCmd("cplayer", tab { offlinePlayers }) { sender, args ->
            val name = args.getOrNull(0) ?: return@createCmd sender.send("&b[CPlayer] &cプレイヤーを入力してください")
            getOfflinePlayer(name)?.let { sender.check(it) }
        }
    }

    /*
    今いるところ(ログアウトした場所)
    経験値レベル
    ショートカットコマンド(インベントリ, エンチェス, ギルド情報)
    */
    private val format = DateTimeFormatter.ofPattern("yy/MM/dd")

    fun CommandSender.check(p: OfflinePlayer) {
        val rank = p.rank
        val gp = p.guildOfflinePlayer
        val g = gp.guild()
        val bed = p.bedSpawnLocation
        send(
            "&b[Player] &fプレイヤー情報\n" to null,
            "&7ID: " + (if(p.isOnline) "&a" else "&c") + "${p.name}\n" to null,
            if(p is Player) "&7IP: &6${p.address.address.hostAddress}\n" to JsonAction(
                hover = "&aCopy", click = JsonClickType.TypeText to p.address.address.hostAddress
            ) else "" to null,
            "&7UUID: &6${p.uniqueId}\n" to JsonAction(
                hover = "&aCopy", click = JsonClickType.TypeText to p.uniqueId.toString()
            ),
            "&7Rank: &6${rank.name} &7- " to null,
            "&bCommand " to JsonAction(hover = "&a${rank.cmd.joinToString(", ")}"),
            "&bPerm\n" to JsonAction(hover = "&a${rank.perm.joinToString(", ")}"),
            "&7Play: &6${p.time.show}\n" to null,
            "&7War: &6${gp.win} 回\n" to null,
            "&7Money: &6${p.money} JPY\n" to null,
            if(g != null) "&7Guild: &6${g.name}\n" to JsonAction(
                hover = "&aギルド情報", click = JsonClickType.TypeText to "/guild info ${g.name}"
            ) else "&7Guild: &6未所属\n" to null,
            if(bed != null) "&7Bed: &6${bed.world?.name}, ${bed.x}, ${bed.y}, ${bed.z}\n" to JsonAction(
                hover = "&aテレポート",
                click = JsonClickType.TypeText to "/wtp ${bed.world?.name} ${bed.x} ${bed.y} ${bed.z}"
            ) else "" to null,
            "&7First-Played: &6${Instant.ofEpochMilli(p.firstPlayed).atZone(ZoneId.systemDefault()).format(
                format
            )}\n" to null,
            "&7Last-Played: &6${Instant.ofEpochMilli(p.lastPlayed).atZone(ZoneId.systemDefault()).format(
                format
            )}\n" to null
        )

    }
}