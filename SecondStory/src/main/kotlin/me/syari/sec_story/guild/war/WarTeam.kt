package me.syari.sec_story.guild.war

import me.syari.sec_story.hook.MythicMobs.getItemFromMythicMobs
import org.bukkit.inventory.ItemStack

enum class WarTeam(val id: String) {
    RED("WarRedTeam"),
    BLUE("WarBlueTeam");

    val helmet get(): ItemStack?{
        return getItemFromMythicMobs(id, 1)?.toOneItemStack
    }
}