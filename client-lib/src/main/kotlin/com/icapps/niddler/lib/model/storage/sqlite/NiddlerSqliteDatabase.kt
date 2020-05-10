package com.icapps.niddler.lib.model.storage.sqlite

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.icapps.niddler.lib.connection.model.NetworkNiddlerMessage
import com.icapps.niddler.lib.connection.model.NiddlerMessage
import java.io.Closeable
import java.io.File
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

internal class NiddlerSqliteDatabase(file: File) : Closeable {

    private companion object {
        private const val NIDDLER_META_TABLE = "niddler_meta"  //This can never change without breaking compatibility
        private const val NIDDLER_META_KEY_VERSION = "version" //This can never change without breaking compatibility
        private const val STORAGE_VERSION = 1
        private const val NIDDLER_MESSAGE_TABLE = "niddler_messages"

        init {
            Class.forName("org.sqlite.JDBC")
        }
    }

    private val connection = DriverManager.getConnection("jdbc:sqlite:${file.absolutePath}")
    private val gson = GsonBuilder().create()

    init {
        exec("CREATE TABLE IF NOT EXISTS $NIDDLER_META_TABLE (key TEXT PRIMARY KEY NOT NULL, value TEXT)")

        val version = getSingleValue("SELECT value FROM $NIDDLER_META_TABLE WHERE key='$NIDDLER_META_KEY_VERSION'", ResultSet::getInt)
        if (version == null) {
            initNew()
        } else if (version < STORAGE_VERSION) {
            upgradeFrom(version)
        }
    }

    override fun close() {
        connection.close()
    }

    fun insert(message: NiddlerMessage) {
        connection.prepareStatement("INSERT OR IGNORE INTO $NIDDLER_MESSAGE_TABLE VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)").use {
            bind(message, topLevel = true, preparedStatement = it)
            it.executeUpdate()
            it.clearParameters()
            message.networkRequest?.let { networkRequest ->
                bind(networkRequest, topLevel = false, preparedStatement = it)
                it.executeUpdate()
            }
            message.networkReply?.let { networkReply ->
                bind(networkReply, topLevel = false, preparedStatement = it)
                it.executeUpdate()
            }
        }
    }

    fun clearMessages() {
        exec("DELETE * FROM $NIDDLER_MESSAGE_TABLE")
    }

    fun count(): Int {
        return getSingleValue("SELECT COUNT(*) FROM $NIDDLER_MESSAGE_TABLE WHERE topLevel != 0", ResultSet::getInt) ?: 0
    }

    fun getById(messageId: String, nested: Boolean = true): NiddlerMessage? {
        return getRow("SELECT * FROM $NIDDLER_MESSAGE_TABLE WHERE messageId=? AND statusCode=NULL", messageId, messageId)?.let { row ->
            mapRow(row, nested)
        }
    }

    fun findRequest(requestId: String): NiddlerMessage? {
        return getRow("SELECT * FROM $NIDDLER_MESSAGE_TABLE WHERE requestId=? AND statusCode=NULL AND topLevel!=0 LIMIT 1", requestId)?.let { row ->
            mapRow(row, nested = true)
        }
    }

    fun findResponse(requestId: String): NiddlerMessage? {
        return getRow("SELECT * FROM $NIDDLER_MESSAGE_TABLE WHERE requestId=? AND statusCode!=NULL AND topLevel!=0 LIMIT 1", requestId)?.let { row ->
            mapRow(row, nested = true)
        }
    }

    fun getMessages(requestId: String): List<NiddlerMessage> {
        return mapRows("SELECT * FROM $NIDDLER_MESSAGE_TABLE WHERE requestId=? AND topLevel!=0", { mapRow(it, true) }, requestId).orEmpty()
    }

    private fun mapRow(resultSet: ResultSet, nested: Boolean): NiddlerMessage {
        return NetworkNiddlerMessage(
                requestId = resultSet.getString(1),
                messageId = resultSet.getString(2),
                timestamp = resultSet.getLong(3),
                url = resultSet.getString(4),
                method = resultSet.getString(5),
                body = resultSet.getString(6),
                headers = resultSet.getString(7).fromJson(),
                statusCode = resultSet.optInt(8),
                statusLine = resultSet.getString(9),
                writeTime = resultSet.optInt(10),
                readTime = resultSet.optInt(11),
                waitTime = resultSet.optInt(12),
                httpVersion = resultSet.getString(13),
                networkRequest = resultSet.getString(16)?.let { if (nested) getById(it, nested = false) else null },
                networkReply = resultSet.getString(17)?.let { if (nested) getById(it, nested = false) else null },
                trace = resultSet.getString(14).fromJson(),
                context = resultSet.getString(15).fromJson()
        )
    }

    private fun bind(message: NiddlerMessage, topLevel: Boolean, preparedStatement: PreparedStatement) {
        preparedStatement.setString(1, message.messageId)
        preparedStatement.setString(2, message.requestId)
        preparedStatement.setLong(3, message.timestamp)
        preparedStatement.optSetString(4, message.url)
        preparedStatement.optSetString(5, message.method)
        preparedStatement.optSetString(6, message.body)
        preparedStatement.optSetString(7, message.headers.json())
        preparedStatement.optSetInt(8, message.statusCode)
        preparedStatement.optSetString(9, message.statusLine)
        preparedStatement.optSetInt(10, message.writeTime)
        preparedStatement.optSetInt(11, message.readTime)
        preparedStatement.optSetInt(12, message.waitTime)
        preparedStatement.optSetString(13, message.httpVersion)
        preparedStatement.optSetString(14, message.trace.json())
        preparedStatement.optSetString(15, message.context.json())
        preparedStatement.optSetString(16, message.networkRequest?.messageId)
        preparedStatement.optSetString(17, message.networkReply?.messageId)
        preparedStatement.setBoolean(18, topLevel)
    }

    private fun initNew() {
        exec("REPLACE INTO $NIDDLER_META_TABLE (key, value) VALUES('$NIDDLER_META_KEY_VERSION', $STORAGE_VERSION)")
        exec("CREATE TABLE $NIDDLER_MESSAGE_TABLE (" +
                "messageId TEXT PRIMARY KEY NOT NULL, " +
                "requestId TEXT NOT NULL, " +
                "timestamp INTEGER NOT NULL, " +
                "url TEXT, " +
                "method TEXT, " +
                "body TEXT, " +
                "headers TEXT, " +
                "statusCode INTEGER, " +
                "statusLine TEXT, " +
                "writeTime INTEGER, " +
                "readTime INTEGER, " +
                "waitTime INTEGER, " +
                "httpVersion TEXT, " +
                "traces TEXT, " +
                "context TEXT, " +
                "networkRequestId TEXT, " +
                "networkReplyId TEXT," +
                "topLevel INTEGER" +
                ")")
    }

    private fun upgradeFrom(version: Int) {
        TODO("Not supported")
    }

    private fun exec(sqlStatement: String): Boolean {
        return connection.prepareStatement(sqlStatement).use {
            it.execute()
        }
    }

    private fun <T> getSingleValue(sqlStatement: String, valueGetter: ResultSet.(Int) -> T): T? {
        connection.prepareStatement(sqlStatement).use {
            val set = it.executeQuery()
            if (!set.next()) return null
            return set.valueGetter(1)
        }
    }

    private fun getRow(sqlStatement: String, vararg arguments: Any?): ResultSet? {
        connection.prepareStatement(sqlStatement).use {
            arguments.forEachIndexed { index, value ->
                it.setString(index + 1, value?.toString())
            }
            val set = it.executeQuery()
            if (!set.next()) return null
            return set
        }
    }

    private fun mapRows(sqlStatement: String, mapper: (ResultSet) -> NiddlerMessage, vararg arguments: Any?): List<NiddlerMessage>? {
        connection.prepareStatement(sqlStatement).use {
            arguments.forEachIndexed { index, value ->
                it.setString(index + 1, value?.toString())
            }
            val set = it.executeQuery()

            if (!set.next()) return null
            val results = mutableListOf<NiddlerMessage>()
            do {
                results += mapper(set)
            } while (set.next())
            return results
        }
    }

    private fun <T> List<T>?.json(): String? {
        if (this == null) return null
        return gson.toJson(this)
    }

    private fun <T, U> Map<T, List<U>>?.json(): String? {
        if (this == null) return null
        return gson.toJson(this)
    }

    private fun <T> String?.fromJson(): T? {
        if (this == null) return null
        return gson.fromJson(this, object : TypeToken<T>() {
        }.type)
    }
}

private fun PreparedStatement.optSetString(position: Int, optString: String?) {
    if (optString == null)
        setNull(position, Types.VARCHAR)
    else
        setString(position, optString)
}

private fun PreparedStatement.optSetInt(position: Int, optInt: Int?) {
    if (optInt == null)
        setNull(position, Types.INTEGER)
    else
        setInt(position, optInt)
}

private fun ResultSet.optInt(position: Int): Int? {
    val value = getInt(position)
    if (wasNull()) return null
    return value
}