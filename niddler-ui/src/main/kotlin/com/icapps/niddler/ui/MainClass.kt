package com.icapps.niddler.ui

import com.icapps.niddler.lib.utils.LoggerFactory
import com.icapps.niddler.ui.form.MainThreadDispatcher
import com.icapps.niddler.ui.form.NiddlerWindow
import com.icapps.niddler.ui.form.components.impl.SwingComponentsFactory
import com.icapps.niddler.ui.form.impl.SwingMainThreadDispatcher
import com.icapps.niddler.ui.form.impl.SwingNiddlerUserInterface
import com.icapps.niddler.ui.util.JavaLogFactory
import com.icapps.niddler.ui.util.SwingImageHelper
import com.icapps.niddler.ui.util.iconLoader
import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.logging.SimpleFormatter
import java.util.logging.StreamHandler
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants


/**
 * @author Nicola Verbeeck
 * @date 10/11/16.
 */
fun main(args: Array<String>) {
    initLogging(args)
    MainThreadDispatcher.instance = SwingMainThreadDispatcher()
    iconLoader = SwingImageHelper()

    val factory = SwingComponentsFactory()

    val ui = SwingNiddlerUserInterface(factory)
    val window = NiddlerWindow(ui, emptyList())

    val panel = JPanel(BorderLayout())
    panel.add(ui.asComponent, BorderLayout.CENTER)

    val frame = JFrame()
    frame.add(panel)
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

private fun initLogging(args: Array<String>) {
    if (args.contains("--log")) {
        LoggerFactory.instance = JavaLogFactory(args.contains("--verbose"))

        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1\$tY-%1\$tm-%1\$td %1\$tH:%1\$tM:%1\$tS.%1\$tL %4$-7s [%3\$s] (%2\$s) %5\$s %6\$s%n")

        val consoleHandler = object : StreamHandler(System.out, SimpleFormatter()) {
            override fun publish(record: LogRecord?) {
                super.publish(record)
                flush()
            }
        }
        consoleHandler.level = Level.FINEST

        Logger.getLogger("com.icapps").apply {
            level = Level.FINEST
            addHandler(consoleHandler)
        }
    }
}
