package me.syari.sec_story.shop

import me.syari.sec_story.config.content.*
import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.lib.StringEditor.toColor
import me.syari.sec_story.lib.config.CreateConfig.config
import me.syari.sec_story.lib.config.CreateConfig.configDir
import me.syari.sec_story.shop.Shops.addShop
import me.syari.sec_story.shop.Shops.clearShop
import me.syari.sec_story.shop.buy.BuyItem
import me.syari.sec_story.shop.buy.BuyShop
import me.syari.sec_story.shop.jump.Jump
import me.syari.sec_story.shop.jump.ShopJump
import me.syari.sec_story.shop.need.NeedList
import me.syari.sec_story.shop.need.NeedNotHasPet
import me.syari.sec_story.shop.other.CmdShop
import me.syari.sec_story.shop.other.SlotShop
import me.syari.sec_story.shop.sell.SellShop
import org.bukkit.Material
import org.bukkit.command.CommandSender

object ShopConfig {
    fun isLoadedSell(id: String) = sellMythicMobsItem.contains(id)

    private val sellMythicMobsItem = mutableListOf<String>()

    fun CommandSender.loadShop(){
        clearShop()
        configDir("Shop", false){
            output = this@loadShop

            getSection("")?.forEach { id ->
                val name = getString("$id.name", "SHOPNAME")
                val npc = getString("$id.npc", "NPCNAME", false).toColor
                val line = getInt("$id.line", 3, false)
                val jump = mutableMapOf<Int, Jump>()
                getSection("$id.jump", false)?.forEach { i ->
                    val index = i.toIntOrNull()
                    val s = getString("$id.jump.$i", "shop")
                    val t = s.split(Regex("\\s+"))
                    val item = if(t.size == 3){
                        getItem(t[1], t[2]) ?: CustomItemStack(Material.STONE)
                    } else {
                        CustomItemStack(Material.NAME_TAG)
                    }
                    if(index != null) {
                        jump[index] = ShopJump(t[0], item)
                    }
                }
                when(getString("$id.type", "buy").toLowerCase()) {
                    "buy" -> {
                        val b = BuyShop(npc, id, name, if (line in 1..6) line else 3)
                        getSection("$id.list", false)?.forEach loop@ { l ->
                            val index = l.toIntOrNull()
                            if(index != null){
                                var target: ConfigContent = ConfigItemStack(CustomItemStack(Material.STONE, "&cSHOP BUY ITEM ERROR"))
                                var loadTarget = false
                                val req = ConfigContents()
                                val need = NeedList()
                                getStringList("$id.list.$l", listOf()).forEach { s ->
                                    val t = s.split(Regex("\\s+"))
                                    when(t[0].toLowerCase()){
                                        "need" -> {
                                            val msg = need.addNeed(s, 1)
                                            if(msg != null){
                                                send(msg.replace("#name", name))
                                            }
                                        }
                                        "jump" -> {
                                            if(t.size == 4){
                                                val item = getItem(t[2], t[3])
                                                if(item != null){
                                                    jump[index] = ShopJump(t[1], item, need)
                                                    return@loop
                                                }
                                            } else {
                                                send("&cBuyShop $name - shop jump $s format error")
                                            }
                                        }
                                        else -> {
                                            when(val c = getConfigContent("Shop Buy", s)){
                                                is ConfigItemStack -> {
                                                    if(!loadTarget){
                                                        loadTarget = true
                                                        target = c
                                                    } else {
                                                        c.item.toItemStack.forEach { i ->
                                                            req.addContent(ConfigItemStack(CustomItemStack(i)))
                                                        }
                                                    }
                                                }
                                                is ConfigContentError -> {
                                                    val msg = c.msg.replace("#id", id).replace("#name", name)
                                                    send(msg)
                                                    if(!loadTarget) {
                                                        loadTarget = true
                                                        need.setDisable()
                                                    }
                                                }
                                                else -> {
                                                    if(!loadTarget){
                                                        loadTarget = true
                                                        target = c
                                                        if(c is ConfigMyPet){
                                                            need.addNeed(NeedNotHasPet())
                                                        }
                                                    } else {
                                                        req.addContent(c)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                b.setItem(index, BuyItem(target, req, need))
                            } else {
                                send("&cShop Buy $id - $l index null")
                            }
                        }
                        b.jump = jump
                        addShop(b)
                    }
                    "slot" -> {
                        val req = getSlotContent(getString("$id.req", ""))
                        val need = NeedList()
                        getStringList("$id.need", listOf(), false).forEach { f ->
                            val msg = need.addNeed(f, 0)
                            if(msg != null){
                                send(msg.replace("#name", name))
                            }
                        }
                        val slot = SlotShop(npc, id, name, req?.first ?: ConfigMoneyJPY(0), need)
                        getStringList("$id.list", listOf()).forEach { s ->
                            val item = getSlotContent(s)
                            if(item != null){
                                slot.addItem(item.first, item.second)
                            } else {
                                send("&cShop Slot $id - $s item null")
                            }
                        }
                        addShop(slot)
                    }
                    "cmd" -> {
                        val cmd = getString("$id.cmd", "")
                        val console = getBoolean("$id.console", def = false)
                        val need = NeedList()
                        getStringList("$id.need", listOf(), false).forEach { f ->
                            val msg = need.addNeed(f, 0)
                            if(msg != null){
                                send(msg.replace("#name", name))
                            }
                        }
                        val shop = CmdShop(npc, id, name, cmd, console, need)
                        addShop(shop)
                    }
                }
            }
        }

        sellMythicMobsItem.clear()
        config("sell.yml", false){
            output = this@loadShop

            val id = getString("id", "Sell")
            val name = getString("name", "SHOPNAME")
            val npc = getString("npc", "NPCNAME", false).toColor
            val line = getInt("line", 3, false)
            val display = getInt("display", 0)
            val sell = SellShop(npc, id, name, line, display)
            getSection("list")?.forEach { sec ->
                getStringList("list.$sec", listOf()).forEach { s ->
                    val t = s.split(Regex("\\s+"))
                    if(t.size == 3){
                        val item = getItem(t[0], t[1])
                        if(item != null){
                            if(t[0] == "mm") sellMythicMobsItem.add(t[1])
                            val price = t[2].toIntOrNull()
                            if(price != null){
                                sell.addSellItem(item, price)
                            } else {
                                send("&cShop Sell - ${t[1]} Price null")
                            }
                        } else {
                            send("&cShop Sell - ${t[1]} Item null")
                        }
                    } else {
                        send("&cShop Sell - $s format error")
                    }
                }
            }
            addShop(sell)
        }
    }
}