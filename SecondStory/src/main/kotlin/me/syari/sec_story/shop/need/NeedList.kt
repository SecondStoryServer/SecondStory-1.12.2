package me.syari.sec_story.shop.need

import org.bukkit.entity.Player

class NeedList {
    private val need = mutableListOf<Need>()

    fun setDisable(){
        need.add(NeedEditConfig())
    }

    fun addNeed(n: Need){
        need.add(n)
    }

    fun addNeed(raw: String, slip: Int): String?{
        val s = raw.split("\\s+".toRegex())
        if(slip < 0) throw Exception("addNeed must be unsigned int")
        when(s.getOrNull(0 + slip)?.toLowerCase()){
            "guildlevel" -> {
                val rawLv = s.getOrNull(1 + slip) ?: return "&cShop #name - need GuildLevel level is not found"
                val lv = rawLv.toIntOrNull() ?: return "&cShop #name - need GuildLevel level is not Int"
                addNeed(NeedGuildLevel(lv))
            }
            "op" -> {
                addNeed(NeedOP())
            }
            "perm" -> {
                val perm = s.getOrNull(1 + slip) ?: return "&cShop #name - need Permission perm is not found"
                addNeed(NeedPerm(perm))
            }
            "donate" -> {
                val rawValue = s.getOrNull(1 + slip) ?: return "&cShop #name - need Donate value is not found"
                val value = rawValue.toIntOrNull() ?: return "&cShop #name - need Donate value is not Int"
                addNeed(NeedDonate(value))
            }
            else -> {
                return "&cShop #name - need $s format error"
            }
        }
        return null
    }

    fun getDisplay(p: Player): List<String>{
        val list = mutableListOf<String>()
        need.forEach { f ->
            if(!f.check(p)) list.add(f.reqMessage)
        }
        return list
    }
}