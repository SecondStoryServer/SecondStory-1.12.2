package me.syari.sec_story.lib

import com.sk89q.worldedit.Vector
import me.syari.sec_story.plugin.Plugin.plugin
import org.bukkit.Location
import org.bukkit.World

class CustomLocation(var world: World, var x: Double, var y: Double, var z: Double, var yaw: Float = 0.0F, var pitch: Float = 0.0F) {
    constructor(location: Location): this(location.world, location.x, location.y, location.z, location.yaw, location.pitch)
    constructor(world: com.sk89q.worldedit.world.World, vector: Vector): this(plugin.server.getWorld(world.name), vector.x, vector.y, vector.z)

    override fun toString() = "${world.name}, $x, $y, $z"

    val toStringWithYawPitch get() = "${world.name}, $x, $y, $z, $yaw, $pitch"

    val toLocation get() = Location(world, x, y, z, yaw, pitch)
}