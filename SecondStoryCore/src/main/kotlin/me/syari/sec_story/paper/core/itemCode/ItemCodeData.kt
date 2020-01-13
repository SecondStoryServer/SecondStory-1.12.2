package me.syari.sec_story.paper.core.itemCode

import me.syari.sec_story.paper.core.itemCode.ItemCode.codes
import me.syari.sec_story.paper.library.config.CustomConfig
import me.syari.sec_story.paper.library.config.content.ConfigContents
import org.bukkit.entity.Player
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

data class ItemCodeData(
    val code: String, val contents: ConfigContents, val limit: Date?, private val cfg: CustomConfig
) {
    fun delete() {
        cfg.delete()
        codes.remove(this)
    }

    fun checkLimit(today: LocalDate) {
        val diff = limit?.compareTo(Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant())) ?: return
        if(diff < 0) {
            cfg.delete()
        }
    }

    fun getItemCode(p: Player, justGet: Boolean): ConfigContents? {
        return if(justGet) {
            contents
        } else {
            val r = received.toMutableList()
            if(r.contains(p.uniqueId.toString())) return null
            r.add(p.uniqueId.toString())
            received = r
            contents
        }
    }

    var received: List<String>
        get() = cfg.getStringList("received", mutableListOf(), false)
        set(value) {
            cfg.set("received", value)
        }
}