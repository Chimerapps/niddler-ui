package com.icapps.niddler.lib.model.storage.binary

import com.icapps.niddler.lib.connection.model.NetworkNiddlerMessage
import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.NiddlerMessageInfo
import com.icapps.niddler.lib.model.storage.NiddlerMessageStorage
import com.icapps.niddler.lib.utils.LruCache
import java.io.Closeable
import java.io.DataInput
import java.io.DataOutput
import java.io.File
import java.io.RandomAccessFile
import java.util.LinkedHashMap

/**
 * Message format that combines efficient memory storage with on-disk body storage
 *
 * There is no need for an extensible format here, these files are not meant to survive the process
 */
class QuickBinaryMessageStorage : NiddlerMessageStorage, Closeable {

    private companion object {
        private const val MAX_LRU_SIZE = 20
    }

    private val tempFile = File.createTempFile("ndlr", "dat").also { it.deleteOnExit() }
    private val file = RandomAccessFile(tempFile, "rw")

    private val lruCache = LruCache<String, NiddlerMessage>(MAX_LRU_SIZE)
    private val lruHeadersCache = LruCache<String, Map<String, List<String>>>(MAX_LRU_SIZE)

    private val offsetMap = hashMapOf<String, Long>()
    private var currentOffset = 0L
    private val header = BinaryHeader()

    override fun addMessage(message: NiddlerMessage) {
        synchronized(offsetMap) {
            writeMessage(message)

            message.networkRequest?.let(::writeMessage)
            message.networkReply?.let(::writeMessage)
        }
    }

    override fun allMessages(): List<NiddlerMessage> {
        return emptyList() //Not supported
    }

    override fun loadMessage(message: NiddlerMessageInfo): NiddlerMessage? {
        synchronized(offsetMap) {
            lruCache[message.messageId]?.let { return it }
            val offset = offsetMap[message.messageId] ?: return null

            file.seek(offset)
            val loaded = readMessage(message, nested = false)
            lruCache[message.messageId] = loaded

            return loaded
        }
    }

    override fun loadMessageHeaders(message: NiddlerMessageInfo): Map<String, List<String>>? {
        synchronized(offsetMap) {
            lruHeadersCache[message.messageId]?.let { return it }
            val offset = offsetMap[message.messageId] ?: return null

            file.seek(offset)

            header.read(file)
            val headers = readHeaders(header.numHeaders)
            lruHeadersCache[message.messageId] = headers
            return headers
        }
    }

    override fun clear() {
        synchronized(offsetMap) {
            file.seek(0)
            currentOffset = 0
            offsetMap.clear()
        }
    }

    override fun isEmpty(): Boolean = synchronized(offsetMap) { offsetMap.isEmpty() }

    override fun close() {
        file.close()
        tempFile.delete()
    }

    private fun readMessage(source: NiddlerMessageInfo, nested: Boolean): NiddlerMessage {
        header.read(file)

        val headers = readHeaders(header.numHeaders)
        val traces = if (header.numTraces <= 0) null else readStringList(header.numTraces)
        val context = if (header.numContext <= 0) null else readStringList(header.numContext)
        val body = readOptString(header.bodySize)
        val httpVersion = readOptString()

        return NetworkNiddlerMessage(
                requestId = source.requestId,
                messageId = source.messageId,
                statusLine = source.statusLine,
                statusCode = source.statusCode,
                timestamp = source.timestamp,
                method = source.method,
                url = source.url,
                body = body,
                context = context,
                trace = traces,
                headers = headers,
                httpVersion = httpVersion,
                readTime = header.readTime,
                writeTime = header.writeTime,
                waitTime = header.waitTime,
                networkRequest = if (nested && source.networkRequest != null) readNestedMessage(source.networkRequest) else null,
                networkReply = if (nested && source.networkReply != null) readNestedMessage(source.networkReply) else null
        )
    }

    private fun writeMessage(message: NiddlerMessage) {
        file.seek(currentOffset)
        offsetMap[message.messageId] = currentOffset

        header.numHeaders = message.headers?.size ?: 0
        header.numTraces = message.trace?.size ?: -1
        header.numContext = message.context?.size ?: -1
        header.bodySize = message.body?.length ?: -1
        header.readTime = message.readTime ?: -1
        header.writeTime = message.writeTime ?: -1
        header.waitTime = message.waitTime ?: -1
        header.write(file)

        message.headers?.let(::writeHeaders)
        writeStringList(message.trace, writeSize = false)
        writeStringList(message.context, writeSize = false)
        writeString(message.body, writeSize = false)
        writeString(message.httpVersion, writeSize = true)

        currentOffset = file.length()
    }

    private fun readNestedMessage(source: NiddlerMessageInfo): NiddlerMessage? {
        val offset = offsetMap[source.messageId] ?: return null
        file.seek(offset)
        return readMessage(source, nested = false)
    }

    private fun readHeaders(numHeaders: Int): Map<String, List<String>> {
        val map = LinkedHashMap<String, List<String>>()
        for (i in 0 until numHeaders) {
            map[readString()] = readStringList(file.readInt())
        }
        return map
    }

    private fun writeHeaders(headers: Map<String, List<String>>) {
        headers.forEach { (key, values) ->
            writeString(key, writeSize = true)
            writeStringList(values, writeSize = true)
        }
    }

    private fun readStringList(numValues: Int): List<String> {
        val values = mutableListOf<String>()
        for (k in 0 until numValues) {
            values += readString()
        }
        return values
    }

    private fun writeStringList(items: List<String>?, writeSize: Boolean) {
        if (items == null) {
            if (writeSize)
                file.writeInt(-1)
            return
        }
        if (writeSize)
            file.writeInt(items.size)
        items.forEach { writeString(it, writeSize = true) }
    }

    private fun readString(): String {
        return readOptString(file.readInt())!!
    }

    private fun readOptString(): String? {
        return readOptString(file.readInt())
    }

    private fun readOptString(numBytes: Int): String? {
        if (numBytes < 0) return null
        if (numBytes == 0) return ""

        val bytes = ByteArray(numBytes)
        file.read(bytes)
        return String(bytes, 0, numBytes, Charsets.UTF_8)
    }

    private fun writeString(string: String?, writeSize: Boolean) {
        val bytes = string?.toByteArray(Charsets.UTF_8)
        if (bytes == null) {
            if (writeSize)
                file.writeInt(-1)
            return
        }
        if (writeSize)
            file.writeInt(bytes.size)
        file.write(bytes)
    }
}

private class BinaryHeader(var numHeaders: Int = 0,
                           var numTraces: Int = 0,
                           var numContext: Int = 0,
                           var bodySize: Int = -1,
                           var writeTime: Int = -1,
                           var readTime: Int = -1,
                           var waitTime: Int = -1
) {
    companion object {
        const val HEADER_BINARY_SIZE = 7 * 4 //7 intrs
    }

    fun read(input: DataInput) {
        numHeaders = input.readInt()
        numTraces = input.readInt()
        numContext = input.readInt()
        bodySize = input.readInt()
        writeTime = input.readInt()
        readTime = input.readInt()
        waitTime = input.readInt()
    }

    fun write(output: DataOutput) {
        output.writeInt(numHeaders)
        output.writeInt(numTraces)
        output.writeInt(numContext)
        output.writeInt(bodySize)
        output.writeInt(writeTime)
        output.writeInt(readTime)
        output.writeInt(waitTime)
    }
}