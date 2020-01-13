package me.syari.sec_story.rpg

import me.syari.sec_story.data.SaveData.loadSave
import me.syari.sec_story.data.SaveData.saveExp
import me.syari.sec_story.data.SaveData.saveInventory
import me.syari.sec_story.data.SaveData.saveLocation
import me.syari.sec_story.lib.CreateBossBar.createBossBar
import me.syari.sec_story.lib.CreateScoreBoard.createBoard
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.message.SendMessage.title
import me.syari.sec_story.lib.StringEditor.toColor
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.lib.command.CreateCommand.element
import me.syari.sec_story.lib.command.CreateCommand.tab
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.Plugin.plugin
import me.syari.sec_story.rpg.Quests.openQuest
import me.syari.sec_story.server.CommandBlock
import me.syari.sec_story.server.CommandBlock.addDisAllowCmd
import me.syari.sec_story.server.CommandBlock.addIgnoreWildCmd
import me.syari.sec_story.server.CommandBlock.clearDisAllowCmd
import me.syari.sec_story.server.CommandBlock.clearIgnoreWildCmd
import me.syari.sec_story.server.Server.hideBoard
import net.citizensnpcs.api.event.NPCRightClickEvent
import org.bukkit.Location
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.InventoryHolder
import org.bukkit.scheduler.BukkitRunnable

object RPG : Listener, Init(){

    // TODO Respawn

    override fun init() {
        createCmd("rpg",
                tab { element("join", "leave") }
        ){ sender, args ->
            fun help() = sender.send("""
                &b[RPG] &fコマンド
                &a/rpg join &7RPGに参加します
                &a/rpg leave &7RPGから脱退します
            """.trimIndent())
            when (args.whenIndex(0)) {
                "end" -> {
                    if (!sender.isOp) return@createCmd help()
                    if(!held) return@createCmd sender.send("&b[RPG] &cRPGは開催されていません")
                    end()
                }
                "join" -> {
                    if (sender is Player) {
                        if(!wait) return@createCmd sender.send("&b[RPG] &c既に開始しています")
                        if(sender.nowRPG) return@createCmd sender.send("&b[RPG] &c既に参加しています")
                        if(!held) held()
                        sender.joinRPG()
                    }
                }
                "leave" -> {
                    if (sender is Player) {
                        if(!sender.nowRPG) return@createCmd sender.send("&b[RPG] &c参加していません")
                        sender.leaveRPG()
                    }
                }
                else -> help()
            }
        }
    }

    var lobby: Location? = null
    var spawn: Location? = null

    fun showBoard(){
        players.forEach { r ->
            createBoard("&a&lRPG", r.player,
                    -1 to "&e&m------------------------&1",
                    -2 to "&a&l残り時間 &7≫ &e%02d:%02d&2".format(time / 60, time % 60),
                    -3 to "&3",
                    -4 to "&a&lお金 &7≫ &e${r.eme} EME&4",
                    -5 to "&5",
                    -6 to "&a&lプレイヤー数 &7≫ &e${players.size}&6",
                    -7 to "&e&m------------------------&7"
            )
        }
    }

    private var time = 0
    private var held = false

    private fun held(){
        val bar = createBossBar("&f&lRPGが始まります &a&l/rpg join &f&lで参加しましょう", BarColor.GREEN, BarStyle.SOLID, true)
        players.clear()
        held = true
        time = 2 * 60
        object : BukkitRunnable(){
            override fun run() {
                if(!held) {
                    bar.delete()
                    cancel()
                    return
                }
                bar.progress = time / 120.0
                showBoard()
                if(time == 0){
                    bar.delete()
                    start()
                    cancel()
                }
                time --
            }
        }.runTaskTimer(plugin, 0, 20)
    }

    fun start(){
        wait = false
        players.forEach { r ->
            if(!r.player.isOnline || r.player.isDead) {
                players.remove(r)
                r.player.loadSave()
                r.player.hideBoard = false
            } else {
                r.player.teleport(spawn)
                r.player.title("&a&l冒険開始", "", 0, 50, 0)
            }
        }
        if(players.isEmpty()){
            held = false
            return
        }
        time = 5 * 60
        object : BukkitRunnable(){
            override fun run() {
                if(!held) {
                    cancel()
                    return
                }
                showBoard()
                if(time == 0){
                    end()
                    cancel()
                }
                time --
            }
        }.runTaskTimer(plugin, 0, 20)
    }

    fun end(){
        held = false
        wait = true
        players.forEach { r ->
            r.player.hideBoard = false
            r.player.loadSave()
            r.player.clearDisAllowCmd(CommandBlock.CommandAddCause.RPG)
            r.player.clearIgnoreWildCmd(CommandBlock.CommandAddCause.RPG)
        }
        players.clear()
        clearQuests.clear()
    }

    fun announce(msg: String){
        val c = msg.toColor
        players.forEach { r -> r.player.send(c) }
    }

    val players = mutableListOf<RPGPlayer>()

    data class RPGPlayer(val player: Player){
        var eme = 0
        var nowQuest: Quest? = null
    }

    val clearQuests = mutableListOf<Quest>()

    private fun Player.joinRPG(){
        players.add(RPGPlayer(this))
        hideBoard = true
        closeInventory()
        saveInventory()
        saveLocation()
        saveExp()
        announce("&7 >> &aRPG&fに&a$displayName&fが参加しました")
        teleport(lobby)
        addDisAllowCmd(CommandBlock.CommandAddCause.RPG, "*")
        addIgnoreWildCmd(CommandBlock.CommandAddCause.RPG, "rpg")
    }

    private fun Player.leaveRPG(){
        if(!players.contains(RPGPlayer(this))) return
        announce("&7 >> &aRPG&fから&a$displayName&fが脱退しました")
        players.remove(RPGPlayer(this))
        if(players.isEmpty()){
            end()
        }
        hideBoard = false
        loadSave()
        clearDisAllowCmd(CommandBlock.CommandAddCause.RPG)
        clearIgnoreWildCmd(CommandBlock.CommandAddCause.RPG)
    }

    @EventHandler
    fun on(e: PlayerQuitEvent){
        e.player.leaveRPG()
    }

    private val Player.nowRPG get() = players.contains(RPGPlayer(this))

    private var wait = true

    val Player.data get() = players.firstOrNull { r -> r.player == this }

    var Player.eme: Int
        get() = data?.eme ?: 0
        set(value) {
            data?.eme = value
        }

    @EventHandler
    fun on(e: NPCRightClickEvent){
        val p = e.clicker
        val npc = e.npc.name
        p.openQuest(npc)
    }

    @EventHandler
    fun on(e: PlayerDropItemEvent){
        if(!e.player.nowRPG) return
        val item = e.itemDrop
        val i = item.itemStack
        val bool = i.itemMeta?.hasEnchant(Enchantment.BINDING_CURSE) ?: return
        if(bool){
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: PlayerInteractEvent){
        if(!e.player.nowRPG) return
        if(e.action == Action.LEFT_CLICK_BLOCK) e.isCancelled = true
        else if(e.action == Action.RIGHT_CLICK_BLOCK && e.clickedBlock.state is InventoryHolder) e.isCancelled = true
    }

    @EventHandler
    fun on(e: BlockPlaceEvent){
        if(!e.player.nowRPG) return
        e.isCancelled = true
    }

    @EventHandler
    fun on(e: BlockBreakEvent){
        if(!e.player.nowRPG) return
        e.isCancelled = true
    }

    @EventHandler
    fun on(e: EntityDamageByEntityEvent){
        val victim = e.entity as? Player ?: return
        when(e.damager){
            is Player -> {
                val attacker = e.damager as? Player ?: return
                if(victim.nowRPG || attacker.nowRPG) {
                    e.isCancelled = true
                }
            }
            is Projectile -> {
                val pr = e.damager as? Projectile ?: return
                val attacker = pr.shooter as? Player ?: return
                if(victim.nowRPG || attacker.nowRPG) {
                    e.isCancelled = true
                }
            }
        }
    }
}