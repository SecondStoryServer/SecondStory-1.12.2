package me.syari.sec_story.paper.library

import me.syari.sec_story.paper.library.config.ConfigContentRegister
import me.syari.sec_story.paper.library.date.Date
import me.syari.sec_story.paper.library.display.CreateBossBar
import me.syari.sec_story.paper.library.event.CustomEventListener
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.inv.CreateInventory
import me.syari.sec_story.paper.library.inv.CreateInventory.isOpenCustomInv
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.java.JavaPlugin

class Main: JavaPlugin() {
    companion object {
        lateinit var plugin: JavaPlugin
    }

    override fun onEnable() {
        plugin = this
        ConfigurationSerialization.registerClass(CustomItemStack::class.java)
        ConfigContentRegister.register()
        EventInit.register(this, CreateBossBar, CustomEventListener, CreateInventory)
        Date.onEnable()
    }

    override fun onDisable() {
        CreateBossBar.onDisable()
        server.onlinePlayers.forEach { p ->
            if(p.isOpenCustomInv) p.closeInventory()
        }
        Date.onDisable()
    }
}