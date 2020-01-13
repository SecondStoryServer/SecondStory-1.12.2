package me.syari.sec_story.paper.core.rank.req

import org.bukkit.entity.Player

data class ItemReq(val display: String, val amount: Int, val use: Boolean): RankReq {
    override fun check(p: Player): Boolean {
        var count = 0
        p.inventory.forEach { f ->
            if(f != null && f.hasItemMeta() && f.itemMeta.hasDisplayName() && f.itemMeta.displayName == display) {
                count += f.amount
            }
            if(amount <= count) return true
        }
        return false
    }

    fun remove(p: Player) {
        var count = amount
        val inv = p.inventory.contents
        inv.forEach { f ->
            if(f != null && f.hasItemMeta() && f.itemMeta.hasDisplayName() && f.itemMeta.displayName == display) {
                val a = f.amount
                if(a < count) {
                    count -= a
                    f.amount = 0
                } else {
                    f.amount = a - count
                    p.inventory.contents = inv
                    return
                }
            }
        }
    }
}