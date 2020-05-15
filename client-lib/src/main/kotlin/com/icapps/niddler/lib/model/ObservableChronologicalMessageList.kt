package com.icapps.niddler.lib.model

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.storage.NiddlerMessageStorage
import com.icapps.niddler.lib.utils.ObservableMutableList
import java.lang.ref.WeakReference

class ObservableChronologicalMessageList(private val storage: NiddlerMessageContainer) {

    companion object {
        internal data class InsertPosition(val index: Int, val isDuplicateTimeStamp: Boolean)

        internal fun calculateInsertPosition(message: NiddlerMessageInfo, list: List<NiddlerMessageInfo>): InsertPosition {
            val size = list.size
            var insertIndex = 0
            var duplicateTimestamps = false
            for (i in 0 until size) {
                val item = list[size - i - 1]
                if (item.timestamp < message.timestamp) {
                    insertIndex = size - i
                    break
                } else if (item.timestamp == message.timestamp) {
                    duplicateTimestamps = true
                    insertIndex = size - i
                    break
                }
            }
            return InsertPosition(insertIndex, duplicateTimestamps)
        }
    }

    private val internalList = ArrayList<NiddlerMessageInfo>()
    private val views = ArrayList<WeakReference<ChronologicalMessagesView>>()

    fun addMessage(message: NiddlerMessageInfo): Boolean {
        synchronized(internalList) {
            val insertPos = calculateInsertPosition(message, internalList)
            //Damn, we found a duplicate timestamp. Check if we have a duplicate message
            if (insertPos.isDuplicateTimeStamp && internalList.indexOfFirst { it.messageId == message.messageId } != -1) {
                return false
            }
            internalList.add(insertPos.index, message)
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

    fun newView(filter: NiddlerMessageStorage.Filter?, messageListener: ObservableMutableList.Observer): ChronologicalMessagesView {
        return ChronologicalMessagesView(messageListener, storage, filter).also {
            synchronized(internalList) {
                it.notifyMessagesChanged(internalList)
            }
            synchronized(views) {
                views += WeakReference(it)
            }
        }
    }

    private fun dispatchToViews(toDispatch: ChronologicalMessagesView.() -> Unit) {
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

    fun isEmpty(): Boolean {
        synchronized(internalList) {
            return internalList.isEmpty()
        }
    }

}

class ChronologicalMessagesView(messageListener: ObservableMutableList.Observer,
                                private val storage: NiddlerMessageContainer,
                                private val filter: NiddlerMessageStorage.Filter?) {

    private val filteredMessages = ObservableMutableList<NiddlerMessageInfo>(ArrayList()).also { it.observer = messageListener }

    val size: Int
        get() = synchronized(this) { filteredMessages.size }

    operator fun get(index: Int): NiddlerMessageInfo? = synchronized(this) { filteredMessages.getOrNull(index) }

    fun notifyMessageInsert(message: NiddlerMessageInfo) {
        synchronized(this) {
            if (filter?.messageFilter(message, storage) == false)
                return

            val insertPos = ObservableChronologicalMessageList.calculateInsertPosition(message, filteredMessages)

            filteredMessages.add(insertPos.index, message)
        }
    }

    fun notifyMessagesCleared() {
        synchronized(this) {
            filteredMessages.clear()
        }
    }

    fun notifyMessagesChanged(newList: List<NiddlerMessageInfo>) {
        synchronized(this) {
            filteredMessages.clear()
            val filter = filter
            if (filter == null)
                filteredMessages.addAll(newList)
            else
                newList.filterTo(filteredMessages) { filter.messageFilter(it, storage) }
        }
    }

    fun indexOf(message: NiddlerMessageInfo): Int {
        return synchronized(this) { filteredMessages.indexOf(message) }
    }

}