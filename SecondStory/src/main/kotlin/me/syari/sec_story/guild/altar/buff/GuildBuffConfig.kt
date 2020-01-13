package me.syari.sec_story.guild.altar.buff

import me.syari.sec_story.guild.altar.buff.data.BuffData
import me.syari.sec_story.guild.altar.buff.data.BuffType
import me.syari.sec_story.lib.config.CreateConfig.config
import org.bukkit.command.CommandSender

object GuildBuffConfig {
    var buffList = mapOf<BuffType, BuffData>()
        private set

    fun CommandSender.loadGuildBuffConfig(){
        val newBuffList = mutableMapOf<BuffType, BuffData>()
        config("Guild/Altar/buff.yml"){
            BuffType.values().forEach { type ->
                val name = type.name
                val data = BuffData()
                getSection("$name.level")?.forEach nextLevel@ { rawLv ->
                    val res = data.addLevel(
                        rawLv.toIntOrNull()
                            ?: return@nextLevel nullError("$name.level.$rawLv", "Int"),
                        getInt("$name.level.$rawLv.gp")
                            ?: return@nextLevel nullError("$name.level.$rawLv.gp", "Int"),
                        getInt("$name.level.$rawLv.value")
                            ?: return@nextLevel nullError("$name.level.$rawLv.value", "Int")
                    )
                    if(res != -1){
                        unloadError("$name.level.$rawLv", "Not Level Order. Please Add $res.")
                    }
                }
                newBuffList[type] = data
            }
        }
        buffList = newBuffList
    }
}