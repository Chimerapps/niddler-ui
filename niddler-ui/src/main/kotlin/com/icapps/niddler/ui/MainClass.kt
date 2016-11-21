package com.icapps.niddler.ui

import com.icapps.niddler.ui.form.NiddlerWindow
import com.icapps.niddler.ui.form.components.impl.SwingInterfaceFactory
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.WindowConstants


/**
 * @author Nicola Verbeeck
 * @date 10/11/16.
 */
fun main(args: Array<String>) {
    val window = NiddlerWindow(SwingInterfaceFactory())

    val frame = JFrame()
    frame.add(window)
    frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
    frame.pack()

    frame.addWindowListener(object : WindowAdapter() {
        override fun windowClosing(e: WindowEvent?) {
            super.windowClosing(e)
            window.onWindowInvisible()
        }

        override fun windowOpened(e: WindowEvent?) {
            super.windowOpened(e)
            window.onWindowVisible()
        }
    })

    window.init()
    frame.setSize(1300, 600)
    frame.isVisible = true
}
