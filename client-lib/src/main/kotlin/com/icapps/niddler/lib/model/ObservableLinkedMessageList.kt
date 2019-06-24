package com.icapps.niddler.lib.model

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.utils.ObservableMutableList
import java.lang.ref.WeakReference

class ObservableLinkedMessageList<T : NiddlerMessage>(private val storage: NiddlerMessageStorage<T>) {

    internal companion object {
        fun <T : NiddlerMessage> addMessage(message: T, list: MutableList<LinkedMessageHolder<T>>) {
            val messageRequestId = message.requestId
            val existing = list.lastOrNull { it.requestId == messageRequestId }
            if (existing != null) {
                var requiresSort = false
                if (message.isRequest) {
                    existing.request = message
                    requiresSort = true
                } else {
                    if (message.timestamp < existing.time) {
                        existing.time = message.timestamp
                        requiresSort = false
                    }
                    existing.addResponse(message)
                }
                if (requiresSort)
                    list.sortBy { it.time }
            } else {
                val newEntry = LinkedMessageHolder(messageRequestId, message.timestamp, if (message.isRequest) message else null)
                if (!message.isRequest)
                    newEntry.responses.add(message)

                insert(newEntry, list)
            }
        }

        fun <T : NiddlerMessage> insert(entry: LinkedMessageHolder<T>, list: MutableList<LinkedMessageHolder<T>>) {
            if (list.isEmpty()) {
                list += entry
            } else {
                val time = entry.time
                if (list.last().time < time) {
                    list += entry
                } else {
                    val index = list.indexOfLast { it.time > time }
                    if (index == -1) //Can't find any, this should not be the case anyway
                        list.add(entry)
                    else
                        list.add(index, entry)
                }
            }
        }
    }

    private val internalList = mutableListOf<LinkedMessageHolder<T>>()
    private val views = ArrayList<WeakReference<ObservableLinkedMessagesView<T>>>()

    fun addMessage(message: T): Boolean {
        synchronized(internalList) {
            addMessage(message, internalList)

            dispatchToViews { notifyMessageInsert(message) }
        }
        return true
    }

    fun clear() {
        synchronized(internalList) {
            internalList.clear()
            dispatchToViews { notifyMessagesCleared() }
        }
    }

    fun isEmpty(): Boolean {
        synchronized(internalList) {
            return internalList.isEmpty()
        }
    }

    operator fun get(requestId: String): List<T>? {
        return synchronized(internalList) {
            internalList.find { it.requestId == requestId }?.let {
                val request = it.request
                if (request == null)
                    it.responses
                else
                    it.responses + request
            }
        }
    }

    fun newView(filter: NiddlerMessageStorage.Filter<T>?, rootMessageListener: ObservableMutableList.Observer?): ObservableLinkedMessagesView<T> {
        return ObservableLinkedMessagesView(rootMessageListener, storage, filter).also {
            synchronized(internalList) {
                it.notifyMessagesChanged(internalList)
            }
            synchronized(views) {
                views += WeakReference(it)
            }
        }
    }

    private fun dispatchToViews(toDispatch: ObservableLinkedMessagesView<T>.() -> Unit) {
        synchronized(views) {
            val it = views.iterator()
            while (it.hasNext()) {
                val view = it.next().get()
                if (view == null)
                    it.remove()
                else
                    view.toDispatch()
            }
        }
    }

}

class ObservableLinkedMessagesView<T : NiddlerMessage>(rootMessageListener: ObservableMutableList.Observer?,
                                                       private val storage: NiddlerMessageStorage<T>,
                                                       private val filter: NiddlerMessageStorage.Filter<T>?) {

    private val filteredMessages = ObservableMutableList<LinkedMessageHolder<T>>(ArrayList()).also { it.observer = rootMessageListener }

    val size: Int
        get() = synchronized(this) { filteredMessages.size }

    operator fun get(index: Int): LinkedMessage<T>? = synchronized(this) { filteredMessages.getOrNull(index)?.let { LinkedMessage(it.request, it.responses) } }

    fun notifyMessageInsert(message: T) {
        synchronized(this) {
            if (filter?.messageFilter(message, storage) == false)
                return

            ObservableLinkedMessageList.addMessage(message, filteredMessages)
        }
    }

    fun notifyMessagesCleared() {
        synchronized(this) {
            filteredMessages.clear()
        }
    }

    internal fun snapshot(): List<LinkedMessageHolder<T>> {
        synchronized(this) {
            return ArrayList(filteredMessages)
        }
    }

    internal fun notifyMessagesChanged(newList: List<LinkedMessageHolder<T>>) {
        synchronized(this) {
            filteredMessages.clear()
            val filter = filter
            if (filter == null)
                filteredMessages.addAll(newList)
            else
                newList.filterTo(filteredMessages) {
                    val request = it.request
                    if (request == null)
                        false
                    else
                        filter.messageFilter(request, storage)
                }
        }
    }

}

data class LinkedMessage<T : NiddlerMessage>(val request: T?, val responses: List<T>)

internal data class LinkedMessageHolder<T : NiddlerMessage>(val requestId: String,
                                                            var time: Long,
                                                            var request: T?,
                                                            val responses: ObservableMutableList<T> = ObservableMutableList(mutableListOf())) {

    fun addResponse(message: T) {
        if (responses.isEmpty()) {
            responses.add(message)
        } else {
            val time = message.timestamp
            if (responses.last().timestamp < time) {
                responses.add(message)
            } else {
                val index = responses.indexOfLast { it.timestamp > time }
                if (index == -1) //Can't find any, this should not be the case anyway
                    responses.add(message)
                else
                    responses.add(index, message)
            }
        }
    }

}