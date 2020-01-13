package me.syari.sec_story.paper.library.scoreboard

import org.bukkit.entity.Player

interface ScoreBoardLine {
    fun get(player: Player): String

    class Constant(private val line: String):
        ScoreBoardLine {
        override fun get(player: Player) = line
    }

    class Invoke(private val run: Player.() -> String):
        ScoreBoardLine {
        override fun get(player: Player) = run.invoke(player)
    }
}