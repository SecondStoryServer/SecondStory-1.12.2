package me.syari.sec_story.paper.core.shop.other

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.shop.Shop
import me.syari.sec_story.paper.core.shop.need.NeedList
import me.syari.sec_story.paper.library.config.content.ConfigContentAdd
import me.syari.sec_story.paper.library.config.content.ConfigContentRemove
import me.syari.sec_story.paper.library.config.content.ConfigItemStack
import me.syari.sec_story.paper.library.inv.CreateInventory.inventory
import me.syari.sec_story.paper.library.item.CustomItemStack
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runRepeatTimes
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runTimer
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryType

class SlotShop(
    npc: String, id: String, name: String, val req: ConfigContentRemove, val need: NeedList
): Shop(npc, id, name, 0) {
    private enum class SlotPlayerStatus(val display: String, val damage: Short) {
        Can("&a回す", 5),
        NotHasReq("&c回せません", 14),
        NotHasNeed("&c回す権限がありません", 4)
    }

    override fun open(p: Player) {
        open(p, items.firstOrNull()?.first ?: ConfigItemStack(CustomItemStack(Material.STONE)))
    }

    private fun open(p: Player, result: ConfigContentAdd) {
        inventory(name, InventoryType.FURNACE) {
            item(0, req.display(p))
            val status = when {
                need.getDisplay(p).isNotEmpty() -> SlotPlayerStatus.NotHasNeed
                req.has(p) -> SlotPlayerStatus.Can
                else -> SlotPlayerStatus.NotHasReq
            }
            item(1, Material.STAINED_GLASS_PANE, status.display, damage = status.damage).event(ClickType.LEFT) {
                if(status == SlotPlayerStatus.Can) {
                    req.remove(p)
                    rollSlot(p)
                } else {
                    this@SlotShop.open(p, result)
                }
            }
            val tmp = result.display(p).copy()
            tmp.addLore(
                "", "&6クリックで当たり一覧を開く"
            )
            item(2, tmp.toOneItemStack).event(ClickType.LEFT) { openList(p) }
        }.open(p)
    }

    private val items = mutableListOf<Pair<ConfigContentAdd, Int>>()
    private var perMax = 0

    fun addItem(item: ConfigContentAdd, per: Int) {
        items.add(item to per)
        perMax += per
    }

    private fun getRandom(): ConfigContentAdd {
        var r = (0 until perMax).random()
        items.forEach { f ->
            r -= f.second
            if(r < 0) return f.first
        }
        return ConfigItemStack(CustomItemStack(Material.STONE))
    }

    private fun rollSlot(p: Player) {
        var result: ConfigContentAdd = ConfigItemStack(CustomItemStack(Material.STONE))
        runRepeatTimes(plugin, 8, 4) {
            result = getRandom()
            inventory(name, InventoryType.FURNACE) {
                item(0, req.display(p))
                item(1, Material.STAINED_GLASS_PANE, "&eスロット中", damage = 4)
                item(2, result.display(p))
            }.open(p)
            p.playSound(p.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0F, 1.0F)
        }?.onEndRepeat {
            result.add(p)
            p.send("&b[Slot] &a${result.display(p).display ?: result.display(p).type.name}&fがあたりました")
            open(p, result)
            p.playSound(p.location, Sound.ENTITY_ITEM_PICKUP, 1.0F, 1.0F)
        }
    }

    private fun openList(p: Player) {
        inventory("&9&lあたり一覧", 6) {
            items.forEachIndexed { i, f ->
                item(i, f.first.display(p))
                item(53, Material.BARRIER, "&c戻る").event(ClickType.LEFT) { this@SlotShop.open(p) }
            }
        }.open(p)
    }
}