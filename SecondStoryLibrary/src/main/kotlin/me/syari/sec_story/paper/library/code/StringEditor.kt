package me.syari.sec_story.paper.library.code

import org.bukkit.ChatColor

object StringEditor {
    val String.toColor get() : String = ChatColor.translateAlternateColorCodes('&', this)
    val Iterable<String>.toColor get() = map { it.toColor }
    val Array<out String>.toColor get() = map { it.toColor }
    val String.toUncolor: String get() = ChatColor.stripColor(toColor) ?: this
}