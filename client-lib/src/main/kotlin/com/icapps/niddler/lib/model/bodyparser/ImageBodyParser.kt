package com.icapps.niddler.lib.model.bodyparser

import com.icapps.niddler.lib.model.BodyFormat
import com.icapps.niddler.lib.model.BodyParser
import com.icapps.niddler.lib.utils.debug
import com.icapps.niddler.lib.utils.logger
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO

/**
 * @author Nicola Verbeeck
 *
 * Tries to parse the data as an image
 */
class ImageBodyParser : BodyParser<BufferedImage> {

    override fun parse(bodyFormat: BodyFormat, bytes: ByteArray): BufferedImage? {
        if (bodyFormat.rawMimeType?.toLowerCase() == "image/webp")
            return readWebPImage(bytes)

        //TODO svg here or separately?

        return try {
            ImageIO.read(ByteArrayInputStream(bytes))
        } catch (e: Throwable) {
            logger<ImageBodyParser>().debug("Failed to parse image:", e)
            return null
        }
    }

    private fun readWebPImage(bytes: ByteArray): BufferedImage? {
        if (!File("/usr/local/bin/dwebp").exists())
            return null
        val source = File.createTempFile("tmp_img", "dat")
        source.writeBytes(bytes)
        val converted = File.createTempFile("tmp_img", "png")
        val proc = ProcessBuilder()
                .command("/usr/local/bin/dwebp", source.absolutePath, "-o", converted.absolutePath) //TODO fix
                .start()
        proc.waitFor()
        try {
            return ImageIO.read(converted)
        } finally {
            source.delete()
            converted.delete()
        }
    }

}