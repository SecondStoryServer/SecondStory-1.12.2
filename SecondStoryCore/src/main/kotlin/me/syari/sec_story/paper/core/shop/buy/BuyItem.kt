package me.syari.sec_story.paper.core.shop.buy

import me.syari.sec_story.paper.core.shop.need.NeedList
import me.syari.sec_story.paper.library.config.content.ConfigContentAdd
import me.syari.sec_story.paper.library.config.content.ConfigContents
import me.syari.sec_story.paper.library.message.SendMessage.send
import org.bukkit.entity.Player

data class BuyItem(val item: ConfigContentAdd, val req: ConfigContents, val need: NeedList) {

    fun canBuy(p: Player) = req.hasContents(p)

    fun buy(p: Player) {
        if(p.inventory.firstEmpty() in 0 until 36) {
            req.removeContentsFromPlayer(p)
            item.add(p)
            p.send("&b[Shop] &f購入しました")
        } else {
            p.send("&b[Shop] &cインベントリがいっぱいです")
        }
    }
}