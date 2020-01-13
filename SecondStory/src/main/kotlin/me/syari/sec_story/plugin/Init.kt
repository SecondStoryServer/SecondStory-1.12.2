package me.syari.sec_story.plugin

import me.syari.sec_story.bungee.Bungee
import me.syari.sec_story.data.SaveData
import me.syari.sec_story.game.event.Contest
import me.syari.sec_story.game.kit.GameKit
import me.syari.sec_story.game.mobArena.MobArena
import me.syari.sec_story.game.summonArena.MobPoint
import me.syari.sec_story.game.summonArena.SummonArena
import me.syari.sec_story.guild.Guild
import me.syari.sec_story.guild.quest.GuildQuest
import me.syari.sec_story.guild.war.GuildWar
import me.syari.sec_story.home.Home
import me.syari.sec_story.hook.MyPet
import me.syari.sec_story.hook.MythicArtifact
import me.syari.sec_story.hook.mythicMobs.CustomMechanic
import me.syari.sec_story.item.ItemPost
import me.syari.sec_story.item.Vote
import me.syari.sec_story.item.code.ItemCode
import me.syari.sec_story.lib.CreateBossBar
import me.syari.sec_story.lib.event.CustomEventListener
import me.syari.sec_story.lib.inv.CreateInventory
import me.syari.sec_story.measure.*
import me.syari.sec_story.message.Chat
import me.syari.sec_story.perm.Permission
import me.syari.sec_story.player.Donate
import me.syari.sec_story.player.Hide
import me.syari.sec_story.player.Money
import me.syari.sec_story.player.Time
import me.syari.sec_story.plugin.Plugin.plugin
import me.syari.sec_story.rank.Ranks
import me.syari.sec_story.rpg.RPG
import me.syari.sec_story.server.*
import me.syari.sec_story.shop.Shops
import me.syari.sec_story.shop.player.PlayerShop
import me.syari.sec_story.tour.Tour
import me.syari.sec_story.trade.Trade
import me.syari.sec_story.world.AllowWorld
import me.syari.sec_story.world.portal.Portal
import me.syari.sec_story.world.spawn.SpawnCmd
import org.bukkit.event.Listener

open class Init {
    companion object {
        fun register(){
            listOf(
                Portal, Chat, SaveData, Guild, GuildWar, CreateBossBar,
                CreateInventory, Hide, Home, Money, Time, Ranks, RPG,
                Commands, Donate, DoubleJump, IPBlackList, MobArena,
                SummonArena, OpenShulker, Repair, Server, Vote, Shops,
                ItemPost, ItemCode, Tour, SummonArena,
                CommandBlock, Trade, GuildQuest, AllowWorld, PlayerShop,
                MyPet, ItemFrameCommand, MobPoint, SpawnCmd, CustomMechanic,
                CustomEventListener, Contest, Bungee, Permission,
                GameKit, OnlyOneCrackShotWeapon, ReduceDamageCauseUnLuck,
                CancelAutoFish, CancelCauseColoredNameItem, CancelHasLoreItemCraft,
                CheckItemChange, LeaveVehicle, MythicArtifact
            ).forEach {
                it.init()
                if(it is Listener){
                    plugin.server.pluginManager.registerEvents(it, plugin)
                }
            }
        }
    }

    open fun init() {}
}