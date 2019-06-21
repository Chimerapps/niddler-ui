package com.chimerapps.niddler.ui.model.renderer.impl.xml

import com.intellij.util.ui.EmptyClipboardOwner
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.io.StringWriter
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.TransferHandler
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

internal class XmlTreeTransferHandler(private val delegate: TransferHandler) : TransferHandler() {

    override fun exportToClipboard(comp: JComponent, clip: Clipboard, action: Int) {
        if (comp !is JTree) {
            delegate.exportToClipboard(comp, clip, action)
            return
        }
        val node = (comp.selectionPath?.lastPathComponent as? XMLTreeNode)
        if (node == null) {
            delegate.exportToClipboard(comp, clip, action)
            return
        }

        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        transformer.setOutputProperty(OutputKeys.METHOD, "xml")
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")

        val writer = StringWriter()
        transformer.transform(DOMSource(node.xmlElement), StreamResult(writer))

        val text = writer.toString()

        clip.setContents(StringSelection(text), EmptyClipboardOwner.INSTANCE)
    }

    override fun getSourceActions(c: JComponent?): Int = COPY

    override fun canImport(support: TransferSupport?): Boolean = false

    override fun canImport(comp: JComponent?, transferFlavors: Array<out DataFlavor>?): Boolean = false

}