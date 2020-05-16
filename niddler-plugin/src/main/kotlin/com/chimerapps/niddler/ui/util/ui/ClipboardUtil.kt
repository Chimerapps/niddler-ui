package com.chimerapps.niddler.ui.util.ui

import com.icapps.niddler.lib.model.BodyFormatType
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.intellij.ide.ClipboardSynchronizer
import com.intellij.openapi.application.runWriteAction
import com.intellij.util.ui.EmptyClipboardOwner
import java.awt.Image
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.ByteArrayInputStream
import java.io.IOException
import javax.imageio.ImageIO

object ClipboardUtil {

    fun copyToClipboard(transferable: Transferable) {
        ClipboardSynchronizer.getInstance().setContent(transferable, EmptyClipboardOwner.INSTANCE)
    }

    fun imageTransferableFromBytes(bytes: ByteArray?): Transferable? {
        return try {
            val inputStream = ByteArrayInputStream(bytes)
            TransferableImage(ImageIO.read(inputStream))
        } catch (e: IOException) {
            null
        }
    }

    private class TransferableImage(internal var i: Image?) : Transferable {

        @Throws(UnsupportedFlavorException::class, IOException::class)
        override fun getTransferData(flavor: DataFlavor): Any {
            if (flavor.equals(DataFlavor.imageFlavor) && i != null) {
                return i as Image
            } else {
                throw UnsupportedFlavorException(flavor)
            }
        }

        override fun getTransferDataFlavors(): Array<DataFlavor?> {
            val flavors = arrayOfNulls<DataFlavor>(1)
            flavors[0] = DataFlavor.imageFlavor
            return flavors
        }

        override fun isDataFlavorSupported(flavor: DataFlavor): Boolean {
            val flavors = transferDataFlavors
            for (i in flavors.indices) {
                if (flavor.equals(flavors[i])) {
                    return true
                }
            }
            return false
        }
    }

    fun copyToClipboard(message: ParsedNiddlerMessage) {
        var clipboardData: Transferable? = null
        when (message.bodyFormat.type) {
            BodyFormatType.FORMAT_IMAGE -> {
                clipboardData = imageTransferableFromBytes(message.message.getBodyAsBytes)
            }
            BodyFormatType.FORMAT_PLAIN,
            BodyFormatType.FORMAT_JSON,
            BodyFormatType.FORMAT_HTML,
            BodyFormatType.FORMAT_FORM_ENCODED,
            BodyFormatType.FORMAT_XML -> {
                clipboardData = StringSelection(message.message.getBodyAsString(message.bodyFormat.encoding))
            }
            BodyFormatType.FORMAT_BINARY -> {
                message.message.getBodyAsBytes?.let { data ->
                    chooseSaveFile("Save data to", "")?.let { file ->
                        runWriteAction {
                            file.writeBytes(data)
                        }
                    }
                }
            }
            else -> {
            } //Nothing to copy
        }
        clipboardData?.let { copyToClipboard(it) }
    }

}
