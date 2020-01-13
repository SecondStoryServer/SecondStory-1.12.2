package me.syari.sec_story.paper.core.init

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.bungee.Bungee
import me.syari.sec_story.paper.core.chat.Chat
import me.syari.sec_story.paper.core.chat.ChatCommand
import me.syari.sec_story.paper.core.command.CommandCancel
import me.syari.sec_story.paper.core.command.OtherCommand
import me.syari.sec_story.paper.core.data.SaveData
import me.syari.sec_story.paper.core.data.SaveDataEvent
import me.syari.sec_story.paper.core.donate.Donate
import me.syari.sec_story.paper.core.game.event.Contest
import me.syari.sec_story.paper.core.game.kit.GameKit
import me.syari.sec_story.paper.core.game.mobArena.MobArena
import me.syari.sec_story.paper.core.game.mobArena.MobArenaEvent
import me.syari.sec_story.paper.core.game.summonArena.MobPoint
import me.syari.sec_story.paper.core.game.summonArena.SummonArena
import me.syari.sec_story.paper.core.game.summonArena.SummonArenaEvent
import me.syari.sec_story.paper.core.guild.Guild
import me.syari.sec_story.paper.core.guild.GuildCommand
import me.syari.sec_story.paper.core.guild.altar.buff.GuildBuff
import me.syari.sec_story.paper.core.guild.quest.GuildQuest
import me.syari.sec_story.paper.core.guild.war.GuildWar
import me.syari.sec_story.paper.core.hide.Hide
import me.syari.sec_story.paper.core.hide.HideEvent
import me.syari.sec_story.paper.core.home.Home
import me.syari.sec_story.paper.core.hook.MyPet
import me.syari.sec_story.paper.core.hook.MythicArtifact
import me.syari.sec_story.paper.core.ip.IPBlackList
import me.syari.sec_story.paper.core.itemCode.ItemCode
import me.syari.sec_story.paper.core.itemFrame.ItemFrameCommand
import me.syari.sec_story.paper.core.itemPost.ItemPost
import me.syari.sec_story.paper.core.measure.*
import me.syari.sec_story.paper.core.perm.Permission
import me.syari.sec_story.paper.core.player.Money
import me.syari.sec_story.paper.core.player.PlayerInfo
import me.syari.sec_story.paper.core.player.Time
import me.syari.sec_story.paper.core.rank.RankEvent
import me.syari.sec_story.paper.core.rank.Ranks
import me.syari.sec_story.paper.core.rpg.RPG
import me.syari.sec_story.paper.core.rpg.RPGEvent
import me.syari.sec_story.paper.core.server.DoubleJump
import me.syari.sec_story.paper.core.server.OpenShulker
import me.syari.sec_story.paper.core.server.Repair
import me.syari.sec_story.paper.core.server.Server
import me.syari.sec_story.paper.core.shop.Shops
import me.syari.sec_story.paper.core.shop.player.PlayerShop
import me.syari.sec_story.paper.core.tour.Tour
import me.syari.sec_story.paper.core.trade.Trade
import me.syari.sec_story.paper.core.vote.Vote
import me.syari.sec_story.paper.core.vote.VoteEvent
import me.syari.sec_story.paper.core.world.AllowWorld
import me.syari.sec_story.paper.core.world.portal.Portal
import me.syari.sec_story.paper.core.world.portal.PortalEvent
import me.syari.sec_story.paper.core.world.spawn.SpawnCmd
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.init.FunctionInit

object Init {
    fun register() {
        FunctionInit.register(
            Bungee,
            ChatCommand,
            CommandCancel,
            OtherCommand,
            SaveData,
            Donate,
            Contest,
            GameKit,
            MobArena,
            MobPoint,
            SummonArena,
            GuildCommand,
            Hide,
            Home,
            MyPet,
            ItemCode,
            Permission,
            Money,
            Time,
            Ranks,
            RPG,
            Shops,
            PlayerShop,
            Tour,
            Trade,
            Vote,
            Portal,
            SpawnCmd,
            ItemPost,
            PlayerInfo
        )

        EventInit.register(
            plugin,
            Chat,
            CommandCancel,
            SaveDataEvent,
            Donate,
            MobArenaEvent,
            SummonArenaEvent,
            Guild,
            GuildBuff,
            GuildQuest,
            GuildWar,
            HideEvent,
            MythicArtifact,
            IPBlackList,
            CancelAutoFish,
            CancelCauseColoredNameItem,
            CancelHasLoreItemCraft,
            CheckItemChange,
            LeaveVehicle,
            OnlyOneCrackShotWeapon,
            ReduceDamageCauseUnLuck,
            RankEvent,
            RPGEvent,
            DoubleJump,
            ItemFrameCommand,
            OpenShulker,
            Repair,
            Server,
            Shops,
            PlayerShop,
            Tour,
            VoteEvent,
            AllowWorld,
            PortalEvent
        )
    }
}