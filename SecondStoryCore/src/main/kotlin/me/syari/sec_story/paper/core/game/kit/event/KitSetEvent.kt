package me.syari.sec_story.paper.core.game.kit.event

import me.syari.sec_story.paper.core.game.kit.GameKitData
import me.syari.sec_story.paper.library.event.CustomEvent
import org.bukkit.entity.Player

class KitSetEvent(val player: Player, val kit: GameKitData): CustomEvent()