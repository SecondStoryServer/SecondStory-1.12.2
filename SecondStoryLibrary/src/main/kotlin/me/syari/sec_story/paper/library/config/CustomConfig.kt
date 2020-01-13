package me.syari.sec_story.paper.library.config

import me.syari.sec_story.paper.library.config.content.ConfigContent
import me.syari.sec_story.paper.library.config.content.ConfigContentError
import me.syari.sec_story.paper.library.config.content.ConfigContents
import me.syari.sec_story.paper.library.config.content.ConfigItemStack
import me.syari.sec_story.paper.library.item.CustomItemStack
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.message.SendMessage.sendConsole
import me.syari.sec_story.paper.library.server.Server.getWorldSafe
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.util.*

class CustomConfig(val plugin: JavaPlugin, val file_name: String, dir: File, deleteIfEmpty: Boolean) {
    var output: CommandSender = plugin.server.consoleSender

    private var file = File(dir, file_name)
    private val config: YamlConfiguration
    private val path: String
    private val unused: MutableSet<String>

    init {
        config = YamlConfiguration.loadConfiguration(file)
        path = file.path.substringAfter(plugin.dataFolder.path).substring(1)
        if(! file.exists()) {
            try {
                file.createNewFile()
                sendConsole("[${plugin.name} | CustomConfig] &f$path の作成に成功しました")
            } catch(ex: IOException) {
                sendConsole("[${plugin.name} | CustomConfig] &c$path の作成に失敗しました")
            }
        } else if(file.length() == 0L && deleteIfEmpty) {
            sendConsole("[${plugin.name} | CustomConfig] &e$path は中身が存在しないので削除されます")
            delete()
        }
        unused = config.getConfigurationSection("")?.getKeys(true) ?: mutableSetOf()
    }

    inline fun with(run: CustomConfig.() -> Unit) {
        run.invoke(this)
    }

    fun checkUnused() {
        unused.forEach {
            unusedError(it)
        }
    }

    fun get(path: String): Any? {
        unused.remove(path)
        return config.get(path)
    }

    fun getInt(path: String, sendNotFound: Boolean = true): Int? {
        return if(config.contains(path)) {
            val g = get(path)
            if(g is Int) {
                g
            } else {
                typeMismatchError<Int>(path, "Int")
            }
        } else {
            notFoundError(path, sendNotFound)
        }
    }

    fun getInt(path: String, def: Int, sendNotFound: Boolean = true): Int {
        return getInt(path, sendNotFound) ?: def
    }

    fun getDouble(path: String, sendNotFound: Boolean = true): Double? {
        return if(config.contains(path)) {
            when(val g = get(path)) {
                is Double -> g
                is Int -> g.toDouble()
                is Long -> g.toDouble()
                else -> typeMismatchError<Double>(path, "Double")
            }
        } else {
            notFoundError(path, sendNotFound)
        }
    }

    fun getDouble(path: String, def: Double, sendNotFound: Boolean = true): Double {
        return getDouble(path, sendNotFound) ?: def
    }

    private fun getLong(path: String, sendNotFound: Boolean = true): Long? {
        return if(config.contains(path)) {
            when(val g = get(path)) {
                is Int -> g.toLong()
                is Long -> g
                else -> typeMismatchError<Long>(path, "Long")
            }
        } else {
            notFoundError(path, sendNotFound)
        }
    }

    fun getLong(path: String, def: Long, sendNotFound: Boolean = true): Long {
        return getLong(path, sendNotFound) ?: def
    }

    fun getString(path: String, sendNotFound: Boolean = true): String? {
        return if(config.contains(path)) {
            val g = get(path)
            if(g is String) {
                g
            } else {
                typeMismatchError<String>(path, "String")
            }
        } else {
            notFoundError(path, sendNotFound)
        }
    }

    fun getString(path: String, def: String, sendNotFound: Boolean = true): String {
        return getString(path, sendNotFound) ?: def
    }

    fun getStringList(path: String, sendNotFound: Boolean = true): List<String>? {
        return if(config.contains(path)) {
            val g = get(path)
            if(g is List<*>) {
                val list = mutableListOf<String>()
                g.forEachIndexed { i, f ->
                    if(f is String) {
                        list.add(f)
                    } else {
                        typeMismatchInListError(path, i, "String")
                    }
                }
                list
            } else {
                typeMismatchError<List<String>>(path, "List")
            }
        } else {
            notFoundError(path, sendNotFound)
        }
    }

    fun getStringList(path: String, def: Collection<String>, sendNotFound: Boolean = true): MutableList<String> {
        return (getStringList(path, sendNotFound) ?: def).toMutableList()
    }

    private fun getLocation(g: String): Location? {
        val s = g.split(",\\s*".toRegex())
        when(s.size) {
            4, 6 -> {
                val w = getWorldSafe(s[0]) ?: return nullError<Location>(path, "World(${s[0]})")
                val x = s[1].toDoubleOrNull() ?: return typeMismatchError<Location>(path, "Double")
                val y = s[2].toDoubleOrNull() ?: return typeMismatchError<Location>(path, "Double")
                val z = s[3].toDoubleOrNull() ?: return typeMismatchError<Location>(path, "Double")
                if(s.size == 4) return Location(w, x, y, z)
                val yaw = s[4].toFloatOrNull() ?: return typeMismatchError<Location>(path, "Float")
                val pitch = s[5].toFloatOrNull() ?: return typeMismatchError<Location>(path, "Float")
                return Location(w, x, y, z, yaw, pitch)
            }
        }
        return formatMismatchError(path)
    }

    fun getLocation(path: String, sendNotFound: Boolean = true): Location? {
        return if(config.contains(path)) {
            val g = get(path)
            if(g is String) {
                getLocation(g) ?: nullError<Location>(path, "Location")
            } else {
                typeMismatchError<Location>(path, "String(Location)")
            }
        } else {
            notFoundError(path, sendNotFound)
        }
    }

    fun getLocationList(path: String, sendNotFound: Boolean = true): List<Location>? {
        return if(config.contains(path)) {
            val g = get(path)
            if(g is List<*>) {
                val list = mutableListOf<Location>()
                g.forEachIndexed { i, f ->
                    if(f is String) {
                        val loc = getLocation(f)
                        if(loc != null) {
                            list.add(loc)
                        } else {
                            nullError(path, "Location")
                        }
                    } else {
                        typeMismatchInListError(path, i, "String(Location)")
                    }
                }
                list
            } else {
                typeMismatchError<List<Location>>(path, "List")
            }
        } else {
            notFoundError(path, sendNotFound)
        }
    }

    fun getLocationList(path: String, def: List<Location>, sendNotFound: Boolean = true): List<Location> {
        return getLocationList(path, sendNotFound) ?: def
    }

    /*
    fun getLocation(path: String, def: Location, sendNotFound: Boolean = true): Location {
        return getLocation(path, sendNotFound) ?: def
    }
    */

    private fun getBoolean(path: String, sendNotFound: Boolean = true): Boolean? {
        return if(config.contains(path)) {
            val g = get(path)
            if(g is Boolean) {
                g
            } else {
                typeMismatchError<Boolean>(path, "Boolean")
            }
        } else {
            notFoundError(path, sendNotFound)
        }
    }

    fun getBoolean(path: String, def: Boolean, sendNotFound: Boolean = true): Boolean {
        return getBoolean(path, sendNotFound) ?: def
    }

    private fun getCustomItemStackListFromStringList(
        path: String, sendNotFound: Boolean = true, slip: Int = 0
    ): ConfigLoadItemList? {
        return if(config.contains(path)) {
            val g = get(path)
            if(g is List<*>) {
                val list = ConfigLoadItemList()
                g.forEachIndexed { i, f ->
                    if(f is String) {
                        val s = f.split("\\s+".toRegex())
                        when(s.size) {
                            2 + slip, 3 + slip -> {
                                val item = ConfigItemStack.getItem(s[0 + slip], s[1 + slip], s.getOrNull(2 + slip))
                                if(item != null) {
                                    list.add(item, f)
                                } else {
                                    nullError<CustomItemStack>(path, "CustomItemStack(${s[0 + slip]} ${s[1 + slip]})")
                                }
                            }
                            else -> formatMismatchError(path)
                        }
                    } else {
                        typeMismatchInListError(path, i, "String(CustomItemStack)")
                    }
                }
                list
            } else {
                typeMismatchError<ConfigLoadItemList>(path, "List")
            }
        } else {
            notFoundError(path, sendNotFound)
        }
    }

    fun getCustomItemStackListFromStringList(
        path: String, def: List<CustomItemStack>, sendNotFound: Boolean = true, slip: Int = 0
    ): MutableList<CustomItemStack> {
        return (getCustomItemStackListFromStringList(path, sendNotFound, slip)?.getItemList() ?: def).toMutableList()
    }

    fun getCustomItemStackFromString(path: String, sendNotFound: Boolean = true, slip: Int = 0): CustomItemStack? {
        return if(config.contains(path)) {
            val g = get(path)
            if(g is String) {
                val s = g.split("\\s+".toRegex())
                when(s.size) {
                    2 + slip, 3 + slip -> ConfigItemStack.getItem(
                        s[0 + slip],
                        s[1 + slip],
                        s.getOrNull(2 + slip)
                    ) ?: nullError<CustomItemStack>(path, "CustomItemStack(${s[0 + slip]} ${s[1 + slip]})")
                    else -> formatMismatchError(path)
                }
            } else {
                typeMismatchError<CustomItemStack>(path, "String(CustomItemStack)")
            }
        } else {
            notFoundError(path, sendNotFound)
        }
    }

    fun getCustomItemStackFromString(
        path: String, def: CustomItemStack, sendNotFound: Boolean = true, slip: Int = 0
    ): CustomItemStack {
        return getCustomItemStackFromString(path, sendNotFound, slip) ?: def
    }

    /*
    fun getCustomItemStack(path: String, sendNotFound: Boolean = true): CustomItemStack? {
        return if(me.syari.sec_story.paper.library.config.contains(path)){
            val g = get(path)
            if(g is CustomItemStack){
                g
            } else {
                typeMismatchError<CustomItemStack>(path, "CustomItemStack")
            }
        } else {
            notFoundError(path, sendNotFound)
        }
    }

    fun getCustomItemStack(path: String, def: CustomItemStack, sendNotFound: Boolean = true): CustomItemStack {
        return getCustomItemStack(path, sendNotFound) ?: def
    }
    */

    private fun getCustomItemStackList(path: String, sendNotFound: Boolean = true): List<CustomItemStack>? {
        return if(config.contains(path)) {
            val g = get(path)
            if(g is List<*>) {
                val list = mutableListOf<CustomItemStack>()
                g.forEachIndexed { i, f ->
                    if(f is CustomItemStack) {
                        list.add(f)
                    } else {
                        typeMismatchInListError(path, i, "CustomItemStack")
                    }
                }
                list
            } else {
                typeMismatchError<List<CustomItemStack>>(path, "List")
            }
        } else {
            notFoundError(path, sendNotFound)
        }
    }

    fun getCustomItemStackList(
        path: String, def: Collection<CustomItemStack>, sendNotFound: Boolean = true
    ): MutableList<CustomItemStack> {
        return (getCustomItemStackList(path, sendNotFound) ?: def).toMutableList()
    }

    fun getItemStack(path: String, sendNotFound: Boolean = true): ItemStack? {
        return if(config.contains(path)) {
            val g = get(path)
            if(g is ItemStack) {
                g
            } else {
                typeMismatchError<ItemStack>(path, "ItemStack")
            }
        } else {
            notFoundError(path, sendNotFound)
        }
    }

    fun getDate(path: String, sendNotFound: Boolean = true): Date? {
        return if(config.contains(path)) {
            val g = get(path)
            if(g is Date) {
                g
            } else {
                typeMismatchError<Date>(path, "Date")
            }
        } else {
            notFoundError(path, sendNotFound)
        }
    }

    private fun getColor(path: String, sendNotFound: Boolean = true): Color? {
        return if(config.contains(path)) {
            val g = get(path)
            if(g is Color) {
                g
            } else {
                typeMismatchError<Color>(path, "Color")
            }
        } else {
            notFoundError(path, sendNotFound)
        }
    }

    fun getColor(path: String, def: Color, sendNotFound: Boolean = true): Color {
        return getColor(path, sendNotFound) ?: def
    }

    fun contains(path: String) = config.contains(path)

    fun getSection(
        path: String, sendNotFound: Boolean = true
    ) = config.getConfigurationSection(path)?.getKeys(false) ?: notFoundError<Set<String>>(path, sendNotFound)

    fun set(path: String, value: Any?, save: Boolean = true) {
        config.set(path, value)
        if(save) save()
    }

    fun save() {
        config.save(file)
        if(file.length() == 0L) {
            delete()
        }
    }

    fun delete() {
        file.delete()
        sendConsole("[${plugin.name} | CustomConfig] &f$path の削除に成功しました")
    }

    fun send(msg: String) {
        output.send("&b[$path] $msg")
    }

    private fun unusedError(path: String) {
        send("&d$path is not used")
    }

    private fun <T> notFoundError(path: String, sendNotFound: Boolean): T? {
        if(sendNotFound) send("&c$path is not found.")
        return null
    }

    fun unloadError(path: String, cause: String) {
        send("&c$path is unused. ($cause)")
    }

    fun nullError(path: String, thing: String) {
        send("&c$path $thing is null.")
    }

    private fun <T> nullError(path: String, thing: String): T? {
        nullError(path, thing)
        return null
    }

    private fun <T> formatMismatchError(path: String): T? {
        send("&c$path is incorrect format.")
        return null
    }

    fun typeMismatchError(path: String, type: String) {
        send("&c$path is not $type type.")
    }

    private fun <T> typeMismatchError(path: String, type: String): T? {
        typeMismatchError(path, type)
        return null
    }

    private fun typeMismatchInListError(path: String, index: Int, type: String) {
        send("&c$path:${index + 1} is not $type type.")
    }

    fun getConfigContentsFromList(path: String, sendNotFound: Boolean = true): ConfigContents {
        val list = ConfigContents()
        getStringList(path, sendNotFound)?.forEach { s ->
            val c = getConfigContent(s)
            if(c is ConfigContentError) {
                send(c.msg)
            } else {
                list.addContent(c)
            }
        }
        return list
    }

    fun getConfigContent(s: String): ConfigContent {
        return ConfigContent.getContent(s)
    }
}