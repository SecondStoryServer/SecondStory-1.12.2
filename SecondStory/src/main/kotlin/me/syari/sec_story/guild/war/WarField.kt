package me.syari.sec_story.guild.war

import org.bukkit.Location

data class WarField(val redSpawn: Location, val blueSpawn: Location){
    companion object {
        var list  = mapOf<String, WarField>()

        fun random(): WarField? {
            val r = list.values.filterNot { it.isUsed }
            return if(r.isNotEmpty()) r.random() else null
        }

        fun get(name: String) = list[name]
    }

    var isUsed = false
}