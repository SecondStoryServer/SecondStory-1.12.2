package me.syari.sec_story.shop.other

import me.syari.sec_story.config.content.ConfigContent
import me.syari.sec_story.config.content.ConfigItemStack
import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.inv.CreateInventory.inventory
import me.syari.sec_story.lib.inv.CreateInventory.open
import me.syari.sec_story.plugin.Plugin.plugin
import me.syari.sec_story.shop.Shop
import me.syari.sec_story.shop.need.NeedList
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryType
import org.bukkit.scheduler.BukkitRunnable

class SlotShop(npc: String, id: String, name: String, val req: ConfigContent, val need: NeedList) : Shop(npc, id, name, 0){
    private enum class SlotPlayerStatus(val display: String, val damage: Short) {
        Can("&a回す", 5),
        NotHasReq("&c回せません", 14),
        NotHasNeed("&c回す権限がありません", 4)
    }

    override fun open(p: Player) {
        open(p, items.firstOrNull()?.first ?: ConfigItemStack(CustomItemStack(Material.STONE)))
    }

    private fun open(p: Player, result: ConfigContent){
        inventory(name, InventoryType.FURNACE){
            item(0, req.display(p))
            val status = when {
                need.getDisplay(p).isNotEmpty() -> SlotPlayerStatus.NotHasNeed
                req.has(p) -> SlotPlayerStatus.Can
                else -> SlotPlayerStatus.NotHasReq
            }
            item(1, Material.STAINED_GLASS_PANE, status.display, damage = status.damage)
                .event(ClickType.LEFT){
                    if(status == SlotPlayerStatus.Can){
                        req.rem(p)
                        rollSlot(p)
                    } else {
                        this@SlotShop.open(p, result)
                    }
                }
            val tmp = result.display(p).copy()
            tmp.addLore(
                "",
                "&6クリックで当たり一覧を開く"
            )
            item(2, tmp.toOneItemStack)
                .event(ClickType.LEFT){ openList(p) }
        }.open(p)
    }

    private val items = mutableListOf<Pair<ConfigContent, Int>>()
    private var perMax = 0

    fun addItem(item: ConfigContent, per: Int){
        items.add(item to per)
        perMax += per
    }

    private fun getRandom(): ConfigContent{
        var r = (0 until perMax).random()
        items.forEach { f ->
            r -= f.second
            if(r < 0) return f.first
        }
        return ConfigItemStack(CustomItemStack(Material.STONE))
    }

    private fun rollSlot(p: Player){
        var cnt = 0
        object : BukkitRunnable(){
            override fun run() {
                val result = getRandom()
                if(cnt == 4){
                    cancel()
                    result.add(p)
                    p.send("&b[Slot] &a${result.display(p).display ?: result.display(p).type.name}&fがあたりました")
                    open(p, result)
                    p.playSound(p.location, Sound.ENTITY_ITEM_PICKUP, 1.0F, 1.0F)
                } else {
                    inventory(name, InventoryType.FURNACE){
                        item(0, req.display(p))
                        item(1, Material.STAINED_GLASS_PANE, "&eスロット中", damage = 4)
                        item(2, result.display(p))
                    }.open(p)
                    p.playSound(p.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0F, 1.0F)
                    cnt += 1
                }
            }
        }.runTaskTimer(plugin, 0, 8)
    }

    private fun openList(p: Player){
        inventory("&9&lあたり一覧", 6) {
            items.forEachIndexed { i, f ->
                item(i, f.first.display(p))
                item(53, Material.BARRIER, "&c戻る")
                        .event(ClickType.LEFT){ this@SlotShop.open(p) }
            }
        }.open(p)
    }
}