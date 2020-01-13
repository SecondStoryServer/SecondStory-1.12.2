package me.syari.sec_story.lib.config

import me.syari.sec_story.config.ConfigLoadItemList
import me.syari.sec_story.config.content.*
import me.syari.sec_story.hook.CrackShot.getItemFromCrackShot
import me.syari.sec_story.hook.CrackShotPlus.getItemFromCrackShotPlus
import me.syari.sec_story.hook.Magic.getMagicSpell
import me.syari.sec_story.hook.Magic.getMagicWand
import me.syari.sec_story.hook.Minecraft.getItemFromMineCraft
import me.syari.sec_story.hook.MythicMobs.getItemFromMythicMobs
import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.plugin.Plugin
import me.syari.sec_story.tour.Tour
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File
import java.io.IOException
import java.util.*

class CustomConfig(val file_name: String, dir: File, deleteIfEmpty: Boolean) {
    var output: CommandSender = Plugin.plugin.server.consoleSender

    private var file = File(dir, file_name)
    private val config: YamlConfiguration
    private val path: String
    private val unused: MutableSet<String>

    init {
        config = YamlConfiguration.loadConfiguration(file)
        path = file.path.substringAfter(Plugin.plugin.dataFolder.path).substring(1)
        if (!file.exists()) {
            try {
                file.createNewFile()
                Plugin.info("$path の作成に成功しました")
            } catch (ex: IOException) {
                Plugin.error("$path の作成に失敗しました")
            }
        } else if(file.length() == 0L && deleteIfEmpty){
            Plugin.info("$path は中身が存在しないので削除されます")
            delete()
        }
        unused = config.getConfigurationSection("").getKeys(true)
    }

    inline fun with(run: CustomConfig.() -> Unit){
        run.invoke(this)
    }

    fun checkUnused(){
        unused.forEach {
            unusedError(it)
        }
    }

    fun get(path: String): Any {
        unused.remove(path)
        return config.get(path)
    }

    fun getInt(path: String, sendNotFound: Boolean = true): Int? {
        return if(config.contains(path)){
            val g = get(path)
            if(g is Int){
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
        return if(config.contains(path)){
            when (val g = get(path)) {
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
        return if(config.contains(path)){
            when(val g = get(path)){
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
        return if(config.contains(path)){
            val g = get(path)
            if(g is String){
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
        return if(config.contains(path)){
            val g = get(path)
            if(g is List<*>){
                val list = mutableListOf<String>()
                g.forEachIndexed { i, f ->
                    if(f is String){
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
                val w = Bukkit.getWorld(s[0]) ?: return nullError<Location>(path, "World(${s[0]})")
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
        return if(config.contains(path)){
            val g = get(path)
            if(g is String){
                getLocation(g) ?: nullError<Location>(path, "Location")
            } else {
                typeMismatchError<Location>(path, "String(Location)")
            }
        } else {
            notFoundError(path, sendNotFound)
        }
    }

    fun getLocationList(path: String, sendNotFound: Boolean = true): List<Location>? {
        return if(config.contains(path)){
            val g = get(path)
            if(g is List<*>){
                val list = mutableListOf<Location>()
                g.forEachIndexed { i, f ->
                    if(f is String){
                        val loc = getLocation(f)
                        if(loc != null){
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
        return if(config.contains(path)){
            val g = get(path)
            if(g is Boolean){
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

    private fun getCustomItemStackListFromStringList(path: String, sendNotFound: Boolean = true, slip: Int = 0): ConfigLoadItemList? {
        return if(config.contains(path)){
            val g = get(path)
            if(g is List<*>){
                val list = ConfigLoadItemList()
                g.forEachIndexed { i, f ->
                    if(f is String){
                        val s = f.split("\\s+".toRegex())
                        when(s.size){
                            2 + slip, 3 + slip -> {
                                val item = getItem(s[0 + slip], s[1 + slip], s.getOrNull(2 + slip))
                                if(item != null){
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

    fun getCustomItemStackListFromStringList(path: String, def: List<CustomItemStack>, sendNotFound: Boolean = true, slip: Int = 0): MutableList<CustomItemStack> {
        return (getCustomItemStackListFromStringList(path, sendNotFound, slip)?.getItemList() ?: def).toMutableList()
    }

    fun getCustomItemStackFromString(path: String, sendNotFound: Boolean = true, slip: Int = 0): CustomItemStack? {
        return if(config.contains(path)){
            val g = get(path)
            if(g is String){
                val s = g.split("\\s+".toRegex())
                when(s.size){
                    2 + slip, 3 + slip -> getItem(s[0 + slip], s[1 + slip], s.getOrNull(2 + slip)) ?: nullError<CustomItemStack>(path, "CustomItemStack(${s[0 + slip]} ${s[1 + slip]})")
                    else -> formatMismatchError(path)
                }
            } else {
                typeMismatchError<CustomItemStack>(path, "String(CustomItemStack)")
            }
        } else {
            notFoundError(path, sendNotFound)
        }
    }

    fun getCustomItemStackFromString(path: String, def: CustomItemStack, sendNotFound: Boolean = true, slip: Int = 0): CustomItemStack {
        return getCustomItemStackFromString(path, sendNotFound, slip) ?: def
    }

    /*
    fun getCustomItemStack(path: String, sendNotFound: Boolean = true): CustomItemStack? {
        return if(config.contains(path)){
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
        return if(config.contains(path)){
            val g = get(path)
            if(g is List<*>){
                val list = mutableListOf<CustomItemStack>()
                g.forEachIndexed { i, f ->
                    if(f is CustomItemStack){
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

    fun getCustomItemStackList(path: String, def: Collection<CustomItemStack>, sendNotFound: Boolean = true): MutableList<CustomItemStack> {
        return (getCustomItemStackList(path, sendNotFound) ?: def).toMutableList()
    }

    fun getItemStack(path: String, sendNotFound: Boolean = true): ItemStack? {
        return if(config.contains(path)){
            val g = get(path)
            if(g is ItemStack){
                g
            } else {
                typeMismatchError<ItemStack>(path, "ItemStack")
            }
        } else {
            notFoundError(path, sendNotFound)
        }
    }

    fun getDate(path: String, sendNotFound: Boolean = true): Date? {
        return if(config.contains(path)){
            val g = get(path)
            if(g is Date){
                g
            } else {
                typeMismatchError<Date>(path, "Date")
            }
        } else {
            notFoundError(path, sendNotFound)
        }
    }

    fun contains(path: String) = config.contains(path)

    fun getSection(path: String, sendNotFound: Boolean = true) = config.getConfigurationSection(path)?.getKeys(false) ?: notFoundError<Set<String>>(path, sendNotFound)

    fun set(path: String, value: Any?, save: Boolean = true){
        config.set(path, value)
        if(save) save()
    }

    fun save(){
        config.save(file)
        if(file.length() == 0L){
            delete()
        }
    }

    fun delete(){
        file.delete()
        Plugin.info("$path の削除に成功しました")
    }

    fun send(msg: String){
        output.send("&b[$path] $msg")
    }

    private fun unusedError(path: String) {
        send("&d$path is not used")
    }

    private fun <T> notFoundError(path: String, sendNotFound: Boolean): T?{
        if(sendNotFound) send("&c$path is not found.")
        return null
    }

    fun unloadError(path: String, cause: String){
        send("&c$path is unused. ($cause)")
    }

    fun nullError(path: String, thing: String) {
        send("&c$path $thing is null.")
    }

    private fun <T> nullError(path: String, thing: String): T?{
        nullError(path, thing)
        return null
    }

    private fun <T> formatMismatchError(path: String): T?{
        send("&c$path is incorrect format.")
        return null
    }

    fun typeMismatchError(path: String, type: String) {
        send("&c$path is not $type type.")
    }

    private fun <T> typeMismatchError(path: String, type: String): T?{
        typeMismatchError(path, type)
        return null
    }

    private fun typeMismatchInListError(path: String, index: Int, type: String){
        send("&c$path:${index + 1} is not $type type.")
    }

    fun getItem(type: String, id: String, amount: String?) =
        getItem(type, id, amount?.toIntOrNull() ?: 1)

    fun getItem(type: String, id: String, amount: Int = 1): CustomItemStack?{
        return when(type.toLowerCase()){
            "mm" -> getItemFromMythicMobs(id, amount)
            "cs" -> getItemFromCrackShot(id, amount)
            "csp" -> getItemFromCrackShotPlus(id, amount)
            "mc" -> getItemFromMineCraft(id, amount)
            "mg", "mg-wand" -> getMagicWand(id, amount)
            "mg-spell" -> getMagicSpell(id, amount)
            "ss-ticket" -> Tour.getTicket(id, amount)
            else -> null
        }
    }

    fun getConfigContentsFromList(from: String, path: String, sendNotFound: Boolean = true): ConfigContents {
        val list = ConfigContents()
        getStringList(path, sendNotFound)?.forEach { s ->
            val c = getConfigContent(from, s)
            if(c is ConfigContentError){
                send(c.msg)
            } else {
                list.addContent(c)
            }
        }
        return list
    }

    fun getConfigContent(from: String, s: String): ConfigContent {
        val t = s.split(Regex("\\s+"))
        return when(t[0].toLowerCase()){
            "money" -> {
                when(t.size){
                    2 -> {
                        val p = t[1].toLongOrNull()
                        if(p != null){
                            ConfigMoneyJPY(p)
                        } else {
                            ConfigContentError("&c$from #id - ${t[1]} price null")
                        }
                    }
                    3 -> {
                        val p = t[1].toLongOrNull()
                        if(p != null){
                            when(t[2].toLowerCase()){
                                "jpy" -> {
                                    ConfigMoneyJPY(p)
                                }
                                "eme" -> {
                                    ConfigMoneyEme(p.toInt())
                                }
                                else -> {
                                    ConfigContentError("&c$from #id - ${t[2]} money type not found")
                                }
                            }
                        } else {
                            ConfigContentError("&c$from #id - ${t[1]} price null")
                        }
                    }
                    else -> ConfigContentError("&c$from #id - item $s format error")
                }
            }
            "exp" -> {
                if(t.size == 2){
                    val p = t[1].toIntOrNull()
                    if(p != null){
                        ConfigExp(p)
                    } else {
                        ConfigContentError("&c$from #name - exp value is not Int")
                    }
                } else {
                    ConfigContentError("&c$from #name - exp $t format error")
                }
            }
            "item" -> {
                if(t.size == 3 || t.size == 4){
                    val item = getItem(t[1], t[2], t.getOrNull(3))
                    if(item != null){
                        ConfigItemStack(item)
                    } else {
                        ConfigContentError("&c$from #id - $s item is null")
                    }
                } else {
                    ConfigContentError("&c$from #id - $s item format error")
                }
            }
            "gp" -> {
                if(t.size == 2){
                    val p = t[1].toIntOrNull()
                    if(p != null){
                        ConfigGuildPoint(p)
                    } else {
                        ConfigContentError("&c$from #name - gp value is not Int")
                    }
                } else {
                    ConfigContentError("&c$from #name - gp $t format error")
                }
            }
            "sp" -> {
                if(t.size == 2){
                    val p = t[1].toIntOrNull()
                    if(p != null){
                        return ConfigMagicSP(p)
                    } else {
                        ConfigContentError("&c$from #name - sp value is not Int")
                    }
                } else {
                    ConfigContentError("&c$from #name - sp $s format error")
                }
            }
            "pet" -> {
                if(t.size == 2){
                    ConfigMyPet.fromTypeName(t[1]) ?: ConfigContentError("&c$from #name - pet type is not found")
                } else {
                    ConfigContentError("&c$from #name - pet $s format error")
                }
            }
            "cmd" -> {
                if(t.size != 1){
                    val cmd = t.joinToString(" ").substring(t[0].length + 1)
                    ConfigRunCmd(cmd)
                } else {
                    ConfigContentError("&c$from #name - cmd is null")
                }
            }
            else -> ConfigContentError("&c$from #name - $s type is not found")
        }
    }

    fun getSlotContent(s: String): Pair<ConfigContent, Int>?{
        val t = s.split(Regex("\\s+"))
        when(t[0].toLowerCase()){
            "exp" -> {
                when(t.size){
                    3 -> {
                        val value = t[1].toIntOrNull()
                        if(value != null){
                            val per = t[2].toIntOrNull()
                            if(per != null){
                                return ConfigExp(value) to per
                            } else {
                                send("&cSlot - $s Per null")
                            }
                        } else {
                            send("&cSlot - $s value null")
                        }
                    }
                    2 -> {
                        val value = t[1].toIntOrNull()
                        if(value != null){
                            return ConfigExp(value) to 1
                        } else {
                            send("&cSlot - $s value null")
                        }
                    }
                    else -> send("&cSlot - $s exp format error")
                }
            }
            "money" -> {
                when(t.size){
                    3 -> {
                        val price = t[1].toLongOrNull()
                        if(price != null){
                            val per = t[2].toIntOrNull()
                            if(per != null) {
                                return ConfigMoneyJPY(price) to per
                            } else {
                                send("&cSlot - $s Per null")
                            }
                        } else {
                            send("&cSlot - $s Price null")
                        }
                    }
                    2 -> {
                        val price = t[1].toLongOrNull()
                        if(price != null){
                            return ConfigMoneyJPY(price) to 1
                        } else {
                            send("&cSlot - $s Price null")
                        }
                    }
                    else -> send("&cSlot - $s money format error")
                }
            }
            "item" -> {
                when(t.size){
                    5 -> {
                        val item = getItem(t[1], t[2], t[3])
                        if(item != null){
                            val per = t[4].toIntOrNull()
                            if(per != null){
                                return ConfigItemStack(item) to per
                            } else {
                                send("&cSlot - $s Per null")
                            }
                        } else {
                            send("&cSlot - $s Item null")
                        }
                    }
                    4 -> {
                        val item = getItem(t[1], t[2], t[3])
                        if(item != null){
                            return ConfigItemStack(item) to 1
                        } else {
                            send("&cSlot - $s Item null")
                        }
                    }
                    else -> send("&cSlot - $s Item Format error")
                }
            }
            else -> {
                send("&cSlot - $s type not found")
            }
        }
        return null
    }
}