package com.icapps.niddler.lib.utils

class ObservableMutableList<T>(private val delegate: MutableList<T>) : MutableList<T> by delegate {

    interface Observer {
        fun itemsInserted(startIndex: Int, endIndex: Int)
        fun itemChanged(index: Int)
        fun itemsRemoved(startIndex: Int, endIndex: Int)
    }

    var observer: Observer? = null

    override fun add(element: T): Boolean {
        val position = delegate.size
        val res = delegate.add(element)
        if (res) {
            observer?.itemsInserted(position, position)
        }
        return res
    }

    override fun add(index: Int, element: T) {
        delegate.add(index, element)
        observer?.itemsInserted(index, index)
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val changed = delegate.addAll(index, elements)
        if (changed) {
            observer?.itemsInserted(index, index + elements.size - 1)
        }
        return changed
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val start = size
        val changed = delegate.addAll(elements)
        if (changed) {
            observer?.itemsInserted(start, start + elements.size - 1)
        }
        return changed
    }

    override fun clear() {
        if (isEmpty())
            return
        val oldSize = size
        delegate.clear()
        observer?.itemsRemoved(0, oldSize - 1)
    }

    override fun remove(element: T): Boolean {
        val index = delegate.indexOf(element)
        if (index != -1) {
            removeAt(index)
            return true
        }
        return false
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        var changed = false
        elements.forEach { changed = remove(it) || changed }
        return changed
    }

    override fun removeAt(index: Int): T {
        val removed = delegate.removeAt(index)
        observer?.itemsRemoved(index, index)
        return removed
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        var modified = false
        val e = iterator()
        while (e.hasNext()) {
            if (!elements.contains(e.next())) {
                e.remove()
                modified = true
            }
        }
        return modified
    }

    override fun set(index: Int, element: T): T {
        val old = delegate[index]
        if (old == element)
            return element

        observer?.itemChanged(index)

        return old
    }
}