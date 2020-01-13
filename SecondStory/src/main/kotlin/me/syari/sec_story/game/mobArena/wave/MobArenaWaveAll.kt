package me.syari.sec_story.game.mobArena.wave

data class MobArenaWaveAll(var base: Int, var per: Int){
    fun getAll(memberSize: Int) = base + per * memberSize
}