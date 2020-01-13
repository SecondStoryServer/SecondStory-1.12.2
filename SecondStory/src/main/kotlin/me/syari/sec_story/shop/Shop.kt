package me.syari.sec_story.shop

import me.syari.sec_story.lib.inv.CreateInventory.CustomInventory
import me.syari.sec_story.shop.jump.Jump
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

open class Shop(val npc: String, val id: String, val name: String, val line: Int) {
    open fun open(p: Player){}

    var jump = mapOf<Int, Jump>()

    fun CustomInventory.loadJump(p: Player) {
        jump.forEach { f ->
            item(f.key, f.value.getDisplay(p)).event(ClickType.LEFT) { f.value.run(p) }
        }
    }
}