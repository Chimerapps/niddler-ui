package com.icapps.niddler.ui.form.debug.view

import com.icapps.niddler.lib.utils.debug
import com.icapps.niddler.lib.utils.logger
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetDropEvent
import java.awt.dnd.DropTargetEvent
import java.awt.dnd.DropTargetListener
import java.io.File
import javax.swing.TransferHandler

/**
 * @author nicolaverbeeck
 */
class DebugDetailDropHandler(private val component: DebugDetailView) : TransferHandler(), DropTargetListener {

    private companion object {
        private val logger = logger<DebugDetailDropHandler>()
    }

    override fun canImport(support: TransferSupport): Boolean {
        val fileFlavor = support.dataFlavors.find { it.isFlavorJavaFileListType }
        component.repaint()
        return fileFlavor != null
    }

    override fun importData(support: TransferSupport): Boolean {
        logger.debug("Drop: $support")

        @Suppress("UNCHECKED_CAST")
        val files = support.transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<File> ?: return false

        logger.debug("Files: $files")
        component.repaint()
        return true
    }

    override fun dropActionChanged(dtde: DropTargetDragEvent?) {
    }

    override fun drop(dtde: DropTargetDropEvent?) {
        component.inDrop = false
        component.repaint()
    }

    override fun dragOver(dtde: DropTargetDragEvent?) {
    }

    override fun dragExit(dte: DropTargetEvent?) {
        component.inDrop = false
        component.repaint()
    }

    override fun dragEnter(dtde: DropTargetDragEvent) {
        component.inDrop = true
        component.repaint()
    }

}