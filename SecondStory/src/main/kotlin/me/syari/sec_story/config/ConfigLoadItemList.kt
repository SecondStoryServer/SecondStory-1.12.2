package me.syari.sec_story.config

import me.syari.sec_story.lib.CustomItemStack

class ConfigLoadItemList {
    private val list = mutableListOf<ConfigLoadItem>()

    fun add(item: CustomItemStack, raw: String){
        list.add(ConfigLoadItem(item, raw))
    }

    fun getItemList() = list.map { it.value }
}