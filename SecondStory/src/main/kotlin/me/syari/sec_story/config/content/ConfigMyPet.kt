package me.syari.sec_story.config.content

import de.Keyle.MyPet.api.entity.MyPetType
import me.syari.sec_story.hook.MyPet.getPet
import me.syari.sec_story.hook.MyPet.getPetType
import me.syari.sec_story.lib.CustomItemStack
import org.bukkit.Material
import org.bukkit.entity.Player

class ConfigMyPet(val type: MyPetType): ConfigContent() {
    override fun add(p: Player) {
        p.getPet(type)
    }

    override fun display(p: Player): CustomItemStack {
        return CustomItemStack(Material.MONSTER_EGG, "&e${type.name}")
    }

    companion object {
        fun fromTypeName(name: String): ConfigMyPet? {
            return getPetType(name)?.let { ConfigMyPet(it) }
        }
    }
}