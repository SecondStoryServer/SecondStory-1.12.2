package me.syari.sec_story.plugin

import me.syari.sec_story.plugin.Plugin.error
import me.syari.sec_story.plugin.Plugin.info
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

object SQL {
    var host: String? = null
    var port: Int? = null
    var db: String? = null
    var user: String? = null
    var pass: String? = null

    fun load(){
        val res = sql {
            executeUpdate("CREATE TABLE IF NOT EXISTS Home (Player VARCHAR(255), UUID VARCHAR(36), Name VARCHAR(255), World VARCHAR(255), X FLOAT(10, 1), Y FLOAT(10, 1), Z FLOAT(10, 1), PRIMARY KEY(UUID, Name));")
            executeUpdate("CREATE TABLE IF NOT EXISTS Guild (GuildID VARCHAR(36) PRIMARY KEY, Name VARCHAR(255), Leader VARCHAR(255), LeaderUUID VARCHAR(36), Money INT UNSIGNED, FF BIT(1), Point INT UNSIGNED NOT NULL, AltarExp INT UNSIGNED NULL);")
            executeUpdate("CREATE TABLE IF NOT EXISTS GuildArea (GuildID VARCHAR(36), World VARCHAR(255), X INT, Z INT, PRIMARY KEY (World, X, Z));")
            executeUpdate("CREATE TABLE IF NOT EXISTS GuildBuff (GuildID VARCHAR(36), Name VARCHAR(255), Level INT UNSIGNED, Active BIT(1), PRIMARY KEY (GuildID, Name));")
            executeUpdate("CREATE TABLE IF NOT EXISTS PlayerData (Player VARCHAR(255), UUID VARCHAR(36) PRIMARY KEY, Money BIGINT UNSIGNED, Rank VARCHAR(255), PlayTime INT UNSIGNED, Guild VARCHAR(36), WarWin INT UNSIGNED, VoteCount INT UNSIGNED);")
            executeUpdate("CREATE TABLE IF NOT EXISTS Donate (Player VARCHAR(255), UUID VARCHAR(36) PRIMARY KEY, Price MEDIUMINT UNSIGNED, Suffix VARCHAR(255))")
            executeUpdate("CREATE TABLE IF NOT EXISTS SummonPoint (Player VARCHAR(255), UUID VARCHAR(36) PRIMARY KEY, POINT INT UNSIGNED)")
            executeUpdate("CREATE TABLE IF NOT EXISTS Permission (UUID VARCHAR(36), Perm VARCHAR(255), PRIMARY KEY (UUID, Perm))")
        }
        if(res){
            info("データベースの接続に成功しました")
        } else {
            error("データベースの接続に失敗しました")
        }
    }

    fun sql(command: Statement.() -> Unit): Boolean{
        if(host != null && port != null && db != null && user != null && pass != null){
            var connection: Connection? = null
            var statement: Statement? = null
            return try {
                connection = DriverManager.getConnection("jdbc:mysql://$host:$port/$db",
                    user,
                    pass
                )
                statement = connection.createStatement()
                statement.command()
                true
            } catch (ex: SQLException){
                false
            } finally {
                statement?.close()
                connection?.close()
            }
        } else {
            return false
        }
    }
}