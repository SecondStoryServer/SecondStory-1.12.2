package me.syari.sec_story.game.summonArena

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import me.syari.sec_story.game.summonArena.MobPoint.addWeekPoint
import me.syari.sec_story.guild.event.GuildMemberTeleportEvent
import me.syari.sec_story.guild.event.GuildWarStartEvent
import me.syari.sec_story.home.HomeSetEvent
import me.syari.sec_story.lib.message.SendMessage.action
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.StringEditor.toColor
import me.syari.sec_story.lib.StringEditor.toUncolor
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.lib.command.CreateCommand.element
import me.syari.sec_story.lib.command.CreateCommand.elementIfOp
import me.syari.sec_story.lib.command.CreateCommand.onlinePlayers
import me.syari.sec_story.lib.command.CreateCommand.tab
import me.syari.sec_story.lib.event.PlayerDeathWithCtEvent
import me.syari.sec_story.lib.inv.CreateInventory.inventory
import me.syari.sec_story.lib.inv.CreateInventory.open
import me.syari.sec_story.lib.inv.CreateInventory.reopen
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.Plugin.plugin
import me.syari.sec_story.world.spawn.SpawnTeleportEvent
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.ItemMergeEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object SummonArena : Init(), Listener {
    override fun init() {
        createCmd("sa",
            tab { sender ->
                element("leave", "allow", "option").joinIfOp(sender, "join")
            },
            tab("join"){ sender -> elementIfOp(sender, arenaNames) },
            tab("allow"){ onlinePlayers() }
        ){ sender, args ->
            if(sender is Player){
                when(args.whenIndex(0)){
                    "join" -> {
                        val group = args.getOrNull(1) ?: return@createCmd
                        sender.selectArena(group)
                    }
                    "leave" -> {
                        sender.getArena()?.leave(sender) ?: return@createCmd sender.send("&b[SummonArena] &c召喚アリーナにいません")
                    }
                    "allow" -> {
                        val arena = sender.getArena() ?: return@createCmd sender.send("&b[SummonArena] &c召喚アリーナにいません")
                        if(!arena.needAllow) return@createCmd sender.send("&b[SummonArena] &c参加申請が不要なアリーナです")
                        val rawPlayer = args.getOrNull(1) ?: return@createCmd
                        val player = plugin.server.getPlayer(rawPlayer) ?: return@createCmd sender.send("&b[SummonArena] &cそのプレイヤーはオンラインではありません")
                        if(!arena.waitAllow.contains(player.uniqueId)) return@createCmd sender.send("&b[SummonArena] &c参加申請を出していないプレイヤーです")
                        arena.allowJoin(player)
                    }
                    "option" -> {
                        sender.getArena()?.openOption(sender) ?: return@createCmd sender.send("&b[SummonArena] &c召喚アリーナにいません")
                    }
                }
            }
        }
    }

    private val mobs = mutableListOf<SummonArenaMob>()

    fun clearMob(){
        mobs.clear()
    }

    fun addMob(summonArenaMob: SummonArenaMob){
        mobs.add(summonArenaMob)
    }

    fun getMob(id: String) = mobs.firstOrNull { f -> f.id == id }

    fun Player.selectMob(summonArenaData: SummonArenaData) {
        inventory("&9&l召喚モンスター選択", summonArenaData.line) {
            var index = 0
            mobs.forEach { m ->
                if(m.group in summonArenaData.group){
                    item(index, m.getIcon(this@selectMob))
                        .event(ClickType.LEFT){
                            summonArenaData.setMob(this@selectMob, m)
                            closeInventory()
                        }
                        .event(ClickType.RIGHT){
                            if(isOp){
                                m.openChildInfo(this@selectMob, summonArenaData)
                            }
                        }
                    index ++
                }
            }
        }.open(this)
    }

    private var arenas = listOf<SummonArenaData>()

    private val arenaNames = mutableSetOf<String>()

    fun setArenas(summonArenaData: List<SummonArenaData>){
        arenaNames.clear()
        val mutable = arenas.toMutableList()
        summonArenaData.forEach { f ->
            val inh = mutable.firstOrNull { a -> a.id == f.id }
            if(inh != null){
                mutable.remove(inh)
                f.players = inh.players
                f.summon = inh.summon
                f.needAllow = inh.needAllow
                f.waitAllow = inh.waitAllow
            }
            arenaNames.add(f.arenaGroup)
        }
        arenas = summonArenaData
    }

    private fun Player.selectArena(arenaGroup: String){
        var index = 0
        inventory("&9&lアリーナ選択", 6) {
            arenas.forEach { a ->
                if(a.arenaGroup == arenaGroup){
                    val canJoin = a.canJoin()
                    val pair = if(canJoin){
                        if(a.players.isEmpty()){
                            8 to 1
                        } else {
                            10 to a.players.size
                        }
                    } else {
                        9 to a.maxPlayer
                    }
                    item(index, Material.INK_SACK, a.name, "&7プレイヤー数 : &6${a.players.size} &7/ &6${a.maxPlayer}", "&7参加申請 : &6${if(a.needAllow) "必要" else "不必要"}", damage = pair.first.toShort(), amount = pair.second)
                        .event(ClickType.LEFT){
                            if(canJoin){
                                a.join(this@selectArena)
                                reopen(id){ p -> p.selectArena(arenaGroup) }
                            }
                        }
                    index ++
                }
            }
        }.open(this)
    }

    fun Player.getArena(): SummonArenaData? {
        return arenas.firstOrNull { f -> f.players.contains(uniqueId) }
    }

    /*
    mob:
        mobID:
            enable: [true/false]
            group: MobGroup
            child:
            - mobID
            - mobID
            exp: 1
            summon: 1
            reward: 1
            icon: mm itemID
            author: mcID
            diff: 1
    arena:
        arenaID:
            name: "ArenaName"
            player: 1
            group:
            - MobGroup1
            - MobGroup2
            spawn: World, X, Y, Z
            button: World, X, Y, Z
            frame: World, X, Y, Z
            tp: World, X, Y, Z
     */

    @EventHandler
    fun on(e: PlayerInteractEntityEvent){
        val frame = e.rightClicked as? ItemFrame ?: return
        val p = e.player ?: return
        val arena = p.getArena() ?: return
        if(arena.itemFrame.getNearbyEntities(1.0, 1.0, 1.0).contains(frame)) {
            p.selectMob(arena)
            e.isCancelled = true
        }
    }

    private val ct = mutableListOf<UUID>()

    @EventHandler
    fun on(e: PlayerInteractEvent){
        if(e.action != Action.RIGHT_CLICK_BLOCK) return
        val p = e.player ?: return
        if(p.isSneaking) return
        if(!ct.contains(p.uniqueId)){
            val arena = p.getArena() ?: return
            val b = e.clickedBlock ?: return
            if(b.isBlockIndirectlyPowered) return
            val loc = b.location ?: return
            if(arena.button != loc) return
            val held = p.inventory.itemInMainHand
            if(held != null && held.type != Material.AIR){
                e.isCancelled = true
                return p.action("&c&l召喚は素手でしか行えません")
            }
            val summon = arena.summon ?: return p.action("&c&l召喚モンスターを選択してください")
            val allSummonPoint = arena.getAllSummonPoint()
            val countPoint = arena.getUsedSummonPoint()
            val summonPoint = summon.summon
            if(allSummonPoint <= countPoint) return p.send("&b[Arena] &c一度に召喚可能な数を上回っています 出したモンスターを倒してください ランクを上げることで上限解放されます")
            arena.spawnMob(summon)
            p.action("&c&n召喚ポイント ${countPoint + summonPoint} / $allSummonPoint")
            ct.add(p.uniqueId)
            object : BukkitRunnable(){
                override fun run() {
                    ct.remove(p.uniqueId)
                }
            }.runTaskLater(plugin, 1)
        }
    }

    @EventHandler
    fun on(e: MythicMobDeathEvent){
        val p = e.killer as? Player ?: return
        val entity = e.entity ?: return
        val uuid = entity.uniqueId ?: return
        val arena = p.getArena() ?: return
        val mob = arena.getMob(uuid) ?: mobs.firstOrNull { f -> f.id == e.mobType.internalName } ?: return
        arena.removeMob(uuid)
        val r = mob.reward
        val xp = mob.exp
        p.addWeekPoint(r)
        p.giveExp(xp)
        p.action("&a討伐ポイント${r} と 経験値${xp} を取得しました")
        safeDrop(p, e.entity.location, e.drops)
        e.drops.clear()
    }

    @EventHandler
    fun on(e: PlayerQuitEvent){
        val p = e.player
        p.getArena()?.leave(p)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun on(e: EntityPickupItemEvent){
        val p = e.entity as? Player ?: return
        val item = e.item ?: return
        val name = item.customName ?: return
        if(item.isCustomNameVisible && name.toUncolor != p.displayName) e.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: ItemMergeEvent){
        val t = e.target
        val b = e.entity
        if(t.isCustomNameVisible != b.isCustomNameVisible || t.customName != b.customName) e.isCancelled = true
    }

    private fun safeDrop(p: Player, loc: Location, items: Collection<ItemStack>){
        items.forEach { i ->
            val item = loc.world.dropItemNaturally(loc, i)
            item.customName = "&a${p.displayName}".toColor
            item.isCustomNameVisible = true
            object : BukkitRunnable(){
                override fun run() {
                    item.customName = null
                    item.isCustomNameVisible = false
                }
            }.runTaskLater(plugin, 20 * 60)
        }
    }

    @EventHandler
    fun on(e: CreatureSpawnEvent){
        if(e.spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER){
            val entity = e.entity
            object : BukkitRunnable(){
                override fun run() {
                    if(!entity.isDead){
                        entity.remove()
                    }
                }
            }.runTaskLater(plugin, 60 * 20)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onClickSign(e: PlayerInteractEvent){
        val p = e.player
        if(e.action != Action.RIGHT_CLICK_BLOCK) return
        if(e.clickedBlock.type == Material.SIGN || e.clickedBlock.type == Material.SIGN_POST || e.clickedBlock.type == Material.WALL_SIGN){
            val sign = e.clickedBlock.state as? Sign ?: return
            val arena = p.getArena() ?: return
            if(sign.getLine(0) == "&6[SummonArena]".toColor){
                e.isCancelled = true
                when(sign.getLine(1)?.toLowerCase()?.toUncolor){
                    "leave" -> arena.leave(p)
                    "option" -> arena.openOption(p)
                    else -> if(p.isOp) p.send("&6[SummonArena] &c看板の2行目を「leave, option」にしてください")
                }
            }
        }
    }

    @EventHandler
    fun on(e: PlayerDeathWithCtEvent){
        val p = e.player
        val a = p.getArena() ?: return
        e.isCancelled = true
        object : BukkitRunnable(){
            override fun run() {
                p.teleport(a.tpTo)
                PlayerRespawnEvent(p, a.tpTo, false).callEvent()
            }
        }.runTaskLater(plugin, 3)
    }

    @EventHandler
    fun on(e: GuildWarStartEvent){
        val g = e.guild
        g.warGuild?.getMember()?.forEach { m ->
            m.player.getArena()?.leave(m.player)
        }
    }

    @EventHandler
    fun on(e: GuildMemberTeleportEvent){
        val p = e.player
        val t = e.target
        val pa = p.getArena()
        val ta = t.getArena()
        if(ta != null){
            if(pa != ta){
                if(ta.canJoin()){
                    pa?.leave(p)
                    ta.join(p)
                } else {
                    e.isCancelled = true
                }
            }
        } else {
            pa?.leave(p)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: HomeSetEvent){
        val p = e.player
        if(p.getArena() != null) e.isCancelled = true
    }

    @EventHandler
    fun on(e: SpawnTeleportEvent){
        val p = e.player
        val a = p.getArena() ?: return
        a.leave(p)
        e.isCancelled = true
    }

    @EventHandler
    fun on(e: PlayerMoveEvent){
        val p = e.player ?: return
        val a = p.getArena() ?: return
        val t = e.to
        if(!a.inRegion(t)){
            p.teleport(a.tpTo)
        }
    }

    fun onDisable(){
        arenas.forEach { a ->
            a.leaveAll()
        }
    }
}