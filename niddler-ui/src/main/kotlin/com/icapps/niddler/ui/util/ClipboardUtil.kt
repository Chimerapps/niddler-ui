package com.icapps.niddler.ui.util

import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.ByteArrayInputStream
import java.io.IOException
import javax.imageio.ImageIO


/**
 * Created by maartenvangiel on 20/04/2017.
 */
object ClipboardUtil {

    fun copyToClipboard(transferable: Transferable) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(transferable, null)
    }

    fun imageTransferableFromBytes(bytes: ByteArray?): Transferable? {
        try {
            val inputStream = ByteArrayInputStream(bytes)
            return TransferableImage(ImageIO.read(inputStream))
        } catch (e: IOException) {
            return null
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

}
