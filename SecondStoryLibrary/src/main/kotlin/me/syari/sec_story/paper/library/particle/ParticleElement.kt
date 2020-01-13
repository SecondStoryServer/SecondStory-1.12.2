package me.syari.sec_story.paper.library.particle

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.inventory.ItemStack
import org.bukkit.material.MaterialData

sealed class ParticleElement(private val type: Particle){
    open fun spawn(location: Location, count: Int, offsetX: Double, offsetY: Double, offsetZ: Double, speed: Double){
        location.world.spawnParticle(type, location, count, speed, offsetX, offsetY, offsetZ)
    }

    class ItemCrack(private val material: Material): ParticleElement(Particle.ITEM_CRACK){
        override fun spawn(location: Location, count: Int, offsetX: Double, offsetY: Double, offsetZ: Double, speed: Double) {
            location.world.spawnParticle(Particle.ITEM_CRACK, location, count, speed, offsetX, offsetY, offsetZ, ItemStack(material))
        }
    }

    class BlockCrack(private val material: Material): ParticleElement(Particle.BLOCK_CRACK){
        override fun spawn(location: Location, count: Int, offsetX: Double, offsetY: Double, offsetZ: Double, speed: Double) {
            location.world.spawnParticle(Particle.BLOCK_CRACK, location, count, speed, offsetX, offsetY, offsetZ, MaterialData(material))
        }
    }

    class BlockDust(private val material: Material): ParticleElement(Particle.BLOCK_DUST){
        override fun spawn(location: Location, count: Int, offsetX: Double, offsetY: Double, offsetZ: Double, speed: Double) {
            location.world.spawnParticle(Particle.BLOCK_DUST, location, count, speed, offsetX, offsetY, offsetZ, MaterialData(material))
        }
    }

    class FallingDust(private val material: Material): ParticleElement(Particle.FALLING_DUST){
        override fun spawn(location: Location, count: Int, offsetX: Double, offsetY: Double, offsetZ: Double, speed: Double) {
            location.world.spawnParticle(Particle.FALLING_DUST, location, count, speed, offsetX, offsetY, offsetZ, MaterialData(material))
        }
    }

    class RedStone(private val red: Int, private val blue: Int, private val green: Int): ParticleElement(Particle.REDSTONE){
        private fun convertColorValue(value: Int): Double {
            return if (0 < value) {
                value / 255.0
            } else {
                -1.0
            }
        }

        override fun spawn(location: Location, count: Int, offsetX: Double, offsetY: Double, offsetZ: Double, speed: Double) {
            location.world.spawnParticle(Particle.REDSTONE, location, count, speed, convertColorValue(red), convertColorValue(blue), convertColorValue(green))
        }
    }

    class Spell(private val red: Int, private val blue: Int, private val green: Int): ParticleElement(Particle.SPELL){
        private fun convertColorValue(value: Int): Double {
            return if (0 < value) {
                value / 255.0
            } else {
                -1.0
            }
        }

        override fun spawn(location: Location, count: Int, offsetX: Double, offsetY: Double, offsetZ: Double, speed: Double) {
            location.world.spawnParticle(Particle.SPELL, location, count, speed, convertColorValue(red), convertColorValue(blue), convertColorValue(green))
        }
    }

    class MobSpell(private val red: Int, private val blue: Int, private val green: Int): ParticleElement(Particle.SPELL_MOB){
        private fun convertColorValue(value: Int): Double {
            return if (0 < value) {
                value / 255.0
            } else {
                -1.0
            }
        }

        override fun spawn(location: Location, count: Int, offsetX: Double, offsetY: Double, offsetZ: Double, speed: Double) {
            location.world.spawnParticle(Particle.SPELL_MOB, location, count, speed, convertColorValue(red), convertColorValue(blue), convertColorValue(green))
        }
    }
}