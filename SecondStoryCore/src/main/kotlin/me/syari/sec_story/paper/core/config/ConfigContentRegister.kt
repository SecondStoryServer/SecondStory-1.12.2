package me.syari.sec_story.paper.core.config

import me.syari.sec_story.paper.core.config.content.*
import me.syari.sec_story.paper.core.hook.CrackShot
import me.syari.sec_story.paper.core.hook.CrackShotPlus
import me.syari.sec_story.paper.core.hook.Magic
import me.syari.sec_story.paper.core.hook.MythicMobs
import me.syari.sec_story.paper.core.tour.Tour
import me.syari.sec_story.paper.library.config.content.ConfigContent
import me.syari.sec_story.paper.library.config.content.ConfigContentError
import me.syari.sec_story.paper.library.config.content.ConfigItemStack

object ConfigContentRegister {
    fun register() {
        ConfigItemStack.register(
            "mm" to { id -> MythicMobs.getItemFromMythicMobs(id) },
            "cs" to { id -> CrackShot.getItemFromCrackShot(id) },
            "csp" to { id -> CrackShotPlus.getItemFromCrackShotPlus(id) },
            "mg-wand" to { id -> Magic.getMagicWand(id) },
            "mg-spell" to { id -> Magic.getMagicSpell(id) },
            "ss-ticket" to { id -> Tour.getTicket(id) })

        ConfigContent.register("item" to { raw, split ->
            if(split.size in 3..4) {
                val item = ConfigItemStack.getItem(split[1], split[2], split.getOrNull(3))
                if(item != null) {
                    ConfigItemStackOverride(item)
                } else {
                    ConfigContentError("$raw item is null")
                }
            } else {
                ConfigContentError("$raw item format error")
            }
        },

            "money" to { raw, split ->
                when(split.size) {
                    2 -> {
                        val p = split[1].toLongOrNull()
                        if(p != null) {
                            ConfigMoneyJPY(p)
                        } else {
                            ConfigContentError("${split[1]} price null")
                        }
                    }
                    3 -> {
                        val p = split[1].toLongOrNull()
                        if(p != null) {
                            when(split[2].toLowerCase()) {
                                "jpy" -> {
                                    ConfigMoneyJPY(p)
                                }
                                "eme" -> {
                                    ConfigMoneyEme(p.toInt())
                                }
                                else -> {
                                    ConfigContentError("${split[2]} money type not found")
                                }
                            }
                        } else {
                            ConfigContentError("${split[1]} price null")
                        }
                    }
                    else -> ConfigContentError("item $raw format error")
                }
            },

            "gp" to { raw, split ->
                if(split.size == 2) {
                    val p = split[1].toIntOrNull()
                    if(p != null) {
                        ConfigGuildPoint(p)
                    } else {
                        ConfigContentError("gp value is not Int")
                    }
                } else {
                    ConfigContentError("gp $raw format error")
                }
            },

            "sp" to { raw, split ->
                if(split.size == 2) {
                    val p = split[1].toIntOrNull()
                    if(p != null) {
                        ConfigMagicSP(p)
                    } else {
                        ConfigContentError("sp value is not Int")
                    }
                } else {
                    ConfigContentError("sp $raw format error")
                }
            },

            "pet" to { raw, split ->
                if(split.size == 2) {
                    ConfigMyPet.fromTypeName(split[1]) ?: ConfigContentError("pet type is not found")
                } else {
                    ConfigContentError("pet $raw format error")
                }
            })
    }
}