package me.syari.sec_story.game.pvp.field

import me.syari.sec_story.lib.Region
import org.bukkit.Location

open class PvPField(pos1: Location, pos2: Location) {
    private val region = Region(pos1, pos2)

    fun inRegion(loc: Location) = region.inRegion(loc)
}