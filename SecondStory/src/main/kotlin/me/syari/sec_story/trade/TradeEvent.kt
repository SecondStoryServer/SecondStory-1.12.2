package me.syari.sec_story.trade

import me.syari.sec_story.lib.event.CustomCancellableEvent
import org.bukkit.entity.Player

open class TradeEvent(val player: Player, val partner: Player): CustomCancellableEvent()