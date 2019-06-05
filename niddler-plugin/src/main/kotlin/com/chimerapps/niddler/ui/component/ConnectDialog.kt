package com.chimerapps.niddler.ui.component

import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Window
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.KeyStroke
import javax.swing.WindowConstants

class ConnectDialog(parent: Window?) : ConnectDialogUI(parent, "Select a device to connect to") {

    companion object {
        fun show(parent: Window?) {
            val dialog = ConnectDialog(parent)
            dialog.pack()
            dialog.setSize(500, 350)
            if (dialog.parent != null)
                dialog.setLocationRelativeTo(parent)
            dialog.isVisible = true
        }
    }

    override fun onCancel() {
        dispose()
    }

    override fun onConnect() {
        dispose()
    }

}

abstract class ConnectDialogUI(parent: Window?, title: String) : JDialog(parent, title, ModalityType.APPLICATION_MODAL) {

    protected val devicesTree = Tree()
    protected val cancelButton = JButton("Cancel").also {
        it.addActionListener { onCancel() }
    }
    protected val connectButton = JButton("Connect").also {
        it.addActionListener { onConnect() }
    }
    private val buttonContainer = JPanel(FlowLayout(FlowLayout.RIGHT)).also {
        it.add(connectButton)
        it.add(cancelButton)
    }
    private val topContainer = JPanel(BorderLayout()).also {
        it.add(JBScrollPane(devicesTree), BorderLayout.NORTH)
    }
    private val rootContainer = JPanel(BorderLayout()).also {
        it.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        it.add(buttonContainer, BorderLayout.SOUTH)
        it.add(topContainer, BorderLayout.CENTER)
    }

    init {
        contentPane = rootContainer
        rootPane.defaultButton = connectButton

        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                onCancel()
            }
        })
        rootContainer.registerKeyboardAction({ onCancel() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
    }

    //Make final
    final override fun addWindowListener(l: WindowListener?) {
        super.addWindowListener(l)
    }

    protected abstract fun onCancel()

    protected abstract fun onConnect()

}