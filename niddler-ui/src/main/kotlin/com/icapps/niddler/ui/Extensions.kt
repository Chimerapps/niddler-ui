package com.icapps.niddler.ui

import com.icapps.niddler.ui.form.MainThreadDispatcher
import java.awt.Color
import java.awt.Dimension
import java.beans.PropertyChangeEvent
import java.util.*
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document


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

fun Color.toHex(): String {
    var hexColour = Integer.toHexString(rgb and 0xffffff)
    if (hexColour.length < 6) {
        hexColour = "000000".substring(0, 6 - hexColour.length) + hexColour
    }
    return "#$hexColour"
}

fun getDeviceIcon(emulator: Boolean): String {
    return if (emulator) "/ic_device_emulator.png" else "/ic_device_real.png"
}