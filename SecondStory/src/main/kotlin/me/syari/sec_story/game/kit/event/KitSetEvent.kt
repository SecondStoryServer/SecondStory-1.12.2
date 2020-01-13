package me.syari.sec_story.game.kit.event

import me.syari.sec_story.game.kit.GameKitData
import me.syari.sec_story.lib.event.CustomEvent
import org.bukkit.entity.Player

class KitSetEvent(val player: Player, val kit: GameKitData): CustomEvent()