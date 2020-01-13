package me.syari.sec_story.paper.core.itemCode

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.itemCode.ItemCode.codes
import me.syari.sec_story.paper.library.config.CreateConfig.getConfigDir
import org.bukkit.command.CommandSender

object ItemCodeConfig {
    fun CommandSender.loadItemCode() {
        val newCodes = mutableSetOf<ItemCodeData>()
        getConfigDir(plugin, "ItemCode", false).forEach { (yml, cfg) ->
            val code = yml.substringBefore(".yml")
            cfg.with {
                output = this@loadItemCode

                val items = getConfigContentsFromList("list")
                val limit = getDate("limit", false)
                if(items.isNotEmpty()) {
                    newCodes.add(
                        ItemCodeData(
                            code, items, limit, cfg
                        )
                    )
                } else {
                    send("&cItemCode $file_name - Items is Empty")
                }
            }
        }
        codes = newCodes
    }
}