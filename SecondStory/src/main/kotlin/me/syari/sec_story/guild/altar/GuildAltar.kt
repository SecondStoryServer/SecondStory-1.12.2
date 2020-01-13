package me.syari.sec_story.guild.altar

import me.syari.sec_story.guild.Guild.guild
import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.lib.ItemStackPlus.give
import me.syari.sec_story.lib.config.CreateConfig.config
import me.syari.sec_story.lib.inv.CreateInventory.inventory
import me.syari.sec_story.lib.inv.CreateInventory.open
import me.syari.sec_story.lib.inv.CreateInventory.reopen
import me.syari.sec_story.shop.Shops.getShop
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

object GuildAltar {
    private val data = mutableListOf<AltarData>()

    private val items = mutableListOf<CustomItemStack>()

    fun getDataFromExp(exp: Int): AltarData {
        return data.firstOrNull { f -> f.exp.contains(exp) } ?: data.last()
    }

    private var shop: String? = null

    fun CommandSender.loadGuildAltarConfig(){
        data.clear()
        items.clear()
        config("Guild/Altar/config.yml", false){
            output = this@loadGuildAltarConfig

            var preExp = -1
            shop = getString("shop")
            getSection("level")?.forEach { lv ->
                val lvInt = lv.toIntOrNull()
                if(lvInt != null){
                    val exp = getInt("level.$lv.exp")
                    val weekly = getInt("level.$lv.weekly", 2)
                    val daily = getInt("level.$lv.daily", 2)
                    val member = getInt("level.$lv.member", 2)
                    if(exp != null && preExp < exp){
                        data.add(AltarData(lvInt, preExp + 1 .. exp, weekly, daily, member))
                        preExp = exp
                    }
                } else {
                    typeMismatchError("level.$lv", "Int")
                }
            }
            getStringList("item", listOf()).forEach { s ->
                val t = s.split("\\s+".toRegex())
                if(t.size == 3){
                    val item = getItem(t[0], t[1], t[2])
                    if(item != null){
                        items.add(item)
                    } else {
                        send("&cGuildAltar - $s item is null")
                    }
                } else {
                    send("&cGuildAltar - $s format error")
                }
            }
        }
    }

    fun Player.openAltarTop(){
        val g = guild ?: return
        val display = g.altarData.getDisplay(g.altarExp)
        inventory("&9&lギルド祭壇", 1){
            id = "GuildAltar-${g.id}"
            item(3, Material.STORAGE_MINECART, "&a&l奉献", display)
                .event(ClickType.LEFT){
                    openAltarDelivery()
                }
            item(5, Material.BLAZE_POWDER, "&c&l活性化")
                .event(ClickType.LEFT){
                    val tmp = shop
                    if(tmp != null){
                        getShop(tmp)?.open(this@openAltarTop)
                    }
                }
        }.open(this)
    }

    private fun Player.openAltarDelivery(){
        val g = guild ?: return
        val data = g.altarData
        val display = data.getDisplay(g.altarExp)
        inventory("&9&lギルド祭壇 奉献", 4){
            id = "GuildAltar-${g.id}-Delivery"
            cancel = false
            cancel = false
            onClick = { e -> onClickAltarDelivery(e) }
            onClose = { _ ->
                for(ci in 18..35) {
                    val item = inventory.getItem(ci)
                    if(item != null && item.type != Material.AIR){
                        give(CustomItemStack(item), "&bギルド奉献返却", 5)
                    }
                }
            }
            item(0, Material.EYE_OF_ENDER, "&a&lギルド祭壇", display)
            for(i in 1..3){
                item(i, Material.STAINED_GLASS_PANE, "", damage = 8)
            }
            item(4, Material.STORAGE_MINECART, "&6奉献", "&a左クリックで奉献", "&a右クリックで一覧を表示")
                .event(ClickType.LEFT){
                    var calc = 0
                    for(ci in 18..35) {
                        val item = inventory.getItem(ci)
                        if(item != null && item.type != Material.AIR){
                            inventory.setItem(ci, null)
                            val list = items.firstOrNull { f -> f.isSimilar(item) }
                            if(list != null){
                                val exp = list.amount
                                val multi = item.amount
                                calc += exp * multi
                            } else {
                                give(CustomItemStack(item), "&bギルド奉献返却", 5)
                            }
                        }
                    }
                    if(calc != 0){
                        g.altarExp += calc
                        val newLv = g.altarData
                        if(newLv.level == data.level){
                            g.announce("&b[Guild] &a${displayName}&fがギルド祭壇に&a${calc}xp&f奉献しました")
                        } else {
                            g.announce("&b[Guild] &a${displayName}&fがギルド祭壇に&a${calc}xp&f奉献し、&a${newLv.level}レベル&fになりました")
                            g.rawWeeklyQuest = null
                            g.rawDailyQuest = null
                        }
                        reopen("GuildAltar-${g.id}") { p ->
                            p.openAltarTop()
                        }
                        reopen("GuildAltar-${g.id}-Delivery") { p ->
                            p.openAltarDelivery()
                        }
                    }
                }
                .event(ClickType.RIGHT){
                    openAltarList()
                }
            for(i in 5..7){
                item(i, Material.STAINED_GLASS_PANE, "", damage = 8)
            }
            item(8, Material.BARRIER, "&c戻る")
                .event(ClickType.LEFT){
                    openAltarTop()
                }
            for(i in 9..17){
                item(i, Material.STAINED_GLASS_PANE, "", damage = 15)
            }

        }.open(this)
    }

    private fun onClickAltarDelivery(e: InventoryClickEvent) {
        if(e.click == ClickType.NUMBER_KEY){
            e.isCancelled = true
            return
        }
        val i = e.currentItem ?: return
        if(i.type != Material.AIR && items.firstOrNull { f -> f.isSimilar(i) } == null){
            e.isCancelled = true
        }
    }

    private fun Player.openAltarList(){
        val g = guild ?: return
        val display = g.altarData.getDisplay(g.altarExp)
        inventory("&9&lギルド祭壇 一覧", 4){
            id = "GuildAltar-List"
            item(0, Material.EYE_OF_ENDER, "&a&lギルド祭壇", display)
            for(i in 1..7){
                item(i, Material.STAINED_GLASS_PANE, "", damage = 8)
            }
            item(8, Material.BARRIER, "&c戻る")
                .event(ClickType.LEFT){
                    openAltarDelivery()
                }
            for(i in 9..17){
                item(i, Material.STAINED_GLASS_PANE, "", damage = 15)
            }
            for(i in 18..35){
                val item = items.getOrNull(i - 18) ?: break
                val tmp = item.copy()
                tmp.display = "${tmp.display}&d  ${tmp.amount}xp"
                item(i, tmp)
            }
        }.open(this)
    }
}