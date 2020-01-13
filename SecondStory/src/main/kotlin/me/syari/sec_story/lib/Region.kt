package me.syari.sec_story.lib

import com.sk89q.worldedit.Vector
import com.sk89q.worldedit.world.World
import org.bukkit.Location

class Region(pos1: CustomLocation, pos2: CustomLocation) {
    companion object {
        fun fromNullable(pos1: Location?, pos2: Location?): Region? {
            return if(pos1 != null && pos2 != null) Region(pos1, pos2) else null
        }

        fun fromNullable(world: World?, min: Vector?, max: Vector?): Region? {
            return if(world != null && min != null && max != null) Region(world, min, max) else null
        }
    }

    constructor(pos1: Location, pos2: Location): this(CustomLocation(pos1), CustomLocation(pos2))

    constructor(world: World, min: Vector, max: Vector): this(CustomLocation(world, min), CustomLocation(world, max))

    val max: CustomLocation
    val min: CustomLocation

    init {
        val raw = Triple(
            if(pos1.x < pos2.x) {
                pos2.x to pos1.x
            } else {
                pos1.x to pos2.x
            },
            if(pos1.y < pos2.y) {
                pos2.y to pos1.y
            } else {
                pos1.y to pos2.y
            },
            if(pos1.z < pos2.z) {
                pos2.z to pos1.z
            } else {
                pos1.z to pos2.z
            }
        )
        max = CustomLocation(pos1.world, raw.first.first, raw.second.first, raw.third.first)
        min = CustomLocation(pos1.world, raw.first.second, raw.second.second, raw.third.second)
    }

    fun inRegion(loc: Location): Boolean{
        return loc.world == max.world && loc.x in min.x..max.x && loc.y in min.y..max.y && loc.z in min.z..max.z
    }
}