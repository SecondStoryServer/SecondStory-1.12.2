package me.syari.sec_story.paper.core.trade

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.item.GiveItem.give
import me.syari.sec_story.paper.core.player.Money.hasMoney
import me.syari.sec_story.paper.core.player.Money.money
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.onlinePlayers
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.inv.CreateInventory.inventory
import me.syari.sec_story.paper.library.item.CustomItemStack
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import me.syari.sec_story.paper.library.server.Server.getPlayer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

object Trade: FunctionInit {
    override fun init() {
        createCmd("trade", tab { onlinePlayers }) { sender, args ->
            if(sender is Player) {
                val rawPartner = args.getOrNull(0) ?: return@createCmd sender.send("&b[Trade] &cトレード相手を入力してください")
                val partner = getPlayer(rawPartner) ?: return@createCmd sender.send("&b[Trade] &cプレイヤーが見つかりませんでした")
                sender.sendTrade(partner)
            }
        }
    }

    private val nowTrades = mutableListOf<UUID>()

    private val Player.nowTrade get() = nowTrades.contains(uniqueId)

    private val inviteTrade = mutableListOf<Pair<UUID, UUID>>()

    private fun Player.sendTrade(partner: Player) {
        if(this == partner) return send("&b[Trade] &c自分にトレード申請を送ることはできません")
        if(nowTrade) return send("&b[Trade] &cあなたはトレード中です")
        if(partner.nowTrade) return send("&b[Trade] &cそのプレイヤーは他のプレイヤーとトレード中です")
        if(inviteTrade.contains(uniqueId to partner.uniqueId)) return send("&b[Trade] &c既にトレード申請しています")
        if(inviteTrade.contains(partner.uniqueId to uniqueId)) {
            inviteTrade.remove(partner.uniqueId to uniqueId)
            val e = TradeStartEvent(this, partner)
            e.callEvent()
            if(!e.isCancelled) {
                openTrade(partner)
                partner.openTrade(this)
            } else {
                send("&b[Trade] &c${partner.displayName}とのトレードはキャンセルされました")
                partner.send("&b[Trade] &c${displayName}とのトレードはキャンセルされました")
            }
        } else {
            val e = TradeInviteEvent(this, partner)
            e.callEvent()
            if(!e.isCancelled) {
                inviteTrade.add(uniqueId to partner.uniqueId)
                send("&b[Trade] &a${partner.displayName}&fにトレード申請を送りました")
                partner.send("&b[Trade] &a${displayName}&fからトレード申請がきました &a/trade $name")
                runLater(plugin, 60 * 20) {
                    if(inviteTrade.contains(uniqueId to partner.uniqueId)) {
                        inviteTrade.remove(uniqueId to partner.uniqueId)
                        send("&b[Trade] &a${partner.displayName}&fへのトレード申請がキャンセルされました")
                        partner.send("&b[Trade] &a${displayName}&fからトレード申請がキャンセルされました")
                    }
                }
            } else {
                send("&b[Trade] &cトレードの招待を送れませんでした")
            }
        }
    }

    private val Player.isReady get() = openInventory.topInventory.getItem(21)?.durability == 10.toShort()

    private fun Inventory.isReady(slot: Int) = getItem(slot)?.durability == 10.toShort()

    private fun Player.openTrade(partner: Player) {
        var isReady = false
        var jpy = 0L

        fun updateJPY(add: Long) {
            if(isReady) return
            val af = jpy + add
            if(hasMoney(af)) {
                jpy = if(af < 0) 0 else af
                val item = CustomItemStack(
                    Material.GOLD_INGOT,
                    "&6${String.format("%,d", jpy)}JPY",
                    "&7左クリック: &a+10万JPY",
                    "&7シフト左クリック: &a+100万JPY",
                    "&7右クリック: &a-10万JPY",
                    "&7シフト右クリック: &a-100万JPY"
                ).toOneItemStack
                openInventory.topInventory.setItem(12, item)
                partner.openInventory.topInventory.setItem(14, item)
            }
        }

        nowTrades.add(uniqueId)
        inventory("&9&lトレード") {
            id = "trade"
            cancel = false
            onClick = { e -> onClickTrade(e, partner) }
            onClose = { e -> onCloseTrade(e, partner) }
            for(i in listOf(3, 5, 6, 7, 8, 14, 15, 16, 17, 24, 25, 26)) {
                item(i, Material.STAINED_GLASS_PANE, "", damage = 8)
            }
            item(
                12,
                Material.GOLD_INGOT,
                "&6${String.format("%,d", jpy)}JPY",
                "&7左クリック: &a+10万JPY",
                "&7シフト左クリック: &a+100万JPY",
                "&7右クリック: &a-10万JPY",
                "&7シフト右クリック: &a-100万JPY"
            ).event(ClickType.LEFT) {
                updateJPY(100000)
            }.event(ClickType.SHIFT_LEFT) {
                updateJPY(1000000)
            }.event(ClickType.RIGHT) {
                updateJPY(- 100000)
            }.event(ClickType.SHIFT_RIGHT) {
                updateJPY(- 1000000)
            }
            for(i in listOf(4, 13, 22)) {
                item(i, Material.STAINED_GLASS_PANE, "", damage = 15)
            }
            item(
                14,
                Material.GOLD_INGOT,
                "&6${String.format("%,d", jpy)}JPY",
                "&7左クリック: &a+10万JPY",
                "&7シフト左クリック: &a+100万JPY",
                "&7右クリック: &a-10万JPY",
                "&7シフト右クリック: &a-100万JPY"
            )
            item(21, Material.INK_SACK, "&a準備完了", damage = 8).event(ClickType.LEFT) {
                isReady = ! isReady
                val item = CustomItemStack(
                    Material.INK_SACK, "&a準備完了", durability = if(isReady) 10 else 8
                ).toOneItemStack
                openInventory.topInventory.setItem(21, item)
                partner.openInventory.topInventory.setItem(23, item)
                if(isReady && partner.isReady) {
                    closeInventory()
                    partner.closeInventory()
                }
            }
            item(23, Material.INK_SACK, "&a準備完了", damage = 8)
        }.open(this)
    }

    private fun onClickTrade(e: InventoryClickEvent, partner: Player) {
        val p = e.whoClicked as Player
        if(p.isReady) {
            e.isCancelled = true
            return
        }
        val allow = listOf(0, 1, 2, 9, 10, 11, 18, 19, 20)
        if(e.clickedInventory != e.inventory || e.slot in allow) {
            runLater(plugin, 1) {
                val inv = p.openInventory.topInventory
                for(i in allow) {
                    val item = inv.getItem(i) ?: ItemStack(Material.STAINED_GLASS_PANE, 1, 8)
                    partner.openInventory.topInventory.setItem(i + 6, item)
                }
            }
        } else {
            e.isCancelled = true
        }
    }

    private fun onCloseTrade(e: InventoryCloseEvent, partner: Player) {
        val p = e.player as Player
        val inv = e.inventory
        if(inv.isReady(21) && inv.isReady(23)) {
            for(i in listOf(0, 1, 2, 9, 10, 11, 18, 19, 20)) {
                val cItem = CustomItemStack(inv.getItem(i))
                if(! cItem.isAir) {
                    partner.give(cItem, "&6トレードアイテム", 3)
                }
            }
            val rawMoney = CustomItemStack(inv.getItem(12)).display
            if(rawMoney != null) {
                val money = rawMoney.substring(2..(rawMoney.length - 4)).replace(",", "").toLongOrNull()
                if(money != null && p.hasMoney(money)) {
                    p.money -= money
                    partner.money += money
                }
            }
        } else {
            for(i in listOf(0, 1, 2, 9, 10, 11, 18, 19, 20)) {
                val cItem = CustomItemStack(inv.getItem(i))
                if(! cItem.isAir) {
                    p.give(cItem, "&6トレード返却分", 3)
                }
            }
            runLater(plugin, 3) {
                if(partner.nowTrade) {
                    partner.closeInventory()
                }
            }
        }
        nowTrades.remove(p.uniqueId)
    }
}