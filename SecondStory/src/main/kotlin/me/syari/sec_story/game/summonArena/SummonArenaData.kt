package me.syari.sec_story.game.summonArena

import me.syari.sec_story.game.summonArena.SummonArena.getArena
import me.syari.sec_story.hook.MythicMobs.allMythicMobs
import me.syari.sec_story.hook.MythicMobs.spawnMythicMobs
import me.syari.sec_story.lib.Region
import me.syari.sec_story.lib.message.SendMessage
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.inv.CreateInventory
import me.syari.sec_story.lib.inv.CreateInventory.open
import me.syari.sec_story.plugin.Plugin
import me.syari.sec_story.rank.Ranks.rank
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

data class SummonArenaData(
    val id: String,
    val arenaGroup: String,
    val maxPlayer: Int,
    val name: String,
    val line: Int,
    val group: List<String>,
    val spawn: Location,
    val button: Location,
    val itemFrame: Location,
    val tpTo: Location,
    val tpBack: Location,
    val pos1: Location,
    val pos2: Location
){
    var players = mutableListOf<UUID>()
    var needAllow = false
    var waitAllow = mutableListOf<UUID>()
    var summon: SummonArenaMob? = null
    private val region = Region(pos1, pos2)
    private val mobs = mutableMapOf<UUID, SummonArenaMob>()

    fun announce(msg: String){
        players.forEach { uuid -> Plugin.plugin.server.getPlayer(uuid).send(msg) }
    }

    fun join(p: Player){
        when {
            players.contains(p.uniqueId) -> p.send("&b[SummonArena] &c既に別のアリーナに入っています")
            needAllow -> sendReq(p)
            else -> onJoin(p)
        }
    }

    private fun sendReq(from: Player){
        if(waitAllow.contains(from.uniqueId)) return from.send("&b[SummonArena] &c既に参加申請を送っています")
        waitAllow.add(from.uniqueId)
        players.forEach { uuid ->
            Plugin.plugin.server.getPlayer(uuid)?.send("&b[SummonArena] &a${from.displayName}&fがアリーナに入ろうとしています " to null, "&a&l許可する" to SendMessage.Action(hover = "&6クリック", click = SendMessage.ClickType.RunCommand to "/sa allow ${from.name}"))
        }
        object : BukkitRunnable(){
            override fun run() {
                if(waitAllow.contains(from.uniqueId) && from.getArena() == null){
                    waitAllow.remove(from.uniqueId)
                    from.send("&b[SummonArena] &a${arenaGroup}&fの&a${name}&fへの参加申請がキャンセルされました")
                }
            }
        }.runTaskLater(Plugin.plugin, 60 * 20)
    }

    fun allowJoin(p: Player){
        waitAllow.remove(p.uniqueId)
        onJoin(p)
    }

    private fun onJoin(p: Player){
        players.add(p.uniqueId)
        p.teleport(tpTo)
        announce("&b[SummonArena] &a${p.displayName}&fが&a${name}&fに入場しました")
    }

    fun leaveAll(){
        players.forEach { uuid ->
            Plugin.plugin.server.getPlayer(uuid).teleport(tpBack)
        }
        players.clear()
        onLeaveAll()
    }

    fun leave(p: Player){
        if(!players.contains(p.uniqueId)) return
        announce("&b[SummonArena] &a${p.displayName}&fが&a${name}&fから出場しました")
        players.remove(p.uniqueId)
        p.teleport(tpBack)
        if(players.isEmpty()) {
            onLeaveAll()
        }
    }

    private fun onLeaveAll(){
        setMob(null, null)
        clearMob()
        needAllow = false
    }

    private enum class OptionNeedAllow(val display: String, val damage: Short){
        NEED("&c必要", 14),
        NOT("&a不必要", 5)
    }

    fun openOption(p: Player){
        val optionNeedAllow = if(needAllow) OptionNeedAllow.NEED else OptionNeedAllow.NOT
        CreateInventory.inventory("&0&lアリーナ設定", 1) {
            id = "SummonArena_Option_${this@SummonArenaData.id}"
            item(
                4,
                Material.STAINED_GLASS_PANE,
                "&6&l&n参加許可&f ${optionNeedAllow.display}",
                damage = optionNeedAllow.damage
            )
                .event(ClickType.LEFT) {
                    needAllow = !needAllow
                    CreateInventory.reopen("SummonArena_Option_${this@SummonArenaData.id}") { player -> openOption(player) }
                }
        }.open(p)
    }

    fun setMob(changer: Player?, summonArenaMob: SummonArenaMob?){
        val frame = itemFrame.getNearbyEntities(1.0, 1.0, 1.0).firstOrNull { e -> e is ItemFrame } as? ItemFrame
        if(frame != null){
            if(summonArenaMob != null){
                val icon = summonArenaMob.icon
                icon.display = "&f${summonArenaMob.name}"
                frame.item = icon.toOneItemStack
                if(changer != null){
                    announce("&b[SummonArena] &a${changer.displayName}&fが召喚モンスターを&a${summonArenaMob.name}&fに変更しました")
                }
            } else {
                frame.item = null
            }
            summon = summonArenaMob
        } else {
            changer?.send("&b[SummonArena] &c召喚モンスターの変更に失敗しました")
        }
    }

    fun canJoin() = players.size < maxPlayer

    fun inRegion(loc: Location) = region.inRegion(loc)

    fun getAllSummonPoint(): Int {
        var sum = 0
        players.forEach { uuid -> sum += Plugin.plugin.server.getPlayer(uuid).rank.summon }
        return sum
    }

    fun getUsedSummonPoint(): Int {
        var sum = 0
        val delete = mutableListOf<UUID>()
        mobs.forEach { (uuid, mob) ->
            if(Plugin.plugin.server.getEntity(uuid) == null){
                delete.add(uuid)
            } else {
                sum += mob.summon
            }
        }
        delete.forEach { uuid -> mobs.remove(uuid) }
        return sum
    }

    fun getMob(uuid: UUID) = mobs[uuid]

    fun spawnMob(summon: SummonArenaMob){
        val entity = spawnMythicMobs(summon.id, spawn) ?: return
        val living = entity.livingEntity
        val uuid = living.uniqueId
        mobs[uuid] = summon
    }

    fun removeMob(uuid: UUID){
        mobs.remove(uuid)
    }

    private fun clearMob(){
        allMythicMobs.forEach { f ->
            if(inRegion(f.location)){
                f.remove()
            }
        }
        mobs.clear()
    }
}