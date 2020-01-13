package me.syari.sec_story.paper.core.shop.player

import me.syari.sec_story.paper.library.code.StringEditor.toColor
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Villager
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

open class PlayerShopBase(
    val id: String, val loc: Location, private val name: String, private val owner: OfflinePlayer?
) {
    lateinit var entity: Entity

    init {
        spawn()
    }

    fun checkNPC() {
        if(loc.world.playerCount == 0) {
            if(! entity.isDead) {
                entity.remove()
            }
        } else if(entity.isDead) {
            spawn()
        }
    }

    private fun spawn() {
        val tmp = loc.clone()
        tmp.yaw = (- 180..180).random().toFloat()
        val e = tmp.world.spawnEntity(tmp, EntityType.VILLAGER) as Villager
        e.customName = name.toColor
        e.isSilent = true
        e.setGravity(true)
        e.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).baseValue = 0.0
        e.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).baseValue = 1.0
        e.profession = if(owner != null) Villager.Profession.LIBRARIAN else Villager.Profession.NITWIT
        e.isInvulnerable = true
        e.isCollidable = false
        e.addPotionEffect(PotionEffect(PotionEffectType.JUMP, Int.MAX_VALUE, - 1, false, false))
        e.addScoreboardTag("2nd-PlayerShop")
        entity = e
    }

    fun remove() {
        entity.remove()
    }
}