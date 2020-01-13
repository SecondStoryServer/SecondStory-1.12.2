package me.syari.sec_story.item.code

import me.syari.sec_story.item.code.ItemCode.codes
import me.syari.sec_story.lib.config.CreateConfig.getConfigDir
import org.bukkit.command.CommandSender

object ItemCodeConfig {
    fun CommandSender.loadItemCode(){
        val newCodes = mutableSetOf<ItemCodeData>()
        getConfigDir("ItemCode", false).forEach { (yml, cfg) ->
            val code = yml.substringBefore(".yml")
            cfg.with {
                output = this@loadItemCode

                val items = getConfigContentsFromList("ItemCode", "list")
                val limit = getDate("limit", false)
                if (items.isNotEmpty()) {
                    newCodes.add(ItemCodeData(code, items, limit, cfg))
                } else {
                    send("&cItemCode $file_name - Items is Empty")
                }
            }
        }
        codes = newCodes
    }
}