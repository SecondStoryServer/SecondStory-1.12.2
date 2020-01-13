package me.syari.sec_story.paper.library.config

import me.syari.sec_story.paper.library.config.content.*
import me.syari.sec_story.paper.library.hook.Minecraft

object ConfigContentRegister {
    fun register() {
        ConfigItemStack.register("mc" to { id -> Minecraft.getItemFromMineCraft(id) })

        ConfigContent.register("exp" to { raw, split ->
            if(split.size == 2) {
                val p = split[1].toIntOrNull()
                if(p != null) {
                    ConfigExp(p)
                } else {
                    ConfigContentError("exp value is not Int")
                }
            } else {
                ConfigContentError("$raw format error")
            }
        },

            "item" to { raw, split ->
                if(split.size in 3..4) {
                    val item = ConfigItemStack.getItem(split[1], split[2], split.getOrNull(3))
                    if(item != null) {
                        ConfigItemStack(item)
                    } else {
                        ConfigContentError("$raw item is null")
                    }
                } else {
                    ConfigContentError("$raw item format error")
                }
            },

            "cmd" to { _, split ->
                if(split.size != 1) {
                    val cmd = split.joinToString(" ").substring(split[0].length + 1)
                    ConfigRunCmd(cmd)
                } else {
                    ConfigContentError("cmd is null")
                }
            })
    }
}