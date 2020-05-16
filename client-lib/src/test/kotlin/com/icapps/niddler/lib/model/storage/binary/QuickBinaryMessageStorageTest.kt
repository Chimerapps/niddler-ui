package com.icapps.niddler.lib.model.storage.binary

import com.icapps.niddler.lib.connection.model.NetworkNiddlerMessage
import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.NiddlerMessageInfo
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class QuickBinaryMessageStorageTest {

    private val request = NetworkNiddlerMessage(
            requestId = UUID.randomUUID().toString(),
            messageId = UUID.randomUUID().toString(),
            statusLine = null,
            statusCode = null,
            timestamp = System.currentTimeMillis(),
            method = "GET",
            url = "http://www.example.com",
            body = "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdCwgc2VkIGRvIGVpdXNtb2QgdGVtcG9yIGluY2lkaWR1bnQgdXQgbGFib3JlIGV0IGRvbG9yZSBtYWduYSBhbGlxdWEuIFV0IGVuaW0gYWQgbWluaW0gdmVuaWFtLCBxdWlzIG5vc3RydWQgZXhlcmNpdGF0aW9uIHVsbGFtY28gbGFib3JpcyBuaXNpIHV0IGFsaXF1aXAgZXggZWEgY29tbW9kbyBjb25zZXF1YXQuIER1aXMgYXV0ZSBpcnVyZSBkb2xvciBpbiByZXByZWhlbmRlcml0IGluIHZvbHVwdGF0ZSB2ZWxpdCBlc3NlIGNpbGx1bSBkb2xvcmUgZXUgZnVnaWF0IG51bGxhIHBhcmlhdHVyLiBFeGNlcHRldXIgc2ludCBvY2NhZWNhdCBjdXBpZGF0YXQgbm9uIHByb2lkZW50LCBzdW50IGluIGN1bHBhIHF1aSBvZmZpY2lhIGRlc2VydW50IG1vbGxpdCBhbmltIGlkIGVzdCBsYWJvcnVt",
            context = listOf("Testing", "123"),
            trace = listOf("Line1", "Line47"),
            headers = LinkedHashMap<String, List<String>>().also {
                it["content-type"] to listOf("application/json")
                it["cookie"] = listOf("one", "two", "three!")
            },
            httpVersion = "http/1.1",
            readTime = 1,
            writeTime = 2,
            waitTime = 3,
            networkRequest = null,
            networkReply = null
    )

    private val response = NetworkNiddlerMessage(
            requestId = UUID.randomUUID().toString(),
            messageId = UUID.randomUUID().toString(),
            statusLine = "OK",
            statusCode = 200,
            timestamp = System.currentTimeMillis(),
            method = null,
            url = null,
            body = "U2VkIHV0IHBlcnNwaWNpYXRpcyB1bmRlIG9tbmlzIGlzdGUgbmF0dXMgZXJyb3Igc2l0IHZvbHVwdGF0ZW0gYWNjdXNhbnRpdW0gZG9sb3JlbXF1ZSBsYXVkYW50aXVtLCB0b3RhbSByZW0gYXBlcmlhbSwgZWFxdWUgaXBzYSBxdWFlIGFiIGlsbG8gaW52ZW50b3JlIHZlcml0YXRpcyBldCBxdWFzaSBhcmNoaXRlY3RvIGJlYXRhZSB2aXRhZSBkaWN0YSBzdW50IGV4cGxpY2Fiby4gTmVtbyBlbmltIGlwc2FtIHZvbHVwdGF0ZW0gcXVpYSB2b2x1cHRhcyBzaXQgYXNwZXJuYXR1ciBhdXQgb2RpdCBhdXQgZnVnaXQ=",
            context = listOf("Testing_", "123_"),
            trace = listOf("Line1_", "Line47_"),
            headers = LinkedHashMap<String, List<String>>().also {
                it["content-type"] to listOf("application/jsonish")
                it["cookie"] = listOf("one_", "two_", "three!_")
            },
            httpVersion = null,
            readTime = 4,
            writeTime = 5,
            waitTime = 6,
            networkRequest = NetworkNiddlerMessage(
                    requestId = UUID.randomUUID().toString(),
                    messageId = UUID.randomUUID().toString(),
                    statusLine = null,
                    statusCode = null,
                    timestamp = System.currentTimeMillis(),
                    method = "GET",
                    url = "http://www.example.com",
                    body = "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdCwgc2VkIGRvIGVpdXNtb2QgdGVtcG9yIGluY2lkaWR1bnQgdXQgbGFib3JlIGV0IGRvbG9yZSBtYWduYSBhbGlxdWEuIFV0IGVuaW0gYWQgbWluaW0gdmVuaWFtLCBxdWlzIG5vc3RydWQgZXhlcmNpdGF0aW9uIHVsbGFtY28gbGFib3JpcyBuaXNpIHV0IGFsaXF1aXAgZXggZWEgY29tbW9kbyBjb25zZXF1YXQuIER1aXMgYXV0ZSBpcnVyZSBkb2xvciBpbiByZXByZWhlbmRlcml0IGluIHZvbHVwdGF0ZSB2ZWxpdCBlc3NlIGNpbGx1bSBkb2xvcmUgZXUgZnVnaWF0IG51bGxhIHBhcmlhdHVyLiBFeGNlcHRldXIgc2ludCBvY2NhZWNhdCBjdXBpZGF0YXQgbm9uIHByb2lkZW50LCBzdW50IGluIGN1bHBhIHF1aSBvZmZpY2lhIGRlc2VydW50IG1vbGxpdCBhbmltIGlkIGVzdCBsYWJvcnVt",
                    context = listOf("Testing", "123"),
                    trace = listOf("Line1", "Line47"),
                    headers = LinkedHashMap<String, List<String>>().also {
                        it["content-type"] to listOf("application/json")
                        it["cookie"] = listOf("one", "two", "three!")
                        it["user-agent"] = listOf("Mario")
                    },
                    httpVersion = "http/1.1",
                    readTime = 1,
                    writeTime = 2,
                    waitTime = 3,
                    networkRequest = null,
                    networkReply = null
            ),
            networkReply = NetworkNiddlerMessage(
                    requestId = UUID.randomUUID().toString(),
                    messageId = UUID.randomUUID().toString(),
                    statusLine = "OK",
                    statusCode = 200,
                    timestamp = System.currentTimeMillis(),
                    method = null,
                    url = null,
                    body = "U2VkIHV0IHBlcnNwaWNpYXRpcyB1bmRlIG9tbmlzIGlzdGUgbmF0dXMgZXJyb3Igc2l0IHZvbHVwdGF0ZW0gYWNjdXNhbnRpdW0gZG9sb3JlbXF1ZSBsYXVkYW50aXVtLCB0b3RhbSByZW0gYXBlcmlhbSwgZWFxdWUgaXBzYSBxdWFlIGFiIGlsbG8gaW52ZW50b3JlIHZlcml0YXRpcyBldCBxdWFzaSBhcmNoaXRlY3RvIGJlYXRhZSB2aXRhZSBkaWN0YSBzdW50IGV4cGxpY2Fiby4gTmVtbyBlbmltIGlwc2FtIHZvbHVwdGF0ZW0gcXVpYSB2b2x1cHRhcyBzaXQgYXNwZXJuYXR1ciBhdXQgb2RpdCBhdXQgZnVnaXQ=",
                    context = listOf("Testing_", "123_"),
                    trace = listOf("Line1_", "Line47_"),
                    headers = LinkedHashMap<String, List<String>>().also {
                        it["content-type"] to listOf("application/jsonish")
                        it["cookie"] = listOf("one_", "two_", "three!_")
                        it["serverId"] = listOf("ServerId")
                    },
                    httpVersion = null,
                    readTime = 4,
                    writeTime = 5,
                    waitTime = 6,
                    networkRequest = null,
                    networkReply = null
            )
    )

    private val requestAsInfo = NiddlerMessageInfo.fromMessage(request)
    private val responseAsInfo = NiddlerMessageInfo.fromMessage(response)

    @Test
    fun addMessage() {
        val storage = QuickBinaryMessageStorage()
        storage.addMessage(request)
        storage.addMessage(response)

        assertDeepishEquals(request, storage.loadMessage(requestAsInfo)!!)
        assertDeepishEquals(response, storage.loadMessage(responseAsInfo)!!)
    }

    @Test
    fun readHeaders() {
        val storage = QuickBinaryMessageStorage()
        storage.addMessage(request)
        storage.addMessage(response)

        assertEquals(request.headers, storage.loadMessageHeaders(requestAsInfo))
        assertEquals(response.headers, storage.loadMessageHeaders(responseAsInfo))
        assertEquals(response.networkRequest!!.headers, storage.loadMessageHeaders(responseAsInfo.networkRequest!!))
        assertEquals(response.networkReply!!.headers, storage.loadMessageHeaders(responseAsInfo.networkReply!!))
    }

    private fun assertDeepishEquals(expected: NiddlerMessage, got: NiddlerMessage) {
        assertEquals(expected.messageId, got.messageId)
        assertEquals(expected.requestId, got.requestId)
        assertEquals(expected.timestamp, got.timestamp)
        assertEquals(expected.url, got.url)
        assertEquals(expected.method, got.method)
        assertEquals(expected.body, got.body)
        assertEquals(expected.headers, got.headers)
        assertEquals(expected.statusCode, got.statusCode)
        assertEquals(expected.statusLine, got.statusLine)
        assertEquals(expected.writeTime, got.writeTime)
        assertEquals(expected.readTime, got.readTime)
        assertEquals(expected.waitTime, got.waitTime)
        assertEquals(expected.httpVersion, got.httpVersion)
        assertEquals(expected.trace, got.trace)
        assertEquals(expected.context, got.context)
    }

}