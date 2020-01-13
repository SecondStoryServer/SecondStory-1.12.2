package me.syari.sec_story.paper.core.config.content

import de.Keyle.MyPet.api.entity.MyPetType
import me.syari.sec_story.paper.core.hook.MyPet.getPet
import me.syari.sec_story.paper.core.hook.MyPet.getPetType
import me.syari.sec_story.paper.library.config.content.ConfigContentAdd
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.Material
import org.bukkit.entity.Player

class ConfigMyPet(val type: MyPetType): ConfigContentAdd {
    override fun add(p: Player) {
        p.getPet(type)
    }

    override fun display(p: Player): CustomItemStack {
        return CustomItemStack(
            Material.MONSTER_EGG, "&e${type.name}"
        )
    }

    companion object {
        fun fromTypeName(name: String): ConfigMyPet? {
            return getPetType(name)?.let { ConfigMyPet(it) }
        }
    }
}