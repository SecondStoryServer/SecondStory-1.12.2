package me.syari.sec_story.world.portal

import com.sk89q.worldedit.Vector
import com.sk89q.worldedit.world.World
import me.syari.sec_story.hook.FastAsyncWorldEdit.fawePlayer
import me.syari.sec_story.lib.CustomLocation
import me.syari.sec_story.lib.message.SendMessage
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.lib.command.CreateCommand.element
import me.syari.sec_story.lib.command.CreateCommand.tab
import me.syari.sec_story.lib.config.CreateConfig.config
import me.syari.sec_story.lib.config.CustomConfig
import me.syari.sec_story.plugin.Init
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import java.util.*

object Portal : Listener, Init() {
    override fun init() {
        createCmd("portal",
            tab { element("list", "info", "create", "edit", "delete", "to", "to") },
            tab("info", "edit", "delete"){ element(portals.keys) },
            tab("edit *"){ element("enable", "perm", "to", "needSneak") },
            tab("to"){ element("save", "clear") }
        ){ sender, args ->
            when(args.whenIndex(0)){
                "list" -> {
                    sender.send("&b[Portal] &fポータル一覧")
                    portals.keys.forEach { f ->
                        sender.send("&7- &a$f" to SendMessage.Action(click = SendMessage.ClickType.TypeText to "/portal info $f"))
                    }
                }
                "info" -> {
                    if (args.size < 2) return@createCmd sender.send("&b[Portal] &cポータル名を入力してください")
                    val portal = portals[args[1]] ?: return@createCmd sender.send("&b[Portal] &cポータルが見つかりませんでした")
                    sender.send(
                        """
                        &b[Portal] &fポータル情報
                        &7ポータル名: ${if (portal.enable) "&a" else "&c"}${args[1]}
                        &7必要権限: &a${portal.perm ?: "なし"}
                        &7しゃがみ必要: &a${if(portal.needSneak) "あり" else "なし"}
                        &7ポータル: &a${CustomLocation(portal.min)} &7>> &a${CustomLocation(portal.max)}
                        &7テレポート先: &a${CustomLocation(portal.to).toStringWithYawPitch}
                    """.trimIndent()
                    )
                }
                "create" -> {
                    if(sender is Player){
                        val name = args.getOrNull(1) ?: return@createCmd sender.send("&b[Portal] &cポータル名を入力してください")
                        if(portals.containsKey(name)) return@createCmd sender.send("&b[Portal] &c既に存在するポータルです")
                        val fawePlayer = sender.fawePlayer
                        val pos = fawePlayer.selection
                        val w = pos.world ?: return@createCmd sender.send("&b[Portal] &cワールドが見つかりませんでした")
                        val min = pos.minimumPoint
                        val max = pos.maximumPoint
                        if(min != null && max != null){
                            sender.createPortal(name, w, min, max)
                            sender.loadPortal()
                            sender.send("&b[Portal] &fポータル&a${name}&fを新規作成しました")
                        } else {
                            sender.send("&b[Portal] &c二箇所選択していません")
                        }
                    }
                }
                "edit" -> {
                    val name = args.getOrNull(1) ?: return@createCmd sender.send("&b[Portal] &cポータル名を入力してください")
                    val portal = portals[name] ?: return@createCmd sender.send("&b[Portal] &cポータルが見つかりませんでした")
                    when(args.whenIndex(2)){
                        "enable" -> {
                            portal.enable = !portal.enable
                            sender.send("&b[Portal] &a${name}&fを${if(portal.enable) "&a有効化" else "&c無効化"}&fしました")
                        }
                        "perm" -> {
                            val perm = args.getOrNull(3)
                            portal.perm = perm
                            sender.send("&b[Portal] &a${name}&fの必要権限を&a${perm ?: "なし"}&fに変更しました")
                        }
                        "to" -> {
                            if(sender is Player){
                                val loc = sender.location
                                portal.to = loc
                                sender.send("&b[Portal] &a${name}&fの移動先を&a${CustomLocation(loc).toStringWithYawPitch}&fに変更しました")
                            }
                        }
                        "needsneak" -> {
                            val set = !portal.needSneak
                            portal.needSneak = set
                            sender.send("&b[Portal] &a${name}&fの移動にスニークを必要${if(set) "に" else "なく"}しました")
                        }
                        else -> {
                            sender.send("""
                                &b[Portal] &fコマンド
                                &7- &a/portal edit <Name> enable &7ポータルの有効・無効を切り替えます
                                &7- &a/portal edit <Name> perm <Perm> &7必要権限を変更します
                                &7- &a/portal edit <Name> to &7現在座標を移動先に変更します
                                &7- &a/portal edit <Name> needSneak &7スニークの必要の有無を変更します
                            """.trimIndent())
                        }
                    }
                }
                "delete" -> {
                    val name = args.getOrNull(1) ?: return@createCmd sender.send("&b[Portal] &cポータル名を入力してください")
                    config.set("portal.$name", null)
                    portals.remove(name)
                    sender.send("&b[Portal] &fポータル&a${name}&fを削除しました")
                }
                "to" -> {
                    when(args.whenIndex(1)){
                        "save" -> {
                            if(sender is Player){
                                saveTos[sender.uniqueId] = sender.location
                                sender.send("&b[Portal] &f移動先を事前設定しました")
                            }
                        }
                        "clear" -> {
                            if(sender is Player){
                                saveTos.remove(sender.uniqueId)
                                sender.send("&b[Portal] &f事前設定した移動先を消去しました")
                            }
                        }
                        else -> sender.send("""
                            &b[Portal] &fコマンド
                            &7- &a/portal to save &7新規作成時に使用する移動先を設定します
                            &7- &a/portal to clear &7事前設定した移動先を消去します
                            """.trimIndent())
                    }
                }
                else -> {
                    sender.send("""
                        &b[Portal] &fコマンド
                        &7- &a/portal list &7ポータルの一覧を表示します
                        &7- &a/portal info <Name> &7ポータルの情報を表示します
                        &7- &a/portal create <Name> &7ポータルを新規作成します
                        &7- &a/portal edit <Name> &7ポータルの設定を変更します
                        &7- &a/portal delete <Name> &7ポータルを削除します
                        &7- &a/portal to &7新規作成時に使用する移動先を設定します
                        """.trimIndent())
                }
            }
        }
    }

    /* portal.yml
    first-spawn: World, X, Y, Z
    portal:
      portal-name:
        enable: true
        perm: *
        corner-1: World, X, Y, Z
        corner-2: World, X, Y, Z
        to: World, X, Y, Z, Yaw, Pitch
     */

    private val saveTos = mutableMapOf<UUID, Location>()

    private fun Player.createPortal(name: String, w: World, pos1: Vector, pos2: Vector){
        config.set("portal.$name.corner-1", "${CustomLocation(w, pos1)}", false)
        config.set("portal.$name.corner-2", "${CustomLocation(w, pos2)}", false)
        val loc = saveTos.getOrDefault(uniqueId, location)
        config.set("portal.$name.to", CustomLocation(loc).toStringWithYawPitch, false)
        config.save()
    }

    @EventHandler
    fun on(e: PlayerMoveEvent){
        val f = e.from.toBlockLocation()
        val t = e.to.toBlockLocation()
        if(f != t){
            val p = e.player
            val portal = portals.values.firstOrNull { g -> g.enable && g.canUse(p) && g.inPortal(t)} ?: return
            portal.tp(p)
        }
    }

    var first: Location? = null

    private val portals = mutableMapOf<String, PortalData>()

    lateinit var config: CustomConfig

    fun CommandSender.loadPortal(){
        portals.clear()
        config = config("portal.yml", false) {
            output = this@loadPortal

            val first = getLocation("first-spawn")
            if(first != null){
                Portal.first = first
            } else {
                send("&cPortal - First Spawn Location null")
            }
            getSection("portal")?.forEach { name ->
                val enable = getBoolean("portal.$name.enable", true, sendNotFound = false)
                val perm: String? = getString("portal.$name.perm", false)
                val c1 = getLocation("portal.$name.corner-1")
                val c2 = getLocation("portal.$name.corner-2")
                val to = getLocation("portal.$name.to")
                val needSneak = getBoolean("portal.$name.needSneak", false, sendNotFound = false)
                val cmdOnTp = getStringList("portal.$name.cmd", listOf(), sendNotFound = false)
                if(c1 != null && c2 != null && to != null){
                    portals[name] = PortalData(name, c1, c2, to, enable, perm, needSneak, cmdOnTp)
                } else {
                    send("&cPortal - $name cannot create")
                }
            }
        }
    }
}