package me.syari.sec_story.paper.library.particle

import me.syari.sec_story.paper.library.scheduler.CustomScheduler
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runTimer
import me.syari.sec_story.paper.library.scheduler.CustomTask
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class ParticleCircle(private val element: ParticleElement, center: Location, radius: Double, amount: Int, addX: Double = 0.0, addY: Double = 0.0, addZ: Double = 0.0){
    private val circle: List<Location>

    init {
        val world = center.world
        val increment: Double = 2 * PI / amount
        val circle = mutableListOf<Location>()
        val vector = center.clone().add(addX, addY, addZ)
        for (i in 0 until amount) {
            val angle = i * increment
            val x = vector.x + radius * cos(angle)
            val z = vector.z + radius * sin(angle)
            circle.add(Location(world, x, vector.y, z))
        }
        this.circle = circle
    }

    fun spawn(count: Int, speed: Double){
        circle.forEach {
            element.spawn(it, count, speed, 0.0, 0.0, 0.0)
        }
    }

    fun spawnTimer(count: Int, speed: Double, plugin: JavaPlugin, period: Long, delay: Long = 0): CustomTask? {
        return runTimer(plugin, period, delay) {
            spawn(count, speed)
        }
    }
}