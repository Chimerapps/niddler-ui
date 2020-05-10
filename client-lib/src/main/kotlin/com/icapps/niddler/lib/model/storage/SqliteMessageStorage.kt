package com.icapps.niddler.lib.model.storage

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.storage.sqlite.NiddlerSqliteDatabase
import java.io.Closeable
import java.io.File

class SqliteMessageStorage(file: File) : NiddlerMessageStorage, Closeable {

    private val niddlerSqliteDatabase = NiddlerSqliteDatabase(file)

    override fun close() {
        niddlerSqliteDatabase.close()
    }

    override fun addMessage(message: NiddlerMessage) {
        niddlerSqliteDatabase.insert(message)
    }

    override fun allMessages(): List<NiddlerMessage> {
        return niddlerSqliteDatabase.getMessages()
    }

    override fun clear() {
        niddlerSqliteDatabase.clearMessages()
    }

    override fun isEmpty(): Boolean {
        return niddlerSqliteDatabase.count() == 0
    }

}