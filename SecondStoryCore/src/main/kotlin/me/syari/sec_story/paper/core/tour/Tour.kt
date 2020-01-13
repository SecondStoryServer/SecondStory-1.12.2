package me.syari.sec_story.paper.core.tour

import com.elmakers.mine.bukkit.api.event.PreCastEvent
import com.shampaggon.crackshot.events.WeaponDamageEntityEvent
import com.shampaggon.crackshot.events.WeaponPrepareShootEvent
import com.shampaggon.crackshot.events.WeaponTriggerEvent
import me.syari.sec_story.paper.core.command.CommandCancel.CommandAddCause
import me.syari.sec_story.paper.core.command.CommandCancel.addDisAllowCmd
import me.syari.sec_story.paper.core.command.CommandCancel.addIgnoreWildCmd
import me.syari.sec_story.paper.core.command.CommandCancel.clearDisAllowCmd
import me.syari.sec_story.paper.core.guild.event.GuildWarStartEvent
import me.syari.sec_story.paper.core.item.GiveItem.give
import me.syari.sec_story.paper.core.itemFrame.RunCommandItemFrameEvent
import me.syari.sec_story.paper.core.tour.TourConfig.tours
import me.syari.sec_story.paper.core.trade.TradeStartEvent
import me.syari.sec_story.paper.core.world.portal.PortalTeleportEvent
import me.syari.sec_story.paper.library.CreateHelp.createHelp
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.element
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.item.CustomItemStack
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.scheduler.CustomTask
import net.citizensnpcs.api.event.NPCRightClickEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

object Tour: FunctionInit, EventInit {
    private val help = createHelp(
        "Help",
        "/help" to "&7コマンド一覧を表示します",
        "/web" to "&7公式サイトのURLを表示します",
        "/youtube" to "&7公式YoutubeのURLを表示します",
        "/discord" to "&7公式DiscordのURLを表示します",
        "/auth" to "&7公式Discordの認証を行います",
        "/vote" to "&7投票ページのURLを表示します",
        "/money" to "&7所持金を管理するコマンドです",
        "/shop" to "&7ショップを開きます",
        "/tell <Player> <Message>" to "&7プレイヤーにダイレクトメッセージを送信します",
        "/r <Message>" to "&7ダイレクトメッセージに返信します",
        "/guild" to "&7ギルドを管理するコマンドです",
        "/rank" to "&7ランクを管理するコマンドです",
        "/ma-debug" to "&7モブアリーナを管理するコマンドです",
        "/play" to "&7プレイ時間を表示します",
        "/ch" to "&7チャットチャンネルを変更します",
        "/mob" to "&7討伐ポイントを表示します",
        "/post" to "&7アイテムポストを開きます",
        "/code" to "&7コードを使ってアイテムを受けとります",
        "/spawn" to "&7ワールドスポーンに移動します",
        "/home" to "&7ホームを管理するコマンドです &cCitizenから",
        "/trade" to "&7トレードをするためのコマンドです",
        helpCmd = "/help"
    )

    override fun init() {
        createCmd("help") { sender, args ->
            val page = args.getOrNull(0)?.toIntOrNull() ?: 1
            help.send(sender, page)
        }

        createCmd(
            "tour",
            tab { element("start", "ticket") },
            tab("start") { element(tours.mapNotNull { t -> if(! t.hide) t.id else null }) },
            tab("ticket") { element(tours.mapNotNull { t -> if(t.ticket != null) t.id else null }) }) { sender, args ->
            if(sender is Player) {
                val play = sender.getPlay()
                if(play != null) {
                    val number = args.getOrNull(0) ?: return@createCmd
                    if(number != play.number) return@createCmd
                    val id = args.getOrNull(1) ?: return@createCmd
                    val tour = getTour(id) ?: return@createCmd
                    if(tour.canStart(sender)) {
                        sender.end(true)
                        tour.start(sender)
                    }
                } else {
                    when(args.whenIndex(0)) {
                        "start" -> {
                            val id = args.getOrNull(1) ?: return@createCmd
                            val tour = getTour(id) ?: return@createCmd
                            if(tour.canStart(sender)) {
                                tour.start(sender)
                            } else {
                                sender.send("&b[Tour] &cツアーのチケットを持っていません")
                            }
                        }
                        "ticket" -> {
                            val id = args.getOrNull(1) ?: return@createCmd
                            val ticket = getTicket(id)
                            if(ticket != null) {
                                args.getOrNull(2)?.toIntOrNull()?.let { ticket.amount = it }
                                sender.give(ticket, "&6&lチケット &a&l${ticket.display}")
                            }
                        }
                    }
                }
            }
        }
    }

    fun getTour(id: String) = tours.firstOrNull { f -> f.id == id }

    private val nowPlay = mutableListOf<TourPlayer>()

    fun Player.getPlay() = nowPlay.firstOrNull { f -> f.player == uniqueId }

    fun Player.nowPlay() = getPlay() != null

    fun Player.start(id: String, tasks: List<CustomTask>) {
        closeInventory()
        nowPlay.add(TourPlayer(uniqueId, id, tasks))
        addDisAllowCmd(CommandAddCause.Help, "*")
        addIgnoreWildCmd(CommandAddCause.Help, "tour")
    }

    fun Player.end(taskCancel: Boolean) {
        val n = getPlay() ?: return
        if(taskCancel) n.tasks.forEach { t -> t.cancel() }
        nowPlay.remove(n)
        clearDisAllowCmd(CommandAddCause.Help)
        cancelMove.remove(uniqueId)
    }

    fun getTicket(id: String): CustomItemStack? {
        val tour = getTour(id) ?: return null
        return tour.ticket?.copy()
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun on(e: PlayerInteractEvent) {
        val p = e.player ?: return
        if(p.nowPlay()) e.isCancelled = true
    }

    @EventHandler
    fun on(e: WeaponPrepareShootEvent) {
        val p = e.player ?: return
        if(p.nowPlay()) e.isCancelled = true
    }

    @EventHandler
    fun on(e: WeaponDamageEntityEvent) {
        val p = e.player ?: return
        if(p.nowPlay()) e.isCancelled = true
    }

    @EventHandler
    fun on(e: WeaponTriggerEvent) {
        val p = e.player ?: return
        if(p.nowPlay()) e.isCancelled = true
    }

    @EventHandler
    fun on(e: PreCastEvent) {
        val p = e.mage.player ?: return
        if(p.nowPlay()) e.isCancelled = true
    }

    @EventHandler
    fun on(e: TradeStartEvent) {
        val p = e.player
        if(p.nowPlay()) e.isCancelled = true
    }

    @EventHandler
    fun on(e: GuildWarStartEvent) {
        e.guild.warGuild?.member?.forEach { m ->
            m.player.end(true)
        }
    }

    @EventHandler
    fun on(e: EntityDamageEvent) {
        val p = e.entity as? Player ?: return
        if(p.nowPlay()) e.isCancelled = true
    }

    val cancelMove = mutableListOf<UUID>()

    @EventHandler
    fun on(e: PlayerMoveEvent) {
        val p = e.player ?: return
        if(cancelMove.contains(p.uniqueId)) e.isCancelled = true
    }

    @EventHandler
    fun on(e: PlayerQuitEvent) {
        val p = e.player ?: return
        p.end(true)
    }

    @EventHandler
    fun on(e: NPCRightClickEvent) {
        val name = e.npc.name
        val p = e.clicker
        val t = tours.firstOrNull { f -> f.npc == name } ?: return
        if(t.canStart(p)) {
            t.start(p)
        } else {
            p.send("&b[Tour] &cツアーのチケットを持っていません")
        }
    }

    @EventHandler
    fun on(e: PortalTeleportEvent) {
        val p = e.player
        if(p.nowPlay()) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: RunCommandItemFrameEvent) {
        val p = e.player
        if(p.nowPlay()) {
            e.isCancelled = true
        }
    }
}