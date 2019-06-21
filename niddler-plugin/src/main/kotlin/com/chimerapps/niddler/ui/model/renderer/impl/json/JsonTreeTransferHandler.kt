package com.chimerapps.niddler.ui.model.renderer.impl.json

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.intellij.util.ui.EmptyClipboardOwner
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.TransferHandler

internal class JsonTreeTransferHandler(private val delegate: TransferHandler) : TransferHandler() {

    companion object {
        fun formatJson(jsonElement: JsonElement): String {
            return GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(jsonElement)
        }
    }

    override fun exportToClipboard(comp: JComponent, clip: Clipboard, action: Int) {
        if (comp !is JTree) {
            delegate.exportToClipboard(comp, clip, action)
            return
        }
        val node = (comp.selectionPath?.lastPathComponent as? JsonNode<*>)
        if (node == null) {
            delegate.exportToClipboard(comp, clip, action)
            return
        }
        clip.setContents(StringSelection(formatJson(node.jsonElement)), EmptyClipboardOwner.INSTANCE)
    }

    override fun getSourceActions(c: JComponent?): Int = COPY

    override fun canImport(support: TransferSupport?): Boolean = false

    override fun canImport(comp: JComponent?, transferFlavors: Array<out DataFlavor>?): Boolean = false

}