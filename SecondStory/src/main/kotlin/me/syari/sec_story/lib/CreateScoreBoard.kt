package me.syari.sec_story.lib

import me.syari.sec_story.lib.StringEditor.toColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import java.util.*

object CreateScoreBoard {
    fun createBoard(title: String, player: Player, vararg score: Pair<Int, String>){
        val m = Bukkit.getScoreboardManager()
        val b = m.newScoreboard
        val o = b.registerNewObjective(UUID.randomUUID().toString().substring(0..14), "dummy")
        o.displaySlot = DisplaySlot.SIDEBAR
        o.displayName = title.toColor
        score.forEach { f ->
            o.getScore(f.second.toColor).score = f.first
        }
        if(player.scoreboard != b) player.scoreboard = b
    }
}