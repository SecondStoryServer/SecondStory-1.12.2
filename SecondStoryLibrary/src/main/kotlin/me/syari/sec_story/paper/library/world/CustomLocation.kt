package me.syari.sec_story.paper.library.world

import org.bukkit.Location
import org.bukkit.World

class CustomLocation(
    var world: World, var x: Double, var y: Double, var z: Double, var yaw: Float = 0.0F, var pitch: Float = 0.0F
) {
    constructor(location: Location): this(
        location.world, location.x, location.y, location.z, location.yaw, location.pitch
    )

    override fun toString() = "${world.name}, $x, $y, $z"

    val toStringWithYawPitch get() = "${world.name}, $x, $y, $z, $yaw, $pitch"

    val toLocation get() = Location(world, x, y, z, yaw, pitch)
}