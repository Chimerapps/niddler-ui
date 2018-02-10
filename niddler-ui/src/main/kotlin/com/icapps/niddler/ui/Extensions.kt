package com.icapps.niddler.ui

import com.icapps.niddler.ui.form.MainThreadDispatcher
import java.awt.Dimension
import java.awt.Font
import java.beans.PropertyChangeEvent
import java.util.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath


/**
 * @author Nicola Verbeeck
 * @date 10/11/16.
 */
fun String.prefixList(elements: Array<out String>): List<String> {
    val list = ArrayList<String>(elements.size + 1)
    list.add(this)
    list.addAll(elements)
    return list
}

fun nop() {
}

fun <T> Iterator<T>.asEnumeration(): Enumeration<T> {
    return object : Enumeration<T> {
        override fun hasMoreElements(): Boolean {
            return hasNext()
        }

        override fun nextElement(): T {
            return next()
        }

    }
}

fun JTable.setColumnFixedWidth(columnIndex: Int, width: Int) {
    val column = columnModel.getColumn(columnIndex)
    column?.minWidth = width
    column?.maxWidth = width
    column?.preferredWidth = width
}

fun JTable.setColumnMinWidth(columnIndex: Int, width: Int) {
    val column = columnModel.getColumn(columnIndex)
    column?.minWidth = width
    column?.preferredWidth = width
}

fun JLabel.setFixedWidth(width: Int) {
    minimumSize = Dimension(width, 32)
    maximumSize = Dimension(width, 32)
    preferredSize = Dimension(width, 32)
}

fun JTextField.addChangeListener(changeListener: (JTextField) -> Unit) {
    Objects.requireNonNull(text)
    Objects.requireNonNull(changeListener)
    val dl = object : DocumentListener {
        private var lastChange = 0
        private var lastNotifiedChange = 0

        override fun insertUpdate(e: DocumentEvent) {
            changedUpdate(e)
        }

        override fun removeUpdate(e: DocumentEvent) {
            changedUpdate(e)
        }

        override fun changedUpdate(e: DocumentEvent?) {
            lastChange++
            MainThreadDispatcher.dispatch {
                if (lastNotifiedChange != lastChange) {
                    lastNotifiedChange = lastChange
                    changeListener.invoke(this@addChangeListener)
                }
            }
        }
    }
    addPropertyChangeListener("document") { e: PropertyChangeEvent ->
        (e.oldValue as Document?)?.removeDocumentListener(dl)
        (e.newValue as Document?)?.addDocumentListener(dl)
        dl.changedUpdate(null)
    }
    document?.addDocumentListener(dl)
}

fun <T> Iterable<T>.split(block: (T) -> Boolean): Pair<List<T>, List<T>> {
    val left = mutableListOf<T>()
    val right = mutableListOf<T>()
    forEach {
        if (block(it))
            left += it
        else
            right += it
    }
    return left to right
}

fun button(title: String, listener: () -> Unit): JButton {
    return JButton(title).apply {
        addActionListener { listener() }
    }
}

operator fun JComponent.plusAssign(component: JComponent) {
    add(component)
}

fun JComponent.left(): JComponent {
    alignmentX = JComponent.LEFT_ALIGNMENT
    return this
}

fun JLabel.bold(): JLabel {
    font = font.deriveFont(font.style or Font.BOLD)
    return this
}

fun TreeNode.path(): TreePath {
    val nodes = ArrayList<Any>()
    nodes.add(this)

    var treeNode = parent
    while (treeNode != null) {
        nodes.add(0, treeNode)
        treeNode = treeNode.parent
    }
    return TreePath(nodes.toTypedArray())
}