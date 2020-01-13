package me.syari.sec_story.paper.library.config.content

interface ConfigContent {
    companion object {
        private val contentList = mutableMapOf<String, (String, List<String>) -> ConfigContent>()

        fun register(vararg pair: Pair<String, (String, List<String>) -> ConfigContent>) {
            pair.forEach {
                contentList[it.first.toLowerCase()] = it.second
            }
        }

        fun getContent(s: String): ConfigContent {
            val split = s.split("\\s+".toRegex())
            return contentList[split[0].toLowerCase()]?.invoke(s, split) ?: ConfigContentError("type is not found")
        }
    }
}