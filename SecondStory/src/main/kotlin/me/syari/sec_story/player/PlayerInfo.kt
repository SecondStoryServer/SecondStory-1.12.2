package me.syari.sec_story.player

import me.syari.sec_story.guild.Guild.guildOfflinePlayer
import me.syari.sec_story.lib.message.SendMessage
import me.syari.sec_story.lib.message.SendMessage.Action
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.player.Money.money
import me.syari.sec_story.player.Time.show
import me.syari.sec_story.player.Time.time
import me.syari.sec_story.rank.Ranks.rank
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.time.Instant
import java.time.ZoneId

object PlayerInfo {

/*
・プレイヤー情報
UUID
IP(再起動まで保存)
今いるところ(ログアウトした場所)
ランク(カーソルでコマンド・権限表示)
プレイ時間
投票回数
戦争勝利回数
所持金
ギルド
名前変更履歴(カーソル)
ベッドの場所(クリックでテレポート)
最初にログインした日
最後にログインした日
経験値レベル
ショートカットコマンド(インベントリ, エンチェス, ギルド情報)
*/

    fun CommandSender.check(p: OfflinePlayer){
        val rank = p.rank
        val gp = p.guildOfflinePlayer
        val g = gp.guild()
        val bed = p.bedSpawnLocation
        send(
                "&b[Player] &fプレイヤー情報\n" to null,
                "&7ID: " + (if(p.isOnline) "&a" else "&c") + "${p.name}\n" to null, // Add Name History
                "&7UUID: &6${p.uniqueId}\n" to Action(hover = "&aCopy", click = SendMessage.ClickType.TypeText to p.uniqueId.toString()),
                "&7Rank: &6${rank.name} &7-" to null, "&bCommand " to Action(hover = rank.cmd.joinToString("\n")), "&bPerm\n" to Action(hover = rank.perm.joinToString("\n")),
                "&7Play: &6${p.time.show}\n" to null,
                "&7War: &6${gp.win}\n" to null,
                "&7Money: &6${p.money}JPY\n" to null,
                if(g != null) "&7Guild: &6${g.name}\n" to Action(hover = "&aギルド情報", click = SendMessage.ClickType.RunCommand to "/guild info ${g.name}") else "&7Guild: &6未所属\n" to null,
                "&7Bed: &6${bed?.world?.name}, ${bed?.x}, ${bed?.y}, ${bed?.z}\n" to Action(hover = "&aテレポート", click = SendMessage.ClickType.TypeText to "/wtp ${bed?.world?.name} ${bed?.x} ${bed?.y} ${bed?.z}"),
                "&7First-Played: &6${Instant.ofEpochMilli(p.firstPlayed).atZone(ZoneId.systemDefault()).toLocalDate()}" to null // TODO format

        )
    }
}