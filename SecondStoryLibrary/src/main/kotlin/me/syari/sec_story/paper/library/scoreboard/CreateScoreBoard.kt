package me.syari.sec_story.paper.library.scoreboard

import org.bukkit.plugin.java.JavaPlugin

object CreateScoreBoard {
    fun createBoard(plugin: JavaPlugin, title: String, priority: ScoreBoardPriority, run: CustomScoreBoardEdit.() -> Unit): CustomScoreBoard {
        val board = CustomScoreBoard(plugin, title, priority)
        run.invoke(board)
        return board
    }
}