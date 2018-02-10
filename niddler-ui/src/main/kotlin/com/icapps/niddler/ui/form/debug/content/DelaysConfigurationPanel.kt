package com.icapps.niddler.ui.form.debug.content

import com.icapps.niddler.ui.addChangeListener
import com.icapps.niddler.ui.bold
import com.icapps.niddler.ui.debugger.model.DebuggerDelays
import com.icapps.niddler.ui.debugger.model.saved.TemporaryDebuggerConfiguration
import com.icapps.niddler.ui.form.MainThreadDispatcher
import com.icapps.niddler.ui.left
import java.awt.BorderLayout
import java.awt.Dimension
import java.text.NumberFormat
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.text.NumberFormatter

/**
 * @author nicolaverbeeck
 */
class DelaysConfigurationPanel(private var configuration: TemporaryDebuggerConfiguration,
                               changeListener: () -> Unit)
    : JPanel(BorderLayout()), ContentPanel {

    private val preBlacklist = DelayPanel("Before blacklist", changeListener)
    private val postBlacklist = DelayPanel("After blacklist", changeListener)
    private val ensureCallTime = DelayPanel("Ensure call time", changeListener)

    private val enabledFlag: JCheckBox = JCheckBox("Enabled")
    override var enableListener: ((enabled: Boolean) -> Unit)? = null

    init {
        val rootBox = Box.createVerticalBox()

        rootBox.add(JLabel("Delay configuration").bold().left())
        rootBox.add(Box.createVerticalStrut(8))

        rootBox.add(enabledFlag.left())
        enabledFlag.addActionListener { enableListener?.invoke(enabledFlag.isSelected) }
        rootBox.add(Box.createVerticalStrut(8))

        rootBox.add(preBlacklist.left())
        rootBox.add(Box.createVerticalStrut(8))
        rootBox.add(postBlacklist.left())
        rootBox.add(Box.createVerticalStrut(8))
        rootBox.add(ensureCallTime.left())
        rootBox.add(Box.createVerticalGlue())

        rootBox.border = EmptyBorder(6, 12, 6, 12)

        add(rootBox, BorderLayout.NORTH)

        val currentConfig = configuration.delayConfiguration.item
        enabledFlag.isSelected = configuration.delayConfiguration.enabled
        set(preBlacklist, currentConfig.preBlacklist)
        set(postBlacklist, currentConfig.postBlacklist)
        set(ensureCallTime, currentConfig.timePerCall)

        MainThreadDispatcher.dispatch {
            preBlacklist.initComplete()
            postBlacklist.initComplete()
            ensureCallTime.initComplete()
        }
    }

    override fun updateEnabledFlag(enabled: Boolean) {
        enabledFlag.isSelected = enabled
    }

    override fun apply(isEnabled: Boolean) {
        val pre = extractTime(preBlacklist)
        val post = extractTime(postBlacklist)
        val ensureTime = extractTime(ensureCallTime)

        configuration.delayConfiguration.item = DebuggerDelays(pre, post, ensureTime)
        configuration.delayConfiguration.enabled = isEnabled
        enabledFlag.isSelected = isEnabled
    }

    private fun extractTime(panel: DelayPanel): Long? {
        return panel.field.text.toLongOrNull()
    }

    private fun set(panel: DelayPanel, time: Long?) {
        panel.field.text = time?.toString() ?: ""
    }

    private class DelayPanel(title: String,
                             private var changeListener: () -> Unit) : JPanel(BorderLayout()) {

        val field: JFormattedTextField = JFormattedTextField(numberFormatter())
        private var initComplete = false

        init {
            add(JLabel(title).apply { border = EmptyBorder(0, 3, 0, 0) }, BorderLayout.NORTH)

            field.apply {
                maximumSize = Dimension(maximumSize.width, preferredSize.height)
            }
            field.addChangeListener {
                if (initComplete)
                    changeListener()
            }

            add(field, BorderLayout.CENTER)

            preferredSize = Dimension(300, preferredSize.height)
            minimumSize = Dimension(300, minimumSize.height)
        }

        fun initComplete() {
            initComplete = true
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