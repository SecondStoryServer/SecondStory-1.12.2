package me.syari.sec_story.shop.buy

import me.syari.sec_story.config.content.ConfigContent
import me.syari.sec_story.config.content.ConfigContents
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.shop.need.NeedList
import org.bukkit.entity.Player

data class BuyItem(val item: ConfigContent, val req: ConfigContents, val need: NeedList){

    fun canBuy(p: Player) = req.hasContents(p)

    fun buy(p: Player){
        if(p.inventory.firstEmpty() in 0 until 36){
            req.removeContentsFromPlayer(p)
            item.add(p)
            p.send("&b[Shop] &f購入しました")
        } else {
            p.send("&b[Shop] &cインベントリがいっぱいです")
        }
    }
}