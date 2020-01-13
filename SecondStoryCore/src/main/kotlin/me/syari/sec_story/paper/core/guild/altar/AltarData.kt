package me.syari.sec_story.paper.core.guild.altar

data class AltarData(val level: Int, val exp: IntRange, val maxWeekly: Int, val maxDaily: Int, val maxMember: Int) {
    fun getDisplay(altarExp: Int): List<String> {
        val req = altarExp - exp.first
        val next = exp.last
        val s = StringBuilder()
        for(i in 0 until 10) {
            if(i < req * 10 / next) {
                s.append("&e█")
            } else {
                s.append("&7█")
            }
        }
        val progress = s.toString()
        return """
                &e&lLv.$level  $progress  &e$req &7/ &e$next
                
                &7デイリークエスト数: &e$maxDaily
                &7ウィークリークエスト数: &e$maxWeekly
                &7最大メンバー数: &e$maxMember
            """.trimIndent().split("\n".toRegex())
    }
}