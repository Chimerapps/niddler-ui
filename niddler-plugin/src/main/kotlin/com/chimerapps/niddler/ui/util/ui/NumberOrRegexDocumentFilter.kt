package com.chimerapps.niddler.ui.util.ui

import javax.swing.text.AttributeSet
import javax.swing.text.Document
import javax.swing.text.DocumentFilter

class NumberOrRegexDocumentFilter(private val allowEmpty: Boolean = true,
                                  private val allowNegative: Boolean = false) : DocumentFilter() {

    override fun insertString(fb: FilterBypass, offset: Int, string: String?,
                              attr: AttributeSet?) {
        val doc: Document = fb.document
        val sb = StringBuilder()
        sb.append(doc.getText(0, doc.length))
        sb.insert(offset, string)
        if (test(sb.toString())) {
            super.insertString(fb, offset, string, attr)
        }
    }

    private fun test(text: String): Boolean {
        if (allowEmpty && text.isEmpty())
            return true

        val intValue = text.replace("*", "").replace("?", "").toIntOrNull() ?: return false
        return (intValue >= 0 || allowNegative)
    }

    override fun replace(fb: FilterBypass, offset: Int, length: Int, text: String?,
                         attrs: AttributeSet?) {
        val doc: Document = fb.document
        val sb = StringBuilder()
        sb.append(doc.getText(0, doc.getLength()))
        sb.replace(offset, offset + length, text)
        if (test(sb.toString())) {
            super.replace(fb, offset, length, text, attrs)
        }
    }

    override fun remove(fb: FilterBypass, offset: Int, length: Int) {
        val doc: Document = fb.document
        val sb = StringBuilder()
        sb.append(doc.getText(0, doc.getLength()))
        sb.delete(offset, offset + length)
        if (test(sb.toString())) {
            super.remove(fb, offset, length)
        }
    }
}