package com.icapps.niddler.ui

import com.icapps.niddler.ui.form.MainThreadDispatcher
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.beans.PropertyChangeEvent
import java.util.*
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.JTree
import javax.swing.border.EmptyBorder
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

fun <T : JComponent> T.setFixedWidth(width: Int): T {
    minimumSize = Dimension(width, 32)
    maximumSize = Dimension(width, 32)
    preferredSize = Dimension(width, 32)
    return this
}

fun JTextField.addChangeListener(changeListener: (JTextField) -> Unit) {
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


fun button(title: String, listener: () -> Unit): JButton {
    return JButton(title).apply {
        addActionListener { listener() }
    }
}

operator fun JComponent.plusAssign(component: Component) {
    add(component)
}

fun <T : JComponent> T.left(): T {
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

fun <T : JComponent> T.singleLine(): T {
    maximumSize = Dimension(maximumSize.width, preferredSize.height)
    return this
}

fun JComponent.forEach(each: (Component) -> Unit) {
    for (i in 0 until componentCount) {
        each(getComponent(i))
    }
}

fun JLabel.offsetLeft(): JLabel {
    border = EmptyBorder(0, 4, 0, 0)
    return this
}

fun Color.toHex(): String {
    return String.format("#%02x%02x%02x", red, green, blue)
}

fun String.hexToColor(): Color {
    return Color(Integer.valueOf(substring(1, 3), 16),
            Integer.valueOf(substring(3, 5), 16),
            Integer.valueOf(substring(5, 7), 16))
}

fun getDeviceIcon(emulator: Boolean): String {
    return if (emulator) "/ic_device_emulator.png" else "/ic_device_real.png"
}

fun JTree.expandAllNodes(startingIndex: Int, rowCount: Int) {
    for (i in startingIndex until rowCount) {
        expandRow(i)
    }

    if (rowCount != rowCount) {
        expandAllNodes(rowCount, rowCount)
    }
}

fun Color.isBright(): Boolean {
    val darkness = 1 - (0.299 * red + 0.587 * green + 0.114 * blue) / 255.0
    return darkness <= 0.5
}