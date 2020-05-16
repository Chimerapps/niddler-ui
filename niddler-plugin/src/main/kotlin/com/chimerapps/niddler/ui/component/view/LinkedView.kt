package com.chimerapps.niddler.ui.component.view

import com.chimerapps.niddler.ui.component.renderer.LinkedViewCellRenderer
import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.BaseUrlHider
import com.icapps.niddler.lib.model.LinkedMessageHolder
import com.icapps.niddler.lib.model.NiddlerMessageContainer
import com.icapps.niddler.lib.model.NiddlerMessageInfo
import com.icapps.niddler.lib.model.ObservableLinkedMessagesView
import com.icapps.niddler.lib.model.ParsedNiddlerMessageProvider
import com.icapps.niddler.lib.model.storage.NiddlerMessageStorage
import com.icapps.niddler.lib.utils.ObservableMutableList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeSelectionModel

class LinkedView(messageContainer: NiddlerMessageContainer,
                 private val selectionListener: MessageSelectionListener,
                 private val parsedNiddlerMessageProvider: ParsedNiddlerMessageProvider,
                 private val baseUrlHideListener: BaseUrlHideListener) : JPanel(BorderLayout()), MessagesView {

    private val model = LinkedTreeModel(messageContainer)
    private val tree = Tree(model).also {
        it.isRootVisible = false
        it.showsRootHandles = true
        it.isEditable = false
        it.dragEnabled = false

        it.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        it.cellRenderer = LinkedViewCellRenderer(parsedNiddlerMessageProvider)
        it.rowHeight = 32
        it.selectionModel.addTreeSelectionListener { _ ->
            when (val selection = it.selectionPath?.lastPathComponent) {
                is LinkedResponseNode -> selectionListener.onMessageSelectionChanged(selection.response)
                is LinkedRootNode -> selectionListener.onMessageSelectionChanged(selection.entry.request)
                null -> selectionListener.onMessageSelectionChanged(null)
            }
        }
    }

    init {
        add(JBScrollPane(tree), BorderLayout.CENTER)
    }

    override var urlHider: BaseUrlHider? = null //TODO

    override var filter: NiddlerMessageStorage.Filter?
        get() = model.filter
        set(value) {
            model.filter = value
        }

    private val scrollToEndListener = object : ComponentAdapter() {
        override fun componentResized(e: ComponentEvent?) {
            //TODO tree.scrollRectToVisible(tree.getRo(tableView.rowCount - 1, 0, true))
        }
    }

    override fun onMessagesUpdated() {
    }

    override fun updateScrollToEnd(scrollToEnd: Boolean) {
        tree.removeComponentListener(scrollToEndListener)
        if (scrollToEnd)
            tree.addComponentListener(scrollToEndListener)
    }
}

private class LinkedTreeModel(private val messageContainer: NiddlerMessageContainer)
    : DefaultTreeModel(DefaultMutableTreeNode()), ObservableMutableList.Observer {

    internal val treeRoot = root as DefaultMutableTreeNode

    var filter: NiddlerMessageStorage.Filter? = null
        set(value) {
            if (field == value)
                return
            field = value

            treeRoot.forEach { removeNodeFromParent(it) }
            synchronized(this) {
                viewSet = false
                deferredEvents.clear()
                view = messageContainer.messagesLinked.newView(filter, rootMessageListener = this)
                viewSet = true
                processEvents()
            }
        }

    private var viewSet = false
    private val deferredEvents = mutableListOf<DeferredListEvent>()
    private var view: ObservableLinkedMessagesView

    init {
        synchronized(this) {
            viewSet = false
            view = messageContainer.messagesLinked.newView(filter, rootMessageListener = this)
            viewSet = true
            processEvents()
        }
    }

    override fun itemsInserted(startIndex: Int, endIndex: Int) {
        synchronized(this) {
            if (!viewSet) {
                deferredEvents += InsertEvent(startIndex, endIndex)
                return
            }
            processEvents()

            for (i in startIndex..endIndex) {
                val entry = view[i] ?: return
                insertNodeInto(LinkedRootNode(entry, this, view), treeRoot, i)
            }
        }
    }

    override fun itemChanged(index: Int) {
        synchronized(this) {
            if (!viewSet) {
                deferredEvents += ChangeEvent(index)
                return
            }
            processEvents()

            treeRoot.getChildAt(index)?.let { child -> nodeChanged(child) }
        }
    }

    override fun itemsRemoved(startIndex: Int, endIndex: Int) {
        synchronized(this) {
            if (!viewSet) {
                deferredEvents += RemoveEvent(startIndex, endIndex)
                return
            }
            processEvents()

            for (i in endIndex downTo startIndex) {
                treeRoot.getChildAt(i)?.let { child -> removeNodeFromParent(child as MutableTreeNode) }
            }
        }
    }

    private fun processEvents() {
        val copy = ArrayList(deferredEvents)
        deferredEvents.clear()
        copy.forEach { it.apply(this) }
    }
}

internal class LinkedRootNode(val entry: LinkedMessageHolder,
                              private val treeParent: DefaultTreeModel,
                              private val view: ObservableLinkedMessagesView) : DefaultMutableTreeNode(), ObservableMutableList.Observer {

    private val responses = entry.responses

    init {
        synchronized(view) {
            responses.observer = this
            responses.forEach {
                it.networkRequest?.let { network ->
                    treeParent.insertNodeInto(LinkedResponseNode(network), this, childCount)
                }
                it.networkReply?.let { network ->
                    treeParent.insertNodeInto(LinkedResponseNode(network), this, childCount)
                }

                treeParent.insertNodeInto(LinkedResponseNode(it), this, childCount)
            }
        }
    }

    override fun getAllowsChildren(): Boolean = true

    override fun toString(): String {
        return entry.request?.let { "${it.url}" } ?: ""
    }

    override fun itemsInserted(startIndex: Int, endIndex: Int) {
        synchronized(view) {
            var actualIndex = startIndex
            for (i in startIndex..endIndex) {
                val entry = responses.getOrNull(i) ?: return

                entry.networkRequest?.let {
                    treeParent.insertNodeInto(LinkedResponseNode(it), this, actualIndex++)
                }
                entry.networkReply?.let {
                    treeParent.insertNodeInto(LinkedResponseNode(it), this, actualIndex++)
                }

                treeParent.insertNodeInto(LinkedResponseNode(entry), this, actualIndex++)
            }
        }
    }

    override fun itemChanged(index: Int) {
        synchronized(view) {
            getChildAt(index)?.let { child -> treeParent.nodeChanged(child) }
        }
    }

    override fun itemsRemoved(startIndex: Int, endIndex: Int) {
        synchronized(view) {
            for (i in endIndex downTo startIndex) {
                getChildAt(i)?.let { child -> treeParent.removeNodeFromParent(child as MutableTreeNode) }
            }
        }
    }

}

internal class LinkedResponseNode(val response: NiddlerMessageInfo) : DefaultMutableTreeNode()

private fun DefaultMutableTreeNode.forEach(block: (MutableTreeNode) -> Unit) {
    for (i in childCount - 1 downTo 0) {
        block(getChildAt(i) as MutableTreeNode)
    }
}

private interface DeferredListEvent {
    fun apply(model: LinkedTreeModel)
}

private data class InsertEvent(private val startIndex: Int, private val endIndex: Int) : DeferredListEvent {
    override fun apply(model: LinkedTreeModel) {
        model.itemsInserted(startIndex, endIndex)
    }
}

private data class ChangeEvent(private val index: Int) : DeferredListEvent {
    override fun apply(model: LinkedTreeModel) {
        model.itemChanged(index)
    }
}

private data class RemoveEvent(private val startIndex: Int, private val endIndex: Int) : DeferredListEvent {
    override fun apply(model: LinkedTreeModel) {
        model.itemsRemoved(startIndex, endIndex)
    }
}