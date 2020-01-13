package me.syari.sec_story.paper.core.rank

import me.syari.sec_story.paper.core.rank.req.RankReq
import me.syari.sec_story.paper.library.code.StringEditor.toColor
import me.syari.sec_story.paper.library.item.CustomItemStack

class Rank(val name: String) {
    var prefix = ""
        set(value) {
            field = value.toColor
        }
    val next = mutableMapOf<String, Set<RankReq>>()
    val ncmd = mutableSetOf<String>()
    val nperm = mutableSetOf<String>()
    val depend = mutableSetOf<String>()
    val desc = mutableListOf<String>()
    val reset = mutableListOf<Pair<String, List<CustomItemStack>>>()
    var summon = 0

    var cmd = setOf<String>()
        private set
    var perm = setOf<String>()
        private set

    fun set() {
        val g = load(name)
        cmd = g.first
        perm = g.second
    }

    private val loadedDepend = mutableMapOf<String, MutableSet<Rank>>()

    private fun load(base: String): Pair<Set<String>, MutableSet<String>> {
        val lcmd = ncmd.toMutableSet()
        val lperm = nperm.toMutableSet()
        depend.forEach { d ->
            val f = Ranks.get(d)
            val loaded = loadedDepend.getOrDefault(base, mutableSetOf())
            if(f !in loaded && f.depend.isNotEmpty()) {
                loaded.add(f)
                loadedDepend[base] = loaded
                val g = f.load(base)
                lcmd.addAll(g.first)
                lperm.addAll(g.second)
            }
            lcmd.addAll(f.ncmd)
            lperm.addAll(f.nperm)
        }
        return Pair(lcmd, lperm)
    }
}