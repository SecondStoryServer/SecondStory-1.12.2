package me.syari.sec_story.paper.library.sql

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

data class MySQL(val host: String, val port: Int, val db: String, val user: String, val pass: String){
    companion object {
        fun create(host: String?, port: Int?, db: String?, user: String?, pass: String?): MySQL? {
            return if(host != null && port != null && db != null && user != null && pass != null){
                MySQL(host, port, db, user, pass)
            } else {
                null
            }
        }
    }

    fun use(command: Statement.() -> Unit): Boolean {
        var connection: Connection? = null
        var statement: Statement? = null
        return try {
            connection = DriverManager.getConnection("jdbc:mysql://$host:$port/$db", user, pass)
            statement = connection.createStatement()
            statement.command()
            true
        } catch(ex: SQLException) {
            false
        } finally {
            statement?.close()
            connection?.close()
        }
    }
}