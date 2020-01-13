package me.syari.sec_story.shop

import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.StringEditor.toColor
import me.syari.sec_story.lib.StringEditor.toUncolor
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.lib.command.CreateCommand.element
import me.syari.sec_story.lib.command.CreateCommand.tab
import me.syari.sec_story.plugin.Init
import net.citizensnpcs.api.event.NPCRightClickEvent
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

object Shops : Listener, Init() {
    override fun init() {
        createCmd("openshop",
                tab { element(idList) }
        ){ sender, args ->
            if(sender is Player){
                val name = args.getOrNull(0) ?: return@createCmd sender.send("&b[Shop] &cショップ名を入力してください")
                val shop = getShop(name) ?: return@createCmd sender.send("&b[Shop] &c存在しないショップです")
                shop.open(sender)
            }
        }
    }

    @EventHandler
    fun on(e: NPCRightClickEvent){
        val name = e.npc.name
        val p = e.clicker
        val c = shops.firstOrNull { f -> f.npc == name } ?: return
        c.open(p)
    }

    @EventHandler
    fun on(e: PlayerInteractEvent){
        val p = e.player
        if(e.action != Action.RIGHT_CLICK_BLOCK) return
        if(e.clickedBlock.type == Material.SIGN || e.clickedBlock.type == Material.SIGN_POST || e.clickedBlock.type == Material.WALL_SIGN){
            val sign = e.clickedBlock.state as? Sign ?: return
            if(sign.getLine(0) == "&6[Shop]".toColor){
                getShop(sign.getLine(1).toUncolor)?.open(p)
                e.isCancelled = true
            }
        }
    }

    private val shops = mutableListOf<Shop>()

    fun addShop(shop: Shop){
        shops.add(shop)
    }

    fun clearShop(){
        shops.clear()
    }

    fun getShop(id: String) = shops.firstOrNull { f -> f.id == id }

    private val idList: List<String>
        get() {
            val ret = mutableListOf<String>()
            shops.forEach { s -> ret.add(s.id) }
            return ret
        }
}