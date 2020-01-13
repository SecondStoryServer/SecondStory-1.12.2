package me.syari.sec_story.paper.core.shop.jump

import me.syari.sec_story.paper.core.shop.Shop
import me.syari.sec_story.paper.core.shop.Shops.getShop
import me.syari.sec_story.paper.core.shop.need.NeedList
import me.syari.sec_story.paper.core.shop.other.CmdShop
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.entity.Player

class ShopJump(private val shopId: String, private val displayItem: CustomItemStack, val need: NeedList): Jump {
    private var shop: Shop? = null

    override fun run(p: Player) {
        if(need.getDisplay(p).isNotEmpty()) return
        if(shop == null) shop = getShop(shopId)
        shop?.open(p)
    }

    override fun getDisplay(p: Player): CustomItemStack {
        if(shop == null) shop = getShop(shopId)
        displayItem.display = "&a${shop?.name}"
        displayItem.lore = need.getDisplay(p)
        if(shop !is CmdShop) {
            if(displayItem.lore.isNotEmpty()) displayItem.addLore("")
            displayItem.addLore("&6クリックで開く")
        }
        return displayItem
    }

    constructor(shopId: String, displayItem: CustomItemStack): this(shopId, displayItem, NeedList())
}