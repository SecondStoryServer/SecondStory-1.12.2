package me.syari.sec_story.paper.core.trade

import me.syari.sec_story.paper.library.event.CustomCancellableEvent
import org.bukkit.entity.Player

open class TradeEvent(val player: Player, val partner: Player): CustomCancellableEvent()