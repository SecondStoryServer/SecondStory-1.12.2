package me.syari.sec_story.lib

import org.bukkit.ChatColor

object StringEditor {
    val String.toColor get() : String = ChatColor.translateAlternateColorCodes('&', this)
    val List<String>.toColor get() : List<String> {
        val r = mutableListOf<String>()
        this.forEach { s ->
            r.add(s.toColor)
        }
        return r
    }
    val String.toUncolor: String get() = ChatColor.stripColor(this.toColor)
}