package me.syari.sec_story.paper.library.scoreboard

import org.bukkit.entity.Player

interface CustomScoreBoardEdit {
    fun space() = line("")

    fun line(text: String)

    fun line(text: Player.() -> String)
}