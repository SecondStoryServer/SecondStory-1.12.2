package me.syari.sec_story.lib

import me.syari.sec_story.lib.StringEditor.toColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Utility
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class CustomItemStack() : ConfigurationSerializable {
    lateinit var item: ItemStack

    var amount: Int = 1

    var type: Material
        set(value){
            item.type = value
        }
        get() = item.type ?: Material.AIR

    val hasColorInDisplay get() = display?.contains("§") ?: false

    val hasDisplay get() = itemMeta?.hasDisplayName() ?: false

    var display: String?
        set(value){
            editMeta {
                displayName = value?.toColor
            }
        }
        get() = itemMeta?.displayName

    fun containsLore(s: String): Boolean{
        val c = s.toColor
        lore.forEach { f ->
            if(f == c) return true
        }
        return false
    }

    /*
    fun containsLoreBody(s: String): Boolean{
        val c = s.toColor
        lore.forEach { f ->
            if(f.contains(c)) return true
        }
        return false
    }
     */

    val hasLore get() = itemMeta?.hasLore() ?: false

    fun addLore(newLine: Iterable<String>){
        val l = lore.toMutableList()
        l.addAll(newLine)
        lore = l
    }

    fun addLore(vararg newLine: String){
        addLore(newLine.toList())
    }

    var lore: List<String>
        set(value){
            item.lore = value.map { it.toColor }
        }
        get() = item.lore ?: listOf()

    var durability
        set(value){
            item.durability = value
        }
        get() = item.durability

    var unbreakable: Boolean
        set(value) {
            editMeta {
                isUnbreakable = value
            }
        }
        get() = itemMeta?.isUnbreakable ?: false

    val hasItemMeta get() = item.hasItemMeta()

    private inline fun editMeta(run: ItemMeta.() -> Unit){
        val meta = itemMeta ?: Bukkit.getItemFactory().getItemMeta(type) ?: return
        meta.run()
        itemMeta = meta
    }

    var itemMeta: ItemMeta?
        set(value){
            item.itemMeta = value
        }
        get() = item.itemMeta

    fun addItemFlag(flag: ItemFlag){
        item.addItemFlags(flag)
    }

    private fun addEnchant(enchant: Enchantment, level: Int){
        editMeta {
            addEnchant(enchant, level, true)
        }
    }

    fun setShine(){
        addItemFlag(ItemFlag.HIDE_ENCHANTS)
        addEnchant(Enchantment.ARROW_INFINITE, 0)
    }

    val toItemStack: List<ItemStack>
        get() {
            val ret = mutableListOf<ItemStack>()
            val i64 = item.clone()
            i64.amount = 64
            for (i in 0 until (amount / 64)) {
                ret.add(i64)
            }
            val rem = amount % 64
            if (rem != 0) {
                val iRem = item.clone()
                iRem.amount = rem
                ret.add(iRem)
            }
            return ret
        }

    val toOneItemStack: ItemStack
        get() {
            val i = item.clone()
            i.amount = if (64 < amount) 64 else amount
            return i
        }

    fun isSimilar(cItem: CustomItemStack): Boolean{
        return toOneItemStack.isSimilar(cItem.toOneItemStack)
    }

    fun isSimilar(item: ItemStack): Boolean{
        return toOneItemStack.isSimilar(item)
    }

    inline fun editNMS(run: net.minecraft.server.v1_12_R1.ItemStack.() -> Unit){
        val nmsItem = CraftItemStack.asNMSCopy(item) ?: return
        run.invoke(nmsItem)
        item = nmsItem.asBukkitCopy()
    }

    inline fun editNBTTag(run: net.minecraft.server.v1_12_R1.NBTTagCompound.() -> Unit){
        editNMS {
            val nbtTag = tag ?: return@editNMS
            run.invoke(nbtTag)
            tag = nbtTag
        }
    }

    @Utility
    override fun serialize(): Map<String, Any> {
        val result = LinkedHashMap<String, Any>()

        result["type"] = type.name

        if (durability.toInt() != 0) {
            result["damage"] = durability
        }

        if (amount != 1) {
            result["amount"] = amount
        }

        val meta = itemMeta
        if (meta != null && !Bukkit.getItemFactory().equals(meta, null)) {
            result["meta"] = meta
        }

        return result
    }

    constructor(item: ItemStack?) : this() {
        if(item != null){
            this.item = item.asOne()
            this.amount = item.amount
        } else {
            this.item = ItemStack(Material.AIR)
            this.amount = 0
        }
    }

    constructor(item: ItemStack?, amount: Int) : this() {
        if(item != null){
            this.item = item.asOne()
            this.amount = amount
        } else {
            this.item = ItemStack(Material.AIR)
            this.amount = 0
        }
    }

    constructor(material: Material?, amount: Int = 1) : this() {
        if(material != null){
            this.item = ItemStack(material, 1)
            this.amount = amount
        } else {
            this.item = ItemStack(Material.AIR)
            this.amount = 0
        }
    }

    constructor(material: Material, display: String?, lore: List<String>, durability: Short = 0, amount: Int = 1): this(){
        this.item = ItemStack(material, 1)
        this.display = display
        this.lore = lore
        this.durability = durability
        this.amount = amount
        if(material == Material.WRITTEN_BOOK){
            addItemFlag(ItemFlag.HIDE_POTION_EFFECTS)
        }
    }

    constructor(material: Material, display: String?, vararg lore: String, durability: Short = 0, amount: Int = 1): this(material, display, lore.toList(), durability, amount)

    constructor(args: Map<String, Any>) : this() {
        val type = Material.getMaterial(args["type"] as String)
        var damage: Short = 0
        var amount = 1

        if (args.containsKey("damage")) {
            damage = (args["damage"] as Number).toShort()
        }

        if (args.containsKey("amount")) {
            amount = (args["amount"] as Number).toInt()
        }

        val result = ItemStack(type, 1, damage)

        if (args.containsKey("meta")) {
            val raw = args["meta"]
            if (raw is ItemMeta) {
                result.itemMeta = raw
            }
        }

        this.item = result.ensureServerConversions().asOne()
        this.amount = amount
    }

    fun copy() = CustomItemStack(item, amount)

    companion object {
        fun fromNullable(item: ItemStack?, amount: Int): CustomItemStack?{
            return if(item != null) CustomItemStack(item, amount) else null
        }
    }
}