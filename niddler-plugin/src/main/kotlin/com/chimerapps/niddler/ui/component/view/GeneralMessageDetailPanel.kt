package com.chimerapps.niddler.ui.component.view

import com.chimerapps.niddler.ui.util.ui.ClipboardUtil
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBFont
import com.jgoodies.forms.layout.CellConstraints
import com.jgoodies.forms.layout.FormLayout
import com.jgoodies.forms.layout.RowSpec
import java.awt.BorderLayout
import java.awt.Font
import java.awt.datatransfer.StringSelection
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.border.BevelBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder

class GeneralMessageDetailPanel : JPanel(BorderLayout()) {

    private companion object {
        fun wrapHorizontalScrollbar(component: JComponent): JComponent {
            val scrollPane = JBScrollPane(component, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
            scrollPane.border = BorderFactory.createEmptyBorder(0, 0, 10, 0)

            return scrollPane
        }
    }

    private val generalPanel = JPanel(FormLayout("left:default, 3dlu, pref", "pref, pref, pref, pref, pref, pref"))
    private val headersPanel = JPanel(FormLayout("left:default, 3dlu, pref", "pref, pref, pref, pref, pref, pref"))
    private val tracePanel = JPanel()
    private val contextPanel = JPanel()
    private val detailContainer = JPanel().also {
        it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
        it.add(wrapHorizontalScrollbar(generalPanel))
        it.add(wrapHorizontalScrollbar(headersPanel))
        it.add(wrapHorizontalScrollbar(tracePanel))
        it.add(wrapHorizontalScrollbar(contextPanel))
    }

    private val contentScroller = JBScrollPane(JPanel(BorderLayout()).also {
        it.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        it.add(detailContainer, BorderLayout.NORTH)
    })

    private val valueFont = JBFont.create(Font("Monospaced", 0, 12))
    private val labelFont = valueFont.asBold()
    private val italicFont = valueFont.asItalic()
    private val timestampFormatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    init {
        generalPanel.border = BorderFactory.createTitledBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), EmptyBorder(5, 5, 5, 5)),
                "General", TitledBorder.ABOVE_TOP, TitledBorder.LEFT)
        headersPanel.border = BorderFactory.createTitledBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), EmptyBorder(5, 5, 5, 5)),
                "Headers", TitledBorder.ABOVE_TOP, TitledBorder.LEFT)
        tracePanel.border = BorderFactory.createTitledBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), EmptyBorder(5, 5, 5, 5)),
                "Stacktrace", TitledBorder.ABOVE_TOP, TitledBorder.LEFT)
        contextPanel.border = BorderFactory.createTitledBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), EmptyBorder(5, 5, 5, 5)),
                "Context", TitledBorder.ABOVE_TOP, TitledBorder.LEFT)

        add(contentScroller, BorderLayout.CENTER)
    }

    fun init(message: ParsedNiddlerMessage, other: ParsedNiddlerMessage?) {
        fillGeneral(message, other)
        fillHeaders(message)

        tracePanel.isVisible = false
        contextPanel.isVisible = false
    }

    private fun fillGeneral(message: ParsedNiddlerMessage, other: ParsedNiddlerMessage?) {
        generalPanel.removeAll() //TODO do not aggressively clear all children

        val constraints = CellConstraints()


        generalPanel.add(buildLabel("Timestamp"), constraints.xy(1, 1))
        generalPanel.add(buildValue(timestampFormatter.format(Date(message.timestamp))), constraints.xy(3, 1))

        generalPanel.add(buildLabel("Method"), constraints.xy(1, 2))
        generalPanel.add(buildValue(message.method ?: other?.method), constraints.xy(3, 2))

        generalPanel.add(buildLabel("URL"), constraints.xy(1, 3))
        generalPanel.add(buildValue(message.url ?: other?.url), constraints.xy(3, 3))

        var row = 4

        (message.url ?: other?.url)?.let { url ->
            try {
                val urlDecoded = URLDecoder.decode(url, "utf-8")
                if (urlDecoded != url) {
                    generalPanel.add(buildLabel("Decoded URL"), constraints.xy(1, row))
                    generalPanel.add(buildValue(urlDecoded), constraints.xy(3, row))
                    ++row
                }
            } catch (e: Throwable) {
            }
        }

        generalPanel.add(buildLabel("Status"), constraints.xy(1, row))
        generalPanel.add(buildValue((message.statusCode
                ?: other?.statusCode)?.toString()), constraints.xy(3, row))
        ++row

        generalPanel.add(buildLabel("Execution time"), constraints.xy(1, row))
        generalPanel.add(makeExecutionTimeLabel(message, other), constraints.xy(3, row))
    }

    private fun fillHeaders(message: ParsedNiddlerMessage) {
        headersPanel.removeAll()

        val layout = headersPanel.layout as FormLayout
        val numRows = layout.rowCount
        for (i in 0 until numRows) {
            layout.removeRow(1)
        }
        val spec = RowSpec.decodeSpecs("pref")[0]
        for (i in 1..(message.headers?.size ?: 0)) {
            layout.appendRow(spec)
        }
        val constraints = CellConstraints()
        var row = 1
        message.headers?.forEach {
            headersPanel.add(buildLabel(it.key, withPopupMenu = true), constraints.xy(1, row))
            headersPanel.add(buildValue(it.value.joinToString(", ")), constraints.xy(3, row))

            ++row
        }
    }

    private fun buildLabel(text: String, withPopupMenu: Boolean = false): JBLabel {
        return JBLabel(text).also {
            it.font = labelFont
            if (withPopupMenu) {
                it.componentPopupMenu = JBPopupMenu().also { menu ->

                    menu.add(JBMenuItem("Copy").also { menuItem ->
                        menuItem.addActionListener {
                            ClipboardUtil.copyToClipboard(StringSelection(text))
                        }
                    })
                }
            }
        }
    }

    private fun buildValue(text: String?): JBLabel {
        return JBLabel(text ?: "").also {
            it.font = valueFont
            it.componentPopupMenu = JBPopupMenu().also { menu ->

                menu.add(JBMenuItem("Copy").also { menuItem ->
                    menuItem.addActionListener {
                        ClipboardUtil.copyToClipboard(StringSelection(text))
                    }
                })
            }
        }
    }

    private fun makeExecutionTimeLabel(firstMessage: ParsedNiddlerMessage?, secondMessage: ParsedNiddlerMessage?): JComponent {
        if (firstMessage == null || secondMessage == null) {
            return JBLabel("Unknown").also { it.font = italicFont }
        }
        val time = if (firstMessage.timestamp > secondMessage.timestamp)
            firstMessage.timestamp - secondMessage.timestamp
        else
            secondMessage.timestamp - firstMessage.timestamp
        return buildValue("$time msec")
    }
}