package com.icapps.niddler.lib.model

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.storage.NiddlerMessageStorage
import com.icapps.niddler.lib.utils.ObservableMutableList
import java.lang.ref.WeakReference

class ObservableLinkedMessageList(private val storage: NiddlerMessageContainer) {

    internal companion object {
        fun addMessage(message: NiddlerMessageInfo, list: MutableList<LinkedMessageHolder>) {
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

        fun insert(entry: LinkedMessageHolder, list: MutableList<LinkedMessageHolder>) {
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

    private val internalList = mutableListOf<LinkedMessageHolder>()
    private val views = ArrayList<WeakReference<ObservableLinkedMessagesView>>()

    fun addMessage(message: NiddlerMessageInfo): Boolean {
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

    operator fun get(requestId: String): List<NiddlerMessageInfo>? {
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

    fun newView(filter: NiddlerMessageStorage.Filter?, rootMessageListener: ObservableMutableList.Observer?): ObservableLinkedMessagesView {
        return ObservableLinkedMessagesView(rootMessageListener, storage, filter).also {
            synchronized(internalList) {
                it.notifyMessagesChanged(internalList)
            }
            synchronized(views) {
                views += WeakReference(it)
            }
        }
    }

    private fun dispatchToViews(toDispatch: ObservableLinkedMessagesView.() -> Unit) {
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

class ObservableLinkedMessagesView(rootMessageListener: ObservableMutableList.Observer?,
                                   private val storage: NiddlerMessageContainer,
                                   private val filter: NiddlerMessageStorage.Filter?) {

    private val filteredMessages = ObservableMutableList<LinkedMessageHolder>(ArrayList()).also { it.observer = rootMessageListener }

    val size: Int
        get() = synchronized(this) { filteredMessages.size }

    fun notifyMessageInsert(message: NiddlerMessageInfo) {
        synchronized(this) {
            if (filter?.messageFilter(message, storage) == false)
                return

            ObservableLinkedMessageList.addMessage(message, filteredMessages)
        }
    }

    operator fun get(index: Int): LinkedMessageHolder? {
        return synchronized(this) { filteredMessages.getOrNull(index) }
    }

    fun notifyMessagesCleared() {
        synchronized(this) {
            filteredMessages.clear()
        }
    }

    internal fun snapshot(): List<LinkedMessageHolder> {
        synchronized(this) {
            return ArrayList(filteredMessages)
        }
    }

    internal fun notifyMessagesChanged(newList: List<LinkedMessageHolder>) {
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

data class LinkedMessageHolder(internal val requestId: String,
                               internal var time: Long,
                               var request: NiddlerMessageInfo?,
                               val responses: ObservableMutableList<NiddlerMessageInfo> = ObservableMutableList(mutableListOf())) {

    internal fun addResponse(message: NiddlerMessageInfo) {
        synchronized(this) {
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

    fun indexOf(child: NiddlerMessageInfo): Int {
        return synchronized(this) { responses.indexOf(child) }
    }

}