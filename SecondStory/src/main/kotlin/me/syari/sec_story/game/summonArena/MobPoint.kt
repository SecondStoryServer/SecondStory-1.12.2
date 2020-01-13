package me.syari.sec_story.game.summonArena

import me.syari.sec_story.item.ItemPost.addPost
import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.lib.command.CreateCommand.element
import me.syari.sec_story.lib.command.CreateCommand.elementIfOp
import me.syari.sec_story.lib.command.CreateCommand.tab
import me.syari.sec_story.lib.inv.CreateInventory.inventory
import me.syari.sec_story.lib.inv.CreateInventory.open
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.Plugin
import me.syari.sec_story.plugin.SQL
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import java.util.*

object MobPoint : Init(){
    override fun init(){
        createCmd("mob",
            tab { element("rank", "reward", "check") },
            tab("reward"){ sender ->
                elementIfOp(sender, "reset", "send", "check")
            }
        ) { sender, args ->
            when (args.whenIndex(0)) {
                "reward" -> {
                    if(sender.isOp) when(args.whenIndex(1)){
                        "reset" -> return@createCmd sendReward(true)
                        "send" -> return@createCmd sendReward(false)
                        "check" -> {}
                        else -> {
                            return@createCmd sender.send("""
                                &b[Mob] &fコマンド一覧
                                &7- &a/mob reward reset &7討伐ランキングをリセットし、報酬を配布します
                                &7- &a/mob reward send &7討伐ランキングをリセットせず、報酬を配布します
                                &7- &a/mob reward check &7討伐ランキング報酬を確認します
                            """.trimIndent())
                        }
                    }
                    if(sender is Player) sender.checkReward()
                }
                "rank" -> {
                    val page = args.getOrNull(1)?.toIntOrNull() ?: 1
                    if (page < 1) return@createCmd sender.send("&b[Mob] &cページを入力してください")
                    sender.send("&b[Mob] &f討伐数ランキング")
                    getRank(page).forEachIndexed { i, r ->
                        sender.send(
                            "&6${(page - 1) * 10 + i + 1}. &f${Plugin.plugin.server.getOfflinePlayer(r.first)?.name}  &a${String.format(
                                "%,d",
                                r.second
                            )}pt"
                        )
                    }
                }
                "check" -> {
                    if(sender is Player){
                        sender.send("&b[Mob] &fあなたの討伐ポイントは&a${sender.weekPoint}pt&fです")
                    }
                }
                else -> return@createCmd sender.send(
                    """
                    &b[Mob] &fコマンド
                    &7- &a/mob check &7自分の討伐ポイントを表示します
                    &7- &a/mob rank &7討伐ポイントのランキングを表示します
                    &7- &a/mob reward 討伐報酬を表示します
                """.trimIndent())
            }
        }
    }

    private var rewardMaxRank = 1

    private val rankingReward = mutableMapOf<IntRange, List<CustomItemStack>>()

    private val pointReward = mutableMapOf<Int, List<CustomItemStack>>()

    fun clearReward(){
        rankingReward.clear()
        pointReward.clear()
        rewardMaxRank = 1
    }

    fun addRankReward(rank: IntRange, items: List<CustomItemStack>){
        rankingReward[rank] = items
        val last = rank.last
        if(rewardMaxRank < last) rewardMaxRank = last
    }

    fun addPointReward(point: Int, items: List<CustomItemStack>){
        pointReward[point] = items
    }

    private fun sendReward(reset: Boolean){
        getRank(
            0,
            rewardMaxRank
        ).forEachIndexed loop@ { i, f ->
            val rank = i + 1
            val rew = rankingReward.filter { r -> rank in r.key }
            if(rew.isEmpty()) return@loop
            val p = Plugin.plugin.server.getOfflinePlayer(f.first) ?: return@loop
            p.addPost("&b週間討伐ランキング${rank}位報酬", 7, rew.values.first())
            if(p is Player){
                p.send("&b[Mob] &f週間討伐ランキング&a${rank}位&f報酬が届きました")
            }
        }
        if(reset){
            resetPoint()
        }
    }

    private fun resetPoint(){
        SQL.sql {
            executeUpdate("DELETE FROM Story.SummonPoint")
        }
        weekPoints.clear()
    }

    private fun getRank(from: Int, num: Int): List<Pair<UUID, Int>> {
        val ret = mutableListOf<Pair<UUID, Int>>()
        SQL.sql {
            val res =
                executeQuery("SELECT UUID, POINT FROM Story.SummonPoint WHERE POINT > 0 ORDER BY POINT DESC LIMIT $from,  $num;")
            while (res.next()) {
                try {
                    ret.add(Pair(UUID.fromString(res.getString("UUID")), res.getInt("POINT")))
                } catch (ex: IllegalArgumentException) {
                }
            }
        }
        return ret
    }

    private fun getRank(page: Int) = getRank((page - 1) * 10, 10)

    private val weekPoints = mutableMapOf<UUID, Int>()

    var Player.weekPoint: Int
        get(){
            return weekPoints.getOrPut(uniqueId){
                var ret = 0
                SQL.sql {
                    val res = executeQuery("SELECT (POINT) FROM Story.SummonPoint WHERE UUID = '$uniqueId' LIMIT 1")
                    if (res.next()) ret = res.getInt(1)
                }
                ret
            }
        }
        set(value) {
            val point = if(0 < value) value else 0
            weekPoints[uniqueId] = point
            SQL.sql {
                executeUpdate("INSERT INTO Story.SummonPoint VALUES ('$displayName', '$uniqueId', $point) ON DUPLICATE KEY UPDATE POINT = $point")
            }
        }

    fun Player.addWeekPoint(add: Int){
        val point = weekPoint
        val added = point + add
        val items = pointReward.filter { f -> f.key in (point + 1)..added }
        items.forEach { (p, i) ->
            addPost("&b週間討伐ポイント${p}pt達成報酬", 3, i)
            send("&b[Mob] &f週間討伐ポイント${p}ptを達成しました")
        }
        weekPoint = added
    }

    private fun Player.checkReward(){
        inventory("&9&l討伐報酬", 1){
            item(3, Material.IRON_SWORD, "&6討伐ランキング報酬")
                .event(ClickType.LEFT){ checkRankReward() }
            item(5, Material.CHEST, "&6討伐ポイント報酬")
                .event(ClickType.LEFT){ checkPointReward() }
        }.open(this)
    }

    private fun Player.checkRankReward(){
        inventory("&9&l討伐ランキング報酬", 6){
            var index = 0
            rankingReward.forEach{ (rank, items) ->
                val dis = if(rank.first != rank.last) "&a${rank.first}位～${rank.last}位" else "&a${rank.first}位"
                item(index, Material.NAME_TAG, dis)
                    .event(ClickType.LEFT){
                        checkRewardChild("&9&l討伐ランキング $dis", items){ checkRankReward() }
                    }
                index ++
            }
            item(53, Material.BARRIER, "&c戻る")
                .event(ClickType.LEFT) { checkReward() }
        }.open(this)
    }

    private fun Player.checkPointReward(){
        val already = weekPoint
        inventory("&9&l討伐ポイント報酬", 6){
            var index = 0
            pointReward.forEach { (point, items) ->
                val item = CustomItemStack(Material.NAME_TAG, "&a${String.format("%,d", point)}pt")
                if(point <= already) item.setShine()
                item(index, item)
                    .event(ClickType.LEFT){
                        checkRewardChild("&9&l討伐ランキング ${String.format("%,d", point)}pt", items){ checkPointReward() }
                    }
                index ++
            }
            item(53, Material.BARRIER, "&c戻る")
                .event(ClickType.LEFT) { checkReward() }
        }.open(this)
    }

    private fun Player.checkRewardChild(display: String, items: List<CustomItemStack>, back: Player.() -> Unit){
        inventory(display, 1){
            items.forEachIndexed { i, item ->
                val tmp = item.copy()
                tmp.display = "${tmp.display}&a × ${tmp.amount}"
                item(i, tmp)
            }
            item(8, Material.BARRIER, "&c戻る").event(ClickType.LEFT){ back.invoke(this@checkRewardChild) }
        }.open(this)
    }
}