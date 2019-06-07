package com.icapps.niddler.lib.model

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import java.lang.ref.WeakReference

class ObservableChronologicalMessageList<T : NiddlerMessage>(private val storage: NiddlerMessageStorage<T>) {

    companion object {
        internal data class InsertPosition(val index: Int, val isDuplicateTimeStamp: Boolean)

        internal fun <T : NiddlerMessage> calculateInsertPosition(message: T, list: List<T>): InsertPosition {
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

    private val internalList = ArrayList<T>()
    private val views = ArrayList<WeakReference<ChronologicalMessagesView<T>>>()

    fun addMessage(message: T): Boolean {
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

    fun newView(filter: NiddlerMessageStorage.Filter<T>?, messageListener: ChronologicalMessagesView.MessagesListener): ChronologicalMessagesView<T> {
        return ChronologicalMessagesView(messageListener, storage, filter).also {
            synchronized(internalList) {
                it.notifyMessagesChanged(internalList)
            }
            synchronized(views) {
                views += WeakReference(it)
            }
        }
    }

    private fun dispatchToViews(toDispatch: ChronologicalMessagesView<T>.() -> Unit) {
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

class ChronologicalMessagesView<T : NiddlerMessage>(private val messageListener: MessagesListener,
                                                    private val storage: NiddlerMessageStorage<T>,
                                                    private val filter: NiddlerMessageStorage.Filter<T>?) {

    private val filteredMessages = ArrayList<T>()

    val size: Int
        get() = synchronized(this) { filteredMessages.size }

    operator fun get(index: Int): T? = synchronized(this) { filteredMessages.getOrNull(index) }

    fun notifyMessageInsert(message: T) {
        synchronized(this) {
            if (filter?.messageFilter(message, storage) == true)
                return

            val insertPos = ObservableChronologicalMessageList.calculateInsertPosition(message, filteredMessages)

            filteredMessages.add(insertPos.index, message)
            messageListener.onItemAdded(insertPos.index)
        }
    }

    fun notifyMessagesCleared() {
        synchronized(this) {
            filteredMessages.clear()
            messageListener.onCleared()
        }
    }

    fun notifyMessagesChanged(newList: List<T>) {
        synchronized(this) {
            filteredMessages.clear()
            val filter = filter
            if (filter == null)
                filteredMessages.addAll(newList)
            else
                newList.filterNotTo(filteredMessages) { filter.messageFilter(it, storage) }

            messageListener.onChanged()
        }
    }

    interface MessagesListener {
        fun onChanged()
        fun onItemAdded(index: Int)
        fun onCleared()
    }

}