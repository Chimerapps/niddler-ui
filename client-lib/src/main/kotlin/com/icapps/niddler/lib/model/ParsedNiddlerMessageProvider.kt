package com.icapps.niddler.lib.model

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.storage.NiddlerMessageStorage
import com.icapps.niddler.lib.utils.LruCache
import com.icapps.niddler.lib.utils.ScalingThreadPoolExecutor
import java.util.LinkedHashMap
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class ParsedNiddlerMessageProvider(private val mainDispatcher: (() -> Unit) -> Unit,
                                   private val bodyParser: NiddlerMessageBodyParser,
                                   private val niddlerMessageContainer: NiddlerMessageContainer) {

    private companion object {
        private const val MAX_LRU_SIZE = 200
    }

    private val lruCache = LruCache<String, AsyncMemoizer<ParsedNiddlerMessage>>(MAX_LRU_SIZE)

    fun clean() {
        synchronized(lruCache) {
            lruCache.clear()
        }
    }

    fun provideParsedMessage(messageInfo: NiddlerMessageInfo): ObservableFuture<ParsedNiddlerMessage> {
        val message = niddlerMessageContainer.load(messageInfo) ?: return CompletableObservableFuture(mainDispatcher)

        synchronized(lruCache) {
            return lruCache.getOrPut(message.messageId) { AsyncMemoizer(mainDispatcher) { bodyParser.parseBody(message) } }.future
        }
    }

    fun provideParsedMessage(message: NiddlerMessage): ObservableFuture<ParsedNiddlerMessage> {
        synchronized(lruCache) {
            return lruCache.getOrPut(message.messageId) { AsyncMemoizer(mainDispatcher) { bodyParser.parseBody(message) } }.future
        }
    }
}

interface ObservableFuture<T> {
    fun observe(onData: (T) -> Unit): ObservingToken

    fun get(): T
}

interface ObservingToken {
    fun stopObserving()
}

private interface Memoizer<T> {
    val future: ObservableFuture<T>
}

private class AsyncMemoizer<T>(mainDispatcher: (() -> Unit) -> Unit, private val futureCreator: () -> T) : Memoizer<T> {

    companion object {
        private val exexutor = ScalingThreadPoolExecutor(0, 2, 2, TimeUnit.MINUTES)
    }

    private val lock = Any()
    private var computing = false
    private val delegateFuture = CompletableObservableFuture<T>(mainDispatcher)

    override val future: ObservableFuture<T>
        get() {
            synchronized(lock) {
                if (!computing) {
                    computing = true
                    doCompute()
                }
            }
            return delegateFuture
        }

    private fun doCompute() {
        exexutor.execute {
            try {
                val result = futureCreator()
                delegateFuture.complete(result)
            } catch (e: Throwable) {
            }
        }
    }
}

private class CompletableObservableFuture<T>(private val mainDispatcher: (() -> Unit) -> Unit) : ObservableFuture<T> {

    private companion object {
        val voidToken = object : ObservingToken {
            override fun stopObserving() {
            }
        }
    }

    private val lock = Any()
    private var data: T? = null
    private var dataSet = false
    private val listeners = mutableMapOf<String, (T) -> Unit>()
    private val interalFuture = CompletableFuture<T>()

    @Suppress("UNCHECKED_CAST")
    override fun observe(onData: (T) -> Unit): ObservingToken {
        synchronized(lock) {
            if (dataSet) {
                onData(data as T)
                return voidToken
            }

            val token = UUID.randomUUID().toString()
            listeners[token] = onData
            return object : ObservingToken {
                override fun stopObserving() {
                    synchronized(lock) {
                        listeners.remove(token)
                    }
                }
            }
        }
    }

    fun complete(data: T) {
        interalFuture.complete(data)
        synchronized(lock) {
            dataSet = true
            this.data = data
            mainDispatcher.invoke {
                synchronized(lock) {
                    for (listener in listeners.values) {
                        listener(data)
                    }
                    listeners.clear()
                }
            }
        }
    }

    override fun get(): T {
        return interalFuture.get()
    }
}