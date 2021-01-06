package com.icapps.niddler.lib.model.storage

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.NiddlerMessageInfo
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

    override fun loadMessage(message: NiddlerMessageInfo): NiddlerMessage? {
        //Not sure if we actually need this loaded or not. Let's see if it breaks anything by not loading it
        return niddlerSqliteDatabase.getById(messageId = message.messageId, loadNested = false)
    }

    override fun loadMessageHeaders(message: NiddlerMessageInfo): Map<String, List<String>>? {
        return niddlerSqliteDatabase.getById(messageId = message.messageId, loadNested = false)?.headers
    }

    override fun loadMessageMetadata(message: NiddlerMessageInfo): Map<String, String>? {
        return niddlerSqliteDatabase.getById(messageId = message.messageId, loadNested = false)?.metadata
    }

    override fun clear() {
        niddlerSqliteDatabase.clearMessages()
    }

    override fun isEmpty(): Boolean {
        return niddlerSqliteDatabase.count() == 0
    }

}