package me.syari.sec_story.shop.player

import org.bukkit.Location
import org.bukkit.OfflinePlayer

class PlayerShopData(id: String, val player: OfflinePlayer, loc: Location): PlayerShopBase(id, loc, "&6&lショップ " + "&a${player.name}", player) {

}