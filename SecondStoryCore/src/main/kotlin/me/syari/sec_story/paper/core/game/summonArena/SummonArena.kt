package me.syari.sec_story.paper.core.game.summonArena

import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.element
import me.syari.sec_story.paper.library.command.CreateCommand.elementIfOp
import me.syari.sec_story.paper.library.command.CreateCommand.onlinePlayers
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.inv.CreateInventory.inventory
import me.syari.sec_story.paper.library.inv.CreateInventory.reopen
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.server.Server.getPlayer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

object SummonArena: FunctionInit {
    override fun init() {
        createCmd("sa", tab { sender ->
            element("leave", "allow", "option").joinIfOp(sender, "join")
        }, tab("join") { sender -> elementIfOp(sender, arenaNames) }, tab("allow") { onlinePlayers }) { sender, args ->
            if(sender is Player) {
                when(args.whenIndex(0)) {
                    "join" -> {
                        val group = args.getOrNull(1) ?: return@createCmd
                        sender.selectArena(group)
                    }
                    "leave" -> {
                        sender.getArena()?.leave(sender) ?: return@createCmd sender.send(
                            "&b[SummonArena] &c召喚アリーナにいません"
                        )
                    }
                    "allow" -> {
                        val arena = sender.getArena() ?: return@createCmd sender.send("&b[SummonArena] &c召喚アリーナにいません")
                        if(! arena.needAllow) return@createCmd sender.send("&b[SummonArena] &c参加申請が不要なアリーナです")
                        val rawPlayer = args.getOrNull(1) ?: return@createCmd
                        val player = getPlayer(rawPlayer) ?: return@createCmd sender.send(
                            "&b[SummonArena] &cそのプレイヤーはオンラインではありません"
                        )
                        if(! arena.waitAllow.contains(player.uniqueId)) return@createCmd sender.send(
                            "&b[SummonArena] &c参加申請を出していないプレイヤーです"
                        )
                        arena.allowJoin(player)
                    }
                    "option" -> {
                        sender.getArena()?.openOption(sender) ?: return@createCmd sender.send(
                            "&b[SummonArena] &c召喚アリーナにいません"
                        )
                    }
                }
            }
        }
    }

    private val mobs = mutableListOf<SummonArenaMob>()

    fun clearMob() {
        mobs.clear()
    }

    fun addMob(summonArenaMob: SummonArenaMob) {
        mobs.add(summonArenaMob)
    }

    fun getMob(id: String) = mobs.firstOrNull { f -> f.id == id }

    fun Player.selectMob(summonArenaData: SummonArenaData) {
        inventory("&9&l召喚モンスター選択", summonArenaData.line) {
            var index = 0
            mobs.forEach { m ->
                if(m.group in summonArenaData.group) {
                    item(index, m.getIcon(this@selectMob)).event(ClickType.LEFT) {
                        summonArenaData.setMob(this@selectMob, m)
                        closeInventory()
                    }.event(ClickType.RIGHT) {
                        if(isOp) {
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

    fun setArenas(summonArenaData: List<SummonArenaData>) {
        arenaNames.clear()
        val mutable = arenas.toMutableList()
        summonArenaData.forEach { f ->
            val inh = mutable.firstOrNull { a -> a.id == f.id }
            if(inh != null) {
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

    private fun Player.selectArena(arenaGroup: String) {
        var index = 0
        inventory("&9&lアリーナ選択", 6) {
            arenas.forEach { a ->
                if(a.arenaGroup == arenaGroup) {
                    val canJoin = a.canJoin()
                    val pair = if(canJoin) {
                        if(a.players.isEmpty()) {
                            8 to 1
                        } else {
                            10 to a.players.size
                        }
                    } else {
                        9 to a.maxPlayer
                    }
                    item(
                        index,
                        Material.INK_SACK,
                        a.name,
                        "&7プレイヤー数 : &6${a.players.size} &7/ &6${a.maxPlayer}",
                        "&7参加申請 : &6${if(a.needAllow) "必要" else "不必要"}",
                        damage = pair.first.toShort(),
                        amount = pair.second
                    ).event(ClickType.LEFT) {
                        if(canJoin) {
                            a.join(this@selectArena)
                            reopen(id) { p -> p.selectArena(arenaGroup) }
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

    fun onDisable() {
        arenas.forEach { a ->
            a.leaveAll()
        }
    }
}