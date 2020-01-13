package me.syari.sec_story.paper.core.shop

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.config.content.ConfigMoneyJPY
import me.syari.sec_story.paper.core.config.content.ConfigMyPet
import me.syari.sec_story.paper.core.shop.Shops.addShop
import me.syari.sec_story.paper.core.shop.Shops.clearShop
import me.syari.sec_story.paper.core.shop.buy.BuyItem
import me.syari.sec_story.paper.core.shop.buy.BuyShop
import me.syari.sec_story.paper.core.shop.jump.Jump
import me.syari.sec_story.paper.core.shop.jump.ShopJump
import me.syari.sec_story.paper.core.shop.need.NeedList
import me.syari.sec_story.paper.core.shop.need.NeedNotHasPet
import me.syari.sec_story.paper.core.shop.other.CmdShop
import me.syari.sec_story.paper.core.shop.other.SlotShop
import me.syari.sec_story.paper.core.shop.sell.SellShop
import me.syari.sec_story.paper.library.code.StringEditor.toColor
import me.syari.sec_story.paper.library.config.CreateConfig.config
import me.syari.sec_story.paper.library.config.CreateConfig.configDir
import me.syari.sec_story.paper.library.config.CustomConfig
import me.syari.sec_story.paper.library.config.content.*
import me.syari.sec_story.paper.library.config.content.ConfigItemStack.Companion.getItem
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.Material
import org.bukkit.command.CommandSender

object ShopConfig {
    fun isLoadedSell(id: String) = sellMythicMobsItem.contains(id)

    private val sellMythicMobsItem = mutableListOf<String>()

    fun CommandSender.loadShop() {
        clearShop()
        configDir(plugin, "Shop", false) {
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
                    val item = if(t.size == 3) {
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
                        val b = BuyShop(npc, id, name, if(line in 1..6) line else 3)
                        getSection("$id.list", false)?.forEach loop@{ l ->
                            val index = l.toIntOrNull()
                            if(index != null) {
                                var target: ConfigContentAdd = ConfigItemStack(
                                    CustomItemStack(Material.STONE, "&cSHOP BUY ITEM ERROR")
                                )
                                var loadTarget = false
                                val req = ConfigContents()
                                val need = NeedList()
                                getStringList("$id.list.$l", listOf()).forEach { s ->
                                    val t = s.split(Regex("\\s+"))
                                    when(t[0].toLowerCase()) {
                                        "need" -> {
                                            val msg = need.addNeed(s, 1)
                                            if(msg != null) {
                                                send(msg.replace("#name", name))
                                            }
                                        }
                                        "jump" -> {
                                            if(t.size == 4) {
                                                val item = getItem(t[2], t[3])
                                                if(item != null) {
                                                    jump[index] = ShopJump(t[1], item, need)
                                                    return@loop
                                                }
                                            } else {
                                                send("&cBuyShop $id - shop jump $s format error")
                                            }
                                        }
                                        else -> {
                                            when(val c = getConfigContent(s)) {
                                                is ConfigItemStack -> {
                                                    if(! loadTarget) {
                                                        loadTarget = true
                                                        target = c
                                                    } else {
                                                        c.item.toItemStack.forEach { i ->
                                                            req.addContent(ConfigItemStack(CustomItemStack(i)))
                                                        }
                                                    }
                                                }
                                                is ConfigContentError -> {
                                                    send("&cBuyShop $id - " + c.msg)
                                                    if(! loadTarget) {
                                                        loadTarget = true
                                                        need.setDisable()
                                                    }
                                                }
                                                else -> {
                                                    if(! loadTarget && c is ConfigContentAdd) {
                                                        loadTarget = true
                                                        target = c
                                                        if(c is ConfigMyPet) {
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
                        val req = getSlotContent(getString("$id.req", ""))?.first
                        if(req is ConfigContentRemove) {
                            val need = NeedList()
                            getStringList("$id.need", listOf(), false).forEach { f ->
                                val msg = need.addNeed(f, 0)
                                if(msg != null) {
                                    send(msg.replace("#name", name))
                                }
                            }
                            val slot = SlotShop(npc, id, name, req, need)
                            getStringList("$id.list", listOf()).forEach { s ->
                                val item = getSlotContent(s)
                                if(item != null) {
                                    val target = item.first
                                    if(target is ConfigContentAdd) {
                                        slot.addItem(target, item.second)
                                    } else {
                                        send("&cShop Slot $id - target is not add-content")
                                    }
                                } else {
                                    send("&cShop Slot $id - $s item null")
                                }
                            }
                            addShop(slot)
                        } else {
                            send("&cShop Slot")
                        }
                    }
                    "cmd" -> {
                        val cmd = getString("$id.cmd", "")
                        val console = getBoolean("$id.console", def = false)
                        val need = NeedList()
                        getStringList("$id.need", listOf(), false).forEach { f ->
                            val msg = need.addNeed(f, 0)
                            if(msg != null) {
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
        config(plugin, "sell.yml", false) {
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
                    if(t.size == 3) {
                        val item = getItem(t[0], t[1])
                        if(item != null) {
                            if(t[0] == "mm") sellMythicMobsItem.add(t[1])
                            val price = t[2].toIntOrNull()
                            if(price != null) {
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

    private fun CustomConfig.getSlotContent(s: String): Pair<ConfigContent, Int>? {
        val t = s.split(Regex("\\s+"))
        when(t[0].toLowerCase()) {
            "exp" -> {
                when(t.size) {
                    3 -> {
                        val value = t[1].toIntOrNull()
                        if(value != null) {
                            val per = t[2].toIntOrNull()
                            if(per != null) {
                                return ConfigExp(value) to per
                            } else {
                                send("&cSlot - $s Per null")
                            }
                        } else {
                            send("&cSlot - $s value null")
                        }
                    }
                    2 -> {
                        val value = t[1].toIntOrNull()
                        if(value != null) {
                            return ConfigExp(value) to 1
                        } else {
                            send("&cSlot - $s value null")
                        }
                    }
                    else -> send("&cSlot - $s exp format error")
                }
            }
            "money" -> {
                when(t.size) {
                    3 -> {
                        val price = t[1].toLongOrNull()
                        if(price != null) {
                            val per = t[2].toIntOrNull()
                            if(per != null) {
                                return ConfigMoneyJPY(price) to per
                            } else {
                                send("&cSlot - $s Per null")
                            }
                        } else {
                            send("&cSlot - $s Price null")
                        }
                    }
                    2 -> {
                        val price = t[1].toLongOrNull()
                        if(price != null) {
                            return ConfigMoneyJPY(price) to 1
                        } else {
                            send("&cSlot - $s Price null")
                        }
                    }
                    else -> send("&cSlot - $s money format error")
                }
            }
            "item" -> {
                when(t.size) {
                    5 -> {
                        val item = ConfigItemStack.getItem(t[1], t[2], t[3])
                        if(item != null) {
                            val per = t[4].toIntOrNull()
                            if(per != null) {
                                return ConfigItemStack(item) to per
                            } else {
                                send("&cSlot - $s Per null")
                            }
                        } else {
                            send("&cSlot - $s Item null")
                        }
                    }
                    4 -> {
                        val item = ConfigItemStack.getItem(t[1], t[2], t[3])
                        if(item != null) {
                            return ConfigItemStack(item) to 1
                        } else {
                            send("&cSlot - $s Item null")
                        }
                    }
                    else -> send("&cSlot - $s Item Format error")
                }
            }
            else -> {
                send("&cSlot - $s type not found")
            }
        }
        return null
    }
}