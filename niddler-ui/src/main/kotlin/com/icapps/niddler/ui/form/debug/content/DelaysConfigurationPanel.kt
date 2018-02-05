package com.icapps.niddler.ui.form.debug.content

import com.icapps.niddler.ui.addChangeListener
import com.icapps.niddler.ui.debugger.model.DebuggerDelays
import com.icapps.niddler.ui.debugger.model.DebuggerInterface
import java.awt.BorderLayout
import java.awt.Dimension
import java.text.NumberFormat
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.text.NumberFormatter

/**
 * @author nicolaverbeeck
 */
class DelaysConfigurationPanel(private val debuggerInterface: DebuggerInterface) : JPanel(BorderLayout()) {

    private val preBlacklist = DelayPanel("Before blacklist")
    private val postBlacklist = DelayPanel("After blacklist")
    private val ensureCallTime = DelayPanel("Ensure call time")

    init {
        val rootBox = Box.createVerticalBox()

        rootBox.add(preBlacklist)
        rootBox.add(Box.createVerticalStrut(8))
        rootBox.add(postBlacklist)
        rootBox.add(Box.createVerticalStrut(8))
        rootBox.add(ensureCallTime)
        rootBox.add(Box.createVerticalGlue())

        rootBox.border = EmptyBorder(6, 12, 6, 12)

        add(rootBox, BorderLayout.NORTH)

        initFrom(debuggerInterface.debugDelays())
    }

    private fun initFrom(previousConfiguration: DebuggerDelays?) {
        set(preBlacklist, previousConfiguration?.preBlacklist)
        set(postBlacklist, previousConfiguration?.postBlacklist)
        set(ensureCallTime, previousConfiguration?.timePerCall)
    }

    fun extractDelays(): DebuggerDelays {
        val pre = extractTime(preBlacklist)
        val post = extractTime(postBlacklist)
        val ensureTime = extractTime(ensureCallTime)

        return DebuggerDelays(pre, post, ensureTime)
    }

    private fun extractTime(panel: DelayPanel): Long? {
        if (panel.checkbox.isSelected)
            return panel.field.text.toLongOrNull()
        return null
    }

    private fun set(panel: DelayPanel, time: Long?) {
        panel.checkbox.isSelected = time != null
        panel.field.text = time?.toString() ?: ""
    }

    private class DelayPanel(title: String) : JPanel(BorderLayout()) {

        val checkbox: JCheckBox = JCheckBox()
        val field: JFormattedTextField = JFormattedTextField(numberFormatter())

        init {
            add(JLabel(title), BorderLayout.NORTH)

            field.apply {
                maximumSize = Dimension(maximumSize.width, preferredSize.height)
            }
            field.addChangeListener { checkbox.isSelected = it.text.isNotEmpty() }

            add(checkbox, BorderLayout.WEST)
            add(field, BorderLayout.CENTER)

            preferredSize = Dimension(300, preferredSize.height)
            minimumSize = Dimension(300, minimumSize.height)
        }


        private fun numberFormatter(): NumberFormatter {
            val format = NumberFormat.getIntegerInstance()
            format.isGroupingUsed = false
            val formatter = object : NumberFormatter(format) {
                override fun stringToValue(text: String?): Any? {
                    if (text.isNullOrBlank())
                        return null
                    return text!!.toLong()
                }

                override fun valueToString(value: Any?): String {
                    if (value == null)
                        return ""
                    return value.toString()
                }
            }
            formatter.valueClass = Long::class.java
            formatter.minimum = 0L
            formatter.maximum = Long.MAX_VALUE
            formatter.allowsInvalid = false
            return formatter
        }
    }
}