package me.syari.sec_story.paper.library.particle

import org.bukkit.Location
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object ParticleManager {
    fun circle(element: ParticleElement, center: Location, radius: Double, amount: Int, addX: Double = 0.0, addY: Double = 0.0, addZ: Double = 0.0): ParticleCircle {
        return ParticleCircle(element, center, radius, amount, addX, addY, addZ)
    }
}