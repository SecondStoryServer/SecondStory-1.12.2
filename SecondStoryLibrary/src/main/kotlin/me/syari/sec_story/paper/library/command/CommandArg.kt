package me.syari.sec_story.paper.library.command

class CommandArg(private val array: Array<out String>) {
    operator fun get(index: Int) = array[index]

    fun getOrNull(index: Int) = array.getOrNull(index)

    fun whenIndex(index: Int) = getOrNull(index)?.toLowerCase()

    val size get() = array.size

    val isEmpty get() = size == 0

    val isNotEmpty get() = size != 0

    fun joinToString(separator: CharSequence = ", ") = array.joinToString(separator)

    fun slice(first: Int) = slice(first until size)

    private fun slice(range: IntRange) = array.slice(range)
}