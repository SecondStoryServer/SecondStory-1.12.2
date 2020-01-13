package me.syari.sec_story.plugin

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

object Plugin {
    lateinit var plugin: JavaPlugin

    fun info(msg: String){
        plugin.logger.info(msg)
    }

    fun warn(msg: String){
        plugin.logger.warning(msg)
    }

    fun error(msg: String){
        plugin.logger.severe(msg)
    }

    fun console(cmd: String){
        Bukkit.dispatchCommand(plugin.server.consoleSender, cmd)
    }

    fun CommandSender.cmd(cmd: String){
        Bukkit.dispatchCommand(this, cmd)
    }
}