package me.syari.sec_story.game.mobArena

import me.syari.sec_story.data.event.PlayerDataEvent
import me.syari.sec_story.game.kit.GameKit.getKitsWithFilter
import me.syari.sec_story.game.mobArena.data.MobArenaData
import me.syari.sec_story.game.mobArena.data.MobArenaPlayer
import me.syari.sec_story.game.mobArena.data.MobArenaStatus
import me.syari.sec_story.guild.event.GuildMemberTeleportEvent
import me.syari.sec_story.guild.event.GuildWarStartEvent
import me.syari.sec_story.hook.FastAsyncWorldEdit.fawePlayer
import me.syari.sec_story.hook.MobArena.nowMobArena
import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.lib.CustomLocation
import me.syari.sec_story.lib.InventoryPlus.insertItem
import me.syari.sec_story.lib.Region
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.lib.command.CreateCommand.element
import me.syari.sec_story.lib.command.CreateCommand.elementIf
import me.syari.sec_story.lib.command.CreateCommand.tab
import me.syari.sec_story.lib.config.CreateConfig.getConfigFile
import me.syari.sec_story.lib.event.PlayerDeathWithCtEvent
import me.syari.sec_story.lib.inv.CreateInventory.inventory
import me.syari.sec_story.lib.inv.CreateInventory.open
import me.syari.sec_story.plugin.Init
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

object MobArena : Listener, Init(){
    private val editMode = mutableMapOf<UUID, String>()

    override fun init() {
        createCmd("ma-debug",
            tab { sender ->
                if(sender is Player){
                    if(sender.isEditMode) {
                        element("name", "lobby", "play", "spec", "spawn", "limit", "kit", "wave", "edit")
                    } else {
                        val p = sender.arenaPlayer
                        when {
                            p == null -> element("join", "spec").joinIfOp(sender, "create", "edit", "start", "end")
                            p.play -> element("leave", "ready", "notready", "kit").joinIfOp(sender, "start", "end")
                            else -> element("join", "leave").joinIfOp(sender, "start", "end")
                        }
                    }
                } else null
            },
            tab("join", "j", "spec", "s"){ element(arenas.mapNotNull { a -> if(a.enable) a.id else null }) },
            tab("edit", "start", "end"){ sender ->
                elementIf(sender.isOp && (sender is Player && !sender.isEditMode), arenas.map { it.id })
            },
            tab("lobby", "play", "spec"){ sender ->
                elementIf(sender.isOp && sender is Player && sender.isEditMode, "area", "spawn")
            },
            tab("spawn"){ sender ->
                elementIf(sender.isOp && (sender is Player && sender.isEditMode), "list", "add")
            },
            tab("limit"){ sender ->
                elementIf(sender.isOp && (sender is Player && sender.isEditMode), listOf("kit", "player"))
            },
            tab("kit"){ sender ->
                elementIf(sender.isOp && (sender is Player && sender.isEditMode), "list", "add", "remove")}
        ){ sender, args ->
            if(sender is Player){
                val edit = sender.editArena
                if (edit == null) {
                    fun help(){
                        sender.send("""
                                &b[MobArena] &fコマンド
                                &7- &a/ma-debug join <ArenaID> &7アリーナに参加します
                                &7- &a/ma-debug leave &7アリーナから脱退します
                                &7- &a/ma-debug spec <ArenaID> &7アリーナを観戦します
                                &7- &a/ma-debug ready &7準備を完了します
                                &7- &a/ma-debug notready &7準備完了を取り消します
                                &7- &a/ma-debug kit &7キットを選択します
                                """.trimIndent() + if(sender.isOp) """
                                    
                                    &7- &a/ma-debug start <ArenaID> &7ゲームを強制的に始めます
                                    &7- &a/ma-debug end <ArenaID> &7ゲームを強制的に終わらせます
                                    &7- &a/ma-debug end all &7全てのゲームを強制的に終わらせます
                                    &7- &a/ma-debug create <ArenaID> &7アリーナを新規作成します
                                    &7- &a/ma-debug edit <ArenaID> &7アリーナの編集モードに変更します
                                    """.trimIndent() else ""
                        )
                    }

                    when(args.whenIndex(0)){
                        "join", "j" -> {
                            val id = args.getOrNull(1) ?: return@createCmd sender.send("&b[MobArena] &cモブアリーナを入力してください")
                            val arena = getArena(id) ?: return@createCmd sender.send("&b[MobArena] &cモブアリーナが見つかりませんでした")
                            arena.join(sender)
                        }
                        "leave", "l" -> {
                            val arena = sender.arena ?: return@createCmd sender.send("&b[MobArena] &cモブアリーナに入っていません")
                            arena.leave(sender)
                        }
                        "spec", "s" -> {
                            val id = args.getOrNull(1) ?: return@createCmd sender.send("&b[MobArena] &cモブアリーナを入力してください")
                            val arena = getArena(id) ?: return@createCmd sender.send("&b[MobArena] &cモブアリーナが見つかりませんでした")
                            arena.spec(sender)
                        }
                        "ready", "r" -> {
                            val m = sender.arenaPlayer ?: return@createCmd sender.send("&b[MobArena] &cモブアリーナに入っていません")
                            if(m.play){
                                if(m.kit == null) return@createCmd sender.send("&b[MobArena] &cキットを選択していません")
                                if(m.ready) return@createCmd sender.send("&b[MobArena] &c既に準備完了しています")
                                m.ready = true
                            } else {
                                sender.send("&b[MobArena] &cモブアリーナに参加していません")
                            }
                        }
                        "notready", "nr" -> {
                            val m = sender.arenaPlayer ?: return@createCmd sender.send("&b[MobArena] &cモブアリーナに入っていません")
                            if(m.play){
                                if(!m.ready) return@createCmd sender.send("&b[MobArena] &cまだ準備完了していません")
                                m.ready = false
                            } else {
                                sender.send("&b[MobArena] &cモブアリーナに参加していません")
                            }
                        }
                        "kit", "k" -> {
                            val m = sender.arenaPlayer ?: return@createCmd sender.send("&b[MobArena] &cモブアリーナに入っていません")
                            if(m.play){
                                val arena = m.arena
                                inventory("&b&lキット選択") {
                                    getKitsWithFilter(arena.kits).forEach {
                                        item(it.icon).event(ClickType.LEFT) {
                                            if(arena.canUseKit(it)){
                                                it.setKit(sender)
                                                m.kit = it.id
                                                arena.showBoard(sender)
                                            } else {
                                                sender.send("&b[MobArena] &cキットの制限人数に達しています")
                                            }
                                        }
                                    }
                                }.open(sender)
                            } else {
                                sender.send("&b[MobArena] &cモブアリーナに参加していません")
                            }
                        }
                        "create", "c" -> {
                            if(!sender.isOp) return@createCmd help()
                            val id = args.getOrNull(1) ?: return@createCmd sender.send("&b[MobArena] &cアリーナIDを入力してください")
                            if(getArena(id) != null) return@createCmd sender.send("&b[MobArena] &c既に存在するアリーナIDです")
                            val config = getConfigFile("MobArena/$id.yml")
                            config.with {
                                set("name", id, false)
                                set("limit.player", 5, false)
                                set("limit.kit", 1, false)
                                set("wave-interval", 200, false)
                                save()
                            }
                            val newArena = MobArenaData(
                                id,
                                id,
                                listOf(),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                listOf(),
                                200,
                                5,
                                1,
                                false,
                                config
                            )
                            val newArenas = arenas.toMutableList()
                            newArenas.add(newArena)
                            arenas = newArenas
                            sender.send("&b[MobArena] &a${id}&fを追加しました")
                        }
                        "edit" -> {
                            if(!sender.isOp) return@createCmd help()
                            val id = args.getOrNull(1) ?: return@createCmd sender.send("&b[MobArena] &cアリーナIDを入力してください")
                            if(getArena(id) == null) return@createCmd sender.send("&b[MobArena] &cモブアリーナが見つかりませんでした")
                            editMode[sender.uniqueId] = id
                            sender.send("&b[MobArena] &a${id}&fの編集モードに変更しました")
                        }
                        "start" -> {
                            if(!sender.isOp) return@createCmd help()
                            val id = args.getOrNull(1) ?: return@createCmd sender.send("&b[MobArena] &cアリーナIDを入力してください")
                            val arena = getArena(id) ?: return@createCmd sender.send("&b[MobArena] &cモブアリーナが見つかりませんでした")
                            if(arena.status != MobArenaStatus.WaitReady) return@createCmd sender.send("&b[MobArena] &c準備待機中のアリーナではありません")
                            arena.start()
                            sender.send("&b[MobArena] &a${id}&fのゲームを強制的に始めました")
                        }
                        "end" -> {
                            if(!sender.isOp) return@createCmd help()
                            val id = args.getOrNull(1) ?: return@createCmd sender.send("&b[MobArena] &cアリーナIDを入力してください")
                            if(id.toLowerCase() == "all"){
                                allEnd()
                                sender.send("&b[MobArena] &f全てのアリーナを強制終了しました")
                            } else {
                                val arena = getArena(id) ?: return@createCmd sender.send("&b[MobArena] &cモブアリーナが見つかりませんでした")
                                arena.end(true)
                                sender.send("&b[MobArena] &a${id}&fのゲームを強制的終了しました")
                            }
                        }
                        else -> {
                            help()
                        }
                    }
                } else {
                    val editArena = getArena(edit) ?: return@createCmd sender.send("&b[MobArena] &c編集中のアリーナが見つかりませんでした")
                    when(args.whenIndex(0)){
                        "name" -> {
                            val name = args.getOrNull(1) ?: return@createCmd sender.send("&b[MobArena] &c新しい名前を入力してください")
                            editArena.name = name
                            sender.send("&b[MobArena] &a${edit}&fの名前を&a${name}&fに変更しました")
                        }
                        "lobby", "play", "spec" -> {
                            when(args.getOrNull(1)){
                                "area" -> {
                                    val pos = sender.fawePlayer.selection
                                    val region = Region.fromNullable(pos.world, pos.minimumPoint, pos.maximumPoint)
                                    if(region != null){
                                        when(args.whenIndex(0)){
                                            "lobby" -> {
                                                editArena.lobby = region
                                                sender.send("&b[MobArena] &a${edit}&fのロビーの範囲を設定しました")
                                            }
                                            "play" -> {
                                                editArena.play = region
                                                sender.send("&b[MobArena] &a${edit}&fの戦闘の範囲を設定しました")
                                            }
                                            "spec" -> {
                                                editArena.spec = region
                                                sender.send("&b[MobArena] &a${edit}&fの観戦の範囲を設定しました")
                                            }
                                        }
                                    } else {
                                        sender.send("&b[MobArena] &c二箇所選択していません")
                                    }
                                }
                                "spawn" -> {
                                    when(args.whenIndex(0)){
                                        "lobby" -> {
                                            editArena.lobbySpawn = sender.location
                                            sender.send("&b[MobArena] &a${edit}&fのロビーのスポーン地点を設定しました")
                                        }
                                        "play" -> {
                                            editArena.playerSpawn = sender.location
                                            sender.send("&b[MobArena] &a${edit}&fの戦闘のスポーン地点を設定しました")
                                        }
                                        "spec" -> {
                                            editArena.specSpawn = sender.location
                                            sender.send("&b[MobArena] &a${edit}&fの観戦のスポーン地点を設定しました")
                                        }
                                    }
                                }
                                else -> {
                                    sender.send("""
                                        &b[MobArena] &fコマンド
                                        &7- &a/ma-debug ${args.whenIndex(0)} area &7範囲を設定をします
                                        &7- &a/ma-debug ${args.whenIndex(0)} spawn &7スポーン地点を設定をします
                                    """.trimIndent())
                                }
                            }
                        }
                        "spawn" -> {
                            when(args.whenIndex(1)){
                                "list" -> {
                                    sender.send("&b[MobArena] &fスポーン地点一覧")
                                    editArena.mobSpawn.map { CustomLocation(it).toString() }.forEach { loc ->
                                        sender.send("&7- &a$loc")
                                    }
                                }
                                "add" -> {
                                    val list = editArena.mobSpawn.toMutableList()
                                    list.add(sender.location)
                                    editArena.mobSpawn = list
                                    sender.send("&b[MobArena] &fモンスターのスポーン地点に &a${CustomLocation(sender.location)} &fを追加しました")
                                }
                                else -> {
                                    sender.send("""
                                        &b[MobArena] &fコマンド
                                        &7- &a/ma-debug spawn list &7モンスターのスポーン地点の一覧を表示します
                                        &7- &a/ma-debug spawn add &7モンスターのスポーン地点を追加します
                                    """.trimIndent())
                                }
                            }
                        }
                        "limit" -> {
                            when(args.whenIndex(1)){
                                "kit" -> {
                                    val rawNum = args.getOrNull(2) ?: return@createCmd sender.send("&b[MobArena] &f現在のキット制限人数: &a${editArena.kitLimit}人")
                                    val num = rawNum.toIntOrNull() ?: return@createCmd sender.send("&b[MobArena] &c整数を入力してください")
                                    editArena.kitLimit = num
                                    sender.send("&b[MobArena] &fキットの制限人数を&a${num}人&fに変更しました")
                                }
                                "player" -> {
                                    val rawNum = args.getOrNull(2) ?: return@createCmd sender.send("&b[MobArena] &f現在の参加者制限人数: &a${editArena.playerLimit}人")
                                    val num = rawNum.toIntOrNull() ?: return@createCmd sender.send("&b[MobArena] &c整数を入力してください")
                                    editArena.playerLimit = num
                                    sender.send("&b[MobArena] &f参加者の制限人数を&a${num}人&fに変更しました")
                                }
                                else -> {
                                    sender.send("""
                                        &b[MobArena] &fコマンド
                                        &7- &a/ma-debug limit kit &7キットの制限人数を変更します
                                        &7- &a/ma-debug limit player &7参加者の制限人数を変更します
                                    """.trimIndent())
                                }
                            }
                        }
                        "kit" -> {
                            when(args.whenIndex(1)){
                                "list" -> {
                                    sender.send("&b[MobArena] &fキット一覧 &b制限人数: ${editArena.kitLimit}人")
                                    editArena.kits.forEach { k ->
                                        sender.send("&7- &a$k")
                                    }
                                }
                                "add" -> {
                                    val kit = args.getOrNull(2) ?: return@createCmd sender.send("&b[MobArena] &cキットを入力してください")
                                    if(editArena.containsKit(kit)) return@createCmd sender.send("&b[MobArena] &c既に登録されているキットです")
                                    editArena.addKit(kit)
                                }
                                "remove" -> {
                                    val kit = args.getOrNull(2) ?: return@createCmd sender.send("&b[MobArena] &cキットを入力してください")
                                    if(!editArena.containsKit(kit)) return@createCmd sender.send("&b[MobArena] &c登録されていないキットです")
                                    editArena.remKit(kit)
                                }
                                else -> {
                                    sender.send("""
                                        &b[MobArena] &fコマンド
                                        &7- &a/ma-debug kit list &7キットの一覧を表示します
                                        &7- &a/ma-debug kit add &7キットを追加します
                                        &7- &a/ma-debug kit remove &7キットを削除します
                                    """.trimIndent())
                                }
                            }
                        }
                        "wave" -> {
                            when(args.whenIndex(1)){
                                "interval" -> {
                                    val rawTime = args.getOrNull(2) ?: return@createCmd sender.send("&b[MobArena] &c時間を入力してください")
                                    val time = rawTime.toLongOrNull() ?: return@createCmd sender.send("&b[MobArena] &c整数を入力してください")
                                    editArena.waveInterval = time
                                    sender.send("&b[MobArena] &f間隔を&a${time}&fに変更しました")
                                }
                                else -> {
                                    sender.send("""
                                        &b[MobArena] &fコマンド
                                        &7- &a/ma-debug wave interval &7ウェーブの間隔を変更します
                                    """.trimIndent())
                                }
                            }
                        }
                        "edit" -> {
                            editMode.remove(sender.uniqueId)
                            sender.send("&b[MobArena] &a${edit}&fの編集モードを終了しました")
                        }
                        else -> {
                            sender.send("""
                                &b[MobArena] &fコマンド
                                &7- &a/ma-debug name アリーナ名 &7アリーナの名前を変更します
                                &7- &a/ma-debug lobby &7ロビーの設定をします
                                &7- &a/ma-debug play &7戦闘エリアの設定をします
                                &7- &a/ma-debug spec &7観戦エリアの設定をします
                                &7- &a/ma-debug spawn &7モンスターの湧きを設定します
                                &7- &a/ma-debug limit &7人数制限を設定します
                                &7- &a/ma-debug kit &7キットの設定をします
                                &7- &a/ma-debug wave &7ウェーブの設定をします
                                &7- &a/ma-debug edit &7アリーナの編集モードを終了します
                                """.trimIndent())
                        }
                    }
                }
            }
        }
    }

    private var arenas = listOf<MobArenaData>()

    fun setArenas(mobArenaData: List<MobArenaData>){
        val mutable = arenas.toMutableList()
        mobArenaData.forEach { m ->
            val inh = mutable.firstOrNull { f -> f.id == m.id }
            if(inh != null){
                mutable.remove(inh)
                m.players = inh.players
                m.status = inh.status
                m.mob = inh.mob
                m.wave = inh.wave
                m.mainTask = inh.mainTask
                m.nextWaveTask = inh.nextWaveTask
                m.checkDisTask = inh.checkDisTask
                m.bar = inh.bar
                m.allowStart = inh.allowStart
                m.waitAllKill = inh.waitAllKill
                m.publicChest = inh.publicChest
            }
        }
        arenas = mobArenaData
    }

    val Player.inMobArena get() = arena != null || nowMobArena(this)

    val Player.arenaPlayer: MobArenaPlayer?
        get() {
            arenas.forEach { a ->
                val m = a.getPlayer(this)
                if (m != null) return m
            }
            return null
        }

    val Player.arena get() = arenas.firstOrNull { a -> a.getPlayer(this) != null }

    private val Player.isEditMode get() = editArena != null

    private val Player.editArena get() = editMode[uniqueId]

    private fun getArena(id: String) = arenas.firstOrNull { a -> a.id.toLowerCase() == id.toLowerCase() }

    @EventHandler(ignoreCancelled = true)
    fun on(e: InventoryClickEvent){
        val p = e.whoClicked as? Player ?: return
        if(!p.inMobArena) return
        if(CustomItemStack(e.insertItem).containsLore("&c受け渡し不可")){
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: GuildMemberTeleportEvent){
        val p = e.player
        val t = e.target
        if(p.inMobArena || t.inMobArena) e.isCancelled = true
    }

    @EventHandler
    fun on(e: GuildWarStartEvent){
        val g = e.guild
        val w = g.warGuild ?: return
        w.removeMember(w.getMember().filter { m -> m.player.inMobArena })
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: PlayerMoveEvent){
        val p = e.player ?: return
        val m = p.arenaPlayer ?: return
        if(!m.isAllowMove(e.to)) e.isCancelled = true
    }

    @EventHandler
    fun on(e: PlayerQuitEvent){
        val p = e.player ?: return
        val arena = p.arena ?: return
        arena.leave(p)
    }

    @EventHandler
    fun on(e: PlayerDeathWithCtEvent){
        val p = e.player
        val arena = p.arena ?: return
        e.deathMessage = null
        e.isCancelled = true
        arena.onDeath(p)
    }

    @EventHandler
    fun on(e: EntityDeathEvent){
        val entity = e.entity ?: return
        val arena = arenas.firstOrNull { entity.uniqueId in it.mob } ?: return
        e.droppedExp = 0
        e.drops.clear()
        arena.onKillEntity(entity)
    }

    @EventHandler
    fun on(e: PlayerInteractEvent){
        val p = e.player ?: return
        val b = e.clickedBlock ?: return
        val arena = p.arena ?: return
        e.isCancelled = true
        if(b.type == Material.CHEST && e.action == Action.RIGHT_CLICK_BLOCK){
            p.openInventory(arena.publicChest)
        }
    }

    @EventHandler
    fun on(e: PlayerItemDamageEvent){
        val p = e.player ?: return
        if(p.inMobArena) e.isCancelled = true
    }

    @EventHandler
    fun on(e: EntityDamageByEntityEvent){
        val v = e.entity as? Player ?: return
        val a = (if(e.damager is Player) e.damager as Player else if(e.damager is Projectile) (e.damager as Projectile).shooter as? Player else null) ?: return
        if(v.inMobArena || a.inMobArena) e.isCancelled = true
    }

    @EventHandler
    fun on(e: ItemSpawnEvent){
        val loc = e.location ?: return
        arenas.forEach { f ->
            if(f.play?.inRegion(loc) == true){
                e.isCancelled = true
                return
            }
        }
    }


    @EventHandler(ignoreCancelled = true)
    fun on(e: FoodLevelChangeEvent) {
        val p = e.entity as Player
        if(p.inMobArena) e.isCancelled = true
    }

    @EventHandler
    fun on(e: EntityTargetEvent){
        val entity = e.entity ?: return
        val arena = arenas.firstOrNull { entity.uniqueId in it.mob } ?: return
        if(e.target !is Player) e.target = arena.getLivingPlayers().random().player
    }

    @EventHandler
    fun on(e: PlayerDataEvent){

    }

    private fun allEnd(){
        arenas.forEach {
            it.end(true)
        }
    }

    fun onDisable(){
        allEnd()
    }
}