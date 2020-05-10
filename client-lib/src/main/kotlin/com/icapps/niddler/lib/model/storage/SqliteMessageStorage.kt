package com.icapps.niddler.lib.model.storage

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.ObservableChronologicalMessageList
import com.icapps.niddler.lib.model.ObservableLinkedMessageList
import com.icapps.niddler.lib.model.storage.sqlite.NiddlerSqliteDatabase
import java.io.Closeable
import java.io.File

class SqliteMessageStorage(file: File) : NiddlerMessageStorage, Closeable {

    private val niddlerSqliteDatabase = NiddlerSqliteDatabase(file)

    override val messagesChronological: ObservableChronologicalMessageList
        get() = TODO("Not yet implemented")
    override val messagesLinked: ObservableLinkedMessageList
        get() = TODO("Not yet implemented")

    override fun close() {
        niddlerSqliteDatabase.close()
    }

    override fun addMessage(message: NiddlerMessage) {
        niddlerSqliteDatabase.insert(message)
    }

    override fun getMessagesWithRequestId(requestId: String): List<NiddlerMessage> {
        return niddlerSqliteDatabase.getMessages(requestId)
    }

    override fun findResponse(message: NiddlerMessage): NiddlerMessage? {
        return niddlerSqliteDatabase.findResponse(message.requestId)
    }

    override fun findRequest(message: NiddlerMessage): NiddlerMessage? {
        return findRequest(message.requestId)
    }

    override fun findRequest(requestId: String): NiddlerMessage? {
        return niddlerSqliteDatabase.findRequest(requestId)
    }

    override fun clear() {
        niddlerSqliteDatabase.clearMessages()
    }

    override fun isEmpty(): Boolean {
        return niddlerSqliteDatabase.count() == 0
    }

}